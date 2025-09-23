package com.tpbs.insuranceservice.service.impl;

import com.tpbs.insuranceservice.client.BookingServiceClient;
import com.tpbs.insuranceservice.client.UserServiceClient;
import com.tpbs.insuranceservice.dto.InsuranceDto;
import com.tpbs.insuranceservice.model.Insurance;
import com.tpbs.insuranceservice.repository.InsuranceRepository;
import com.tpbs.insuranceservice.service.InsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InsuranceServiceImpl implements InsuranceService {
    
    private final InsuranceRepository insuranceRepository;
    private final BookingServiceClient bookingServiceClient;
    private final UserServiceClient userServiceClient;
    
    @Override
    @Transactional(readOnly = true)
    public List<InsuranceDto> getAllInsurance() {
        return insuranceRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public InsuranceDto getInsuranceById(Long id) {
        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance not found with id: " + id));
        return toDto(insurance);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InsuranceDto> getInsuranceByUserId(Long userId) {
        return insuranceRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InsuranceDto> getInsuranceByBookingId(Long bookingId) {
        return insuranceRepository.findByBookingId(bookingId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public InsuranceDto createInsurance(InsuranceDto insuranceDto) {
        Insurance insurance = toEntity(insuranceDto);
        insurance.setPolicyNumber(generatePolicyNumber());
        insurance.setStatus("ACTIVE");
        Insurance savedInsurance = insuranceRepository.save(insurance);
        return toDto(savedInsurance);
    }
    
    @Override
    public InsuranceDto updateInsurance(Long id, InsuranceDto insuranceDto) {
        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance not found with id: " + id));
        
        insurance.setPolicyType(insuranceDto.getPolicyType());
        insurance.setPremium(insuranceDto.getPremium());
        insurance.setCoverageAmount(insuranceDto.getCoverageAmount());
        insurance.setStartDate(insuranceDto.getStartDate());
        insurance.setEndDate(insuranceDto.getEndDate());
        insurance.setCoverageDetails(insuranceDto.getCoverageDetails());
        
        Insurance updatedInsurance = insuranceRepository.save(insurance);
        return toDto(updatedInsurance);
    }
    
    @Override
    public void deleteInsurance(Long id) {
        if (!insuranceRepository.existsById(id)) {
            throw new RuntimeException("Insurance not found with id: " + id);
        }
        insuranceRepository.deleteById(id);
    }
    
    @Override
    public InsuranceDto cancelInsurance(Long id) {
        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance not found with id: " + id));
        
        insurance.setStatus("CANCELLED");
        Insurance cancelledInsurance = insuranceRepository.save(insurance);
        return toDto(cancelledInsurance);
    }
    
    @Override
    public InsuranceDto renewInsurance(Long id) {
        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance not found with id: " + id));
        
        // Extend the end date by one year
        insurance.setEndDate(insurance.getEndDate().plusYears(1));
        insurance.setStatus("ACTIVE");
        
        Insurance renewedInsurance = insuranceRepository.save(insurance);
        return toDto(renewedInsurance);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserInsuranceWithBookingDetails(Long userId) {
        List<InsuranceDto> userInsurance = getInsuranceByUserId(userId);
        List<Map<String, Object>> enrichedInsurance = new ArrayList<>();
        
        for (InsuranceDto insurance : userInsurance) {
            Map<String, Object> enrichedPolicy = new HashMap<>();
            enrichedPolicy.put("insurance", insurance);
            
            try {
                // Fetch booking details
                ResponseEntity<Map<String, Object>> bookingResponse = 
                    bookingServiceClient.getBookingById(insurance.getBookingId());
                if (bookingResponse.getStatusCode().is2xxSuccessful() && bookingResponse.getBody() != null) {
                    enrichedPolicy.put("booking", bookingResponse.getBody().get("data"));
                }
            } catch (Exception e) {
                log.error("Failed to fetch booking details for insurance {}: {}", insurance.getInsuranceId(), e.getMessage());
                enrichedPolicy.put("booking", null);
            }
            
            enrichedInsurance.add(enrichedPolicy);
        }
        
        return enrichedInsurance;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getInsuranceWithBookingAndUserDetails(Long insuranceId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            InsuranceDto insurance = getInsuranceById(insuranceId);
            result.put("insurance", insurance);
            
            // Fetch booking details
            ResponseEntity<Map<String, Object>> bookingResponse = 
                bookingServiceClient.getBookingById(insurance.getBookingId());
            if (bookingResponse.getStatusCode().is2xxSuccessful() && bookingResponse.getBody() != null) {
                result.put("booking", bookingResponse.getBody().get("data"));
            }
            
            // Fetch user details
            ResponseEntity<Map<String, Object>> userResponse = 
                userServiceClient.getUserById(insurance.getUserId());
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                result.put("user", userResponse.getBody().get("data"));
            }
            
        } catch (Exception e) {
            log.error("Failed to fetch insurance details with related data for insurance {}: {}", insuranceId, e.getMessage());
            result.put("error", "Failed to fetch insurance details");
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInsuranceQuotes(Long bookingId) {
        List<Map<String, Object>> quotes = new ArrayList<>();
        
        try {
            // Fetch booking details to calculate quotes
            ResponseEntity<Map<String, Object>> bookingResponse = 
                bookingServiceClient.getBookingById(bookingId);
                
            if (bookingResponse.getStatusCode().is2xxSuccessful() && bookingResponse.getBody() != null) {
                // Generate different insurance quotes based on booking
                quotes.add(createQuote("BASIC", new BigDecimal("50.00"), new BigDecimal("10000.00")));
                quotes.add(createQuote("PREMIUM", new BigDecimal("100.00"), new BigDecimal("25000.00")));
                quotes.add(createQuote("COMPREHENSIVE", new BigDecimal("200.00"), new BigDecimal("50000.00")));
            }
            
        } catch (Exception e) {
            log.error("Failed to generate insurance quotes for booking {}: {}", bookingId, e.getMessage());
        }
        
        return quotes;
    }
    
    private Map<String, Object> createQuote(String type, BigDecimal premium, BigDecimal coverage) {
        Map<String, Object> quote = new HashMap<>();
        quote.put("policyType", type);
        quote.put("premium", premium);
        quote.put("coverageAmount", coverage);
        quote.put("benefits", getInsuranceBenefits(type));
        return quote;
    }
    
    private List<String> getInsuranceBenefits(String type) {
        switch (type) {
            case "BASIC":
                return Arrays.asList("Trip cancellation", "Medical emergencies", "Lost luggage");
            case "PREMIUM":
                return Arrays.asList("Trip cancellation", "Medical emergencies", "Lost luggage", "Trip delay", "Flight cancellation");
            case "COMPREHENSIVE":
                return Arrays.asList("Trip cancellation", "Medical emergencies", "Lost luggage", "Trip delay", "Flight cancellation", "Adventure sports", "Pre-existing conditions");
            default:
                return Collections.emptyList();
        }
    }
    
    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis();
    }
    
    private InsuranceDto toDto(Insurance insurance) {
        return new InsuranceDto(
                insurance.getInsuranceId(),
                insurance.getUserId(),
                insurance.getBookingId(),
                insurance.getPolicyType(),
                insurance.getPolicyNumber(),
                insurance.getPremium(),
                insurance.getCoverageAmount(),
                insurance.getStartDate(),
                insurance.getEndDate(),
                insurance.getStatus(),
                insurance.getCoverageDetails(),
                insurance.getCreatedAt(),
                insurance.getUpdatedAt()
        );
    }
    
    private Insurance toEntity(InsuranceDto dto) {
        return new Insurance(
                dto.getInsuranceId(),
                dto.getUserId(),
                dto.getBookingId(),
                dto.getPolicyType(),
                dto.getPolicyNumber(),
                dto.getPremium(),
                dto.getCoverageAmount(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getStatus(),
                dto.getCoverageDetails(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }
}