package com.example.appointment.Notifications.dto;

import com.example.appointment.Common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String data;
    private LocalDateTime readedAt;
    private LocalDateTime createdAt;
    private Long userId;
    private NotificationType type;

    // Constructors

    }
