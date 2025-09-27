package com.tpbs.paymentservice.controller;

import com.tpbs.paymentservice.dto.PaymentDto;
import com.tpbs.paymentservice.dto.PaymentStatusDto;
import com.tpbs.paymentservice.service.PaymentService;
import com.tpbs.paymentservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;
    
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
    
    // JWT-based endpoint for authenticated user's payments
    @GetMapping("/my-payments")
    public ResponseEntity<Map<String, Object>> getMyPayments(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        log.debug("Fetching payments for authenticated user: {}", userId);
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
    public ResponseEntity<Map<String, Object>> createPayment(
            @RequestBody PaymentDto paymentDto,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Set the user ID from JWT token BEFORE validation
        paymentDto.setUserId(userId);
        log.debug("Setting userId from JWT: {}", userId);
        
        // Manual validation after setting userId
        if (paymentDto.getBookingId() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Booking ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (paymentDto.getAmount() == null || paymentDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Amount is required and must be greater than 0");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (paymentDto.getPaymentMethod() == null || paymentDto.getPaymentMethod().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Payment method is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        log.debug("Creating new payment for user: {}", userId);
        PaymentDto createdPayment = paymentService.createPayment(paymentDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdPayment);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestBody PaymentDto paymentDto,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Set the user ID from JWT token BEFORE validation
        paymentDto.setUserId(userId);
        log.debug("Setting userId from JWT: {}", userId);
        
        // Manual validation after setting userId
        if (paymentDto.getBookingId() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Booking ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (paymentDto.getAmount() == null || paymentDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Amount is required and must be greater than 0");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (paymentDto.getPaymentMethod() == null || paymentDto.getPaymentMethod().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Payment method is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        log.debug("Processing payment for user: {}", userId);
        PaymentDto processedPayment = paymentService.processPayment(paymentDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", processedPayment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePayment(
            @PathVariable("id") Long id, 
            @Valid @RequestBody PaymentDto paymentDto,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token for authorization
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Verify the payment belongs to the authenticated user
        PaymentDto existingPayment = paymentService.getPaymentById(id);
        if (!existingPayment.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only update your own payments");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        // Ensure userId is not changed
        paymentDto.setUserId(userId);
        
        log.debug("Updating payment with id: {}", id);
        PaymentDto updatedPayment = paymentService.updatePayment(id, paymentDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedPayment);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(@PathVariable("id") Long id, @Valid @RequestBody PaymentStatusDto statusDto) {
        // This endpoint is typically for admins/system, so no JWT user validation needed
        log.debug("Updating payment status for payment id: {} to status: {}", id, statusDto.getStatus());
        PaymentDto updatedPayment = paymentService.updatePaymentStatus(id, statusDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedPayment);
        response.put("success", true);
        response.put("message", "Payment status updated successfully");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token for authorization
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Verify the payment belongs to the authenticated user
        PaymentDto existingPayment = paymentService.getPaymentById(id);
        if (!existingPayment.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only refund your own payments");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        log.debug("Refunding payment with id: {}", id);
        PaymentDto refundedPayment = paymentService.refundPayment(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", refundedPayment);
        response.put("success", true);
        response.put("message", "Payment refunded successfully");
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePayment(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token for authorization
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Verify the payment belongs to the authenticated user
        PaymentDto existingPayment = paymentService.getPaymentById(id);
        if (!existingPayment.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only delete your own payments");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
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
    
    // JWT-based enhanced endpoint
    @GetMapping("/my-payments/with-details")
    public ResponseEntity<Map<String, Object>> getMyPaymentsWithDetails(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        log.debug("Fetching authenticated user payments with booking details for user: {}", userId);
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