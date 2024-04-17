package com.example.service;

import com.example.model.CatalogProduct;
import com.example.model.Product;
import com.example.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public Product getProductByName(String name) {
        return repository.findByName(name);
    }

    public void convertToSingleLanguageProduct(Product product, String language) {
        // convertToSingleLanguageName(product,language);
        convertToSingleLanguageCharacteristics(product,language);
        convertToSingleLanguageDescription(product,language);
    }

    private void convertToSingleLanguageDescription(Product product, String language) {
        Map<String,String> description = product.getDescription();

        List<String> keysToRemove = new ArrayList<>();


        for (Map.Entry<String, String> entry : description.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith(language) && (key.endsWith("En") ||  key.endsWith("Ru") || key.endsWith("Kz"))) {
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            description.remove(key);
        }

        product.setDescription(description);
    }

    private void convertToSingleLanguageCharacteristics(Product product, String language) {
        Map<String, Map<String, String>> characteristics = product.getCharacteristics();

        List<String> keysToRemove = new ArrayList<>();

        for (Map<String, String> section : characteristics.values()) {
            for (Map.Entry<String, String> entry : section.entrySet()) {
                String key = entry.getKey();
                if (!key.endsWith(language) && (key.endsWith("En") ||  key.endsWith("Ru") || key.endsWith("Kz"))) {
                    keysToRemove.add(key);
                }
            }
        }

        // Removing the keys outside of the iteration loop
        for (String key : keysToRemove) {
            for (Map<String, String> section : characteristics.values()) {
                section.remove(key);
            }
        }
        product.setCharacteristics(characteristics);
    }

    public ResponseEntity<Map<String, Object>> getCatalogByCategoryAndPage(String category, String page) {
        PageRequest pageRequest = PageRequest.of(Integer.parseInt(page), 12);
        Page<CatalogProduct> catalogPage = repository.findByCategoryAndPage(category,pageRequest);

        List<CatalogProduct> catalogProducts = catalogPage.getContent();
        long totalProducts = catalogPage.getTotalElements();

        Map<String, Object> response = new HashMap<>();
        response.put("products", catalogProducts);
        response.put("totalProducts", totalProducts);

        if (!catalogProducts.isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}



