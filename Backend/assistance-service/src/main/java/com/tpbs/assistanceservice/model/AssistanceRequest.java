package com.tpbs.assistanceservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "assistance_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistanceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestID;

    @Column(name = "user_id", nullable = false)
    private Long userID;

    @Column(name = "issue_description")
    private String issueDescription;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "priority", nullable = false)
    private String priority; // low, medium, high, urgent

    @Column(name = "status", nullable = false)
    private String status; // pending, in_progress, completed, cancelled

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "resolution_time")
    private LocalDateTime resolutionTime;

    @Column(name = "resolution_note")
    private String resolutionNote;
}
