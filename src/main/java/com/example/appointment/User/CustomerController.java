package com.example.appointment.User;

import com.example.appointment.Appointment.Appointment;
import com.example.appointment.Appointment.AppointmentDTO;
import com.example.appointment.Appointment.AppointmentService;
import com.example.appointment.Appointment.AvailableSlotDTO;
import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Common.enums.UserRole;
import com.example.appointment.Notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Slf4j
public class CustomerController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final NotificationService notificationService;
    // Get all active appointments for the current customer (excluding cancelled)
    @GetMapping("/appointments/show_active_appointments")
    public ResponseEntity<List<AppointmentDTO>> getActiveAppointments() {
        log.info("CUSTOMER endpoint accessed - /api/customer/appointments/show_active_appointments - CUSTOMER role required");
        log.debug("Debug log: Processing request to get active appointments for customer");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long customerId = currentUser.getId();
            log.debug("Customer ID: {}", customerId);

            List<Appointment> appointments = appointmentService.getAppointmentsByCustomerId(customerId);
            log.debug("Found {} active appointments for customer ID: {}", appointments.size(), customerId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} active appointments for customer ID: {}", appointmentDTOs.size(), customerId);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving active appointments for customer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get all appointments for the current customer (including cancelled)
    @GetMapping("/appointments/show_all_appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        log.info("CUSTOMER endpoint accessed - /api/customer/appointments/show_all_appointments - CUSTOMER role required");
        log.debug("Debug log: Processing request to get all appointments for customer");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long customerId = currentUser.getId();
            log.debug("Customer ID: {}", customerId);

            List<Appointment> appointments = appointmentService.getAllAppointmentsByCustomerId(customerId);
            log.debug("Found {} total appointments for customer ID: {}", appointments.size(), customerId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} total appointments for customer ID: {}", appointmentDTOs.size(), customerId);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving all appointments for customer: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get a specific appointment by ID for the current customer
    @GetMapping("/appointments/show_appointment/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        log.info("CUSTOMER endpoint accessed - /api/customer/appointments/show_appointment/{}", id);

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long customerId = currentUser.getId();

            log.debug("Customer ID: {}, Appointment ID: {}", customerId, id);


            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentByIdAndCustomerId(id, customerId);

            if (appointmentOpt.isPresent()) {
                log.info("Successfully retrieved appointment ID: {} for customer ID: {}", id, customerId);
                return ResponseEntity.ok(convertToDTO(appointmentOpt.get()));
            } else {
                log.warn("Appointment ID: {} not found for customer ID: {}", id, customerId);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error retrieving appointment ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/appointments/cancel_appointment/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        log.info("CUSTOMER endpoint accessed - /api/customer/appointments/cancel_appointment/{} - CUSTOMER role required", id);
        log.debug("Debug log: Processing request to cancel appointment");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long customerId = currentUser.getId();
            log.debug("Customer ID: {}, Appointment ID: {}", customerId, id);
            List<UserModel> admins=userService.findByRole(UserRole.ADMIN);


            Appointment cancelledAppointment = appointmentService.cancelAppointment(id, customerId);
            if (cancelledAppointment != null) {
                log.info("Successfully cancelled appointment ID: {} for customer ID: {}", id, customerId);
                for(UserModel admin : admins){
                    notificationService.createNotification(admin, NotificationType.CANCELLED, " the appointment number : "+cancelledAppointment.getId()+" is Cancelled by user");
                }

                return ResponseEntity.ok("Appointment cancelled successfully");
            } else {
                log.warn("Failed to cancel appointment ID: {} for customer ID: {} - appointment not found", id, customerId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error cancelling appointment ID: {} for customer: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get available time slots for a specific service on a specific date - from jalal
    @GetMapping("/appointments/available_slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("CUSTOMER endpoint accessed - /api/customer/appointments/available_slots - CUSTOMER role required");
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

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setCustomer(appointment.getCustomer());
        dto.setEmployee(appointment.getEmployee());
        dto.setService(appointment.getService());
        dto.setFrom(appointment.getFrom());
        dto.setTo(appointment.getTo());
        dto.setStatus(appointment.getStatus());
        return dto;
    }
}
