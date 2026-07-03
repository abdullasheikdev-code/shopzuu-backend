package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.VendorStatusRequest;
import com.shopzuu.ecommerce.dto.response.AdminDashboardResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.Vendor;
import com.shopzuu.ecommerce.repository.OrderItemRepository;
import com.shopzuu.ecommerce.repository.OrderRepository;
import com.shopzuu.ecommerce.repository.ProductRepository;
import com.shopzuu.ecommerce.repository.SubscriptionRepository;
import com.shopzuu.ecommerce.repository.UserRepository;
import com.shopzuu.ecommerce.repository.VendorRepository;
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
    private final OrderItemRepository orderItemRepository;

    // ===========================
    // Admin Dashboard
    // ===========================

    public AdminDashboardResponse getDashboard() {

        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);

        Double commissionRevenue = orderRepository.totalPlatformCommission();
        Double subscriptionRevenue = subscriptionRepository.totalSubscriptionRevenue();
        Double monthRevenue = orderRepository.platformCommissionBetween(
                monthStart,
                LocalDateTime.now()
        );
        Double totalGMV = orderRepository.totalGMV();

        if (commissionRevenue == null) commissionRevenue = 0.0;
        if (subscriptionRevenue == null) subscriptionRevenue = 0.0;
        if (monthRevenue == null) monthRevenue = 0.0;
        if (totalGMV == null) totalGMV = 0.0;

        List<AdminDashboardResponse.VendorSummary> topVendors =
                vendorRepository.findTopVendors()
                        .stream()
                        .limit(10)
                        .map(vendor -> {

                            Double earnings =
                                    orderRepository.totalEarningsByVendor(vendor.getId());

                            if (earnings == null) {
                                earnings = 0.0;
                            }

                            Integer orders =
                                    orderRepository.countPaidOrdersByVendor(vendor.getId());

                            if (orders == null) {
                                orders = 0;
                            }

                            Double commission =
                                    orderItemRepository.getTotalCommissionByVendor(vendor.getId());

                            if (commission == null) {
                                commission = 0.0;
                            }

                            return AdminDashboardResponse.VendorSummary.builder()
                                    .shopName(vendor.getShopName())
                                    .plan(vendor.getPlan().name())
                                    .totalSales(earnings)
                                    .commissionPaid(commission)
                                    .totalOrders(orders)
                                    .build();
                        })
                        .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalPlatformRevenue(commissionRevenue + subscriptionRevenue)
                .thisMonthRevenue(monthRevenue)
                .subscriptionRevenue(subscriptionRevenue)
                .commissionRevenue(commissionRevenue)
                .totalVendors((int) vendorRepository.count())
                .activeVendors(vendorRepository.countActiveVendors())
                .pendingVendors(vendorRepository.countPendingVendors())
                .totalBuyers((int) userRepository.count())
                .totalOrders(orderRepository.countPaidOrders())
                .totalProducts(productRepository.countActiveProducts())
                .totalGMV(totalGMV)
                .topVendors(topVendors)
                .build();
    }

    // ===========================
    // Approve / Reject Vendor
    // ===========================

    @Transactional
    public String updateVendorStatus(
            Long vendorId,
            VendorStatusRequest request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        vendor.setStatus(
                Vendor.VendorStatus.valueOf(
                        request.getStatus().toUpperCase()
                )
        );

        if (request.getCommissionRate() != null) {
            vendor.setCommissionRate(request.getCommissionRate());
        }

        vendorRepository.save(vendor);

        return "Vendor status updated successfully";
    }

    // ===========================
    // Pending Vendors
    // ===========================

    public List<Vendor> getPendingVendors() {
        return vendorRepository.findByStatus(Vendor.VendorStatus.PENDING);
    }

    // ===========================
    // All Vendors
    // ===========================

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
}