package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.request.AffiliateProductRequest;
import com.shopzuu.ecommerce.dto.request.VendorStatusRequest;
import com.shopzuu.ecommerce.dto.response.AdminDashboardResponse;
import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.dto.response.ProductResponse;
import com.shopzuu.ecommerce.model.Vendor;
import com.shopzuu.ecommerce.service.AdminService;
import com.shopzuu.ecommerce.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin dashboard",
                        adminService.getDashboard()
                )
        );
    }

    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<List<Vendor>>> getAllVendors() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "All vendors",
                        adminService.getAllVendors()
                )
        );
    }

    @GetMapping("/vendors/pending")
    public ResponseEntity<ApiResponse<List<Vendor>>> getPendingVendors() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Pending vendors",
                        adminService.getPendingVendors()
                )
        );
    }

    @PutMapping("/vendors/{vendorId}/status")
    public ResponseEntity<ApiResponse<String>> updateVendorStatus(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorStatusRequest request) {

        String result = adminService.updateVendorStatus(vendorId, request);

        return ResponseEntity.ok(
                ApiResponse.success(result, null)
        );
    }

    @PostMapping("/affiliate-products")
    public ResponseEntity<ApiResponse<ProductResponse>> addAffiliateProduct(
            @Valid @RequestBody AffiliateProductRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Affiliate product added",
                                productService.addAffiliateProduct(request)
                        )
                );
    }
}