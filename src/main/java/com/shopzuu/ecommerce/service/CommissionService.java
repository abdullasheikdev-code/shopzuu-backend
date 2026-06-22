package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.model.*;
import org.springframework.stereotype.Service;

@Service
public class CommissionService {

    // Subscription plan prices in INR
    public static final Double FREE_PLAN_PRICE = 0.0;
    public static final Double BASIC_PLAN_PRICE = 999.0;
    public static final Double PRO_PLAN_PRICE = 2999.0;

    // Commission rates by plan
    public Double getCommissionRate(Vendor vendor) {
        return switch (vendor.getPlan()) {
            case FREE  -> 5.0;   // 5% commission on free plan
            case BASIC -> 3.0;   // 3% commission on basic
            case PRO   -> 1.5;   // 1.5% commission on pro
        };
    }

    // Calculate commission for a single item
    public Double calculateCommission(Double amount, Double rate) {
        return Math.round((amount * rate / 100) * 100.0) / 100.0;
    }

    // Calculate vendor earning after commission
    public Double calculateVendorEarning(Double amount, Double commissionAmount) {
        return Math.round((amount - commissionAmount) * 100.0) / 100.0;
    }

    // Get plan price
    public Double getPlanPrice(Vendor.SubscriptionPlan plan) {
        return switch (plan) {
            case FREE  -> FREE_PLAN_PRICE;
            case BASIC -> BASIC_PLAN_PRICE;
            case PRO   -> PRO_PLAN_PRICE;
        };
    }

    // Get plan features description
    public String getPlanDescription(Vendor.SubscriptionPlan plan) {
        return switch (plan) {
            case FREE  -> "Max 10 products, 5% commission, basic support";
            case BASIC -> "Unlimited products, 3% commission, priority support";
            case PRO   -> "Unlimited products, 1.5% commission, " +
                    "featured listings, dedicated support";
        };
    }
}