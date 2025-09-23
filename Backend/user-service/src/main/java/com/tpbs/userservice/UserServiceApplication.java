package com.tpbs.userservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Slf4j
@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        log.info("User Service started successfully on port 8081");
        log.info("Registered with Eureka Server at http://localhost:8761");
        log.info("JWT Authentication and authorization enabled");
    }
} 