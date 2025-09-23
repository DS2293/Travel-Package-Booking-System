package com.tpbs.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "booking-service", url = "${feign.clients.booking-service.url:http://localhost:8083}")
public interface BookingServiceClient {
    
    @GetMapping("/api/bookings/{id}")
    ResponseEntity<Map<String, Object>> getBookingById(@PathVariable("id") Long id);
}
