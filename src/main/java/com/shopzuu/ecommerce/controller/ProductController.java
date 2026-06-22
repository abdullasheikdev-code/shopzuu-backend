package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.request.ProductRequest;
import com.shopzuu.ecommerce.dto.response.*;
import com.shopzuu.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─── PUBLIC ENDPOINTS ───────────────────────────────────────

    @GetMapping("/api/products/public/all")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(
                ApiResponse.success("Products fetched",
                        productService.getAllActiveProducts())
        );
    }

    @GetMapping("/api/products/public/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched",
                        productService.getProductById(id))
        );
    }

    @GetMapping("/api/products/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeatured() {
        return ResponseEntity.ok(
                ApiResponse.success("Featured products",
                        productService.getFeaturedProducts())
        );
    }

    @GetMapping("/api/products/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(
                ApiResponse.success("Search results",
                        productService.searchProducts(keyword))
        );
    }

    @GetMapping("/api/products/public/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(
                ApiResponse.success("Products by category",
                        productService.getByCategory(categoryId))
        );
    }

    @GetMapping("/api/products/public/price-range")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(
                ApiResponse.success("Products by price range",
                        productService.getByPriceRange(min, max))
        );
    }

    // ─── VENDOR ENDPOINTS ───────────────────────────────────────

    @PostMapping("/api/vendor/products")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added",
                        productService.addProduct(
                                request, userDetails.getUsername())
                ));
    }

    @PutMapping("/api/vendor/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated",
                        productService.updateProduct(
                                id, request, userDetails.getUsername())
                )
        );
    }

    @DeleteMapping("/api/vendor/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.deleteProduct(id, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted", null)
        );
    }

    @GetMapping("/api/vendor/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getVendorProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Your products",
                        productService.getVendorProducts(
                                userDetails.getUsername())
                )
        );
    }
}