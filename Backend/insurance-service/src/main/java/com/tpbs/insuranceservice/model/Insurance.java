package com.tpbs.insuranceservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Insurance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long insuranceId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long bookingId;
    
    @Column(nullable = false)
    private String policyType; // BASIC, PREMIUM, COMPREHENSIVE
    
    @Column(nullable = false)
    private String policyNumber;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premium;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private String status; // ACTIVE, EXPIRED, CANCELLED, CLAIMED
    
    @Column(length = 1000)
    private String coverageDetails;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}