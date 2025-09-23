package com.tpbs.bookingservice.service;

import com.tpbs.bookingservice.dto.BookingDto;

import java.util.List;
import java.util.Map;

public interface BookingService {
    
    List<BookingDto> getAllBookings();
    BookingDto getBookingById(Long id);
    List<BookingDto> getBookingsByUser(Long userId);
    List<BookingDto> getBookingsByPackage(Long packageId);
    BookingDto createBooking(BookingDto bookingDto);
    BookingDto updateBooking(Long id, BookingDto bookingDto);
    void deleteBooking(Long id);
    BookingDto cancelBooking(Long id);
    BookingDto confirmBooking(Long id);
    
    // Enhanced methods with cross-service data
    List<Map<String, Object>> getUserBookingsWithPackageDetails(Long userId);
    Map<String, Object> getAgentDashboardData(Long agentId);
}