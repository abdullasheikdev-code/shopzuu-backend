package com.shopzuu.ecommerce.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private Double totalAmount;
    private Integer totalItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemResponse {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private String productImage;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }
}