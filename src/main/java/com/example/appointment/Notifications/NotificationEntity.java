package com.example.appointment.Notifications;


import com.example.appointment.Common.enums.NotificationType;

import com.example.appointment.User.UserModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "jsonb")
    private String data;

    @Column(nullable = true , name = "readed_at")
    private LocalDateTime readedAt;

    @Column(nullable = false , name = "created_at")
    private  LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;


}
