package com.example.controller;

import com.example.model.Cart;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import com.example.service.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
        String productIdString = productId.get("productId");
        // if there is cookie "cartId"
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
            cart.setCreatedAt(new Date(System.currentTimeMillis()));
            cart.setAccessedAt(new Date(System.currentTimeMillis()));
            cart.setCartId(cartId);
            service.addProductToCart(productIdString,cart);
            Cookie cookie = service.createCookie("cartId",cartId);
            response.addCookie(cookie);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> getAllProductsV2(HttpServletRequest request, @RequestParam("lang") String lang) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> cartCookie = Arrays.stream(cookies)
                    .filter(cookie -> "cartId".equals(cookie.getName()))
                    .findFirst();

            if (cartCookie.isPresent()) {
                try {
                    List<Map<String, Object>> result = service.getAllProductsV2(cartCookie.get().getValue(),lang);
                    return new ResponseEntity<>(result, HttpStatus.OK);
                } catch (NoSuchElementException e) {
                    return new ResponseEntity<>(e, HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(Collections.singletonMap("message", "No cartId cookie found"), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(Collections.singletonMap("message", "No cookies found"), HttpStatus.OK);
        }
    }

    @PostMapping("changeSelected")
    public ResponseEntity<?> changeSelected(
            HttpServletRequest request,
            @RequestParam("productId") String productId,
            @RequestParam("value") boolean value) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cartCookie = Arrays.stream(cookies)
                .filter(cookie -> "cartId".equals(cookie.getName()))
                .findFirst();
        Cart cart = repository.findById(cartCookie.get().getValue()).orElseThrow();
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        List<Map<String, Object>> cartProducts = cart.getProductIds();
        for (Map<String, Object> product: cartProducts) {
            if(product.get("productId").equals(productId)) {
                product.put("selected", value); // Update the "selected" field
                break;
            }
        }
        repository.save(cart);

        return ResponseEntity.ok().build();
    }

    @PostMapping("deleteSelectedList")
    public ResponseEntity<?> deleteSelectedList(
            HttpServletRequest request,
            @RequestBody Map<String, Boolean> selectedList
    ) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cartCookie = Arrays.stream(cookies)
                .filter(cookie -> "cartId".equals(cookie.getName()))
                .findFirst();
        Cart cart = repository.findById(cartCookie.get().getValue()).orElseThrow();
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        for (Map.Entry<String, Boolean> entry : selectedList.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            if (value) {
                List<Map<String, Object>> productList = cart.getProductIds();

                Iterator<Map<String, Object>> iterator = productList.iterator();
                while (iterator.hasNext()) {
                    Map<String, Object> product = iterator.next();
                    for (Object productId : product.values()) {
                        if (key.equals(productId)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                cart.setProductIds(productList);

                // Remove entry with value true
                selectedList.entrySet().removeIf(Map.Entry::getValue);
            }
        }
        repository.save(cart);
        return ResponseEntity.ok().body(selectedList);
    }
}
