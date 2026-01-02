package com.example.appointment.Appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentReservationResponse {
    private boolean success;
    private String message;
    private Long appointmentId;
    private Long employeeId;
    
    public static AppointmentReservationResponse success(Long appointmentId, Long employeeId) {
        return new AppointmentReservationResponse(true, "Appointment reserved successfully", appointmentId, employeeId);
    }
    
    public static AppointmentReservationResponse failure(String message) {
        return new AppointmentReservationResponse(false, message, null, null);
    }
}