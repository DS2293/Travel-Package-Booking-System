package com.tpbs.insuranceservice.service;

import com.tpbs.insuranceservice.dto.InsuranceDto;

import java.util.List;
import java.util.Map;

public interface InsuranceService {
    
    List<InsuranceDto> getAllInsurance();
    InsuranceDto getInsuranceById(Long id);
    List<InsuranceDto> getInsuranceByUserId(Long userId);
    List<InsuranceDto> getInsuranceByBookingId(Long bookingId);
    InsuranceDto createInsurance(InsuranceDto insuranceDto);
    InsuranceDto updateInsurance(Long id, InsuranceDto insuranceDto);
    void deleteInsurance(Long id);
    InsuranceDto cancelInsurance(Long id);
    InsuranceDto renewInsurance(Long id);
    
    // Enhanced methods with cross-service data
    List<Map<String, Object>> getUserInsuranceWithBookingDetails(Long userId);
    Map<String, Object> getInsuranceWithBookingAndUserDetails(Long insuranceId);
    List<Map<String, Object>> getInsuranceQuotes(Long bookingId);
}