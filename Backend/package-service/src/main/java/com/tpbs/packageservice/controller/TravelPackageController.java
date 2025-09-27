package com.tpbs.packageservice.controller;

import com.tpbs.packageservice.dto.TravelPackageDto;
import com.tpbs.packageservice.service.TravelPackageService;
import com.tpbs.packageservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Slf4j
public class TravelPackageController {
    
    private final TravelPackageService packageService;
    private final JwtUtil jwtUtil;
    
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
    
    @GetMapping("/my-packages")
    public ResponseEntity<Map<String, Object>> getMyPackages(@RequestHeader("Authorization") String authHeader) {
        log.debug("Fetching packages for authenticated agent");
        
        Long agentId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (agentId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        List<TravelPackageDto> packages = packageService.getPackagesByAgent(agentId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", packages);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPackage(
            @RequestBody TravelPackageDto packageDto,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Creating new package: {}", packageDto.getTitle());
        
        // Extract agent ID from JWT token FIRST
        Long agentId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (agentId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Set the agent ID from the JWT token BEFORE validation
        packageDto.setAgentId(agentId);
        log.debug("Setting agentId from JWT: {}", agentId);
        
        // Manual validation after setting agentId
        if (packageDto.getTitle() == null || packageDto.getTitle().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Title is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (packageDto.getDuration() == null || packageDto.getDuration().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Duration is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (packageDto.getPrice() == null || packageDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Price is required and must be greater than 0");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        TravelPackageDto createdPackage = packageService.createPackage(packageDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", createdPackage);
        response.put("success", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePackage(
            @PathVariable("id") Long id, 
            @RequestBody TravelPackageDto packageDto,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Updating package with id: {}", id);
        
        // Extract agent ID from JWT token for authorization
        Long agentId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (agentId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Verify the package belongs to the authenticated agent
        TravelPackageDto existingPackage = packageService.getPackageById(id);
        if (!existingPackage.getAgentId().equals(agentId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only update your own packages");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        // Set the agent ID from the JWT token BEFORE validation
        packageDto.setAgentId(agentId);
        log.debug("Setting agentId from JWT: {}", agentId);
        
        // Manual validation after setting agentId
        if (packageDto.getTitle() == null || packageDto.getTitle().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Title is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (packageDto.getDuration() == null || packageDto.getDuration().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Duration is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (packageDto.getPrice() == null || packageDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Price is required and must be greater than 0");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        TravelPackageDto updatedPackage = packageService.updatePackage(id, packageDto);
        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedPackage);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePackage(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Deleting package with id: {}", id);
        
        // Extract agent ID from JWT token for authorization
        Long agentId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (agentId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Verify the package belongs to the authenticated agent
        TravelPackageDto existingPackage = packageService.getPackageById(id);
        if (!existingPackage.getAgentId().equals(agentId)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "You can only delete your own packages");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
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
    
    @GetMapping("/my-packages/with-stats")
    public ResponseEntity<Map<String, Object>> getMyPackagesWithStats(@RequestHeader("Authorization") String authHeader) {
        log.debug("Fetching authenticated agent packages with statistics");
        
        Long agentId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (agentId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        Map<String, Object> agentData = packageService.getAgentPackagesWithStatistics(agentId);
        Map<String, Object> response = new HashMap<>();
        response.put("data", agentData);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}