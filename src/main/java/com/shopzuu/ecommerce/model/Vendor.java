package com.shopzuu.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(name = "shop_description")
    private String shopDescription;

    @Column(name = "shop_logo")
    private String shopLogo;

    @Column(name = "commission_rate")
    private Double commissionRate = 3.0;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    private VendorStatus status = VendorStatus.PENDING;

    @Column(name = "total_earnings")
    private Double totalEarnings = 0.0;

    @Column(name = "platform_commission_paid")
    private Double platformCommissionPaid = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    private List<Subscription> subscriptions;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public enum SubscriptionPlan {
        FREE,
        BASIC,
        PRO
    }

    public enum VendorStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SUSPENDED
    }
}