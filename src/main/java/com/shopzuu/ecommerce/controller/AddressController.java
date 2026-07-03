package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.request.AddressRequest;
import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.Address;
import com.shopzuu.ecommerce.model.User;
import com.shopzuu.ecommerce.repository.AddressRepository;
import com.shopzuu.ecommerce.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(
                ApiResponse.success("Addresses fetched",
                        addressRepository.findByUserId(user.getId()))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .isDefault(request.isDefault())
                .build();

        addressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.success("Address added", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }
}