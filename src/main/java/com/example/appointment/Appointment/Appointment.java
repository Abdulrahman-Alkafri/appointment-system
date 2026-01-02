package com.example.appointment.Appointment;

import com.example.appointment.Services.Service;
import com.example.appointment.User.UserModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;



    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private UserModel customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private UserModel employee;
    @Column(name = "start_time")
    private LocalDateTime from;
    @Column(name = "end_time")
    private LocalDateTime to;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    public enum AppointmentStatus {
        PENDING, SCHEDULED, CANCELLED, COMPLETED, REJECTED
    }
}
