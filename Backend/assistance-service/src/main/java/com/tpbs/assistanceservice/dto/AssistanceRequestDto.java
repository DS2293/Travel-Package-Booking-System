package com.tpbs.assistanceservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistanceRequestDto {
    @JsonProperty("RequestID")
    private Long requestID;

    @NotNull
    @JsonProperty("UserID")
    @JsonAlias({"userID", "userId"})
    private Long userID;

    // Combined description used in some dashboards; optional
    @JsonProperty("IssueDescription")
    @JsonAlias("issueDescription")
    private String issueDescription;

    @NotBlank
    @JsonProperty("Subject")
    @JsonAlias("subject")
    private String subject;

    @NotBlank
    @JsonProperty("Message")
    @JsonAlias("message")
    private String message;

    @NotBlank
    @JsonProperty("Priority")
    @JsonAlias("priority")
    private String priority; // low, medium, high, urgent

    @JsonProperty("Status")
    @JsonAlias("status")
    private String status; // pending, in_progress, completed, cancelled

    @JsonProperty("RequestDate")
    @JsonAlias("requestDate")
    private Instant requestDate;

    @JsonProperty("Timestamp")
    @JsonAlias("timestamp")
    private Instant timestamp;

    @JsonProperty("ResolutionTime")
    @JsonAlias("resolutionTime")
    private Instant resolutionTime;

    // Optional note on resolution
    @JsonProperty("ResolutionNote")
    private String resolutionNote;
    
    // Enhanced fields for cross-service data
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    @JsonProperty("userContactNumber") 
    private String userContactNumber;
    
    @JsonProperty("bookingId")
    private Long bookingId;
    
    @JsonProperty("packageName")
    private String packageName;
    
    @JsonProperty("packageDestination")
    private String packageDestination;
    
    @JsonProperty("travelStartDate")
    private String travelStartDate;
    
    @JsonProperty("travelEndDate") 
    private String travelEndDate;
    
    @JsonProperty("bookingStatus")
    private String bookingStatus;
}
