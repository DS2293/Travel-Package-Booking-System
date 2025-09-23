package com.tpbs.bookingservice.service.impl;

import com.tpbs.bookingservice.client.PackageServiceClient;
import com.tpbs.bookingservice.client.UserServiceClient;
import com.tpbs.bookingservice.dto.BookingDto;
import com.tpbs.bookingservice.model.Booking;
import com.tpbs.bookingservice.repository.BookingRepository;
import com.tpbs.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final PackageServiceClient packageServiceClient;
    private final UserServiceClient userServiceClient;
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return toDto(booking);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByPackage(Long packageId) {
        return bookingRepository.findByPackageId(packageId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public BookingDto createBooking(BookingDto bookingDto) {
        Booking booking = toEntity(bookingDto);
        booking.setBookingId(null);
        booking.setStatus("pending");
        Booking savedBooking = bookingRepository.save(booking);
        return toDto(savedBooking);
    }
    
    @Override
    public BookingDto updateBooking(Long id, BookingDto bookingDto) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        existingBooking.setUserId(bookingDto.getUserId());
        existingBooking.setPackageId(bookingDto.getPackageId());
        existingBooking.setStartDate(bookingDto.getStartDate());
        existingBooking.setEndDate(bookingDto.getEndDate());
        existingBooking.setStatus(bookingDto.getStatus());
        existingBooking.setPaymentId(bookingDto.getPaymentId());
        
        Booking updatedBooking = bookingRepository.save(existingBooking);
        return toDto(updatedBooking);
    }
    
    @Override
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }
    
    @Override
    public BookingDto cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        booking.setStatus("cancelled");
        Booking updatedBooking = bookingRepository.save(booking);
        return toDto(updatedBooking);
    }
      @Override
    public BookingDto confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        booking.setStatus("confirmed");
        Booking updatedBooking = bookingRepository.save(booking);
        return toDto(updatedBooking);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserBookingsWithPackageDetails(Long userId) {
        List<BookingDto> userBookings = getBookingsByUser(userId);
        List<Map<String, Object>> enrichedBookings = new ArrayList<>();
        
        for (BookingDto booking : userBookings) {
            Map<String, Object> enrichedBooking = new HashMap<>();
            enrichedBooking.put("booking", booking);
            
            try {
                // Fetch package details
                ResponseEntity<Map<String, Object>> packageResponse = 
                    packageServiceClient.getPackageById(booking.getPackageId());
                if (packageResponse.getStatusCode().is2xxSuccessful() && packageResponse.getBody() != null) {
                    enrichedBooking.put("package", packageResponse.getBody().get("data"));
                }
            } catch (Exception e) {
                log.error("Failed to fetch package details for booking {}: {}", booking.getBookingId(), e.getMessage());
                enrichedBooking.put("package", null);
            }
            
            enrichedBookings.add(enrichedBooking);
        }
        
        return enrichedBookings;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAgentDashboardData(Long agentId) {
        Map<String, Object> dashboardData = new HashMap<>();
        
        try {
            // Get agent's packages
            ResponseEntity<Map<String, Object>> packagesResponse = 
                packageServiceClient.getPackagesByAgent(agentId);
            
            if (packagesResponse.getStatusCode().is2xxSuccessful() && packagesResponse.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> agentPackages = 
                    (List<Map<String, Object>>) packagesResponse.getBody().get("data");
                
                dashboardData.put("packages", agentPackages);
                
                // Get bookings for agent's packages
                List<Long> packageIds = agentPackages.stream()
                    .map(pkg -> Long.valueOf(pkg.get("packageId").toString()))
                    .collect(Collectors.toList());
                
                List<BookingDto> allAgentBookings = new ArrayList<>();
                for (Long packageId : packageIds) {
                    allAgentBookings.addAll(getBookingsByPackage(packageId));
                }
                
                dashboardData.put("bookings", allAgentBookings);
                
                // Calculate statistics
                long totalBookings = allAgentBookings.size();
                long confirmedBookings = allAgentBookings.stream()
                    .filter(b -> "confirmed".equals(b.getStatus()))
                    .count();
                long pendingBookings = allAgentBookings.stream()
                    .filter(b -> "pending".equals(b.getStatus()))
                    .count();
                
                dashboardData.put("totalBookings", totalBookings);
                dashboardData.put("confirmedBookings", confirmedBookings);
                dashboardData.put("pendingBookings", pendingBookings);
                
            } else {
                dashboardData.put("packages", Collections.emptyList());
                dashboardData.put("bookings", Collections.emptyList());
                dashboardData.put("totalBookings", 0);
                dashboardData.put("confirmedBookings", 0);
                dashboardData.put("pendingBookings", 0);
            }
            
        } catch (Exception e) {
            log.error("Failed to fetch agent dashboard data for agent {}: {}", agentId, e.getMessage());
            dashboardData.put("error", "Failed to fetch dashboard data");
        }
        
        return dashboardData;
    }
    
    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getPackageId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getPaymentId()
        );
    }
    
    private Booking toEntity(BookingDto dto) {
        return new Booking(
                dto.getBookingId(),
                dto.getUserId(),
                dto.getPackageId(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getStatus(),
                dto.getPaymentId()
        );
    }
} 