package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.response.*;
import com.shopzuu.ecommerce.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<VendorDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard loaded",
                        vendorService.getDashboard(userDetails.getUsername()))
        );
    }
}