package com.tpbs.paymentservice.controller;

import com.tpbs.paymentservice.dto.PaymentDto;
import com.tpbs.paymentservice.dto.PaymentStatusDto;
import com.tpbs.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPayments() {
        log.debug("Fetching all payments");
        List<PaymentDto> payments = paymentService.getAllPayments();
        Map<String, Object> response = new HashMap<>();
        response.put("data", payments);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable("id") Long id) {
        log.debug("Fetching payment with id: {}", id);
        PaymentDto payment = paymentService.getPaymentById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", payment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getPaymentsByUser(@PathVariable("userId") Long userId) {
        log.debug("Fetching payments for user: {}", userId);
        List<PaymentDto> payments = paymentService.getPaymentsByUserId(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", payments);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getPaymentsByBooking(@PathVariable("bookingId") Long bookingId) {
        log.debug("Fetching payments for booking: {}", bookingId);
        List<PaymentDto> payments = paymentService.getPaymentsByBookingId(bookingId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", payments);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@Valid @RequestBody PaymentDto paymentDto) {
        log.debug("Creating new payment");
        PaymentDto createdPayment = paymentService.createPayment(paymentDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdPayment);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@Valid @RequestBody PaymentDto paymentDto) {
        log.debug("Processing payment");
        PaymentDto processedPayment = paymentService.processPayment(paymentDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", processedPayment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePayment(@PathVariable("id") Long id, @Valid @RequestBody PaymentDto paymentDto) {
        log.debug("Updating payment with id: {}", id);
        PaymentDto updatedPayment = paymentService.updatePayment(id, paymentDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedPayment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(@PathVariable("id") Long id, @Valid @RequestBody PaymentStatusDto statusDto) {
        log.debug("Updating payment status for payment id: {} to status: {}", id, statusDto.getStatus());
        PaymentDto updatedPayment = paymentService.updatePaymentStatus(id, statusDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedPayment);
        response.put("success", true);
        response.put("message", "Payment status updated successfully");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(@PathVariable("id") Long id) {
        log.debug("Refunding payment with id: {}", id);
        PaymentDto refundedPayment = paymentService.refundPayment(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", refundedPayment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePayment(@PathVariable("id") Long id) {
        log.debug("Deleting payment with id: {}", id);
        paymentService.deletePayment(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payment deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    // Enhanced endpoints with cross-service data
    @GetMapping("/user/{userId}/with-details")
    public ResponseEntity<Map<String, Object>> getUserPaymentsWithDetails(@PathVariable("userId") Long userId) {
        log.debug("Fetching user payments with booking details for user: {}", userId);
        List<Map<String, Object>> enrichedPayments = paymentService.getUserPaymentsWithBookingDetails(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", enrichedPayments);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/with-details")
    public ResponseEntity<Map<String, Object>> getPaymentWithDetails(@PathVariable("id") Long id) {
        log.debug("Fetching payment with booking and user details for payment: {}", id);
        Map<String, Object> enrichedPayment = paymentService.getPaymentWithBookingAndUserDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", enrichedPayment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}