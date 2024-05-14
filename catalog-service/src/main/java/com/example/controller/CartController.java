package com.example.controller;

import com.example.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {
    private CartService service;

    @PostMapping(value = "/add-to-cart", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> addToCart(
            @RequestBody Map<String, String> productId,
            @RequestParam("lang") String lang,
            HttpServletRequest request,
            HttpServletResponse response) {
        return service.addToCart(productId,lang,request,response);
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> getAllProducts(HttpServletRequest request, @RequestParam("lang") String lang) {
        return service.getAllProducts(request,lang);
    }

    @PostMapping("changeSelected")
    public ResponseEntity<?> changeSelected(
            HttpServletRequest request,
            @RequestParam("productId") String productId,
            @RequestParam("value") boolean value) {
        return service.changeSelected(request,productId,value);
    }

    @DeleteMapping("deleteSelectedList")
    public ResponseEntity<?> deleteSelectedList(
            HttpServletRequest request,
            @RequestBody List<Map<String, Object>> data
    ) {
        return service.deleteSelectedList(request,data);
    }

    @PostMapping("changeSelectedList")
    public ResponseEntity<?> changeSelectedList(
            HttpServletRequest request,
            @RequestParam("value") Boolean value,
            @RequestBody List<Map<String, Object>> data
    ) {
        return service.changeSelectedList(request,value,data);
    }

    @DeleteMapping("deleteProduct")
    public ResponseEntity<?> deleteProduct(
            HttpServletRequest request,
            @RequestParam("product") String productId,
            @RequestBody List<Map<String,Object>> data
    ) {
        return service.deleteProduct(request,productId,data);
    }

    @PostMapping("changeAmount")
    public ResponseEntity<?> changeAmount(
            HttpServletRequest request,
            @RequestParam("productId") String productId,
            @RequestParam("amount") Integer amount
    ) {
        return service.changeAmount(request,productId,amount);
    }
}
