package com.example.appointment.User;

import com.example.appointment.Appointment.Appointment;
import com.example.appointment.Appointment.AppointmentDTO;
import com.example.appointment.Appointment.AppointmentService;
import com.example.appointment.Appointment.AvailableSlotDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final AppointmentService appointmentService;

    // Get all active appointments for the current customer (excluding cancelled)
    @GetMapping("/appointments/show_active_appointments")
    public ResponseEntity<List<AppointmentDTO>> getActiveAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long customerId = currentUser.getId();

        List<Appointment> appointments = appointmentService.getAppointmentsByCustomerId(customerId);
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    // Get all appointments for the current customer (including cancelled)
    @GetMapping("/appointments/show_all_appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long customerId = currentUser.getId();

        List<Appointment> appointments = appointmentService.getAllAppointmentsByCustomerId(customerId);
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    // Get a specific appointment by ID for the current customer
    @GetMapping("/appointments/show_appointment/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long customerId = currentUser.getId();

        return appointmentService.getAppointmentByIdAndCustomerId(id, customerId)
                .map(appointment -> ResponseEntity.ok(convertToDTO(appointment)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Cancel an appointment
    @DeleteMapping("/appointments/cancel_appointment/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long customerId = currentUser.getId();

        Appointment cancelledAppointment = appointmentService.cancelAppointment(id, customerId);
        if (cancelledAppointment != null) {
            return ResponseEntity.ok("Appointment cancelled successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get available time slots for a specific service on a specific date - from jalal
    @GetMapping("/appointments/available_slots")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AvailableSlotDTO> availableSlots = appointmentService.getAvailableSlots(serviceId, date);
        return ResponseEntity.ok(availableSlots);
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
