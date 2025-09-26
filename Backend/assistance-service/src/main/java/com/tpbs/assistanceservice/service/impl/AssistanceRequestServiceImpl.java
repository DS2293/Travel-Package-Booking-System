package com.tpbs.assistanceservice.service.impl;

import com.tpbs.assistanceservice.client.BookingServiceClient;
import com.tpbs.assistanceservice.client.UserServiceClient;
import com.tpbs.assistanceservice.dto.AssistanceRequestDto;
import com.tpbs.assistanceservice.model.AssistanceRequest;
import com.tpbs.assistanceservice.repository.AssistanceRequestRepository;
import com.tpbs.assistanceservice.service.AssistanceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistanceRequestServiceImpl implements AssistanceRequestService {
    private final AssistanceRequestRepository repository;
    private final UserServiceClient userServiceClient;
    private final BookingServiceClient bookingServiceClient;    // Enhanced method to get user details via Feign client
    @SuppressWarnings("unchecked")
    private Map<String, Object> getUserDetails(Long userId) {
        try {
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserById(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody().get("data");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user details for userId: {}, error: {}", userId, e.getMessage());
        }
        // Return fallback user info to prevent NPEs
        return java.util.Map.of(
            "name", "Unknown User",
            "email", "unknown@example.com",
            "contactNumber", "N/A"
        );
    }    // Enhanced method to get booking details via Feign client
    @SuppressWarnings("unchecked")
    private Map<String, Object> getBookingDetails(Long userId) {
        try {
            ResponseEntity<Map<String, Object>> response = bookingServiceClient.getBookingsByUser(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> bookings = (List<Map<String, Object>>) response.getBody().get("data");
                if (bookings != null && !bookings.isEmpty()) {
                    // Return the most recent active booking
                    return bookings.stream()
                        .filter(booking -> {
                            String status = (String) booking.get("status");
                            return "CONFIRMED".equals(status) || "ACTIVE".equals(status) || "PENDING".equals(status);
                        })
                        .findFirst()
                        .orElse(bookings.get(0)); // Return first booking if no active ones found
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch booking details for userId: {}, error: {}", userId, e.getMessage());
        }
        // Return fallback booking data
        return Map.of(
            "bookingId", 0L,
            "status", "Unknown",
            "packageTitle", "No booking information available"
        );
    }

    private AssistanceRequestDto toDto(AssistanceRequest ar) {
        return AssistanceRequestDto.builder()
                .requestID(ar.getRequestID())
                .userID(ar.getUserID())
                .issueDescription(ar.getIssueDescription())
                .subject(ar.getSubject())
                .message(ar.getMessage())
                .priority(ar.getPriority())
                .status(ar.getStatus())
                .requestDate(ar.getRequestDate() == null ? null : ar.getRequestDate().toInstant(ZoneOffset.UTC))
                .timestamp(ar.getTimestamp() == null ? null : ar.getTimestamp().toInstant(ZoneOffset.UTC))
                .resolutionTime(ar.getResolutionTime() == null ? null : ar.getResolutionTime().toInstant(ZoneOffset.UTC))
                .resolutionNote(ar.getResolutionNote())
                .build();
    }    // Enhanced toDto method with cross-service data
    private AssistanceRequestDto toDtoEnhanced(AssistanceRequest ar) {
        AssistanceRequestDto dto = toDto(ar);
        
        // Add user details
        Map<String, Object> userDetails = getUserDetails(ar.getUserID());
        if (userDetails != null) {
            dto.setUserName((String) userDetails.get("name"));
            dto.setUserEmail((String) userDetails.get("email"));
            dto.setUserContactNumber((String) userDetails.get("contactNumber"));
        }
        
        // Add booking details with null-safe handling
        Map<String, Object> bookingDetails = getBookingDetails(ar.getUserID());
        if (bookingDetails != null) {
            // Safe handling of booking ID
            Object bookingIdObj = bookingDetails.get("bookingId");
            if (bookingIdObj == null) {
                bookingIdObj = bookingDetails.get("id");  // Try alternative field name
            }
            if (bookingIdObj instanceof Number) {
                dto.setBookingId(((Number) bookingIdObj).longValue());
            }
            
            dto.setPackageName((String) bookingDetails.get("packageName"));
            dto.setPackageDestination((String) bookingDetails.get("destination"));
            dto.setTravelStartDate((String) bookingDetails.get("startDate"));
            dto.setTravelEndDate((String) bookingDetails.get("endDate"));
            dto.setBookingStatus((String) bookingDetails.get("status")); // Changed from bookingStatus to status
        }
        
        return dto;
    }

    private AssistanceRequest toEntity(AssistanceRequestDto dto) {
        return AssistanceRequest.builder()
                .requestID(dto.getRequestID())
                .userID(dto.getUserID())
                .issueDescription(dto.getIssueDescription())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .priority(dto.getPriority())
                .status(dto.getStatus())
                .requestDate(dto.getRequestDate() == null ? null : LocalDateTime.ofInstant(dto.getRequestDate(), ZoneOffset.UTC))
                .timestamp(dto.getTimestamp() == null ? null : LocalDateTime.ofInstant(dto.getTimestamp(), ZoneOffset.UTC))
                .resolutionTime(dto.getResolutionTime() == null ? null : LocalDateTime.ofInstant(dto.getResolutionTime(), ZoneOffset.UTC))
                .resolutionNote(dto.getResolutionNote())
                .build();
    }    @Override
    public List<AssistanceRequestDto> getAll() {
        try {
            return repository.findAll().stream().map(this::toDtoEnhanced).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to load enhanced assistance requests, falling back to basic data", e);
            return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
        }
    }

    @Override
    public AssistanceRequestDto getById(Long id) {
        try {
            return repository.findById(id).map(this::toDtoEnhanced).orElse(null);
        } catch (Exception e) {
            log.error("Failed to load enhanced assistance request {}, falling back to basic data", id, e);
            return repository.findById(id).map(this::toDto).orElse(null);
        }
    }

    @Override
    public List<AssistanceRequestDto> getByUserId(Long userId) {
        try {
            return repository.findAllByUserId(userId).stream().map(this::toDtoEnhanced).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to load enhanced assistance requests for user {}, falling back to basic data", userId, e);
            return repository.findAllByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
        }
    }@Override
    public AssistanceRequestDto create(AssistanceRequestDto dto) {
        AssistanceRequest entity = toEntity(dto);
        // Defaults
        if (entity.getStatus() == null) entity.setStatus("pending");
        if (entity.getPriority() == null) entity.setPriority("medium");
        if (entity.getRequestDate() == null) entity.setRequestDate(LocalDateTime.now());
        if (entity.getTimestamp() == null) entity.setTimestamp(LocalDateTime.now());
        if (entity.getIssueDescription() == null && entity.getSubject() != null && entity.getMessage() != null) {
            entity.setIssueDescription(entity.getSubject() + ": " + entity.getMessage());
        }
        AssistanceRequest saved = repository.save(entity);
        return toDtoEnhanced(saved);
    }

    @Override
    public AssistanceRequestDto update(Long id, AssistanceRequestDto dto) {
        return repository.findById(id).map(existing -> {
            if (dto.getIssueDescription() != null) existing.setIssueDescription(dto.getIssueDescription());
            if (dto.getSubject() != null) existing.setSubject(dto.getSubject());
            if (dto.getMessage() != null) existing.setMessage(dto.getMessage());
            if (dto.getPriority() != null) existing.setPriority(dto.getPriority());
            if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
            if (dto.getRequestDate() != null) existing.setRequestDate(LocalDateTime.ofInstant(dto.getRequestDate(), ZoneOffset.UTC));
            if (dto.getTimestamp() != null) existing.setTimestamp(LocalDateTime.ofInstant(dto.getTimestamp(), ZoneOffset.UTC));
            if (dto.getResolutionTime() != null) existing.setResolutionTime(LocalDateTime.ofInstant(dto.getResolutionTime(), ZoneOffset.UTC));
            if (dto.getResolutionNote() != null) existing.setResolutionNote(dto.getResolutionNote());
            return toDto(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public AssistanceRequestDto updateStatus(Long id, String status) {
        return repository.findById(id).map(existing -> {
            existing.setStatus(status);
            return toDto(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public AssistanceRequestDto resolve(Long id, String resolutionNote) {
        return repository.findById(id).map(existing -> {
            existing.setStatus("completed");
            existing.setResolutionTime(LocalDateTime.now());
            existing.setResolutionNote(resolutionNote);
            return toDto(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
