package com.shopzuu.ecommerce.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String productType;
    private String affiliateUrl;
    private String affiliateSource;

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double discountPrice;
    private Integer stock;
    private List<String> images;
    private String categoryName;
    private String vendorShopName;
    private Long vendorId;
    private boolean isFeatured;
    private boolean isActive;
    private Integer totalSold;
    private Double rating;
    private String createdAt;

}