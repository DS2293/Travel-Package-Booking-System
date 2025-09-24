package com.tpbs.reviewservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewID;

    @Column(name = "user_id", nullable = false)
    private Long userID;

    @Column(name = "package_id", nullable = false)
    private Long packageID;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "agent_reply")
    private String agentReply;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}