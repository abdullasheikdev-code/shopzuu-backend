package com.shopzuu.ecommerce.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String buyerName;
    private String buyerEmail;
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private Double platformCommission;
    private Double vendorPayout;
    private String status;
    private String paymentStatus;
    private String shippingAddress;
    private String createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private String vendorShopName;
        private Integer quantity;
        private Double unitPrice;
        private Double subtotal;
        private Double commissionAmount;
        private Double vendorEarning;
    }
}