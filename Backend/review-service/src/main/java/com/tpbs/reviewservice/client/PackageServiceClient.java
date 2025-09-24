package com.tpbs.reviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "package-service", url = "${feign.clients.package-service.url:http://localhost:8082}")
public interface PackageServiceClient {
    
    @GetMapping("/api/packages/{id}")
    ResponseEntity<Map<String, Object>> getPackageById(@PathVariable("id") Long id);
}