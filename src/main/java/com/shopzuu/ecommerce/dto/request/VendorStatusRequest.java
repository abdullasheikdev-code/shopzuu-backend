package com.shopzuu.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // APPROVED, REJECTED, SUSPENDED

    private Double commissionRate; // admin can set custom commission
}