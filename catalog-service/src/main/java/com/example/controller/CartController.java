package com.example.controller;

import com.example.model.Cart;
import com.example.repository.CartRepository;
import com.example.service.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {
    private CartService service;
    private CartRepository repository;

    @PostMapping("/add-to-cart")
    public ResponseEntity<?> addToCartV2(
            @RequestBody Map<String, String> productId,
            HttpServletRequest request,
            HttpServletResponse response) {
        // if there is cookie "cartId"
        String productIdString = productId.get("productId");
        if (service.findCookieByName(request.getCookies(),"cartId")) {
            boolean isDeleted;
            try {
                isDeleted = service.deleteChosenProduct(service.getOrGenerateCartId(request),productIdString);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if(isDeleted) {
                Cookie cookie = service.createCookie("cartId",service.getOrGenerateCartId(request));
                response.addCookie(cookie);
            } else { // add new product to existing cart
                String cartId = service.getOrGenerateCartId(request);
                Cart cart = repository.findById(cartId).orElseThrow();
                service.addProductToCart(productIdString,cart);
                Cookie cookie = service.createCookie("cartId",cartId);
                response.addCookie(cookie);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            String cartId = service.getOrGenerateCartId(request);
            Cart cart = new Cart();
            cart.setCartId(cartId);
            service.addProductToCart(productIdString,cart);
            Cookie cookie = service.createCookie("cartId",cartId);
            response.addCookie(cookie);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
