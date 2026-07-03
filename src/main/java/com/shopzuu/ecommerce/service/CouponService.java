package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.Coupon;
import com.shopzuu.ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public Coupon validateCoupon(String code, Double orderAmount) {

        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid or expired coupon"));

        if (coupon.getValidUntil() != null &&
                coupon.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (coupon.getMinOrderAmount() != null &&
                orderAmount < coupon.getMinOrderAmount()) {
            throw new RuntimeException(
                    "Minimum order amount is ₹" + coupon.getMinOrderAmount());
        }

        if (coupon.getUsageLimit() != null &&
                coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached");
        }

        return coupon;
    }

    public double calculateDiscount(Coupon coupon, Double orderAmount) {

        double discount =
                orderAmount * (coupon.getDiscountPercent() / 100.0);

        if (coupon.getMaxDiscount() != null &&
                discount > coupon.getMaxDiscount()) {
            discount = coupon.getMaxDiscount();
        }

        return Math.round(discount * 100.0) / 100.0;
    }

    @Transactional
    public void markCouponUsed(Coupon coupon) {

        coupon.setTimesUsed(coupon.getTimesUsed() + 1);

        couponRepository.save(coupon);
    }
}