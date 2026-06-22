package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.*;
import com.shopzuu.ecommerce.dto.response.AuthResponse;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import com.shopzuu.ecommerce.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        User.Role role = User.Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .build();

        userRepository.save(user);

        // If vendor, create vendor profile
        Long vendorId = null;
        if (role == User.Role.VENDOR) {
            if (request.getShopName() == null || request.getShopName().isBlank()) {
                throw new RuntimeException("Shop name is required for vendors");
            }

            Vendor vendor = Vendor.builder()
                    .user(user)
                    .shopName(request.getShopName())
                    .shopDescription(request.getShopDescription())
                    .commissionRate(3.0)
                    .plan(Vendor.SubscriptionPlan.FREE)
                    .status(Vendor.VendorStatus.PENDING)
                    .totalEarnings(0.0)
                    .platformCommissionPaid(0.0)
                    .build();

            vendorRepository.save(vendor);
            vendorId = vendor.getId();
        }

        // Create empty cart for buyer
        if (role == User.Role.BUYER) {
            Cart cart = Cart.builder()
                    .user(user)
                    .build();
            cartRepository.save(cart);
        }

        String token = jwtUtil.generateToken(user.getEmail(), role.name());

        return AuthResponse.builder()
                .token(token)
                .role(role.name())
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getId())
                .vendorId(vendorId)
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        Long vendorId = null;
        if (user.getRole() == User.Role.VENDOR && user.getVendor() != null) {
            vendorId = user.getVendor().getId();
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .userId(user.getId())
                .vendorId(vendorId)
                .message("Login successful")
                .build();
    }
}