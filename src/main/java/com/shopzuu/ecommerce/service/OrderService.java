package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.util.MoneyUtil;
import com.shopzuu.ecommerce.dto.request.OrderRequest;
import com.shopzuu.ecommerce.dto.request.ShipmentRequest;
import com.shopzuu.ecommerce.dto.response.OrderResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final CommissionService commissionService;
    private final CouponService couponService;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String email) {

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(buyer)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();

        double originalTotal = 0.0;

        for (CartItem item : cart.getItems()) {

            Product product = item.getProduct();

            double sellingPrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : product.getPrice();

            originalTotal = MoneyUtil.round(
                    originalTotal + (sellingPrice * item.getQuantity())
            );
        }

        double discountAmount = 0.0;

        if (request.getCouponCode() != null &&
                !request.getCouponCode().isBlank()) {

            Coupon coupon = couponService.validateCoupon(
                    request.getCouponCode(),
                    originalTotal
            );

            discountAmount = couponService.calculateDiscount(
                    coupon,
                    originalTotal
            );

            couponService.markCouponUsed(coupon);
        }

        double finalTotal = MoneyUtil.round(
                originalTotal - discountAmount
        );

        double totalCommission = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for: "
                                + product.getName()
                );
            }

            double sellingPrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : product.getPrice();

            double subtotal = MoneyUtil.round(
                    sellingPrice * cartItem.getQuantity()
            );

            double ratio = subtotal / originalTotal;

            double itemDiscount = MoneyUtil.round(
                    discountAmount * ratio
            );

            double discountedSubtotal = MoneyUtil.round(
                    subtotal - itemDiscount
            );

            Vendor vendor = product.getVendor();

            double commissionRate =
                    commissionService.getCommissionRate(vendor);

            double commissionAmount =
                    commissionService.calculateCommission(
                            discountedSubtotal,
                            commissionRate
                    );

            double vendorEarning =
                    commissionService.calculateVendorEarning(
                            discountedSubtotal,
                            commissionAmount
                    );

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .vendor(vendor)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(sellingPrice)
                    .subtotal(discountedSubtotal)
                    .commissionAmount(commissionAmount)
                    .vendorEarning(vendorEarning)
                    .build();

            orderItems.add(orderItem);

            totalCommission = MoneyUtil.round(
                    totalCommission + commissionAmount
            );
        }

        double vendorPayout = MoneyUtil.round(
                finalTotal - totalCommission
        );

        Order order = Order.builder()
                .buyer(buyer)
                .totalAmount(finalTotal)
                .platformCommission(totalCommission)
                .vendorPayout(vendorPayout)
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        order.setItems(orderItems);

        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse markAsPaid(Long orderId, String paymentId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            return mapToResponse(order);
        }

        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setPaymentId(paymentId);

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                        "Product went out of stock before payment."
                );
            }

            product.setStock(
                    product.getStock() - item.getQuantity()
            );

            product.setTotalSold(
                    product.getTotalSold() + item.getQuantity()
            );

            productRepository.save(product);

            Vendor vendor = item.getVendor();

            vendor.setTotalEarnings(
                    MoneyUtil.round(
                            (vendor.getTotalEarnings() == null
                                    ? 0.0
                                    : vendor.getTotalEarnings())
                                    + item.getVendorEarning()
                    )
            );

            vendor.setPlatformCommissionPaid(
                    MoneyUtil.round(
                            (vendor.getPlatformCommissionPaid() == null
                                    ? 0.0
                                    : vendor.getPlatformCommissionPaid())
                                    + item.getCommissionAmount()
                    )
            );

            vendorRepository.save(vendor);
        }

        orderRepository.save(order);

        return mapToResponse(order);
    }
    // Buyer: get my orders
    public List<OrderResponse> getMyOrders(String email) {

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return orderRepository.findByBuyerId(buyer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Vendor: get orders for my products
    public List<OrderResponse> getVendorOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        return orderRepository.findOrdersByVendorId(vendor.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // Vendor: Ship Order
    @Transactional
    public OrderResponse shipOrder(
            Long orderId,
            ShipmentRequest request,
            String email) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        // Only PAID orders can be shipped
        if (order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            throw new RuntimeException("Cannot ship an unpaid order.");
        }

        // Order must already be CONFIRMED
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Only CONFIRMED orders can be shipped."
            );
        }

        order.setStatus(Order.OrderStatus.SHIPPED);

        order.setCourierName(request.getCourierName());
        order.setTrackingNumber(request.getTrackingNumber());
        order.setTrackingUrl(request.getTrackingUrl());
        order.setShippedAt(LocalDateTime.now());

        orderRepository.save(order);

        return mapToResponse(order);
    }


    // Vendor: Update Order Status
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            String status,
            String email) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        Order.OrderStatus newStatus =
                Order.OrderStatus.valueOf(status.toUpperCase());

        Order.OrderStatus currentStatus = order.getStatus();

        switch (currentStatus) {

            case PENDING -> {
                throw new RuntimeException(
                        "Pending orders can only be confirmed after successful payment."
                );
            }

            case CONFIRMED -> {

                if (newStatus == Order.OrderStatus.CANCELLED) {

                    for (OrderItem item : order.getItems()) {

                        Product product = item.getProduct();

                        product.setStock(
                                product.getStock() + item.getQuantity()
                        );

                        product.setTotalSold(
                                Math.max(
                                        0,
                                        product.getTotalSold() - item.getQuantity()
                                )
                        );

                        productRepository.save(product);

                        Vendor vendor = item.getVendor();

                        vendor.setTotalEarnings(
                                MoneyUtil.round(
                                        Math.max(
                                                0,
                                                vendor.getTotalEarnings()
                                                        - item.getVendorEarning()
                                        )
                                )
                        );

                        vendor.setPlatformCommissionPaid(
                                MoneyUtil.round(
                                        Math.max(
                                                0,
                                                vendor.getPlatformCommissionPaid()
                                                        - item.getCommissionAmount()
                                        )
                                )
                        );

                        vendorRepository.save(vendor);
                    }

                } else if (newStatus != Order.OrderStatus.SHIPPED) {

                    throw new RuntimeException(
                            "Confirmed orders can only be SHIPPED or CANCELLED."
                    );
                }
            }
            case SHIPPED -> {

                if (newStatus != Order.OrderStatus.DELIVERED) {

                    throw new RuntimeException(
                            "Shipped orders can only be DELIVERED."
                    );
                }
            }

            case DELIVERED -> {
                throw new RuntimeException(
                        "Delivered orders cannot be modified."
                );
            }

            case CANCELLED -> {
                throw new RuntimeException(
                        "Cancelled orders cannot be modified."
                );
            }

            case REFUNDED -> {
                throw new RuntimeException(
                        "Refunded orders cannot be modified."
                );
            }
        }

        order.setStatus(newStatus);

        orderRepository.save(order);

        return mapToResponse(order);
    }

    // Admin: Get All Orders
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Entity → DTO Mapper
    public OrderResponse mapToResponse(Order order) {

        List<OrderResponse.OrderItemResponse> itemResponses =
                order.getItems() == null
                        ? List.of()
                        : order.getItems()
                        .stream()
                        .map(item ->
                                OrderResponse.OrderItemResponse.builder()
                                        .productId(item.getProduct().getId())
                                        .productName(item.getProduct().getName())
                                        .vendorShopName(item.getVendor().getShopName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .subtotal(item.getSubtotal())
                                        .commissionAmount(item.getCommissionAmount())
                                        .vendorEarning(item.getVendorEarning())
                                        .build()
                        )
                        .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerName(order.getBuyer().getName())
                .buyerEmail(order.getBuyer().getEmail())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .platformCommission(order.getPlatformCommission())
                .vendorPayout(order.getVendorPayout())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .shippingAddress(order.getShippingAddress())

                // Shipment Tracking
                .courierName(order.getCourierName())
                .trackingNumber(order.getTrackingNumber())
                .trackingUrl(order.getTrackingUrl())
                .shippedAt(
                        order.getShippedAt() != null
                                ? order.getShippedAt().toString()
                                : null
                )

                .createdAt(
                        order.getCreatedAt() != null
                                ? order.getCreatedAt().toString()
                                : null
                )
                .build();
    }
}