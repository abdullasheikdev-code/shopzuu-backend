package com.shopzuu.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Enumerated(EnumType.STRING)
    private Vendor.SubscriptionPlan plan;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        startDate = LocalDateTime.now();
        endDate = LocalDateTime.now().plusMonths(1);
    }

    public enum SubscriptionStatus {
        ACTIVE,
        EXPIRED,
        CANCELLED
    }
}