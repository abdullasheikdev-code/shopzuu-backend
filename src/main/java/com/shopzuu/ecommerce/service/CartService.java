package com.shopzuu.ecommerce.service;

import com.shopzuu.ecommerce.dto.request.CartRequest;
import com.shopzuu.ecommerce.dto.response.CartResponse;
import com.shopzuu.ecommerce.exception.ResourceNotFoundException;
import com.shopzuu.ecommerce.model.Cart;
import com.shopzuu.ecommerce.model.CartItem;
import com.shopzuu.ecommerce.model.Product;
import com.shopzuu.ecommerce.model.User;
import com.shopzuu.ecommerce.repository.CartItemRepository;
import com.shopzuu.ecommerce.repository.CartRepository;
import com.shopzuu.ecommerce.repository.ProductRepository;
import com.shopzuu.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        Double unitPrice = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getPrice();

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                );

        if (existingItem.isPresent()) {

            CartItem item = existingItem.get();

            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(unitPrice * item.getQuantity());
            item.setVendorEarning(item.getSubtotal());

            cartItemRepository.save(item);

        } else {

            Double subtotal = unitPrice * request.getQuantity();

            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .vendor(product.getVendor())
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .commissionAmount(0.0)
                    .vendorEarning(subtotal)
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

            Double unitPrice = item.getProduct().getDiscountPrice() != null
                    ? item.getProduct().getDiscountPrice()
                    : item.getProduct().getPrice();

            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setSubtotal(unitPrice * quantity);
            item.setVendorEarning(unitPrice * quantity);

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
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        var items = cart.getItems() == null
                ? java.util.List.<CartResponse.CartItemResponse>of()
                : cart.getItems().stream()
                .map(item -> {

                    Double unitPrice = item.getProduct().getDiscountPrice() != null
                            ? item.getProduct().getDiscountPrice()
                            : item.getProduct().getPrice();

                    return CartResponse.CartItemResponse.builder()
                            .cartItemId(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .productImage(
                                    item.getProduct().getImages() != null
                                            && !item.getProduct().getImages().isEmpty()
                                            ? item.getProduct().getImages().get(0)
                                            : null
                            )
                            .price(unitPrice)
                            .quantity(item.getQuantity())
                            .subtotal(unitPrice * item.getQuantity())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        Double totalAmount = items.stream()
                .mapToDouble(CartResponse.CartItemResponse::getSubtotal)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(items.size())
                .build();
    }

    // Clear cart after order
    @Transactional
    public void clearCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());
    }
}