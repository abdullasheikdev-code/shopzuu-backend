package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.AffiliateProductRequest;
import com.shopzuu.ecommerce.dto.request.ProductRequest;
import com.shopzuu.ecommerce.dto.response.ProductResponse;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ─── VENDOR PRODUCTS ────────────────────────────────────────

    // Vendor adds a product
    @Transactional
    public ProductResponse addProduct(ProductRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (vendor.getStatus() != Vendor.VendorStatus.APPROVED) {
            throw new UnauthorizedException(
                    "Your vendor account is not approved yet"
            );
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        // FREE plan limit: max 10 products
        if (vendor.getPlan() == Vendor.SubscriptionPlan.FREE) {
            Integer count = productRepository.countByVendorId(vendor.getId());
            if (count >= 10) {
                throw new RuntimeException(
                        "Free plan allows max 10 products. Upgrade to Basic or Pro."
                );
            }
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stock(request.getStock())
                .images(request.getImages())
                .category(category)
                .vendor(vendor)
                .productType(Product.ProductType.VENDOR)
                .isFeatured(request.isFeatured())
                .isActive(true)
                .totalSold(0)
                .rating(0.0)
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    // Update product
    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            ProductRequest request,
            String email) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Make sure vendor owns this product
        if (product.getVendor() == null
                || !product.getVendor().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(
                    "You do not own this product"
            );
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStock(request.getStock());
        product.setImages(request.getImages());
        product.setCategory(category);
        product.setFeatured(request.isFeatured());

        productRepository.save(product);
        return mapToResponse(product);
    }

    // Delete product
    @Transactional
    public void deleteProduct(Long productId, String email) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (product.getVendor() == null
                || !product.getVendor().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not own this product");
        }

        product.setActive(false);
        productRepository.save(product);
    }

    // ─── AFFILIATE PRODUCTS (admin only) ──────────────────────

    @Transactional
    public ProductResponse addAffiliateProduct(AffiliateProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stock(999999) // affiliate products are never "out of stock" on your end
                .images(request.getImages())
                .category(category)
                .vendor(null)
                .productType(Product.ProductType.AFFILIATE)
                .affiliateUrl(request.getAffiliateUrl())
                .affiliateSource(request.getAffiliateSource())
                .isFeatured(request.isFeatured())
                .isActive(true)
                .totalSold(0)
                .rating(0.0)
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    @Transactional
    public void deleteAffiliateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        if (product.getProductType() != Product.ProductType.AFFILIATE) {
            throw new RuntimeException("This is not an affiliate product");
        }

        product.setActive(false);
        productRepository.save(product);
    }

    // ─── PUBLIC READ ENDPOINTS ──────────────────────────────────

    // Public: Get all active products
    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Public: Get featured products
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByIsActiveTrueAndIsFeaturedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Public: Get single product
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    // Public: Search products
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Public: Filter by category
    public List<ProductResponse> getByCategory(Long categoryId) {
        return productRepository.findActiveByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Public: Filter by price range
    public List<ProductResponse> getByPriceRange(
            Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Vendor: Get own products
    public List<ProductResponse> getVendorProducts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        return productRepository.findByVendor(vendor)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── MAPPER ─────────────────────────────────────────────────

    // Map entity to response
    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .stock(product.getStock())
                .images(product.getImages())
                .categoryName(product.getCategory() != null
                        ? product.getCategory().getName() : null)
                .vendorShopName(product.getVendor() != null
                        ? product.getVendor().getShopName()
                        : product.getAffiliateSource())
                .vendorId(product.getVendor() != null
                        ? product.getVendor().getId()
                        : null)
                .isFeatured(product.isFeatured())
                .isActive(product.isActive())
                .totalSold(product.getTotalSold())
                .rating(product.getRating())
                .createdAt(product.getCreatedAt() != null
                        ? product.getCreatedAt().toString() : null)
                .productType(product.getProductType().name())
                .affiliateUrl(product.getAffiliateUrl())
                .affiliateSource(product.getAffiliateSource())
                .build();
    }
}