package com.example.service;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {
    private CartRepository repository;
    private ProductRepository productRepository;

    private String getOrGenerateCartId(HttpServletRequest request) {
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

    private boolean findCookieByName(Cookie[] cookies, String name) {
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

    private String getCookieByName(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean deleteChosenProduct(String cartId, String productId) {
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

    private Cookie createCookie(String name, String cartId) {
        Cookie cookie = new Cookie(name, cartId);
        cookie.setMaxAge(60 * 60 * 24 * 90); // 90 days expiration
        cookie.setPath("/api/catalog/api/cart");
        cookie.setDomain("localhost");
        cookie.setHttpOnly(true);
        return cookie;
    }

    private void addProductToCart(String productIdString, Cart cart, String lang) {
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        Product productData = productRepository.findById(productIdString).orElseThrow();
        List<Map<String, Object>> productList = cart.getProductIds() == null ? new ArrayList<>() : cart.getProductIds();

        Map<String, Object> product = new HashMap<>();
        product.put("productId", productIdString);
        product.put("amount", 1);
        product.put("selected", true);
        product.put("image",productData.getImageURLs().get(0));
        product.put("name",productData.getName());
        product.put("nameEn",productData.getNameEn());
        product.put("nameRu",productData.getNameRu());
        product.put("nameKz",productData.getNameKz());
        product.put("price",productData.getPrice());
        productList.add(product);

        cart.setProductIds(productList);
        repository.save(cart);
    }

    private List<Map<String, Object>> getAllProductsV2(String cartId, String lang) {
        Cart cart = repository.findById(cartId).orElseThrow();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> productEntry : cart.getProductIds()) {
            String productId = (String) productEntry.get("productId");
            Integer quantity = (Integer) productEntry.get("amount");
            boolean selected = (boolean) productEntry.get("selected");

            Product product = productRepository.findById(productId).orElseThrow();
            Map<String, Object> cartProduct = new HashMap<>();
            cartProduct.put("nameEn",product.getNameEn());
            cartProduct.put("nameRu",product.getNameRu());
            cartProduct.put("nameKz",product.getNameKz());
            cartProduct.put("image", product.getImageURLs().get(0));
            cartProduct.put("productId", product.getProduct_id());
            cartProduct.put("price", product.getPrice());
            cartProduct.put("amount", quantity);
            cartProduct.put("selected", selected);
            cartProduct.put("name",product.getName());

            result.add(cartProduct);
        }
        return result;
    }
    public ResponseEntity<String> addToCart(Map<String, String> productId, String lang, HttpServletRequest request, HttpServletResponse response) {
        String productIdString = productId.get("product_id");
        // if there is cookie "cartId"
        if (findCookieByName(request.getCookies(),"cartId")) {
            boolean isDeleted;
            try {
                isDeleted = deleteChosenProduct(getOrGenerateCartId(request),productIdString);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if(isDeleted) {
                Cookie cookie = createCookie("cartId",getOrGenerateCartId(request));
                response.addCookie(cookie);
                return ResponseEntity.ok("removed");
            } else { // add new product to existing cart
                String cartId = getOrGenerateCartId(request);
                Cart cart = repository.findById(cartId).orElseThrow();
                addProductToCart(productIdString,cart,lang);
                Cookie cookie = createCookie("cartId",cartId);
                response.addCookie(cookie);
                return ResponseEntity.ok("added");
            }
        } else {
            String cartId = getOrGenerateCartId(request);
            Cart cart = new Cart();
            cart.setCreatedAt(new Date(System.currentTimeMillis()));
            cart.setAccessedAt(new Date(System.currentTimeMillis()));
            cart.setCartId(cartId);
            addProductToCart(productIdString,cart,lang);
            Cookie cookie = createCookie("cartId",cartId);
            response.addCookie(cookie);
            return ResponseEntity.ok("added");
        }
    }

    public ResponseEntity<?> getAllProducts(HttpServletRequest request, String lang) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> cartCookie = Arrays.stream(cookies)
                    .filter(cookie -> "cartId".equals(cookie.getName()))
                    .findFirst();

            if (cartCookie.isPresent()) {
                try {
                    List<Map<String, Object>> result = getAllProductsV2(cartCookie.get().getValue(),lang);
                    return ResponseEntity.ok().cacheControl(CacheControl.noCache().noStore().mustRevalidate()).body(result);
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

    public ResponseEntity<?> changeSelected(HttpServletRequest request, String productId, boolean value) {
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

    public ResponseEntity<?> deleteSelectedList(HttpServletRequest request, List<Map<String, Object>> data) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cartCookie = Arrays.stream(cookies)
                .filter(cookie -> "cartId".equals(cookie.getName()))
                .findFirst();
        Cart cart = repository.findById(cartCookie.get().getValue()).orElseThrow();
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        Iterator<Map<String, Object>> iterator = data.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> product = iterator.next();
            for(Map.Entry<String, Object> entry: product.entrySet()) {
                if (entry.getKey().equals("selected") && (Boolean) entry.getValue()) {
                    iterator.remove();
                }
            }
        }
        cart.setProductIds(data);
        repository.save(cart);
        return ResponseEntity.ok().body(data);
    }

    public ResponseEntity<?> changeSelectedList(HttpServletRequest request, Boolean value, List<Map<String, Object>> data) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cartCookie = Arrays.stream(cookies)
                .filter(cookie -> "cartId".equals(cookie.getName()))
                .findFirst();
        Cart cart = repository.findById(cartCookie.get().getValue()).orElseThrow();
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        for(Map<String, Object> product: data) {
            product.put("selected", value);
        }
        cart.setProductIds(data);
        repository.save(cart);
        return ResponseEntity.ok().body(data);
    }

    public ResponseEntity<?> deleteProduct(HttpServletRequest request, String productId, List<Map<String, Object>> data) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cartCookie = Arrays.stream(cookies)
                .filter(cookie -> "cartId".equals(cookie.getName()))
                .findFirst();
        Cart cart = repository.findById(cartCookie.get().getValue()).orElseThrow();
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        data.removeIf(product -> product.get("productId").equals(productId));
        cart.setProductIds(data);
        repository.save(cart);
        return ResponseEntity.ok().body(data);
    }

    public ResponseEntity<?> changeAmount(HttpServletRequest request, String productId, Integer amount) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cartCookie = Arrays.stream(cookies)
                .filter(cookie -> "cartId".equals(cookie.getName()))
                .findFirst();
        Cart cart = repository.findById(cartCookie.get().getValue()).orElseThrow();
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));

        List<Map<String, Object>> productList = cart.getProductIds();

        for (Map<String, Object> product: productList) {
            if(product.get("productId").equals(productId)) {
                product.put("amount",amount);
            }
        }
        cart.setProductIds(productList);
        repository.save(cart);
        return ResponseEntity.ok().body(productList);
    }
}
