package com.tpbs.reviewservice.controller;

import com.tpbs.reviewservice.dto.ReviewDto;
import com.tpbs.reviewservice.dto.AgentReplyDto;
import com.tpbs.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

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
    public ReviewDto addReview(@Valid @RequestBody ReviewDto dto) {
        return reviewService.addReview(dto);
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