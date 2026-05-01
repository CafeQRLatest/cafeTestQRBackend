package com.restaurant.pos.expense.service;

import com.restaurant.pos.common.exception.BusinessException;
import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.expense.domain.ExpenseCategory;
import com.restaurant.pos.expense.dto.*;
import com.restaurant.pos.expense.dto.ExpenseDto.CreateExpenseRequest;
import com.restaurant.pos.expense.dto.ExpenseDto.ExpenseResponse;
import com.restaurant.pos.expense.mapper.ExpenseMapper;
import com.restaurant.pos.expense.repository.ExpenseCategoryRepository;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderType;
import com.restaurant.pos.order.repository.OrderRepository;
import com.restaurant.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final com.restaurant.pos.purchasing.repository.CurrencyRepository currencyRepository;

    // ── Categories ──────────────────────────────────────────────────────────────

    @Cacheable(value = "expenseCategories", key = "#root.methodName + '_' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public List<CategoryResponse> getCategories() {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        log.info("Fetching expense categories for client {} and org {}", clientId, orgId);
        return categoryRepository.findByClientIdAndOrgIdOrderBySortOrderAsc(clientId, orgId)
                .stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "expenseCategories", allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        log.info("Creating expense category '{}' for client {} and org {}", request.getName(), clientId, orgId);

        if (categoryRepository.existsByNameIgnoreCaseAndClientIdAndOrgId(request.getName(), clientId, orgId)) {
            throw new com.restaurant.pos.common.exception.DuplicateResourceException("An expense category with this name already exists in this branch.");
        }

        ExpenseCategory category = expenseMapper.toEntity(request);
        category.setOrgId(orgId);

        ExpenseCategory saved = categoryRepository.save(category);
        log.info("Created expense category with id {}", saved.getId());
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "expenseCategories", allEntries = true)
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        log.info("Updating expense category {}", id);

        ExpenseCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        UUID orgId = TenantContext.getCurrentOrg();
        if (orgId != null && cat.getOrgId() != null && !orgId.equals(cat.getOrgId())) {
            throw new org.springframework.security.access.AccessDeniedException("Manager cannot access categories from another branch.");
        }

        if (!cat.getName().equalsIgnoreCase(request.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndClientIdAndOrgId(request.getName(), cat.getClientId(), orgId)) {
                throw new com.restaurant.pos.common.exception.DuplicateResourceException("An expense category with this name already exists in this branch.");
            }
        }

        expenseMapper.updateEntity(cat, request);

        if (cat.getOrgId() == null) {
            cat.setOrgId(orgId);
        }

        cat = categoryRepository.save(cat);

        // Handle status update separately due to PostgreSQL type strictness
        if (request.getIsActive() != null) {
            String status = request.getIsActive() ? "Y" : "N";
            int rows = categoryRepository.updateStatus(id, status);
            if (rows == 0) {
                throw new ResourceNotFoundException("Category not found or access denied.");
            }
            cat.setIsactive(status);
        }

        log.info("Updated expense category {}", id);
        return expenseMapper.toResponse(cat);
    }

    @Transactional
    @CacheEvict(value = "expenseCategories", allEntries = true)
    public void deleteCategory(UUID id) {
        log.info("Soft-deleting expense category {}", id);

        // Verify it exists first
        ExpenseCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        UUID orgId = TenantContext.getCurrentOrg();
        if (orgId != null && cat.getOrgId() != null && !orgId.equals(cat.getOrgId())) {
            throw new org.springframework.security.access.AccessDeniedException("Manager cannot delete categories from another branch.");
        }

        int rows = categoryRepository.updateStatus(id, "N");
        if (rows == 0) {
            throw new ResourceNotFoundException("Category not found or already deleted");
        }
        log.info("Soft-deleted expense category {}", id);
    }

    // ── Expenses (Unified Order Facade) ─────────────────────────────────────────

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        UUID orgId = TenantContext.getCurrentOrg();
        log.info("Creating EXPENSE order for org {} | amount={} | category={}", orgId, request.getAmount(), request.getCategoryId());

        // Validate Category
        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("Invalid category"));

        if ("N".equals(category.getIsactive())) {
            throw new BusinessException("Cannot assign expense to an inactive category");
        }

        // Bug fix 1: use the date the user selected, not server time
        LocalDateTime expenseDate = (request.getExpenseDate() != null)
                ? request.getExpenseDate()
                : LocalDateTime.now();

        // Bug fix 2: capture the payment method before building order
        String paymentMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod()
                : "CASH";

        Order order = new Order();
        order.setOrderType(OrderType.EXPENSE);
        order.setOrderStatus("COMPLETED");
        order.setPaymentStatus("PAID");
        order.setExpenseCategoryId(request.getCategoryId());
        order.setOrderDate(expenseDate);
        order.setReference(paymentMethod);
        
        // Ensure Org, Terminal and Client are set correctly
        UUID finalOrgId = (request.getBranchId() != null) ? request.getBranchId() : orgId;
        order.setOrgId(finalOrgId);
        order.setClientId(TenantContext.getCurrentTenant());
        order.setTerminalId(TenantContext.getCurrentTerminal());
        order.setFulfillmentType(null);
        
        // Set Default Currency
        currencyRepository.findByClientIdAndIsDefaultTrue(TenantContext.getCurrentTenant())
                .stream().findFirst().ifPresent(c -> order.setCurrencyId(c.getId()));

        if (request.getAmount() != null) {
            order.setTotalAmount(request.getAmount());
            order.setGrandTotal(request.getAmount());
        } else {
            order.setTotalAmount(java.math.BigDecimal.ZERO);
            order.setGrandTotal(java.math.BigDecimal.ZERO);
        }

        order.setDescription(request.getDescription());

        // OrderService handles Invoice + Payment generation and Sequence creation automatically
        Order savedOrder = orderService.createOrder(order);

        return mapToExpenseResponse(savedOrder, category.getName(), paymentMethod);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ExpenseResponse> getExpenses(ExpenseSearchCriteria criteria, org.springframework.data.domain.Pageable pageable) {
        log.info("Fetching paginated expenses with criteria: {}", criteria);
        UUID tenantId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        org.springframework.data.jpa.domain.Specification<Order> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            predicates.add(cb.equal(root.get("clientId"), tenantId));
            predicates.add(cb.equal(root.get("orderType"), OrderType.EXPENSE));
            
            if (!com.restaurant.pos.common.util.SecurityUtils.isSuperAdmin() && orgId != null) {
                predicates.add(cb.equal(root.get("orgId"), orgId));
            } else if (criteria.getBranchId() != null) {
                predicates.add(cb.equal(root.get("orgId"), criteria.getBranchId()));
            }

            if (criteria.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), criteria.getFromDate()));
            }
            if (criteria.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), criteria.getToDate()));
            }
            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("expenseCategoryId"), criteria.getCategoryId()));
            }
            if (criteria.getPaymentMethod() != null && !criteria.getPaymentMethod().isBlank()) {
                predicates.add(cb.equal(root.get("reference"), criteria.getPaymentMethod()));
            }
            if (criteria.getSearchTerm() != null && !criteria.getSearchTerm().isBlank()) {
                String pattern = "%" + criteria.getSearchTerm().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("orderNo")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<Order> expensePage = orderRepository.findAll(spec, pageable);
        
        // Fetch categories to avoid N+1 if needed, though for pagination small size it's usually okay.
        // We'll use a cacheable map for category names.
        java.util.Map<UUID, String> catMap = getCategories().stream()
                .collect(java.util.stream.Collectors.toMap(CategoryResponse::getId, CategoryResponse::getName, (a, b) -> a));

        return expensePage.map(o -> {
            String method = (o.getReference() != null && !o.getReference().isBlank()) ? o.getReference() : "CASH";
            return mapToExpenseResponse(o, catMap.getOrDefault(o.getExpenseCategoryId(), "Uncategorized"), method);
        });
    }

    private ExpenseResponse mapToExpenseResponse(Order order, String categoryName, String paymentMethod) {
        return ExpenseResponse.builder()
            .id(order.getId())
            .referenceNumber(order.getOrderNo())
            .categoryId(order.getExpenseCategoryId())
            .categoryName(categoryName)
            .expenseDate(order.getOrderDate())
            .amount(order.getGrandTotal())
            .description(order.getDescription())
            .paymentMethod(paymentMethod)
            .isActive("Y".equals(order.getIsactive()))
            .orgId(order.getOrgId())
            .build();
    }
}
