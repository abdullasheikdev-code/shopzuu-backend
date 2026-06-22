package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.VendorStatusRequest;
import com.shopzuu.ecommerce.dto.response.AdminDashboardResponse;
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
public class AdminService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;

    // Get admin dashboard
    public AdminDashboardResponse getDashboard() {

        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);

        Double totalCommission =
                orderRepository.totalPlatformCommission();
        Double subscriptionRevenue =
                subscriptionRepository.totalSubscriptionRevenue();
        Double monthRevenue = orderRepository
                .platformCommissionBetween(monthStart, LocalDateTime.now());
        Double totalGMV = orderRepository.totalGMV();

        List<AdminDashboardResponse.VendorSummary> topVendors =
                vendorRepository.findTopVendors()
                        .stream()
                        .limit(10)
                        .map(vendor -> AdminDashboardResponse.VendorSummary.builder()
                                .shopName(vendor.getShopName())
                                .plan(vendor.getPlan().name())
                                .totalSales(vendor.getTotalEarnings())
                                .commissionPaid(vendor.getPlatformCommissionPaid())
                                .totalOrders(orderRepository
                                        .findOrdersByVendorId(vendor.getId()).size())
                                .build())
                        .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalPlatformRevenue(
                        (totalCommission != null ? totalCommission : 0.0) +
                                (subscriptionRevenue != null ? subscriptionRevenue : 0.0)
                )
                .thisMonthRevenue(monthRevenue != null ? monthRevenue : 0.0)
                .subscriptionRevenue(
                        subscriptionRevenue != null ? subscriptionRevenue : 0.0
                )
                .commissionRevenue(
                        totalCommission != null ? totalCommission : 0.0
                )
                .totalVendors((int) vendorRepository.count())
                .activeVendors(vendorRepository.countActiveVendors())
                .pendingVendors(vendorRepository.countPendingVendors())
                .totalBuyers((int) userRepository.count())
                .totalOrders(orderRepository.countPaidOrders())
                .totalProducts(productRepository.countActiveProducts())
                .totalGMV(totalGMV != null ? totalGMV : 0.0)
                .topVendors(topVendors)
                .build();
    }

    // Approve or reject vendor
    @Transactional
    public String updateVendorStatus(
            Long vendorId,
            VendorStatusRequest request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        vendor.setStatus(
                Vendor.VendorStatus.valueOf(request.getStatus().toUpperCase())
        );

        if (request.getCommissionRate() != null) {
            vendor.setCommissionRate(request.getCommissionRate());
        }

        vendorRepository.save(vendor);
        return "Vendor status updated to " + request.getStatus();
    }

    // Get all pending vendors
    public List<Vendor> getPendingVendors() {
        return vendorRepository.findByStatus(Vendor.VendorStatus.PENDING);
    }

    // Get all vendors
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
}