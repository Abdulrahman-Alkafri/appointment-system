package com.example.appointment.Appointment;

import com.example.appointment.Services.Service;
import com.example.appointment.User.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {
    private Long id;
    private UserModel customer;
    private UserModel employee;
    private Service service;
    private LocalDateTime from;
    private LocalDateTime to;
    private Appointment.AppointmentStatus status;
}
