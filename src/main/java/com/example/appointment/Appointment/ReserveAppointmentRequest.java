package com.example.appointment.Appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveAppointmentRequest {
    private Long customerId;
    private Long serviceId;
    private LocalDateTime dateTime;
}