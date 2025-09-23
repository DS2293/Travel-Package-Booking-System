package com.tpbs.packageservice.service.impl;

import com.tpbs.packageservice.client.BookingServiceClient;
import com.tpbs.packageservice.client.UserServiceClient;
import com.tpbs.packageservice.dto.TravelPackageDto;
import com.tpbs.packageservice.exception.PackageNotFoundException;
import com.tpbs.packageservice.mapper.TravelPackageMapper;
import com.tpbs.packageservice.model.TravelPackage;
import com.tpbs.packageservice.repository.TravelPackageRepository;
import com.tpbs.packageservice.service.TravelPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TravelPackageServiceImpl implements TravelPackageService {
    
    private final TravelPackageRepository packageRepository;
    private final TravelPackageMapper packageMapper;
    private final BookingServiceClient bookingServiceClient;
    private final UserServiceClient userServiceClient;
    
    @Override
    @Transactional(readOnly = true)
    public List<TravelPackageDto> getAllPackages() {
        log.debug("Fetching all travel packages");
        return packageRepository.findAll().stream()
                .map(packageMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public TravelPackageDto getPackageById(Long id) {
        log.debug("Fetching travel package with id: {}", id);
        TravelPackage travelPackage = packageRepository.findById(id)
                .orElseThrow(() -> new PackageNotFoundException("Package not found with id: " + id));
        return packageMapper.toDto(travelPackage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TravelPackageDto> getPackagesByAgent(Long agentId) {
        log.debug("Fetching travel packages for agent: {}", agentId);
        return packageRepository.findByAgentId(agentId).stream()
                .map(packageMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public TravelPackageDto createPackage(TravelPackageDto packageDto) {
        log.debug("Creating new travel package: {}", packageDto.getTitle());
        TravelPackage travelPackage = packageMapper.toEntity(packageDto);
        travelPackage.setPackageId(null); // Ensure new entity
        TravelPackage savedPackage = packageRepository.save(travelPackage);
        log.info("Created travel package with id: {}", savedPackage.getPackageId());
        return packageMapper.toDto(savedPackage);
    }
    
    @Override
    public TravelPackageDto updatePackage(Long id, TravelPackageDto packageDto) {
        log.debug("Updating travel package with id: {}", id);
        TravelPackage existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new PackageNotFoundException("Package not found with id: " + id));
        
        packageMapper.updateEntityFromDto(packageDto, existingPackage);
        
        TravelPackage updatedPackage = packageRepository.save(existingPackage);
        log.info("Updated travel package with id: {}", id);
        return packageMapper.toDto(updatedPackage);
    }
    
    @Override
    public void deletePackage(Long id) {
        log.debug("Deleting travel package with id: {}", id);
        if (!packageRepository.existsById(id)) {
            throw new PackageNotFoundException("Package not found with id: " + id);
        }
        packageRepository.deleteById(id);
        log.info("Deleted travel package with id: {}", id);
    }
      @Override
    @Transactional(readOnly = true)
    public List<TravelPackageDto> searchPackages(String keyword) {
        log.debug("Searching travel packages with keyword: {}", keyword);
        return packageRepository.searchPackages(keyword).stream()
                .map(packageMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPackageWithBookingDetails(Long packageId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get package details
            TravelPackageDto packageDto = getPackageById(packageId);
            result.put("package", packageDto);
            
            // Get bookings for this package
            ResponseEntity<Map<String, Object>> bookingsResponse = 
                bookingServiceClient.getBookingsByPackage(packageId);
                
            if (bookingsResponse.getStatusCode().is2xxSuccessful() && bookingsResponse.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> bookings = 
                    (List<Map<String, Object>>) bookingsResponse.getBody().get("data");
                result.put("bookings", bookings);
                
                // Calculate booking statistics
                long totalBookings = bookings.size();
                long confirmedBookings = bookings.stream()
                    .filter(b -> "confirmed".equals(b.get("status")))
                    .count();
                
                result.put("totalBookings", totalBookings);
                result.put("confirmedBookings", confirmedBookings);
            } else {
                result.put("bookings", Collections.emptyList());
                result.put("totalBookings", 0);
                result.put("confirmedBookings", 0);
            }
            
        } catch (Exception e) {
            log.error("Failed to fetch package details with bookings for package {}: {}", packageId, e.getMessage());
            result.put("error", "Failed to fetch package details");
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAgentPackagesWithStatistics(Long agentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get agent's packages
            List<TravelPackageDto> agentPackages = getPackagesByAgent(agentId);
            result.put("packages", agentPackages);
            
            // Get agent details
            ResponseEntity<Map<String, Object>> userResponse = 
                userServiceClient.getUserById(agentId);
                
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                result.put("agent", userResponse.getBody().get("data"));
            }
            
            // Calculate overall statistics
            int totalPackages = agentPackages.size();
            result.put("totalPackages", totalPackages);
            
            // Get booking statistics for all packages
            int totalBookingsCount = 0;
            int totalConfirmedBookings = 0;
            
            for (TravelPackageDto pkg : agentPackages) {
                try {
                    ResponseEntity<Map<String, Object>> bookingsResponse = 
                        bookingServiceClient.getBookingsByPackage(pkg.getPackageId());
                        
                    if (bookingsResponse.getStatusCode().is2xxSuccessful() && bookingsResponse.getBody() != null) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> packageBookings = 
                            (List<Map<String, Object>>) bookingsResponse.getBody().get("data");
                            
                        totalBookingsCount += packageBookings.size();
                        totalConfirmedBookings += (int) packageBookings.stream()
                            .filter(b -> "confirmed".equals(b.get("status")))
                            .count();
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch bookings for package {}: {}", pkg.getPackageId(), e.getMessage());
                }
            }
            
            result.put("totalBookings", totalBookingsCount);
            result.put("totalConfirmedBookings", totalConfirmedBookings);
            
        } catch (Exception e) {
            log.error("Failed to fetch agent packages with statistics for agent {}: {}", agentId, e.getMessage());
            result.put("error", "Failed to fetch agent data");
        }
        
        return result;
    }
}