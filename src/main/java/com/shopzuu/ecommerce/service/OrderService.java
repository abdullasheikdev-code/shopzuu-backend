package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.OrderRequest;
import com.shopzuu.ecommerce.dto.response.OrderResponse;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Place order from cart
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String email) {

        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Build order items and calculate totals
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        double totalCommission = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for: " + product.getName()
                );
            }

            Vendor vendor = product.getVendor();
            double commissionRate =
                    commissionService.getCommissionRate(vendor);
            double subtotal = product.getPrice() * cartItem.getQuantity();
            double commissionAmount =
                    commissionService.calculateCommission(subtotal, commissionRate);
            double vendorEarning =
                    commissionService.calculateVendorEarning(
                            subtotal, commissionAmount
                    );

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .vendor(vendor)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .commissionAmount(commissionAmount)
                    .vendorEarning(vendorEarning)
                    .build();

            orderItems.add(orderItem);
            totalAmount += subtotal;
            totalCommission += commissionAmount;

            // Reduce stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setTotalSold(
                    product.getTotalSold() + cartItem.getQuantity()
            );
            productRepository.save(product);

            // Update vendor earnings
            vendor.setTotalEarnings(vendor.getTotalEarnings() + vendorEarning);
            vendor.setPlatformCommissionPaid(
                    vendor.getPlatformCommissionPaid() + commissionAmount
            );
            vendorRepository.save(vendor);
        }

        double vendorPayout = totalAmount - totalCommission;

        // Create order
        Order order = Order.builder()
                .buyer(buyer)
                .totalAmount(totalAmount)
                .platformCommission(totalCommission)
                .vendorPayout(vendorPayout)
                .status(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        orderRepository.save(order);

        // Link order items to order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        order.setItems(orderItems);

        // Clear cart
        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(order);
    }

    // Mark order as paid after payment
    @Transactional
    public OrderResponse markAsPaid(Long orderId, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setPaymentId(paymentId);
        orderRepository.save(order);
        return mapToResponse(order);
    }

    // Buyer: get my orders
    public List<OrderResponse> getMyOrders(String email) {
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByBuyerId(buyer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Vendor: get orders for my products
    public List<OrderResponse> getVendorOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));
        return orderRepository.findOrdersByVendorId(vendor.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Vendor: update order status
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            String status,
            String email) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        orderRepository.save(order);
        return mapToResponse(order);
    }

    // Admin: get all orders
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Map entity to response
    public OrderResponse mapToResponse(Order order) {

        List<OrderResponse.OrderItemResponse> itemResponses =
                order.getItems() == null ? List.of() :
                        order.getItems().stream()
                                .map(item -> OrderResponse.OrderItemResponse.builder()
                                        .productId(item.getProduct().getId())
                                        .productName(item.getProduct().getName())
                                        .vendorShopName(item.getVendor().getShopName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .subtotal(item.getSubtotal())
                                        .commissionAmount(item.getCommissionAmount())
                                        .vendorEarning(item.getVendorEarning())
                                        .build())
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
                .createdAt(order.getCreatedAt() != null
                        ? order.getCreatedAt().toString() : null)
                .build();
    }
}