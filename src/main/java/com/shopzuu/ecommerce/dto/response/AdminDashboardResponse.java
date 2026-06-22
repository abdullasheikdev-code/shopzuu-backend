package com.shopzuu.ecommerce.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    // Platform Revenue
    private Double totalPlatformRevenue;
    private Double thisMonthRevenue;
    private Double subscriptionRevenue;
    private Double commissionRevenue;

    // Platform Stats
    private Integer totalVendors;
    private Integer activeVendors;
    private Integer pendingVendors;
    private Integer totalBuyers;
    private Integer totalOrders;
    private Integer totalProducts;
    private Double totalGMV; // Gross Merchandise Value

    // Vendor Breakdown
    private List<VendorSummary> topVendors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorSummary {
        private String shopName;
        private String plan;
        private Double totalSales;
        private Double commissionPaid;
        private Integer totalOrders;
    }
}