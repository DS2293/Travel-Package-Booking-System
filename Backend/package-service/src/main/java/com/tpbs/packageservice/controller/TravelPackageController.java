package com.tpbs.packageservice.controller;

import com.tpbs.packageservice.dto.TravelPackageDto;
import com.tpbs.packageservice.service.TravelPackageService;
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
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Slf4j
public class TravelPackageController {
    
    private final TravelPackageService packageService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPackages() {
        log.debug("Fetching all packages");
        List<TravelPackageDto> packages = packageService.getAllPackages();
        Map<String, Object> response = new HashMap<>();
        response.put("data", packages);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPackageById(@PathVariable("id") Long id) {
        log.debug("Fetching package with id: {}", id);
        TravelPackageDto packageDto = packageService.getPackageById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", packageDto);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<Map<String, Object>> getPackagesByAgent(@PathVariable("agentId") Long agentId) {
        log.debug("Fetching packages for agent: {}", agentId);
        List<TravelPackageDto> packages = packageService.getPackagesByAgent(agentId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", packages);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPackage(@Valid @RequestBody TravelPackageDto packageDto) {
        log.info("Creating new package: {}", packageDto.getTitle());
        TravelPackageDto createdPackage = packageService.createPackage(packageDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdPackage);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePackage(@PathVariable("id") Long id, @Valid @RequestBody TravelPackageDto packageDto) {
        log.info("Updating package with id: {}", id);
        TravelPackageDto updatedPackage = packageService.updatePackage(id, packageDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedPackage);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePackage(@PathVariable("id") Long id) {
        log.info("Deleting package with id: {}", id);
        packageService.deletePackage(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Package deleted successfully");
        return ResponseEntity.ok(response);
    }
      @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPackages(@RequestParam("keyword") String keyword) {
        log.debug("Searching packages with keyword: {}", keyword);
        List<TravelPackageDto> packages = packageService.searchPackages(keyword);
        Map<String, Object> response = new HashMap<>();
        response.put("data", packages);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    // Enhanced endpoints with cross-service data
    @GetMapping("/{id}/with-details")
    public ResponseEntity<Map<String, Object>> getPackageWithDetails(@PathVariable("id") Long id) {
        log.debug("Fetching package with details for id: {}", id);
        Map<String, Object> packageWithDetails = packageService.getPackageWithBookingDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("data", packageWithDetails);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/agent/{agentId}/with-stats")
    public ResponseEntity<Map<String, Object>> getAgentPackagesWithStats(@PathVariable("agentId") Long agentId) {
        log.debug("Fetching agent packages with statistics for agentId: {}", agentId);
        Map<String, Object> agentData = packageService.getAgentPackagesWithStatistics(agentId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", agentData);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}