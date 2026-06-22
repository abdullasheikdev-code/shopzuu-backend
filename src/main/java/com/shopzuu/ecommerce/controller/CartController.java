package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.request.CartRequest;
import com.shopzuu.ecommerce.dto.response.*;
import com.shopzuu.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Cart fetched",
                        cartService.getCart(userDetails.getUsername()))
        );
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody CartRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart",
                        cartService.addToCart(
                                request, userDetails.getUsername())
                )
        );
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Cart updated",
                        cartService.updateCartItem(
                                cartItemId, quantity, userDetails.getUsername())
                )
        );
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Item removed",
                        cartService.removeFromCart(
                                cartItemId, userDetails.getUsername())
                )
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared", null)
        );
    }
}