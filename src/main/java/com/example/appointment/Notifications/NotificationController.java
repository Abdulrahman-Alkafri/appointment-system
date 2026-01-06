package com.example.appointment.Notifications;

import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Notifications.dto.NotificationRequest;
import com.example.appointment.Notifications.dto.NotificationResponse;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adminstaration/notifications")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        UserModel user;
        try {
            user = userService.getUserByIdOrThrow(request.userId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }

        NotificationType notificationType = NotificationType.valueOf(request.type());
        NotificationEntity notification = notificationService.createNotification(user, notificationType, request.data());

        NotificationResponse response = convertToResponse(notification);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(@PathVariable Long userId) {
        List<NotificationEntity> notifications = notificationService.getNotificationsByUser(userId);
        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotificationsByUser(@PathVariable Long userId) {
        List<NotificationEntity> notifications = notificationService.getUnreadNotificationsByUser(userId);
        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse convertToResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getData(),
                entity.getReadedAt(),
                entity.getCreatedAt(),
                entity.getUser().getId(),
                entity.getType()
        );
    }
}