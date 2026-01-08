package com.example.appointment.Notifications;


import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.User.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(nullable = false)
    private String data;

    @Column(nullable = true , name = "readed_at")
    private LocalDateTime readedAt;

    @Column(nullable = false ,name = "created_at")
    private  LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore  // Prevent circular reference during WebSocket serialization
    private UserModel user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;


}
