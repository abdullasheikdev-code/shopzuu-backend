package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.response.*;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    // Get vendor dashboard
    public VendorDashboardResponse getDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now();

        Double thisMonthEarnings = orderRepository.earningsByVendorBetween(
                vendor.getId(), monthStart, monthEnd
        );

        Integer totalOrders = orderRepository
                .findOrdersByVendorId(vendor.getId()).size();

        Integer pendingOrders = orderRepository
                .findOrdersByVendorIdAndStatus(
                        vendor.getId(),
                        Order.OrderStatus.PENDING
                ).size();

        // Recent orders
        List<VendorDashboardResponse.RecentOrderSummary> recentOrders =
                orderRepository.findOrdersByVendorId(vendor.getId())
                        .stream()
                        .limit(5)
                        .map(order -> VendorDashboardResponse.RecentOrderSummary
                                .builder()
                                .orderNumber(order.getOrderNumber())
                                .buyerName(order.getBuyer().getName())
                                .amount(order.getTotalAmount())
                                .status(order.getStatus().name())
                                .date(order.getCreatedAt().toString())
                                .build())
                        .collect(Collectors.toList());

        // Top products
        List<VendorDashboardResponse.TopProduct> topProducts =
                productRepository.findTopSellingByVendor(vendor.getId())
                        .stream()
                        .limit(5)
                        .map(product -> VendorDashboardResponse.TopProduct.builder()
                                .productName(product.getName())
                                .totalSold(product.getTotalSold())
                                .revenue(product.getPrice() * product.getTotalSold())
                                .build())
                        .collect(Collectors.toList());

        return VendorDashboardResponse.builder()
                .shopName(vendor.getShopName())
                .plan(vendor.getPlan().name())
                .vendorStatus(vendor.getStatus().name())
                .totalEarnings(vendor.getTotalEarnings())
                .platformCommissionPaid(vendor.getPlatformCommissionPaid())
                .totalProducts(productRepository
                        .countByVendorId(vendor.getId()))
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .thisMonthEarnings(
                        thisMonthEarnings != null ? thisMonthEarnings : 0.0
                )
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }
}