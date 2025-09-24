package com.tpbs.reviewservice.service;

import com.tpbs.reviewservice.dto.ReviewDto;
import java.util.List;

public interface ReviewService {
    List<ReviewDto> getAllReviews();
    ReviewDto getReviewById(Long id);
    List<ReviewDto> getReviewsByUserId(Long userId);
    List<ReviewDto> getReviewsByPackageId(Long packageId);
    ReviewDto addReview(ReviewDto dto);
    ReviewDto updateReview(Long id, ReviewDto dto);
    ReviewDto addAgentReply(Long id, String agentReply);
    void deleteReview(Long id);
}