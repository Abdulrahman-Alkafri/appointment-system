package com.example.appointment.User;

import com.example.appointment.Appointment.Appointment;
import com.example.appointment.Appointment.AppointmentDTO;
import com.example.appointment.Appointment.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffController {

    private final AppointmentService appointmentService;

    // Get all appointments assigned to the current staff member (excluding cancelled)
    @GetMapping("/appointments/show_active_appointments")
    public ResponseEntity<List<AppointmentDTO>> getActiveAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long staffId = currentUser.getId();

        List<Appointment> appointments = appointmentService.getAppointmentsByEmployeeId(staffId);
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    // Get all appointments assigned to the current staff member (including cancelled)
    @GetMapping("/appointments/show_all_appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long staffId = currentUser.getId();

        List<Appointment> appointments = appointmentService.getAllAppointmentsByEmployeeId(staffId);
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    // Get a specific appointment by ID assigned to the current staff member
    @GetMapping("/appointments/show_appointment/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel currentUser = (UserModel) authentication.getPrincipal();
        Long staffId = currentUser.getId();

        return appointmentService.getAppointmentByIdAndEmployeeId(id, staffId)
                .map(appointment -> ResponseEntity.ok(convertToDTO(appointment)))
                .orElse(ResponseEntity.notFound().build());
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
