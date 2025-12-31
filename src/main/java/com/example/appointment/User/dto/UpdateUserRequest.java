package com.example.appointment.User.dto;

import com.example.appointment.Common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
        String username,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
        
        @NotBlank(message = "Phone number is required")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,
        
        @NotNull(message = "Role is required")
        UserRole role
) {}
