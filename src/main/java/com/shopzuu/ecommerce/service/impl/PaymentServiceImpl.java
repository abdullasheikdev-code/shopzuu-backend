package com.shopzuu.ecommerce.service.impl;

import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl {

    public Map<String, String> createStripePaymentIntent(Long orderId) {
        return new HashMap<>();
    }
    public Map<String, Object>createRazorpayOrder(Long orderId){
        return new HashMap<>();
    }

    public String verifyRazorpayPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            Long orderId) {
        return "Payment Verified";
    }
    public void handleStripeWebhook(
            String payload,
            String sigHeader) {
    }
}