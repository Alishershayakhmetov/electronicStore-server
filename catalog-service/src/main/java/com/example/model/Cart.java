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
@Document(collection = "carts")
public class Cart {
    @Id
    private String cartId;
    private List<Map<String, Object>> productIds;
    private Date createdAt;
    private Date updatedAt;
    private Date accessedAt;
}
