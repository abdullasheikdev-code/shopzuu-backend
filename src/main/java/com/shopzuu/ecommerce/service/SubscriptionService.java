package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.SubscriptionRequest;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final CommissionService commissionService;

    @Transactional
    public String subscribe(SubscriptionRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        Vendor.SubscriptionPlan plan =
                Vendor.SubscriptionPlan.valueOf(request.getPlan().toUpperCase());

        Double price = commissionService.getPlanPrice(plan);

        // Cancel existing active subscription
        subscriptionRepository.findByVendorIdAndStatus(
                vendor.getId(),
                Subscription.SubscriptionStatus.ACTIVE
        ).ifPresent(existing -> {
            existing.setStatus(Subscription.SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(existing);
        });

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .vendor(vendor)
                .plan(plan)
                .amountPaid(price)
                .stripeSubscriptionId(request.getStripePaymentMethodId())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        subscriptionRepository.save(subscription);

        // Update vendor plan
        vendor.setPlan(plan);
        vendorRepository.save(vendor);

        return "Subscribed to " + plan.name() + " plan successfully";
    }

    public String getCurrentPlan(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vendor vendor = vendorRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));
        return vendor.getPlan().name();
    }
}