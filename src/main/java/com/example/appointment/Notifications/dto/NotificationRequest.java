package com.example.appointment.Notifications.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull
        Long userId,
      @NotNull
     String type,
        @NotNull
        String data

     )
{}
