package com.restaurant.pos.expense.service;

import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.expense.domain.ExpenseCategory;
import com.restaurant.pos.expense.dto.CategoryResponse;
import com.restaurant.pos.expense.dto.CreateCategoryRequest;
import com.restaurant.pos.expense.dto.UpdateCategoryRequest;
import com.restaurant.pos.expense.mapper.ExpenseMapper;
import com.restaurant.pos.expense.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing expense categories.
 * Follows FAANG-level modularity and clean code standards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    @Cacheable(value = "expenseCategories", key = "#root.methodName + '_' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        
        log.info("Fetching expense categories | clientId={} | orgId={}", clientId, orgId);
        
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
        
        log.info("Creating category '{}' | orgId={}", request.getName(), orgId);

        if (categoryRepository.existsByNameIgnoreCaseAndClientIdAndOrgId(request.getName(), clientId, orgId)) {
            throw new com.restaurant.pos.common.exception.DuplicateResourceException(
                    "Expense category '" + request.getName() + "' already exists in this branch."
            );
        }

        ExpenseCategory category = expenseMapper.toEntity(request);
        category.setOrgId(orgId);

        ExpenseCategory saved = categoryRepository.save(category);
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "expenseCategories", allEntries = true)
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        log.info("Updating category | id={}", id);

        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        validateOwnership(category);

        if (!category.getName().equalsIgnoreCase(request.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndClientIdAndOrgId(
                    request.getName(), category.getClientId(), category.getOrgId())) {
                throw new com.restaurant.pos.common.exception.DuplicateResourceException(
                        "Category name '" + request.getName() + "' is already in use."
                );
            }
        }

        expenseMapper.updateEntity(category, request);

        // Handle status via domain behavior
        if (request.getActive() != null) {
            if (request.getActive()) category.activate();
            else category.deactivate();
            
            // Sync DB status for immediate transactional consistency
            categoryRepository.updateStatus(id, category.getIsactive());
        }

        return expenseMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @CacheEvict(value = "expenseCategories", allEntries = true)
    public void deleteCategory(UUID id) {
        log.info("Soft-deleting category | id={}", id);

        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        validateOwnership(category);

        categoryRepository.updateStatus(id, "N");
    }

    private void validateOwnership(ExpenseCategory category) {
        UUID orgId = TenantContext.getCurrentOrg();
        if (orgId != null && category.getOrgId() != null && !orgId.equals(category.getOrgId())) {
            throw new AccessDeniedException("Unauthorized access to branch-specific category.");
        }
    }
}
