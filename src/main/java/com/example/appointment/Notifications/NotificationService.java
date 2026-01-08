package com.example.appointment.Notifications;

import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Notifications.dto.WebSocketNotificationDTO;
import com.example.appointment.User.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;


    public void sendNotificationToUser(Long userId, NotificationEntity notification) {
        try {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println(" send WebSocket notification");
            System.out.println("User ID: " + userId);
            System.out.println("Notification: " + notification.getData());
            System.out.println("Destination: /user/" + userId + "/queue/notifications");

            // Convert to DTO to avoid lazy loading issues
            WebSocketNotificationDTO notificationDTO = WebSocketNotificationDTO.fromEntity(notification);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notificationDTO  // ✅ Use DTO instead of full entity
            );

            System.out.println(" the notification sent succesfully!");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } catch (Exception e) {
            System.err.println(" an erroe ocured : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void broadcastNotification(NotificationEntity notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification.getData()
        );
    }

    public NotificationEntity createNotification(UserModel user, NotificationType type, String data) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setType(type);
        notification.setData(data);
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);


        sendNotificationToUser(user.getId(), notification);

        return notification;
    }

    public List<NotificationEntity> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<NotificationEntity> getUnreadNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdAndReadedAtIsNull(userId);
    }

    public NotificationEntity markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setReadedAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }
        return null;
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}