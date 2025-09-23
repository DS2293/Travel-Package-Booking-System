package com.tpbs.apigateway;

import com.tpbs.apigateway.filter.JwtAuthenticationGatewayFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("API Gateway started successfully on port 8080");
        log.info("Registered with Eureka Server at http://localhost:8761");
        log.info("Using service discovery for routing to user-service");
        log.info("JWT Authentication enabled for protected endpoints");
    }    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationGatewayFilterFactory jwtFilter) {
        log.info("Configuring custom routes...");
        
        return builder.routes()
            // Authentication Routes (Public) - No JWT filter
            .route("auth-routes", r -> r
                .path("/api/auth/**")
                .uri("lb://user-service")
            )
            
            // User Management Routes (Protected) - With JWT filter
            .route("user-routes", r -> r
                .path("/api/users/**")
                .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                .uri("lb://user-service")
            )
            
            // Health check route
            .route("health-route", r -> r
                .path("/health")
                .uri("lb://user-service")
            )
            
            .build();
    }    @Bean
    public CorsWebFilter corsWebFilter() {
        log.info("Configuring CORS filter...");
        
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow specific origins - cannot use * with credentials
        corsConfig.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5174", 
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5174"
        ));
        
        // Allow common HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Expose headers that the frontend can access
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}