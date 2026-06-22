package com.shopzuu.ecommerce.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDashboardResponse {

    private String shopName;
    private String plan;
    private String vendorStatus;
    private Double totalEarnings;
    private Double platformCommissionPaid;
    private Integer totalProducts;
    private Integer totalOrders;
    private Integer pendingOrders;
    private Double thisMonthEarnings;
    private List<RecentOrderSummary> recentOrders;
    private List<TopProduct> topProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentOrderSummary {
        private String orderNumber;
        private String buyerName;
        private Double amount;
        private String status;
        private String date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProduct {
        private String productName;
        private Integer totalSold;
        private Double revenue;
    }
}