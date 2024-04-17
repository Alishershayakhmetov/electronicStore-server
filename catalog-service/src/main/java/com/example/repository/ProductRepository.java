package com.example.repository;

import com.example.model.CatalogProduct;
import com.example.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product,String> {
    @Query(value = "{ 'category' : ?0 }", fields = "{'name' : 1, 'nameEn' : 1, 'nameRu': 1, 'nameKz' : 1, 'imageURLs' : 1, 'price' : 1}")
    List<CatalogProduct> findByCategory(String category);

    @Query(value = "{ 'category' : ?0 }", fields = "{'name' : 1, 'nameEn' : 1, 'nameRu': 1, 'nameKz' : 1, 'imageURLs' : 1, 'price' : 1}")
    Page<CatalogProduct> findByCategoryAndPage(String category, Pageable page);

    Product findByName(String name);

}
