package com.tpbs.packageservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "booking-service", url = "${feign.clients.booking-service.url:http://localhost:8083}")
public interface BookingServiceClient {
    
    @GetMapping("/api/bookings/package/{packageId}")
    ResponseEntity<Map<String, Object>> getBookingsByPackage(@PathVariable("packageId") Long packageId);
}
