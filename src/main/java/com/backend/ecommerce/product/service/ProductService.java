// src/main/java/com/backend/ecommerce/product/service/ProductService.java
package com.backend.ecommerce.product.service;

import com.backend.ecommerce.category.model.Category;
import com.backend.ecommerce.category.repository.CategoryRepository;
import com.backend.ecommerce.category.service.CategoryService;
import com.backend.ecommerce.product.exception.InsufficientStockException;
import com.backend.ecommerce.product.exception.ProductAlreadyExistsException;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.product.model.ProductStatus;
import com.backend.ecommerce.product.payload.ProductCreateRequest;
import com.backend.ecommerce.product.payload.ProductFilterDto;
import com.backend.ecommerce.product.payload.ProductResponseDto;
import com.backend.ecommerce.product.payload.ProductUpdateRequest;
import com.backend.ecommerce.product.repository.ProductRepository;
import com.backend.ecommerce.product.specification.ProductSpecification;
import com.backend.ecommerce.shop.exception.ShopNotFoundException;
import com.backend.ecommerce.shop.exception.ShopNotApprovedException;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.shop.repository.ShopRepository;
import com.backend.ecommerce.shop.service.ShopService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ShopService shopService;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public ProductService(ProductRepository productRepository,
                          ShopRepository shopRepository,
                          ShopService shopService,
                          CategoryRepository categoryRepository,
                          CategoryService categoryService) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.shopService = shopService;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequest request, Long userId)
            throws ProductAlreadyExistsException, ShopNotFoundException, ShopNotApprovedException {

        logger.info("Creating product: {} for shop ID: {}", request.getName(), request.getShopId());

        // Validate shop
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ShopNotFoundException("Shop not found with ID: " + request.getShopId()));

        shopService.validateShopIsActive(shop);

        // Check if product name already exists in the same shop
        if (productRepository.existsByNameAndShopId(request.getName(), request.getShopId())) {
            throw new ProductAlreadyExistsException("Product with name '" + request.getName() +
                    "' already exists in this shop");
        }

        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }

        // Check if slug already exists
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        // Check SKU uniqueness if provided
        if (request.getSku() != null && !request.getSku().isEmpty()) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new ProductAlreadyExistsException("Product with SKU '" + request.getSku() + "' already exists");
            }
        }

        // Validate category if provided
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
        }

        // Create product
        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .sku(request.getSku())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .costPrice(request.getCostPrice())
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .skuBarcode(request.getSkuBarcode())
                .weight(request.getWeight())
                .weightUnit(request.getWeightUnit())
                .dimensions(request.getDimensions())
                .shop(shop)
                .category(category)
                .brand(request.getBrand())
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .status(request.getStatus())
                .isFeatured(request.getIsFeatured())
                .isDigital(request.getIsDigital())
                .digitalFileUrl(request.getDigitalFileUrl())
                .lowStockThreshold(request.getLowStockThreshold())
                .mainImageUrl(request.getMainImageUrl())
                .images(request.getImages())
                .videoUrl(request.getVideoUrl())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .averageRating(0.0)
                .totalReviews(0)
                .totalSales(0)
                .viewCount(0L)
                .createdBy(userId)
                .build();

        Product savedProduct = productRepository.save(product);

        // Update shop product count
        shopService.incrementProductCount(shop.getId());

        // Update category product count
        if (savedProduct.getCategory() != null) {
            categoryService.incrementProductCount(savedProduct.getCategory().getId());
            logger.info("Incremented product count for category ID: {}", savedProduct.getCategory().getId());
        }

        logger.info("Product created successfully: {} with slug: {}", savedProduct.getName(), savedProduct.getSlug());
        return mapToResponseDto(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductUpdateRequest request, Long userId)
            throws ProductNotFoundException, ProductAlreadyExistsException {

        logger.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Store old category for product count update
        Category oldCategory = product.getCategory();

        // Update fields
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.existsByNameAndShopId(request.getName(), product.getShop().getId())) {
                throw new ProductAlreadyExistsException("Product with name '" + request.getName() +
                        "' already exists in this shop");
            }
            product.setName(request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
            if (productRepository.existsBySlug(request.getSlug())) {
                throw new ProductAlreadyExistsException("Product with slug '" + request.getSlug() + "' already exists");
            }
            product.setSlug(request.getSlug());
        }

        if (request.getSku() != null) {
            if (!request.getSku().equals(product.getSku()) &&
                    productRepository.existsBySku(request.getSku())) {
                throw new ProductAlreadyExistsException("Product with SKU '" + request.getSku() + "' already exists");
            }
            product.setSku(request.getSku());
        }

        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCompareAtPrice() != null) product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getSkuBarcode() != null) product.setSkuBarcode(request.getSkuBarcode());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getWeightUnit() != null) product.setWeightUnit(request.getWeightUnit());
        if (request.getDimensions() != null) product.setDimensions(request.getDimensions());

        // Handle category update
        if (request.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(newCategory);
        }

        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getTags() != null) product.setTags(String.join(",", request.getTags()));
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getIsDigital() != null) product.setIsDigital(request.getIsDigital());
        if (request.getDigitalFileUrl() != null) product.setDigitalFileUrl(request.getDigitalFileUrl());
        if (request.getLowStockThreshold() != null) product.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getMainImageUrl() != null) product.setMainImageUrl(request.getMainImageUrl());
        if (request.getImages() != null) product.setImages(request.getImages());
        if (request.getVideoUrl() != null) product.setVideoUrl(request.getVideoUrl());
        if (request.getMetaTitle() != null) product.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) product.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null) product.setMetaKeywords(request.getMetaKeywords());

        product.setUpdatedBy(userId);

        Product updatedProduct = productRepository.save(product);

        // Update category product counts if category changed
        Category newCategory = updatedProduct.getCategory();
        if (oldCategory != null && newCategory != null) {
            if (!oldCategory.getId().equals(newCategory.getId())) {
                categoryService.decrementProductCount(oldCategory.getId());
                categoryService.incrementProductCount(newCategory.getId());
                logger.info("Updated product counts - decremented category ID: {}, incremented category ID: {}",
                        oldCategory.getId(), newCategory.getId());
            }
        } else if (oldCategory != null && newCategory == null) {
            categoryService.decrementProductCount(oldCategory.getId());
            logger.info("Decremented product count for category ID: {}", oldCategory.getId());
        } else if (oldCategory == null && newCategory != null) {
            categoryService.incrementProductCount(newCategory.getId());
            logger.info("Incremented product count for category ID: {}", newCategory.getId());
        }

        logger.info("Product updated successfully: {}", updatedProduct.getName());
        return mapToResponseDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId) throws ProductNotFoundException {
        logger.info("Deleting product with ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        Long shopId = product.getShop().getId();
        Category category = product.getCategory();

        // Delete product
        productRepository.delete(product);

        // Update shop product count
        shopService.decrementProductCount(shopId);
        logger.info("Decremented product count for shop ID: {}", shopId);

        // Update category product count
        if (category != null) {
            categoryService.decrementProductCount(category.getId());
            logger.info("Decremented product count for category ID: {}", category.getId());
        }

        logger.info("Product deleted successfully with ID: {}", productId);
    }

    public ProductResponseDto getProductById(Long productId) throws ProductNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        return mapToResponseDto(product);
    }

    public ProductResponseDto getProductBySlug(String slug) throws ProductNotFoundException {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with slug: " + slug));

        // Increment view count
        productRepository.incrementViewCount(product.getId());
        product.setViewCount(product.getViewCount() + 1);

        return mapToResponseDto(product);
    }

    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    public Page<ProductResponseDto> getPublishedProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.PUBLISHED, pageable)
                .map(this::mapToResponseDto);
    }

    public List<ProductResponseDto> getProductsByShop(Long shopId) {
        return productRepository.findByShopId(shopId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDto> getProductsByShopPaginated(Long shopId, Pageable pageable) {
        return productRepository.findByShopId(shopId, pageable)
                .map(this::mapToResponseDto);
    }

    public List<ProductResponseDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .filter(product -> product.getStatus() == ProductStatus.PUBLISHED)
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDto> getProductsByCategoryPaginated(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToResponseDto);
    }

    public List<ProductResponseDto> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndStatus(ProductStatus.PUBLISHED).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getOnSaleProducts() {
        return productRepository.findOnSaleProducts().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable)
                .map(this::mapToResponseDto);
    }

    public Page<ProductResponseDto> filterProducts(ProductFilterDto filter, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterBy(filter);

        // Apply sorting
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return productRepository.findAll(spec, sortedPageable)
                .map(this::mapToResponseDto);
    }

    public List<ProductResponseDto> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getLowStockProductsByShop(Long shopId) {
        return productRepository.findLowStockProductsByShop(shopId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean reserveStock(Long productId, Integer quantity) throws InsufficientStockException {
        int updated = productRepository.reserveStock(productId, quantity);
        if (updated == 0) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && product.getQuantity() < quantity) {
                throw new InsufficientStockException("Insufficient stock. Available: " +
                        product.getAvailableQuantity() + ", Requested: " + quantity);
            }
            return false;
        }
        return true;
    }

    @Transactional
    public void confirmStockReduction(Long productId, Integer quantity) {
        productRepository.confirmStockReduction(productId, quantity);
    }

    @Transactional
    public void releaseReservedStock(Long productId, Integer quantity) {
        productRepository.releaseReservedStock(productId, quantity);
    }

    /**
     * Reassign all products from one category to another
     * Useful when deleting a category that has products
     */
    @Transactional
    public void reassignProductsCategory(Long oldCategoryId, Long newCategoryId) {
        Category newCategory = categoryRepository.findById(newCategoryId)
                .orElseThrow(() -> new RuntimeException("New category not found with ID: " + newCategoryId));

        List<Product> products = productRepository.findByCategoryId(oldCategoryId);

        for (Product product : products) {
            product.setCategory(newCategory);
            productRepository.save(product);
        }

        logger.info("Reassigned {} products from category ID: {} to category ID: {}",
                products.size(), oldCategoryId, newCategoryId);
    }

    /**
     * Get total product count for a specific shop
     */
    public Long getProductCountByShop(Long shopId) {
        return productRepository.countPublishedProductsByShop(shopId);
    }

    /**
     * Get product by SKU
     */
    public ProductResponseDto getProductBySku(String sku) throws ProductNotFoundException {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return mapToResponseDto(product);
    }

    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getDescription(),
                product.getShortDescription(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getCostPrice(),
                product.getDiscountPercentage(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity(),
                product.getSkuBarcode(),
                product.getWeight(),
                product.getWeightUnit(),
                product.getDimensions(),
                product.getShop().getId(),
                product.getShop().getName(),
                product.getShop().getSlug(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getBrand(),
                product.getTags() != null ? List.of(product.getTags().split(",")) : List.of(),
                product.getStatus(),
                product.getIsFeatured(),
                product.getIsDigital(),
                product.getDigitalFileUrl(),
                product.getLowStockThreshold(),
                product.isLowStock(),
                product.isOutOfStock(),
                product.isInStock(),
                product.getMainImageUrl(),
                product.getImages(),
                product.getVideoUrl(),
                product.getMetaTitle(),
                product.getMetaDescription(),
                product.getMetaKeywords(),
                product.getAverageRating(),
                product.getTotalReviews(),
                product.getTotalSales(),
                product.getViewCount(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getPublishedAt()
        );
    }
}