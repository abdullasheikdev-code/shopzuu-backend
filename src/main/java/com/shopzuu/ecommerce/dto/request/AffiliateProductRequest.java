package com.shopzuu.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be greater than 0")
    private Double price;

    private Double discountPrice;

    private List<String> images;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Affiliate URL is required")
    private String affiliateUrl;

    @NotBlank(message = "Affiliate source is required")
    private String affiliateSource; // Amazon, Flipkart, Meesho

    private boolean isFeatured = false;
}