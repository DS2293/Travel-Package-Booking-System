package com.tpbs.packageservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@Slf4j
public class PackageServiceApplication {

    public static void main(String[] args) {
        log.info("Starting Package Service Application...");
        SpringApplication.run(PackageServiceApplication.class, args);
        log.info("Package Service Application started successfully!");
    }
} 