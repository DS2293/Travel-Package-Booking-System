package com.tpbs.bookingservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    
    private Long bookingId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Package ID is required")
    private Long packageId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private Long paymentId;
} 