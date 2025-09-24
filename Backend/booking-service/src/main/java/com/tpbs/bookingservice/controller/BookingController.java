package com.tpbs.bookingservice.controller;

import com.tpbs.bookingservice.dto.BookingDto;
import com.tpbs.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        Map<String, Object> response = new HashMap<>();
        response.put("data", bookings);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookingById(@PathVariable("id") Long id) {
        BookingDto booking = bookingService.getBookingById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", booking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUser(@PathVariable("userId") Long userId) {
        List<BookingDto> bookings = bookingService.getBookingsByUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", bookings);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/package/{packageId}")
    public ResponseEntity<Map<String, Object>> getBookingsByPackage(@PathVariable("packageId") Long packageId) {
        List<BookingDto> bookings = bookingService.getBookingsByPackage(packageId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", bookings);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody BookingDto bookingDto) {
        BookingDto createdBooking = bookingService.createBooking(bookingDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdBooking);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBooking(@PathVariable("id") Long id, @Valid @RequestBody BookingDto bookingDto) {
        BookingDto updatedBooking = bookingService.updateBooking(id, bookingDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedBooking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBooking(@PathVariable("id") Long id) {
        bookingService.deleteBooking(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Booking deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable("id") Long id) {
        BookingDto cancelledBooking = bookingService.cancelBooking(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", cancelledBooking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
      @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmBooking(@PathVariable("id") Long id) {
        BookingDto confirmedBooking = bookingService.confirmBooking(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", confirmedBooking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    // Enhanced endpoints with cross-service data
    @GetMapping("/user/{userId}/with-details")
    public ResponseEntity<Map<String, Object>> getUserBookingsWithDetails(@PathVariable("userId") Long userId) {
        List<Map<String, Object>> enrichedBookings = bookingService.getUserBookingsWithPackageDetails(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", enrichedBookings);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/agent/{agentId}/dashboard")
    public ResponseEntity<Map<String, Object>> getAgentDashboardData(@PathVariable("agentId") Long agentId) {
        Map<String, Object> dashboardData = bookingService.getAgentDashboardData(agentId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", dashboardData);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}