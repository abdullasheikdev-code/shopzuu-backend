package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.CartRequest;
import com.shopzuu.ecommerce.dto.response.CartResponse;
import com.shopzuu.ecommerce.exception.*;
import com.shopzuu.ecommerce.model.*;
import com.shopzuu.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Add item to cart
    @Transactional
    public CartResponse addToCart(CartRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new RuntimeException("Product is not available");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        // If product already in cart, update quantity
        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                );

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
        }

        return getCart(email);
    }

    // Update quantity
    @Transactional
    public CartResponse updateCartItem(
            Long cartItemId,
            Integer quantity,
            String email) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCart(email);
    }

    // Remove item from cart
    @Transactional
    public CartResponse removeFromCart(Long cartItemId, String email) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found"));
        cartItemRepository.delete(item);
        return getCart(email);
    }

    // Get cart
    public CartResponse getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        List<CartResponse.CartItemResponse> items = cart.getItems() == null
                ? List.of()
                : cart.getItems().stream()
                .map(item -> CartResponse.CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImage(item.getProduct().getImages() != null
                                && !item.getProduct().getImages().isEmpty()
                                ? item.getProduct().getImages().get(0) : null)
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .totalItems(items.size())
                .build();
    }

    // Clear cart after order
    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cartItemRepository.deleteByCartId(cart.getId());
    }
}