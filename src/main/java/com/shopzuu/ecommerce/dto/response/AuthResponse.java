package com.shopzuu.ecommerce.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String role;
    private String name;
    private String email;
    private Long userId;
    private Long vendorId; // null if BUYER
    private String message;
}