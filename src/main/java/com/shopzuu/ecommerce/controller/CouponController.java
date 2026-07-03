package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.model.Coupon;
import com.shopzuu.ecommerce.repository.CouponRepository;
import com.shopzuu.ecommerce.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponRepository couponRepository;
    private final CouponService couponService;

    // Admin: Create Coupon
    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(
            @RequestBody Coupon coupon) {

        couponRepository.save(coupon);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon created", coupon)
        );
    }

    // Admin: List Coupons
    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Coupons fetched",
                        couponRepository.findAll()
                )
        );
    }

    // Buyer: Validate Coupon
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(
            @RequestParam String code,
            @RequestParam Double orderAmount) {

        Coupon coupon =
                couponService.validateCoupon(code, orderAmount);

        double discount =
                couponService.calculateDiscount(coupon, orderAmount);

        double finalAmount = orderAmount - discount;

        Map<String, Object> result = new HashMap<>();
        result.put("code", coupon.getCode());
        result.put("discountAmount", discount);
        result.put("finalAmount", finalAmount);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon applied", result)
        );
    }
}