package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Create Stripe payment intent
    @PostMapping("/stripe/create-intent")
    public ResponseEntity<ApiResponse<Map<String, String>>> createStripeIntent(
            @RequestParam Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> response =
                paymentService.createStripePaymentIntent(orderId);
        return ResponseEntity.ok(
                ApiResponse.success("Payment intent created", response)
        );
    }

    // Create Razorpay order
    @PostMapping("/razorpay/create-order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRazorpayOrder(
            @RequestParam Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response =
                paymentService.createRazorpayOrder(orderId);
        return ResponseEntity.ok(
                ApiResponse.success("Razorpay order created", response)
        );
    }

    // Verify Razorpay payment
    @PostMapping("/razorpay/verify")
    public ResponseEntity<ApiResponse<String>> verifyRazorpay(
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature,
            @RequestParam Long orderId) {
        String result = paymentService.verifyRazorpayPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                orderId
        );
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    // Stripe webhook
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("Webhook received");
    }
}