package com.example.service;

import com.example.model.Cart;
import com.example.repository.CartRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {
    private CartRepository repository;

    public String getOrGenerateCartId(HttpServletRequest request) {
        // Check if the cartId exists in the cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("cartId".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // If cartId doesn't exist, generate a new one
        String cartId = generateCartId(); // custom function to generate a unique cart ID
        return cartId;
    }

    private String generateCartId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public boolean findCookieByName(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean deleteChosenProduct(String cartId, String productId) {
        boolean productDeleted = false;
        Cart cart = repository.findById(cartId).orElseThrow();
        List<Map<String, Integer>> products = cart.getProductIds();

        Iterator<Map<String, Integer>> iterator = products.iterator();
        while (iterator.hasNext()) {
            Map<String, Integer> product = iterator.next();
            for (String key : product.keySet()) {
                if(key.equals(productId)) {
                    iterator.remove();
                    productDeleted = true;
                    break;
                }
            }
        }
        repository.save(cart);
        return productDeleted;
    }

    public Cookie createCookie(String name, String cartId) {
        Cookie cookie = new Cookie(name, cartId);
        cookie.setMaxAge(60 * 60 * 24 * 90); // 90 days expiration
        cookie.setPath("/api/catalog/api/cart/add-to-cart");
        cookie.setDomain("localhost");
        cookie.setHttpOnly(true);
        return cookie;
    }

    public void addProductToCart(String productIdString, Cart cart) {
        cart.setCreatedAt(new Date(System.currentTimeMillis()));
        cart.setAccessedAt(new Date(System.currentTimeMillis()));
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));

        List<Map<String, Integer>> productList = cart.getProductIds();
        Map<String, Integer> product = new HashMap<>();
        product.put(productIdString, 1);
        productList.add(product);

        cart.setProductIds(productList);
        repository.save(cart);
    }
}
