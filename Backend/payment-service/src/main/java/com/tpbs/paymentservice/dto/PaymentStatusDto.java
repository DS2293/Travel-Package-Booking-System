package com.tpbs.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusDto {
    
    @NotBlank(message = "Status is required")
    @JsonProperty("status")
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("description")
    private String description;
}
