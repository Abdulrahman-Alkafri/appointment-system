package com.example.appointment.Services;

import jakarta.validation.constraints.NotNull;

public record LinkServiceToStaffRequest(
        @NotNull(message = "Service ID is required")
        Long serviceId,
        
        @NotNull(message = "Staff user ID is required")
        Long staffUserId
) {}

