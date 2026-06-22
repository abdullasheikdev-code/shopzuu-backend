package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.request.SubscriptionRequest;
import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<String>> subscribe(
            @Valid @RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = subscriptionService.subscribe(
                request, userDetails.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @GetMapping("/current-plan")
    public ResponseEntity<ApiResponse<String>> getCurrentPlan(
            @AuthenticationPrincipal UserDetails userDetails) {
        String plan = subscriptionService.getCurrentPlan(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(
                ApiResponse.success("Current plan", plan)
        );
    }
}