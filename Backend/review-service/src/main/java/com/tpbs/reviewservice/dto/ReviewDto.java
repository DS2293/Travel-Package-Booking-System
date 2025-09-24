package com.tpbs.reviewservice.dto;

import lombok.*;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    @JsonProperty("ReviewID")
    private Long reviewID;

    @NotNull
    @JsonProperty("UserID")
    private Long userID;

    @NotNull
    @JsonProperty("PackageID")
    private Long packageID;

    @Min(1)
    @Max(5)
    @JsonProperty("Rating")
    private int rating;

    @NotBlank
    @JsonProperty("Comment")
    private String comment;

    // Keep lowercase to match frontend usage: review.agentReply
    @JsonProperty("agentReply")
    private String agentReply;

    @JsonProperty("Timestamp")
    @JsonAlias("timestamp")
    private Instant timestamp;
    
    // Enhanced fields for cross-service data
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("userEmail") 
    private String userEmail;
    
    @JsonProperty("packageName")
    private String packageName;
    
    @JsonProperty("packageDestination")
    private String packageDestination;
    
    @JsonProperty("packagePrice")
    private Double packagePrice;
}