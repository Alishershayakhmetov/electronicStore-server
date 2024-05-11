package com.example.service;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
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
    private ProductRepository productRepository;

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
        try {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        } catch(NullPointerException e) {
            return false;
        }

    }

    public String getCookieByName(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public boolean deleteChosenProduct(String cartId, String productId) {
        boolean productDeleted = false;
        Cart cart = repository.findById(cartId).orElseThrow();
        List<Map<String, Object>> products = cart.getProductIds();

        Iterator<Map<String, Object>> iterator = products.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> product = iterator.next();
            for (String key : product.keySet()) {
                if (product.get(key).equals(productId)) {
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
        cookie.setPath("/api/catalog/api/cart");
        cookie.setDomain("localhost");
        cookie.setHttpOnly(true);
        return cookie;
    }

    public void addProductToCart(String productIdString, Cart cart) {
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));

        List<Map<String, Object>> productList = cart.getProductIds() == null ? new ArrayList<>() : cart.getProductIds();

        Map<String, Object> product = new HashMap<>();
        product.put("productId", productIdString);
        product.put("amount", 1);
        product.put("selected", true);
        productList.add(product);

        cart.setProductIds(productList);
        repository.save(cart);
    }

    public List<Map<String, Object>> getAllProductsV2(String cartId, String lang) {
        Cart cart = repository.findById(cartId).orElseThrow();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> productEntry : cart.getProductIds()) {
            String productId = (String) productEntry.get("productId");
            Integer quantity = (Integer) productEntry.get("amount");
            boolean selected = (boolean) productEntry.get("selected");

            Product product = productRepository.findById(productId).orElseThrow();
            Map<String, Object> cartProduct = new HashMap<>();

            switch (lang) {
                case "En":
                    cartProduct.put("name",product.getNameEn());
                    break;
                case "Ru":
                    cartProduct.put("name",product.getNameRu());
                    break;
                case "Kz":
                    cartProduct.put("name",product.getNameKz());
                    break;
            }
            cartProduct.put("image", product.getImageURLs().get(0));
            cartProduct.put("productId", product.getProduct_id());
            cartProduct.put("price", product.getPrice());
            cartProduct.put("amount", quantity);
            cartProduct.put("selected", selected);

            result.add(cartProduct);
        }
        return result;
    }
}
