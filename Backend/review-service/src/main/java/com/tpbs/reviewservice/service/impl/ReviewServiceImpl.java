package com.tpbs.reviewservice.service.impl;

import com.tpbs.reviewservice.client.BookingServiceClient;
import com.tpbs.reviewservice.client.PackageServiceClient;
import com.tpbs.reviewservice.client.UserServiceClient;
import com.tpbs.reviewservice.dto.ReviewDto;
import com.tpbs.reviewservice.model.Review;
import com.tpbs.reviewservice.repository.ReviewRepository;
import com.tpbs.reviewservice.service.ReviewService;
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
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;
    private final PackageServiceClient packageServiceClient;
    private final BookingServiceClient bookingServiceClient;

    private ReviewDto toDto(Review review) {
        Instant ts = review.getTimestamp() == null ? null : review.getTimestamp().toInstant(ZoneOffset.UTC);
        return ReviewDto.builder()
                .reviewID(review.getReviewID())
                .userID(review.getUserID())
                .packageID(review.getPackageID())
                .rating(review.getRating())
                .comment(review.getComment())
                .agentReply(review.getAgentReply())
                .timestamp(ts)
                .build();
    }

    private Review toEntity(ReviewDto dto) {
        LocalDateTime ts = dto.getTimestamp() == null ? null : LocalDateTime.ofInstant(dto.getTimestamp(), ZoneOffset.UTC);
        return Review.builder()
                .reviewID(dto.getReviewID())
                .userID(dto.getUserID())
                .packageID(dto.getPackageID())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .agentReply(dto.getAgentReply())
                .timestamp(ts)
                .build();
    }

    // Enhanced method to get user details via Feign client
    private Map<String, Object> getUserDetails(Long userId) {
        try {
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserById(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody().get("data");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user details for userId: {}, error: {}", userId, e.getMessage());
        }
        return null;
    }
    
    // Enhanced method to get package details via Feign client  
    private Map<String, Object> getPackageDetails(Long packageId) {
        try {
            ResponseEntity<Map<String, Object>> response = packageServiceClient.getPackageById(packageId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody().get("data");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch package details for packageId: {}, error: {}", packageId, e.getMessage());
        }
        return null;
    }
    
    // Enhanced method to validate booking exists
    private boolean validateUserBookedPackage(Long userId, Long packageId) {
        try {
            ResponseEntity<Map<String, Object>> response = bookingServiceClient.getBookingsByUser(userId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> bookings = (List<Map<String, Object>>) response.getBody().get("data");
                return bookings.stream().anyMatch(booking -> 
                    packageId.equals(((Number) booking.get("packageId")).longValue()));
            }
        } catch (Exception e) {
            log.warn("Failed to validate booking for userId: {} and packageId: {}, error: {}", userId, packageId, e.getMessage());
        }
        return false; // Default to false for security
    }

    // Enhanced toDto method with cross-service data
    private ReviewDto toDtoEnhanced(Review review) {
        ReviewDto dto = toDto(review);
        
        // Add user details
        Map<String, Object> userDetails = getUserDetails(review.getUserID());
        if (userDetails != null) {
            dto.setUserName((String) userDetails.get("name"));
            dto.setUserEmail((String) userDetails.get("email"));
        }
        
        // Add package details
        Map<String, Object> packageDetails = getPackageDetails(review.getPackageID());
        if (packageDetails != null) {
            dto.setPackageName((String) packageDetails.get("name"));
            dto.setPackageDestination((String) packageDetails.get("destination"));
            dto.setPackagePrice((Double) packageDetails.get("price"));
        }
        
        return dto;
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        return reviewRepository.findById(id).map(this::toDto).orElse(null);
    }    @Override
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        return reviewRepository.findAllByUserId(userId).stream().map(this::toDtoEnhanced).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByPackageId(Long packageId) {
        return reviewRepository.findAllByPackageId(packageId).stream().map(this::toDtoEnhanced).collect(Collectors.toList());
    }

    @Override
    public ReviewDto addReview(ReviewDto dto) {
        // Validate that user has actually booked the package
        if (!validateUserBookedPackage(dto.getUserID(), dto.getPackageID())) {
            log.warn("User {} attempted to review package {} without booking", dto.getUserID(), dto.getPackageID());
            throw new IllegalArgumentException("You can only review packages you have booked");
        }
        
        Review review = toEntity(dto);
        if (review.getTimestamp() == null) {
            review.setTimestamp(LocalDateTime.now());
        }
        Review saved = reviewRepository.save(review);
        return toDtoEnhanced(saved);
    }

    @Override
    public ReviewDto updateReview(Long id, ReviewDto dto) {
        return reviewRepository.findById(id).map(existing -> {
            if (dto.getRating() != 0) existing.setRating(dto.getRating());
            if (dto.getComment() != null) existing.setComment(dto.getComment());
            if (dto.getAgentReply() != null) existing.setAgentReply(dto.getAgentReply());
            return toDto(reviewRepository.save(existing));
        }).orElse(null);
    }

    @Override
    public ReviewDto addAgentReply(Long id, String agentReply) {
        return reviewRepository.findById(id).map(existing -> {
            existing.setAgentReply(agentReply);
            return toDto(reviewRepository.save(existing));
        }).orElse(null);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}