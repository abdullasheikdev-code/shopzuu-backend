package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import com.shopzuu.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Object>>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Object> products = wishlistRepository.findByUserId(user.getId())
                .stream()
                .map(w -> productService.mapToResponse(w.getProduct()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched", products));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            return ResponseEntity.ok(ApiResponse.success("Already in wishlist", null));
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
        return ResponseEntity.ok(ApiResponse.success("Added to wishlist", null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean exists = wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Checked", exists));
    }
}