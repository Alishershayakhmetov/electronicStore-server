package com.example.service;

import com.example.model.CatalogProduct;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {
    private ProductRepository repository;

    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = repository.findAll();
        if (!products.isEmpty()) {
            return new ResponseEntity<>(products, HttpStatus.OK);
        }
        return new ResponseEntity<>(Collections.emptyList(),HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> createProduct(Product product) {
        try {
            product.setCreatedAt(new Date(System.currentTimeMillis()));
            product.setUpdatedAt(new Date(System.currentTimeMillis()));
            repository.save(product);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch(Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<CatalogProduct>> getCatalogByCategory(String category) {
        List<CatalogProduct> catalogProducts = repository.findByCategory(category);
        if (!catalogProducts.isEmpty()) {
            return new ResponseEntity<>(catalogProducts, HttpStatus.OK);
        }
        return new ResponseEntity<>(Collections.emptyList(),HttpStatus.NOT_FOUND);
    }
    public ResponseEntity<Product> getProductByName(String name) {
        Product product = repository.findByName(name);
        return new ResponseEntity<>(product,HttpStatus.OK);
    }
}
