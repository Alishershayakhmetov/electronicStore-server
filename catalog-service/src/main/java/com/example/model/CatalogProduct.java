package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class CatalogProduct {
    private String name;
    private String nameEn;
    private String nameRu;
    private String nameKz;
    private List<String> imageURLs;
    private int price;
}
