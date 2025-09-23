package com.tpbs.packageservice.service;

import com.tpbs.packageservice.dto.TravelPackageDto;

import java.util.List;
import java.util.Map;

public interface TravelPackageService {
    
    List<TravelPackageDto> getAllPackages();
    TravelPackageDto getPackageById(Long id);
    List<TravelPackageDto> getPackagesByAgent(Long agentId);
    TravelPackageDto createPackage(TravelPackageDto packageDto);
    TravelPackageDto updatePackage(Long id, TravelPackageDto packageDto);
    void deletePackage(Long id);
    List<TravelPackageDto> searchPackages(String keyword);
    
    // Enhanced methods with cross-service data
    Map<String, Object> getPackageWithBookingDetails(Long packageId);
    Map<String, Object> getAgentPackagesWithStatistics(Long agentId);
}