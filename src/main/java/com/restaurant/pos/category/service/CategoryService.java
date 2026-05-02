package com.restaurant.pos.category.service;

import com.restaurant.pos.category.domain.ExpenseCategory;
import com.restaurant.pos.category.dto.CategoryResponse;
import com.restaurant.pos.category.dto.CreateCategoryRequest;
import com.restaurant.pos.category.dto.UpdateCategoryRequest;
import com.restaurant.pos.category.mapper.CategoryMapper;
import com.restaurant.pos.category.repository.ExpenseCategoryRepository;
import com.restaurant.pos.common.exception.DuplicateResourceException;
import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
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
 * Now lives in its own module for cross-domain reusability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ExpenseCategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Cacheable(value = "expenseCategories", key = "#root.methodName + '_' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        
        log.info("Fetching expense categories | clientId={} | orgId={}", clientId, orgId);
        
        return categoryRepository.findByClientIdAndOrgIdOrderBySortOrderAsc(clientId, orgId)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "expenseCategories", allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        
        log.info("Creating category '{}' | orgId={}", request.getName(), orgId);

        if (categoryRepository.existsByNameIgnoreCaseAndClientIdAndOrgId(request.getName(), clientId, orgId)) {
            throw new DuplicateResourceException(
                    "Expense category '" + request.getName() + "' already exists in this branch."
            );
        }

        ExpenseCategory category = categoryMapper.toEntity(request);
        category.setOrgId(orgId);

        ExpenseCategory saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
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
                throw new DuplicateResourceException(
                        "Category name '" + request.getName() + "' is already in use."
                );
            }
        }

        categoryMapper.updateEntity(category, request);

        // Handle status via domain behavior
        if (request.getActive() != null) {
            if (request.getActive()) category.activate();
            else category.deactivate();
            
            categoryRepository.updateStatus(id, category.getIsactive());
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
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
