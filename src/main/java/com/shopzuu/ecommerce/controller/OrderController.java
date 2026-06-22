package com.shopzuu.ecommerce.controller;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.shopzuu.ecommerce.dto.request.*;
import com.shopzuu.ecommerce.dto.response.*;
import com.shopzuu.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name= "bearerAuth")

public class OrderController {

    private final OrderService orderService;

    // ─── BUYER ──────────────────────────────────────────────────

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed",
                        orderService.placeOrder(
                                request, userDetails.getUsername())
                ));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Your orders",
                        orderService.getMyOrders(userDetails.getUsername()))
        );
    }

    @PutMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsPaid(
            @PathVariable Long orderId,
            @RequestParam String paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment confirmed",
                        orderService.markAsPaid(orderId, paymentId))
        );
    }

    // ─── VENDOR ─────────────────────────────────────────────────

    @GetMapping("/vendor/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getVendorOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Vendor orders",
                        orderService.getVendorOrders(userDetails.getUsername()))
        );
    }

    @PutMapping("/vendor/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Status updated",
                        orderService.updateOrderStatus(
                                orderId,
                                request.getStatus(),
                                userDetails.getUsername())
                )
        );
    }

    // ─── ADMIN ──────────────────────────────────────────────────

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(
                ApiResponse.success("All orders",
                        orderService.getAllOrders())
        );
    }
}