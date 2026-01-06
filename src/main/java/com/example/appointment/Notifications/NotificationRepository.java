package com.example.appointment.Notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserId(Long userId);

    List<NotificationEntity> findByUserIdAndType(Long userId, String type);

    List<NotificationEntity> findByUserIdAndReadedAtIsNull(Long userId);
}
