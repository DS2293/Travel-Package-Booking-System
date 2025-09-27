package com.tpbs.packageservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelPackageDto {
    
    private Long packageId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Duration is required")
    private String duration;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    // Custom setter to handle string to BigDecimal conversion
    @JsonSetter("price")
    public void setPrice(Object price) {
        if (price instanceof String) {
            this.price = new BigDecimal((String) price);
        } else if (price instanceof Number) {
            this.price = BigDecimal.valueOf(((Number) price).doubleValue());
        } else if (price instanceof BigDecimal) {
            this.price = (BigDecimal) price;
        }
    }
    
    private String includedServices;
    
    @NotNull(message = "Agent ID is required")
    private Long agentId;
    
    private String image;
} 