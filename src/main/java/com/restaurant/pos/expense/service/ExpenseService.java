package com.restaurant.pos.expense.service;

import com.restaurant.pos.common.exception.BusinessException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.expense.domain.Expense;
import com.restaurant.pos.expense.domain.ExpenseCategory;
import com.restaurant.pos.expense.dto.CategoryResponse;
import com.restaurant.pos.expense.dto.ExpenseDto.CreateExpenseRequest;
import com.restaurant.pos.expense.dto.ExpenseDto.ExpenseResponse;
import com.restaurant.pos.expense.dto.ExpenseSearchCriteria;
import com.restaurant.pos.expense.mapper.ExpenseMapper;
import com.restaurant.pos.expense.repository.ExpenseCategoryRepository;
import com.restaurant.pos.expense.repository.ExpenseRepository;
import com.restaurant.pos.expense.repository.ExpenseSpecification;
import com.restaurant.pos.order.domain.OrderType;
import com.restaurant.pos.order.service.OrderService;
import com.restaurant.pos.purchasing.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing expense transactions.
 * Now fully utilizing the dedicated Expense domain entity and repository 
 * for FAANG-level modularity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final OrderService orderService;
    private final CurrencyRepository currencyRepository;
    private final CategoryService categoryService;

    /**
     * Records a new expense transaction.
     */
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        log.info("Creating expense | categoryId={} | amount={}", request.getCategoryId(), request.getAmount());

        ExpenseCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("Invalid expense category"));

        if (!category.isActive()) {
            throw new BusinessException("Cannot assign expense to an inactive category");
        }

        Expense expense = buildExpenseEntity(request, category);
        
        // Note: We still use OrderService.createOrder because it handles the 
        // complex logic of Sequences, Invoices, and Payments for the unified 'orders' table.
        // Since Expense extends Order, this works perfectly.
        Expense savedExpense = (Expense) orderService.createOrder(expense);

        return expenseMapper.toExpenseResponse(savedExpense, category.getName());
    }

    /**
     * Fetches paginated and filtered expense records.
     */
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getExpenses(ExpenseSearchCriteria criteria, Pageable pageable) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        log.info("Querying expenses | filters={} | page={}", criteria, pageable.getPageNumber());

        Specification<Expense> spec = ExpenseSpecification.filterBy(criteria, clientId, orgId);
        Page<Expense> expensePage = expenseRepository.findAll(spec, pageable);

        // Batch fetch category names for optimized mapping
        Map<UUID, String> categoryNames = categoryService.getCategories().stream()
                .collect(Collectors.toMap(CategoryResponse::getId, CategoryResponse::getName, (a, b) -> a));

        return expensePage.map(expense -> 
            expenseMapper.toExpenseResponse(
                expense, 
                categoryNames.getOrDefault(expense.getExpenseCategoryId(), "Uncategorized")
            )
        );
    }

    private Expense buildExpenseEntity(CreateExpenseRequest request, ExpenseCategory category) {
        LocalDateTime expenseDate = (request.getExpenseDate() != null) ? request.getExpenseDate() : LocalDateTime.now();
        String paymentMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) ? request.getPaymentMethod() : "CASH";

        Expense expense = new Expense(category);
        expense.setOrderDate(expenseDate);
        expense.setReference(paymentMethod);
        
        // Contextual Metadata
        UUID orgId = TenantContext.getCurrentOrg();
        expense.setOrgId((request.getBranchId() != null) ? request.getBranchId() : orgId);
        expense.setClientId(TenantContext.getCurrentTenant());
        expense.setTerminalId(TenantContext.getCurrentTerminal());
        
        // Financials
        expense.setTotalAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        expense.setGrandTotal(expense.getTotalAmount());
        expense.setDescription(request.getDescription());

        // Default Currency
        currencyRepository.findByClientIdAndIsDefaultTrue(TenantContext.getCurrentTenant())
                .stream().findFirst().ifPresent(c -> expense.setCurrencyId(c.getId()));

        return expense;
    }
}
