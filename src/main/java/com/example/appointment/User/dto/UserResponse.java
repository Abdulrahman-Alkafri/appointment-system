package com.example.appointment.User.dto;

import com.example.appointment.Common.enums.UserRole;

public record UserResponse(
        Long id,
        String username,
        String email,
        String phoneNumber,
        UserRole role
) {}
