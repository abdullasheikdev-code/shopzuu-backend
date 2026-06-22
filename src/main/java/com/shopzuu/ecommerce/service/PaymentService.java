package com.shopzuu.ecommerce.service;
import com.stripe.param.PaymentIntentCreateParams;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // ─── STRIPE ─────────────────────────────────────────────────

    public Map<String, String> createStripePaymentIntent(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Stripe.apiKey = stripeSecretKey;

        try {
            // Stripe needs amount in smallest currency unit (paise for INR)
            long amountInPaise = (long) (order.getTotalAmount() * 100);

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInPaise)
                            .setCurrency("inr")
                            .putMetadata("orderId", String.valueOf(orderId))
                            .putMetadata("orderNumber", order.getOrderNumber())
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Save pending payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentGatewayId(intent.getId())
                    .gateway(Payment.PaymentGateway.STRIPE)
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("paymentIntentId", intent.getId());
            response.put("orderId", String.valueOf(orderId));

            return response;

        } catch (Exception e) {
            throw new PaymentException(
                    "Stripe payment intent creation failed: " + e.getMessage()
            );
        }
    }

    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, stripeWebhookSecret
            );
        } catch (Exception e) {
            throw new PaymentException("Invalid webhook signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {

            PaymentIntent intent = (PaymentIntent) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() ->
                            new PaymentException("Unable to deserialize payment intent"));

            Payment payment = paymentRepository
                    .findByPaymentGatewayId(intent.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Payment record not found"));

            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            // Mark order as paid
            orderService.markAsPaid(
                    payment.getOrder().getId(),
                    intent.getId()
            );
        }

        if ("payment_intent.payment_failed".equals(event.getType())) {

            PaymentIntent intent = (PaymentIntent) event
                    .getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (intent != null) {
                paymentRepository.findByPaymentGatewayId(intent.getId())
                        .ifPresent(payment -> {
                            payment.setStatus(Payment.PaymentStatus.FAILED);
                            paymentRepository.save(payment);
                        });
            }
        }
    }

    // ─── RAZORPAY ───────────────────────────────────────────────

    public Map<String, Object> createRazorpayOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        try {
            RazorpayClient client =
                    new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            long amountInPaise = (long) (order.getTotalAmount() * 100);
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", order.getOrderNumber());

            com.razorpay.Order razorpayOrder =
                    client.orders.create(orderRequest);

            // Save pending payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentGatewayId(razorpayOrder.get("id"))
                    .gateway(Payment.PaymentGateway.RAZORPAY)
                    .amount(order.getTotalAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", razorpayOrder.get("id"));
            response.put("amount", amountInPaise);
            response.put("currency", "INR");
            response.put("keyId", razorpayKeyId);
            response.put("orderId", orderId);

            return response;

        } catch (RazorpayException e) {
            throw new PaymentException(
                    "Razorpay order creation failed: " + e.getMessage()
            );
        }
    }

    @Transactional
    public String verifyRazorpayPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            Long orderId) {

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(
                    options, razorpayKeySecret
            );

            if (!isValid) {
                throw new PaymentException("Invalid payment signature");
            }

            Payment payment = paymentRepository
                    .findByPaymentGatewayId(razorpayOrderId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Payment record not found"));

            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaymentGatewayId(razorpayPaymentId);
            paymentRepository.save(payment);

            orderService.markAsPaid(orderId, razorpayPaymentId);

            return "Payment verified successfully";

        } catch (RazorpayException e) {
            throw new PaymentException(
                    "Signature verification failed: " + e.getMessage()
            );
        }
    }
}