package com.example.controller;

import com.example.model.CatalogProduct;
import com.example.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api")
@AllArgsConstructor
public class ProductController {
    private ProductService service;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return service.getAllProducts();
    }
    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return service.createProduct(product);
    }

    @GetMapping("/catalog/{category}")
    public ResponseEntity<List<CatalogProduct>> getCatalogByCategory(@PathVariable("category") String category ) {
        return service.getCatalogByCategory(category);
    }
    /*
    @GetMapping("/catalog/${category}/${page_number}")
    public ResponseEntity<?> getCatalogByCategory(@PathVariable("category") String category, @PathVariable("page_number") String pageNumber ) {
        return service.getCatalogByCategoryAndPageNumber(category,pageNumber);
    }*/
    @GetMapping("/product/{name}")
    public ResponseEntity<Product> getProductByName(@PathVariable("name") String name) {
        return service.getProductByName(name);
    }
}
