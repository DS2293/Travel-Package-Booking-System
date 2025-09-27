package com.tpbs.bookingservice.controller;

import com.tpbs.bookingservice.dto.BookingDto;
import com.tpbs.bookingservice.service.BookingService;
import com.tpbs.bookingservice.util.JwtUtil;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    private final JwtUtil jwtUtil;
    
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
    
    // JWT-based endpoint for authenticated user's bookings
    @GetMapping("/my-bookings")
    public ResponseEntity<Map<String, Object>> getMyBookings(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
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
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestBody BookingDto bookingDto,
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
        bookingDto.setUserId(userId);
        log.debug("Setting userId from JWT: {}", userId);
        
        // Manual validation after setting userId
        if (bookingDto.getPackageId() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Package ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (bookingDto.getStartDate() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Start date is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (bookingDto.getEndDate() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "End date is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (bookingDto.getStartDate() != null && bookingDto.getEndDate() != null && 
            bookingDto.getStartDate().isAfter(bookingDto.getEndDate())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Start date must be before end date");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        BookingDto createdBooking = bookingService.createBooking(bookingDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdBooking);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBooking(
            @PathVariable("id") Long id, 
            @Valid @RequestBody BookingDto bookingDto,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token for authorization
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Verify the booking belongs to the authenticated user
        BookingDto existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only update your own bookings");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        // Ensure userId is not changed
        bookingDto.setUserId(userId);
        
        BookingDto updatedBooking = bookingService.updateBooking(id, bookingDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedBooking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBooking(
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
        
        // Verify the booking belongs to the authenticated user
        BookingDto existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only delete your own bookings");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        bookingService.deleteBooking(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Booking deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
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
        
        // Verify the booking belongs to the authenticated user
        BookingDto existingBooking = bookingService.getBookingById(id);
        if (!existingBooking.getUserId().equals(userId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only cancel your own bookings");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        BookingDto cancelledBooking = bookingService.cancelBooking(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", cancelledBooking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmBooking(@PathVariable("id") Long id) {
        // This endpoint is typically for agents/admins, so no JWT user validation needed
        BookingDto confirmedBooking = bookingService.confirmBooking(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", confirmedBooking);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{bookingId}/payment/{paymentId}")
    public ResponseEntity<Map<String, Object>> updateBookingPayment(
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("paymentId") Long paymentId) {
        
        log.info("Linking payment {} to booking {}", paymentId, bookingId);
        
        try {
            BookingDto updatedBooking = bookingService.updateBookingPayment(bookingId, paymentId);
            Map<String, Object> response = new HashMap<>();
            response.put("data", updatedBooking);
            response.put("success", true);
            response.put("message", "Payment linked to booking successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to link payment {} to booking {}: {}", paymentId, bookingId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to link payment to booking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
    
    // JWT-based endpoint for authenticated user's bookings with details
    @GetMapping("/my-bookings/with-details")
    public ResponseEntity<Map<String, Object>> getMyBookingsWithDetails(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
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