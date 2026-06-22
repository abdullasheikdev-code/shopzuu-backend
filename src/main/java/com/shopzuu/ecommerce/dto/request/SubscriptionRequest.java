package com.shopzuu.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequest {

    @NotBlank(message = "Plan is required")
    private String plan; // FREE, BASIC, PRO

    private String stripePaymentMethodId;
}