package com.example.appointment.Notifications.dto;

import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Notifications.NotificationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketNotificationDTO {
    private Long id;
    private String data;
    private LocalDateTime readedAt;
    private LocalDateTime createdAt;
    private Long userId;
    private String username;  // Instead of the full user object
    private NotificationType type;
    
    // Create from NotificationEntity without triggering lazy loading
    public static WebSocketNotificationDTO fromEntity(NotificationEntity entity) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO();
        dto.setId(entity.getId());
        dto.setData(entity.getData());
        dto.setReadedAt(entity.getReadedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUserId(entity.getUser().getId());
        dto.setUsername(entity.getUser().getUsername()); // Safe to access since it's a basic field
        dto.setType(entity.getType());
        return dto;
    }
}