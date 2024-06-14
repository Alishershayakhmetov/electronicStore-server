package com.example.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("catalog-service", r -> r.path("/api/catalog/**")
                        .filters(f -> f.rewritePath("/api/catalog/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://catalog-service"))
                .route("image-service", r -> r.path("/api/image-service/**")
                        .filters(f -> f.rewritePath("/api/image-service/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://image-service"))
                .route("message-service", r -> r.path("/api/message-service/**")
                        .filters(f -> f.rewritePath("/api/message-service/(?<remaining>.*)", "/${remaining}"))
                        .uri("lb://message-service"))
                .route("discovery-server", r -> r.path("/eureka/web")
                        .filters(f -> f.setPath("/"))
                        .uri("http://localhost:8761"))
                .route("discovery-server-static", r -> r.path("/eureka/**")
                        .uri("http://localhost:8761"))
                .build();
    }
}
