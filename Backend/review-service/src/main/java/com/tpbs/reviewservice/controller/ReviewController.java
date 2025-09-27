package com.tpbs.reviewservice.controller;

import com.tpbs.reviewservice.dto.ReviewDto;
import com.tpbs.reviewservice.dto.AgentReplyDto;
import com.tpbs.reviewservice.service.ReviewService;
import com.tpbs.reviewservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<ReviewDto> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable("id") Long id) {
        ReviewDto dto = reviewService.getReviewById(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    public List<ReviewDto> getReviewsByUser(@PathVariable("userId") Long userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    @GetMapping("/package/{packageId}")
    public List<ReviewDto> getReviewsByPackage(@PathVariable("packageId") Long packageId) {
        return reviewService.getReviewsByPackageId(packageId);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReview(
            @RequestBody ReviewDto dto,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract user ID from JWT token
        Long userId = jwtUtil.extractUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid or missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Set the user ID from JWT token BEFORE validation
        dto.setUserID(userId);
        log.debug("Setting userId from JWT: {}", userId);
        
        // Manual validation after setting userId
        if (dto.getPackageID() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Package ID is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Rating must be between 1 and 5");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        if (dto.getComment() == null || dto.getComment().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Comment is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        try {
            ReviewDto createdReview = reviewService.addReview(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdReview);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable("id") Long id, @RequestBody ReviewDto dto) {
        ReviewDto updated = reviewService.updateReview(id, dto);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ReviewDto> addAgentReply(@PathVariable("id") Long id, @Valid @RequestBody AgentReplyDto replyDto) {
        ReviewDto updated = reviewService.addAgentReply(id, replyDto.getAgentReply());
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        ReviewDto existing = reviewService.getReviewById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}