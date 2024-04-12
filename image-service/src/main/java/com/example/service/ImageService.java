package com.example.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
    private final String IMAGEPATH = System.getenv("IMAGEPATH");

    public ResponseEntity getImage(String imageName) {
        // if image extension is not specified
        if (!imageName.toLowerCase().endsWith(".png") && !imageName.toLowerCase().endsWith(".jpg") && !imageName.toLowerCase().endsWith(".jpeg")) {
            imageName += ".png"; // Default to PNG format
        }
        // Construct the path to the image file
        Path imagePath = Paths.get(IMAGEPATH + imageName);

        try {
            // Load the image as a resource
            Resource resource = new UrlResource(imagePath.toUri());

            // Check if the resource exists and is readable
            if (resource.exists() && resource.isReadable()) {
                // Determine the media type based on the file extension
                String contentType = "image/jpeg"; // default to JPEG
                if (imageName.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                }

                // Return a response entity with the image resource and appropriate content type
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                // Return 404 Not Found if the resource does not exist or is not readable
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            // Return 404 Not Found if there's an error with the resource URL
            return ResponseEntity.notFound().build();
        }
    }
}
