package com.tpbs.packageservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "payment-service", url = "${feign.clients.payment-service.url:http://localhost:8084}")
public interface PaymentServiceClient {
    
    @GetMapping("/api/payments/{id}")
    ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable("id") Long id);
    
    @GetMapping("/api/payments/booking/{bookingId}")
    ResponseEntity<Map<String, Object>> getPaymentsByBookingId(@PathVariable("bookingId") Long bookingId);
}