package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String product_id;
    private String category;
    private String name;
    private String nameEn;
    private String nameKz;
    private String nameRu;
    private List<String> imageURLs;
    private int price;
    private Map<String, Map<String, String>> characteristics;
    private Map<String, String> description;
    private Date createdAt;
    private Date updatedAt;
}
