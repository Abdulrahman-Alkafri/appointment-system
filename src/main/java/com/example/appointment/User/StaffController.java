package com.example.appointment.User;

import com.example.appointment.Appointment.Appointment;
import com.example.appointment.Appointment.AppointmentDTO;
import com.example.appointment.Appointment.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Slf4j
public class StaffController {

    private final AppointmentService appointmentService;

    // Get all appointments assigned to the current staff member (excluding cancelled)
    @GetMapping("/appointments/show_active_appointments")
    public ResponseEntity<List<AppointmentDTO>> getActiveAppointments() {
        log.info("STAFF endpoint accessed - /api/staff/appointments/show_active_appointments - STAFF role required");
        log.debug("Debug log: Processing request to get active appointments for staff");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long staffId = currentUser.getId();
            log.debug("Staff ID: {}", staffId);

            List<Appointment> appointments = appointmentService.getAppointmentsByEmployeeId(staffId);
            log.debug("Found {} active appointments for staff ID: {}", appointments.size(), staffId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} active appointments for staff ID: {}", appointmentDTOs.size(), staffId);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving active appointments for staff: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get all appointments assigned to the current staff member (including cancelled)
    @GetMapping("/appointments/show_all_appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        log.info("STAFF endpoint accessed - /api/staff/appointments/show_all_appointments - STAFF role required");
        log.debug("Debug log: Processing request to get all appointments for staff");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long staffId = currentUser.getId();
            log.debug("Staff ID: {}", staffId);

            List<Appointment> appointments = appointmentService.getAllAppointmentsByEmployeeId(staffId);
            log.debug("Found {} total appointments for staff ID: {}", appointments.size(), staffId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} total appointments for staff ID: {}", appointmentDTOs.size(), staffId);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving all appointments for staff: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get only accepted appointments assigned to the current staff member (SCHEDULED status)
    @GetMapping("/appointments/show_accepted_appointments")
    public ResponseEntity<List<AppointmentDTO>> getAcceptedAppointments() {
        log.info("STAFF endpoint accessed - /api/staff/appointments/show_accepted_appointments - STAFF role required");
        log.debug("Debug log: Processing request to get accepted appointments for staff");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long staffId = currentUser.getId();
            log.debug("Staff ID: {}", staffId);

            List<Appointment> appointments = appointmentService.getAppointmentsByEmployeeIdAndStatus(staffId, Appointment.AppointmentStatus.SCHEDULED);
            log.debug("Found {} accepted appointments for staff ID: {}", appointments.size(), staffId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} accepted appointments for staff ID: {}", appointmentDTOs.size(), staffId);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving accepted appointments for staff: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get only completed appointments assigned to the current staff member (COMPLETED status)
    @GetMapping("/appointments/show_completed_appointments")
    public ResponseEntity<List<AppointmentDTO>> getCompletedAppointments() {
        log.info("STAFF endpoint accessed - /api/staff/appointments/show_completed_appointments - STAFF role required");
        log.debug("Debug log: Processing request to get completed appointments for staff");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserModel currentUser = (UserModel) authentication.getPrincipal();
            Long staffId = currentUser.getId();
            log.debug("Staff ID: {}", staffId);

            List<Appointment> appointments = appointmentService.getAppointmentsByEmployeeIdAndStatus(staffId, Appointment.AppointmentStatus.COMPLETED);
            log.debug("Found {} completed appointments for staff ID: {}", appointments.size(), staffId);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} completed appointments for staff ID: {}", appointmentDTOs.size(), staffId);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving completed appointments for staff: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Get a specific appointment by ID assigned to the current staff member
    @GetMapping("/appointments/show_appointment/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        log.info("CUSTOMER endpoint accessed - /api/customer/appointments/show_appointment/{}", id);

        try {
            // الحصول على المستخدم الحالي
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
