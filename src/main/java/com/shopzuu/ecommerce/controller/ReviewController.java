package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.request.ReviewRequest;
import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductReviews(
            @PathVariable Long productId) {

        List<Review> reviews = reviewRepository
                .findByProductIdOrderByCreatedAtDesc(productId);

        List<Map<String, Object>> reviewList = reviews.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId());
                    map.put("userName", r.getUser().getName());
                    map.put("rating", r.getRating());
                    map.put("comment", r.getComment());
                    map.put("createdAt", r.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());

        Double avgRating = reviewRepository.getAverageRating(productId);

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewList);
        response.put("averageRating", avgRating != null ? avgRating : 0.0);
        response.put("totalReviews", reviews.size());

        return ResponseEntity.ok(ApiResponse.success("Reviews fetched", response));
    }

    @PostMapping("/product/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> addReview(
            @PathVariable Long productId,
            @org.springframework.web.bind.annotation.RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // Update product's average rating
        Double newAvg = reviewRepository.getAverageRating(productId);
        product.setRating(newAvg != null ? newAvg : 0.0);
        productRepository.save(product);

        return ResponseEntity.ok(ApiResponse.success("Review added", null));
    }
}