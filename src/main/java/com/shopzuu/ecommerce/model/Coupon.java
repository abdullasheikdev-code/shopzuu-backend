package com.shopzuu.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent;

    @Column(name = "max_discount")
    private Double maxDiscount;

    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "times_used")
    private Integer timesUsed = 0;
}