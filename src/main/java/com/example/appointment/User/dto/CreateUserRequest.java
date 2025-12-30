package com.example.appointment.User.dto;

import com.example.appointment.Common.enums.UserRole;

public record CreateUserRequest(
        String username,
        String email,
        String phoneNumber,
        String password,
        UserRole role
) {}
