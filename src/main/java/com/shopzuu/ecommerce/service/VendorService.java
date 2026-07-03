package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.response.VendorDashboardResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.Order;
import com.shopzuu.ecommerce.model.User;
import com.shopzuu.ecommerce.model.Vendor;
import com.shopzuu.ecommerce.repository.OrderItemRepository;
import com.shopzuu.ecommerce.repository.OrderRepository;
import com.shopzuu.ecommerce.repository.ProductRepository;
import com.shopzuu.ecommerce.repository.UserRepository;
import com.shopzuu.ecommerce.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public VendorDashboardResponse getDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);

        LocalDateTime monthEnd = LocalDateTime.now();

        Double totalEarnings =
                orderRepository.totalEarningsByVendor(vendor.getId());

        if (totalEarnings == null) {
            totalEarnings = 0.0;
        }

        Double thisMonthEarnings =
                orderRepository.earningsByVendorBetween(
                        vendor.getId(),
                        monthStart,
                        monthEnd
                );

        if (thisMonthEarnings == null) {
            thisMonthEarnings = 0.0;
        }

        Integer totalOrders =
                orderRepository.countPaidOrdersByVendor(vendor.getId());

        if (totalOrders == null) {
            totalOrders = 0;
        }

        Integer pendingOrders =
                orderRepository.countPendingOrdersByVendor(vendor.getId());

        if (pendingOrders == null) {
            pendingOrders = 0;
        }

        List<VendorDashboardResponse.RecentOrderSummary> recentOrders =
                orderRepository.findRecentOrders(vendor.getId())
                        .stream()
                        .limit(5)
                        .map(order ->
                                VendorDashboardResponse.RecentOrderSummary.builder()
                                        .orderNumber(order.getOrderNumber())
                                        .buyerName(order.getBuyer().getName())
                                        .amount(order.getVendorPayout())
                                        .status(order.getStatus().name())
                                        .date(order.getCreatedAt().toString())
                                        .build()
                        )
                        .collect(Collectors.toList());

        List<VendorDashboardResponse.TopProduct> topProducts =
                orderItemRepository.findTopProductsByVendor(vendor.getId())
                        .stream()
                        .limit(5)
                        .map(row ->
                                VendorDashboardResponse.TopProduct.builder()
                                        .productName((String) row[1])
                                        .totalSold(((Long) row[2]).intValue())
                                        .revenue(((Double) row[3]))
                                        .build()
                        )
                        .collect(Collectors.toList());

        return VendorDashboardResponse.builder()
                .shopName(vendor.getShopName())
                .plan(vendor.getPlan().name())
                .vendorStatus(vendor.getStatus().name())
                .totalEarnings(totalEarnings)
                .platformCommissionPaid(
                        vendor.getPlatformCommissionPaid() == null
                                ? 0.0
                                : vendor.getPlatformCommissionPaid()
                )
                .totalProducts(productRepository.countByVendorId(vendor.getId()))
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .thisMonthEarnings(thisMonthEarnings)
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }
}