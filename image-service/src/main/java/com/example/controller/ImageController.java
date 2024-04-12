package com.example.controller;

import com.example.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private final ImageService service;
    public ImageController(ImageService service) {
        this.service = service;
    }

    @GetMapping("/{imageName}")
    public ResponseEntity getImage(@PathVariable String imageName) {
        return service.getImage(imageName);
    }
}
