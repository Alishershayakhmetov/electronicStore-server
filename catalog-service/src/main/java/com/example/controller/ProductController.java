package com.example.controller;

import com.example.model.CatalogProduct;
import com.example.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
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
    @GetMapping("/product/{name}/{language}")
    public ResponseEntity<Product> getProductByName(@PathVariable("name") String name,@PathVariable("language") String language) {
        Product product =  service.getProductByName(name);
        try {
            service.convertToSingleLanguageProduct(product,language);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/catalog/{category}/{page}")
    public ResponseEntity<Map<String, Object>> getCatalogByCategoryAndPage(@PathVariable("category") String category, @PathVariable("page") String page) {
        return service.getCatalogByCategoryAndPage(category,page);
    }
}
