package com.restaurant.pos.product.service;

import com.restaurant.pos.common.exception.BusinessException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.product.domain.Category;
import com.restaurant.pos.product.domain.Product;
import com.restaurant.pos.product.domain.Uom;
import com.restaurant.pos.product.domain.VariantGroup;
import com.restaurant.pos.product.domain.VariantOption;
import com.restaurant.pos.product.repository.CategoryRepository;
import com.restaurant.pos.product.repository.ProductRepository;
import com.restaurant.pos.product.repository.UomRepository;
import com.restaurant.pos.product.repository.VariantGroupRepository;
import com.restaurant.pos.product.repository.VariantOptionRepository;
import com.restaurant.pos.product.dto.ProductListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UomRepository uomRepository;
    private final VariantGroupRepository variantGroupRepository;
    private final VariantOptionRepository variantOptionRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "products_categories_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public List<Category> getCategories() {
        return categoryRepository.findByClientIdAndOrgIdOrGlobal(TenantContext.getCurrentTenant(),
                TenantContext.getCurrentOrg());
    }

    @Transactional
    @CacheEvict(value = "products_categories_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public Category createCategory(Category category) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        // Duplicate Check
        if (categoryRepository.findByNameAndClientIdAndOrgIdOrGlobal(category.getName(), clientId, orgId).isPresent()) {
            throw new BusinessException("Category with this name already exists");
        }

        category.setClientId(clientId);
        category.setOrgId(orgId);
        return categoryRepository.save(category);
    }

    @Transactional
    @CacheEvict(value = "products_categories_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public Category updateCategory(UUID id, Category category) {
        Category existing = categoryRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateOwnership(existing.getClientId(), existing.getOrgId(), "Category");

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setActive(category.isActive());
        existing.setImageUrl(category.getImageUrl());
        return categoryRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "products_categories_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateOwnership(category.getClientId(), category.getOrgId(), "Category");

        category.setActive(false);
        categoryRepository.save(category);
    }

    // --- UOM Methods ---

    @Transactional(readOnly = true)
    @Cacheable(value = "products_uoms_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public List<Uom> getUoms() {
        return uomRepository.findByClientIdAndOrgIdOrGlobal(TenantContext.getCurrentTenant(),
                TenantContext.getCurrentOrg());
    }

    @Transactional
    @CacheEvict(value = "products_uoms_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public Uom createUom(Uom uom) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        if (uomRepository.findByNameAndClientIdAndOrgIdOrGlobal(uom.getName(), clientId, orgId).isPresent()) {
            throw new BusinessException("UOM with this name already exists");
        }

        uom.setClientId(clientId);
        uom.setOrgId(orgId);
        return uomRepository.save(uom);
    }

    @Transactional
    @CacheEvict(value = "products_uoms_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public Uom updateUom(UUID id, Uom uom) {
        Uom existing = uomRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

        validateOwnership(existing.getClientId(), existing.getOrgId(), "UOM");

        existing.setName(uom.getName());
        existing.setShortName(uom.getShortName());
        existing.setActive(uom.isActive());
        existing.setDefault(uom.isDefault());
        existing.setUomPrecision(uom.getUomPrecision());
        return uomRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "products_uoms_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public void deleteUom(UUID id) {
        Uom uom = uomRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

        validateOwnership(uom.getClientId(), uom.getOrgId(), "UOM");

        uom.setActive(false);
        uomRepository.save(uom);
    }

    // --- Variant Methods ---

    @Transactional(readOnly = true)
    @Cacheable(value = "products_variant_groups_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public List<VariantGroup> getVariantGroups() {
        return variantGroupRepository.findByClientIdAndOrgIdOrGlobal(TenantContext.getCurrentTenant(),
                TenantContext.getCurrentOrg());
    }

    @Transactional
    @CacheEvict(value = "products_variant_groups_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public VariantGroup createVariantGroup(VariantGroup group) {
        group.setClientId(TenantContext.getCurrentTenant());
        group.setOrgId(TenantContext.getCurrentOrg());
        
        if (group.getOptions() != null) {
            group.getOptions().forEach(opt -> {
                opt.setGroup(group);
                opt.setClientId(group.getClientId());
                opt.setOrgId(group.getOrgId());
            });
        }
        return variantGroupRepository.save(group);
    }

    @Transactional
    @CacheEvict(value = "products_variant_groups_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public VariantGroup updateVariantGroup(UUID id, VariantGroup group) {
        VariantGroup existing = variantGroupRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Variant Group not found"));

        validateOwnership(existing.getClientId(), existing.getOrgId(), "Variant Group");

        existing.setName(group.getName());
        existing.setActive(group.isActive());

        // Update Cascade Options
        existing.getOptions().clear();
        if (group.getOptions() != null) {
            group.getOptions().forEach(opt -> {
                opt.setGroup(existing);
                opt.setClientId(existing.getClientId());
                opt.setOrgId(existing.getOrgId());
                existing.getOptions().add(opt);
            });
        }
        return variantGroupRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = { "products_variant_groups_v2", "variant_options" }, allEntries = true)
    public void deleteVariantGroup(UUID id) {
        VariantGroup group = variantGroupRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Variant Group not found"));

        validateOwnership(group.getClientId(), group.getOrgId(), "Variant Group");

        group.setActive(false);
        variantGroupRepository.save(group);
    }

    @Cacheable(value = "variant_options", key = "#groupId")
    public List<VariantOption> getVariantOptionsByGroup(UUID groupId) {
        return variantOptionRepository.findByGroupId(groupId);
    }

    @Transactional
    @CacheEvict(value = { "products_variant_groups_v2", "variant_options" }, allEntries = true)
    public VariantOption createVariantOption(VariantOption option) {
        if (option.getGroup() == null || option.getGroup().getId() == null) {
            throw new BusinessException("Variant Group ID is required");
        }

        VariantGroup group = variantGroupRepository.findById(java.util.Objects.requireNonNull(option.getGroup().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Variant Group not found"));

        validateOwnership(group.getClientId(), group.getOrgId(), "Variant Group");

        option.setClientId(TenantContext.getCurrentTenant());
        option.setOrgId(TenantContext.getCurrentOrg());
        return variantOptionRepository.save(option);
    }

    @Transactional
    @CacheEvict(value = { "products_variant_groups_v2", "variant_options" }, allEntries = true)
    public VariantOption updateVariantOption(UUID id, VariantOption option) {
        VariantOption existing = variantOptionRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Variant Option not found"));

        validateOwnership(existing.getClientId(), existing.getOrgId(), "Variant Option");

        existing.setName(option.getName());
        existing.setAdditionalPrice(option.getAdditionalPrice());
        existing.setActive(option.isActive());
        return variantOptionRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = { "products_variant_groups_v2", "variant_options" }, allEntries = true)
    public void deleteVariantOption(UUID id) {
        VariantOption option = variantOptionRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Variant Option not found"));

        validateOwnership(option.getClientId(), option.getOrgId(), "Variant Option");

        option.setActive(false);
        variantOptionRepository.save(option);
    }

    // --- Product Methods ---

    @Transactional(readOnly = true)
    @Cacheable(value = "products_list_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public List<ProductListDto> getProducts() {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        return productRepository.findByClientIdAndOrgIdOrGlobal(clientId, orgId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductListDto mapToDto(Product product) {
        return ProductListDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .isAvailable(product.isAvailable())
                .imageUrl(product.getImageUrl())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .uomName(product.getUom() != null ? product.getUom().getName() : null)
                .productCode(product.getProductCode())
                .taxRate(product.getTaxRate())
                .taxCode(product.getTaxCode())
                .isActive(product.isActive())
                .isPackagedGood(product.isPackagedGood())
                .isIngredient(product.isIngredient())
                .productType(product.getProductType())
                .build();
    }

    @Transactional
    @CacheEvict(value = "products_list_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public Product createProduct(Product product) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        // Deep Validatio
        validateProductIntegrity(product, clientId, orgId);

        // Duplicate Code Check
        if (product.getProductCode() != null && productRepository
                .existsByProductCodeAndClientIdAndOrgIdOrGlobal(product.getProductCode(), clientId, orgId)) {
            throw new BusinessException("Product with this code already exists");
        }

        product.setClientId(clientId);
        product.setOrgId(orgId);

        setProductRelationships(product, clientId, orgId);

        return productRepository.save(product);
    }

    private void setProductRelationships(Product product, UUID clientId, UUID orgId) {
        if (product.getVariantMappings() != null) {
            product.getVariantMappings().forEach(vm -> {
                vm.setProduct(product);
                vm.setClientId(clientId);
                vm.setOrgId(orgId);
            });
        }
        if (product.getVariantPricings() != null) {
            product.getVariantPricings().forEach(vp -> {
                vp.setProduct(product);
                vp.setClientId(clientId);
                vp.setOrgId(orgId);
            });
        }
        if (product.getUpsells() != null) {
            product.getUpsells().forEach(upsell -> {
                // Circular Check
                if (product.getId() != null && product.getId().equals(upsell.getUpsellProduct().getId())) {
                    throw new BusinessException("A product cannot be an upsell to itself");
                }
                upsell.setParentProduct(product);
                upsell.setClientId(clientId);
                upsell.setOrgId(orgId);
            });
        }
    }

    @Transactional
    @CacheEvict(value = "products_list_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public List<Product> bulkCreateProducts(List<Product> products) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();

        java.util.Set<String> batchCodes = new java.util.HashSet<>();

        // Optimization: Pre-fetch everything for validation
        java.util.Set<UUID> validCategoryIds = categoryRepository.findByClientIdAndOrgIdOrGlobal(clientId, orgId)
                .stream().map(Category::getId).collect(Collectors.toSet());
        java.util.Set<UUID> validUomIds = uomRepository.findByClientIdAndOrgIdOrGlobal(clientId, orgId)
                .stream().map(Uom::getId).collect(Collectors.toSet());
        Map<String, Category> categoryNameMap = categoryRepository.findByClientIdAndOrgIdOrGlobal(clientId, orgId)
                .stream().collect(Collectors.toMap(Category::getName, c -> c, (a, b) -> a));

        for (Product product : products) {
            // Batch Duplicate Check
            if (product.getProductCode() != null) {
                if (batchCodes.contains(product.getProductCode())) {
                    throw new BusinessException("Duplicate product code in batch: " + product.getProductCode());
                }
                if (productRepository.existsByProductCodeAndClientIdAndOrgIdOrGlobal(product.getProductCode(), clientId,
                        orgId)) {
                    throw new BusinessException("Product code already exists in DB: " + product.getProductCode());
                }
                batchCodes.add(product.getProductCode());
            }

            product.setClientId(clientId);
            product.setOrgId(orgId);

            // Perform integrity check against pre-fetched sets for O(1) speed
            validateProductIntegrityOptimized(product, clientId, orgId, validCategoryIds, validUomIds);

            setProductRelationships(product, clientId, orgId);

            // Resolve category efficiently
            if (product.getCategory() != null && product.getCategory().getId() == null
                    && product.getCategory().getName() != null) {
                String catName = product.getCategory().getName();
                Category category = categoryNameMap.get(catName);
                if (category == null) {
                    category = new Category();
                    category.setName(catName);
                    category.setClientId(clientId);
                    category.setOrgId(orgId);
                    category = categoryRepository.save(category);
                    categoryNameMap.put(catName, category);
                    validCategoryIds.add(category.getId());
                }
                product.setCategory(category);
            }
        }
        @SuppressWarnings("null")
        List<Product> savedProducts = productRepository.saveAll(products);
        return savedProducts;
    }

    @Transactional
    @CacheEvict(value = "products_list_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public Product updateProduct(UUID id, Product product) {
        Product existing = productRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        validateOwnership(existing.getClientId(), existing.getOrgId(), "Product");

        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        validateProductIntegrity(product, clientId, orgId);

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setAvailable(product.isAvailable());
        existing.setImageUrl(product.getImageUrl());
        existing.setActive(product.isActive());

        // ERP Fields
        existing.setProductType(product.getProductType());
        existing.setVariant(product.isVariant());
        existing.setPackagedGood(product.isPackagedGood());
        existing.setIngredient(product.isIngredient());
        existing.setProductCode(product.getProductCode());
        existing.setTaxRate(product.getTaxRate());
        existing.setTaxCode(product.getTaxCode());
        existing.setMrp(product.getMrp());
        existing.setCostPrice(product.getCostPrice());
        existing.setBarcode(product.getBarcode());
        existing.setMinStockLevel(product.getMinStockLevel());
        existing.setKdsStation(product.getKdsStation());

        existing.setCategory(product.getCategory());
        existing.setUom(product.getUom());

        setProductRelationships(product, clientId, orgId);

        // Update Mappings
        existing.getVariantMappings().clear();
        if (product.getVariantMappings() != null) {
            existing.getVariantMappings().addAll(product.getVariantMappings());
        }

        // Update Pricings
        existing.getVariantPricings().clear();
        if (product.getVariantPricings() != null) {
            existing.getVariantPricings().addAll(product.getVariantPricings());
        }

        // Update Upsells
        existing.getUpsells().clear();
        if (product.getUpsells() != null) {
            existing.getUpsells().addAll(product.getUpsells());
        }

        return productRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "products_list_v2", key = "T(com.restaurant.pos.common.tenant.TenantContext).getCurrentTenant() + ':' + T(com.restaurant.pos.common.tenant.TenantContext).getCurrentOrg()")
    public void deleteProduct(UUID id) {
        Product existing = productRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        validateOwnership(existing.getClientId(), existing.getOrgId(), "Product");

        // Soft delete
        existing.setActive(false);
        productRepository.save(existing);
    }

    private void validateOwnership(UUID ownerClientId, UUID ownerOrgId, String entityName) {
        validateOwnership(ownerClientId, ownerOrgId, entityName, true);
    }

    private void validateOwnership(UUID ownerClientId, UUID ownerOrgId, String entityName, boolean forModification) {
        UUID currentClientId = TenantContext.getCurrentTenant();
        UUID currentOrgId = TenantContext.getCurrentOrg();

        // 1. Cross-Tenant Check
        if (!currentClientId.equals(ownerClientId)) {
            throw new BusinessException("Access denied: " + entityName + " belongs to another tenant");
        }

        // 2. Global Data Protection (Global records have NULL orgId)
        if (forModification && ownerOrgId == null && currentOrgId != null) {
            // Check if user has permission to modify global data (assuming only
            // SuperAdmin/System, for now rejecting all non-system)
            // In a real scenario, we'd check Roles. Here we protect global data from being
            // deleted/updated by org users.
            throw new BusinessException("Access denied: Global " + entityName + " cannot be modified by branch users");
        }

        // 3. Cross-Org Check
        if (ownerOrgId != null && !java.util.Objects.equals(currentOrgId, ownerOrgId)) {
            throw new BusinessException("Access denied: " + entityName + " belongs to another organization");
        }
    }

    private void validateProductIntegrity(Product product, UUID clientId, UUID orgId) {
        // Deep Validation for individual create/update (uses standard lookup)
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(java.util.Objects.requireNonNull(product.getCategory().getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            validateOwnership(cat.getClientId(), cat.getOrgId(), "Category", false);
        }

        if (product.getUom() != null && product.getUom().getId() != null) {
            Uom uom = uomRepository.findById(java.util.Objects.requireNonNull(product.getUom().getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("UOM not found"));
            validateOwnership(uom.getClientId(), uom.getOrgId(), "UOM", false);
        }

        // Upsell Check
        if (product.getUpsells() != null) {
            for (var upsell : product.getUpsells()) {
                if (upsell.getUpsellProduct() != null && upsell.getUpsellProduct().getId() != null) {
                    Product upProduct = productRepository.findById(java.util.Objects.requireNonNull(upsell.getUpsellProduct().getId()))
                            .orElseThrow(() -> new ResourceNotFoundException("Upsell product not found"));
                    validateOwnership(upProduct.getClientId(), upProduct.getOrgId(), "Upsell Product", false);
                }
            }
        }
    }

    private void validateProductIntegrityOptimized(Product product, UUID clientId, UUID orgId,
            java.util.Set<UUID> validCats, java.util.Set<UUID> validUoms) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            if (!validCats.contains(product.getCategory().getId())) {
                throw new BusinessException("Invalid category selected or access denied");
            }
        }
        if (product.getUom() != null && product.getUom().getId() != null) {
            if (!validUoms.contains(product.getUom().getId())) {
                throw new BusinessException("Invalid UOM selected or access denied");
            }
        }
    }
}
