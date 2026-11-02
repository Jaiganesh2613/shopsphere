package com.shopsphere.backend.cart;

import com.shopsphere.backend.product.Product;
import com.shopsphere.backend.product.ProductRepository;
import com.shopsphere.backend.product.ResourceNotFoundException;
import com.shopsphere.backend.user.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    )
    {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();

        response.setId(item.getId());
        response.setCartId(item.getCart().getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setProductPrice(item.getProduct().getPrice());
        response.setQuantity(item.getQuantity());

        return response;
    }

    private Cart findOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    public CartItemResponse addProductToCart(User user, AddToCartRequest addToCartRequest) {
        if (addToCartRequest.getQuantity() == null || addToCartRequest.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Product product = productRepository.findById(addToCartRequest.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found: " + addToCartRequest.getProductId()));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("Product is not available for purchase");
        }

        Cart cart = findOrCreateCart(user);

        Optional<CartItem> cartItem =
                cartItemRepository.findByCartIdAndProductId(cart.getId(), addToCartRequest.getProductId());

        CartItem item;

        if (cartItem.isPresent()) {
            item = cartItem.get();

            int newQuantity = item.getQuantity() + addToCartRequest.getQuantity();

            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock available");
            }

            item.setQuantity(newQuantity);

        } else {
            if (addToCartRequest.getQuantity() > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock available");
            }

            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(addToCartRequest.getQuantity());
        }

        return toCartItemResponse(cartItemRepository.save(item));
    }

    public CartResponse getCart(User user) {
        Cart cart = findOrCreateCart(user);

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            CartItemResponse itemResponse = toCartItemResponse(item);
            items.add(itemResponse);

            BigDecimal subtotal = itemResponse.getProductPrice()
                    .multiply(BigDecimal.valueOf(itemResponse.getQuantity()));

            totalAmount = totalAmount.add(subtotal);
        }

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }
}
