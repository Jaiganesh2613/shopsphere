package com.shopsphere.backend.cart;

import com.shopsphere.backend.product.ResourceNotFoundException;
import com.shopsphere.backend.user.User;
import com.shopsphere.backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @PostMapping("/items")
    public CartItemResponse addProductToCart( @Valid @RequestBody AddToCartRequest addToCartRequest, Authentication authentication) {
        User user = userRepository
                .findByEmail((String) authentication.getPrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
        return cartService.addProductToCart(user, addToCartRequest);
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        User user = userRepository
                .findByEmail((String) authentication.getPrincipal())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User does not exist"));

        return cartService.getCart(user);
    }

}
