package com.tpbs.insuranceservice.controller;

import com.tpbs.insuranceservice.dto.InsuranceDto;
import com.tpbs.insuranceservice.service.InsuranceService;
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
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
@Slf4j
public class InsuranceController {
    
    private final InsuranceService insuranceService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllInsurance() {
        log.debug("Fetching all insurance policies");
        List<InsuranceDto> insurance = insuranceService.getAllInsurance();
        Map<String, Object> response = new HashMap<>();
        response.put("data", insurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInsuranceById(@PathVariable("id") Long id) {
        log.debug("Fetching insurance with id: {}", id);
        InsuranceDto insurance = insuranceService.getInsuranceById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", insurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getInsuranceByUser(@PathVariable("userId") Long userId) {
        log.debug("Fetching insurance for user: {}", userId);
        List<InsuranceDto> insurance = insuranceService.getInsuranceByUserId(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", insurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getInsuranceByBooking(@PathVariable("bookingId") Long bookingId) {
        log.debug("Fetching insurance for booking: {}", bookingId);
        List<InsuranceDto> insurance = insuranceService.getInsuranceByBookingId(bookingId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", insurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createInsurance(@Valid @RequestBody InsuranceDto insuranceDto) {
        log.debug("Creating new insurance policy");
        InsuranceDto createdInsurance = insuranceService.createInsurance(insuranceDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdInsurance);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInsurance(@PathVariable("id") Long id, @Valid @RequestBody InsuranceDto insuranceDto) {
        log.debug("Updating insurance with id: {}", id);
        InsuranceDto updatedInsurance = insuranceService.updateInsurance(id, insuranceDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedInsurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelInsurance(@PathVariable("id") Long id) {
        log.debug("Cancelling insurance with id: {}", id);
        InsuranceDto cancelledInsurance = insuranceService.cancelInsurance(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", cancelledInsurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/renew")
    public ResponseEntity<Map<String, Object>> renewInsurance(@PathVariable("id") Long id) {
        log.debug("Renewing insurance with id: {}", id);
        InsuranceDto renewedInsurance = insuranceService.renewInsurance(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", renewedInsurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInsurance(@PathVariable("id") Long id) {
        log.debug("Deleting insurance with id: {}", id);
        insuranceService.deleteInsurance(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Insurance deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    // Enhanced endpoints with cross-service data
    @GetMapping("/user/{userId}/with-details")
    public ResponseEntity<Map<String, Object>> getUserInsuranceWithDetails(@PathVariable("userId") Long userId) {
        log.debug("Fetching user insurance with booking details for user: {}", userId);
        List<Map<String, Object>> enrichedInsurance = insuranceService.getUserInsuranceWithBookingDetails(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", enrichedInsurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/with-details")
    public ResponseEntity<Map<String, Object>> getInsuranceWithDetails(@PathVariable("id") Long id) {
        log.debug("Fetching insurance with booking and user details for insurance: {}", id);
        Map<String, Object> enrichedInsurance = insuranceService.getInsuranceWithBookingAndUserDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", enrichedInsurance);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/quotes/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getInsuranceQuotes(@PathVariable("bookingId") Long bookingId) {
        log.debug("Generating insurance quotes for booking: {}", bookingId);
        List<Map<String, Object>> quotes = insuranceService.getInsuranceQuotes(bookingId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", quotes);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}