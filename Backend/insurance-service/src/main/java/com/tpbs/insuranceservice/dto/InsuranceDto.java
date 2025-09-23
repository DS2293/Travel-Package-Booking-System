package com.tpbs.insuranceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDto {
    
    private Long insuranceId;
    private Long userId;
    private Long bookingId;
    private String policyType;
    private String policyNumber;
    private BigDecimal premium;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String coverageDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}