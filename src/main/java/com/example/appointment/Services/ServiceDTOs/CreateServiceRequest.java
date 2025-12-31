package com.example.appointment.Services.ServiceDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateServiceRequest(
    @NotBlank(message = "Service name is required")
    @Size(max = 128, message = "Service name must not exceed 128 characters")
    String name,
    
    String description,
    
    @NotNull(message = "Cost is required")
    @Min(value = 0, message = "Cost cannot be negative")
    @Max(value = Integer.MAX_VALUE, message = "Cost is too large")
    Integer cost,
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    Integer duration
) {}