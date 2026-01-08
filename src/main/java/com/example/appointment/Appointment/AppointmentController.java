package com.example.appointment.Appointment;

import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Common.enums.UserRole;
import com.example.appointment.Notifications.NotificationEntity;
import com.example.appointment.Notifications.NotificationService;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final NotificationService notificationService;
    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("PUBLIC endpoint accessed - /api/appointments/available-slots");
        log.debug("Debug log: Processing request to get available slots for service ID: {} on date: {}", serviceId, date);

        try {
            List<AvailableSlotDTO> availableSlots = appointmentService.getAvailableSlots(serviceId, date);
            log.debug("Found {} available slots for service ID: {} on date: {}", availableSlots.size(), serviceId, date);

            log.info("Successfully retrieved {} available slots for service ID: {} on date: {}", availableSlots.size(), serviceId, date);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            log.error("Error retrieving available slots for service ID: {} on date: {} - {}", serviceId, date, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/reserve")
    public ResponseEntity<AppointmentReservationResponse> reserveAppointment(
            @RequestBody AppointmentReservationRequest request) {

        log.info("PUBLIC endpoint accessed - /api/appointments/reserve");
        log.debug("Debug log: Processing request to reserve appointment");

        try {

            if (request.getAppointmentDateTime() == null) {
                log.warn("Appointment date and time is required but was null");
                AppointmentReservationResponse errorResponse = AppointmentReservationResponse.failure("Appointment date and time is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.debug("Attempting to reserve appointment for service ID: {}, customer ID: {}, at time: {}",
                    request.getServiceId(), request.getCustomerId(), request.getAppointmentDateTime());

            AppointmentReservationResponse response = appointmentService.reserveAppointment(
                request.getServiceId(),
                request.getCustomerId(),
                request.getAppointmentDateTime()
            );

            if (response.isSuccess()) {
                log.info("Successfully reserved appointment - Appointment ID: {}, Employee ID: {}",
                        response.getAppointmentId(), response.getEmployeeId());

                List<UserModel> admins=userService.findByRole(UserRole.ADMIN);

                for(UserModel admin : admins){
                    notificationService.createNotification(admin, NotificationType.ORDERED, "order to reserve new appointment in service : " + request.getServiceId());
                }
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to reserve appointment: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }





        } catch (Exception e) {
            log.error("Error reserving appointment: {}", e.getMessage(), e);
            AppointmentReservationResponse errorResponse = AppointmentReservationResponse.failure("Internal server error occurred while reserving appointment");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}