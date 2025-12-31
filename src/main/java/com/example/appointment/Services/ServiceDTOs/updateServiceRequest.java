package com.example.appointment.Services.ServiceDTOs;

import jakarta.validation.constraints.Min;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;


import jakarta.validation.constraints.Size;

public record updateServiceRequest (

    @Nullable
    @Size(max = 128, message = "Service name must not exceed 128 characters")
    String name,
    
    @Nullable
    String description,
    
    @Nullable
    @Min(value = 0, message = "Cost cannot be negative")
    @Max(value = Integer.MAX_VALUE, message = "Cost is too large")
    Integer cost,
    
    @Nullable
    @Min(value = 1, message = "Duration must be at least 1 minute")
    Integer duration
){}
