package com.tpbs.assistanceservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AssistanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistanceServiceApplication.class, args);
        log.info("Assistance Service started successfully on port 8086");
        log.info("Registered with Eureka Server at http://localhost:8761");
    }
}