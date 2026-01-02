package com.example.appointment.Appointment;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AvailableSlotDTO> availableSlots = appointmentService.getAvailableSlots(serviceId, date);
        return ResponseEntity.ok(availableSlots);
    }

    @PostMapping("/reserve")
    public ResponseEntity<AppointmentReservationResponse> reserveAppointment(
            @RequestBody AppointmentReservationRequest request) {

        // Check if appointmentDateTime is null
        if (request.getAppointmentDateTime() == null) {
            AppointmentReservationResponse errorResponse = AppointmentReservationResponse.failure("Appointment date and time is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        AppointmentReservationResponse response = appointmentService.reserveAppointment(
            request.getServiceId(),
            request.getCustomerId(),
            request.getAppointmentDateTime()
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}