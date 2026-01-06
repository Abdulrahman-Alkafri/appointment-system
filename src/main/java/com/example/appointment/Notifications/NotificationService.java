package com.example.appointment.Notifications;

import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.User.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationEntity createNotification(UserModel user, NotificationType type, String data) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setType(type);
        notification.setData(data);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
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