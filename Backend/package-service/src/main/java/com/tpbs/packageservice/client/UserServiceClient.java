package com.tpbs.packageservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service", url = "${feign.clients.user-service.url:http://localhost:8081}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    ResponseEntity<Map<String, Object>> getUserById(
        @PathVariable("id") Long id,
        @RequestHeader("X-Service-Call") String serviceCall
    );
}
