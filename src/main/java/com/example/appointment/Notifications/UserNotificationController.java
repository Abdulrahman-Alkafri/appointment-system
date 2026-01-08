package com.example.appointment.Notifications;

import com.example.appointment.Notifications.dto.NotificationResponse;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/notifications")
@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER','STAFF')")
public class UserNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications() {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<NotificationEntity> notifications = notificationService.getNotificationsByUser(currentUser.getId());
        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadUserNotifications() {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<NotificationEntity> notifications = notificationService.getUnreadNotificationsByUser(currentUser.getId());
        List<NotificationResponse> responses = notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(@PathVariable Long id) {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Verify that the notification belongs to the current user
        NotificationEntity notification = notificationRepository.findById(id).orElse(null);
        if (notification == null || !notification.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        NotificationEntity updatedNotification = notificationService.markAsRead(id);
        if (updatedNotification == null) {
            return ResponseEntity.notFound().build();
        }
        NotificationResponse response = convertToResponse(updatedNotification);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteUserNotification(@PathVariable Long id) {
        UserModel currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }


        NotificationEntity notification = notificationRepository.findById(id).orElse(null);
        if (notification == null || !notification.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }


    private UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {


            return null;
        }

        UserModel user = (UserModel) authentication.getPrincipal();
        return user;
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