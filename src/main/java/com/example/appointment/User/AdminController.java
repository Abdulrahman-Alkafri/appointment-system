package com.example.appointment.User;

import com.example.appointment.Appointment.Appointment;
import com.example.appointment.Appointment.AppointmentDTO;
import com.example.appointment.Appointment.AppointmentService;
import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Notifications.NotificationService;
import lombok.extern.slf4j.Slf4j;
import com.example.appointment.Holiday.DTOs.CreateHolidayRequest;
import com.example.appointment.Holiday.DTOs.HolidayDTO;
import com.example.appointment.Holiday.DTOs.UpdateHolidayRequest;
import com.example.appointment.Holiday.Holiday;
import com.example.appointment.Holiday.HolidayService;
import com.example.appointment.Services.*;
import com.example.appointment.Services.ServiceDTOs.CreateServiceRequest;
import com.example.appointment.Services.ServiceDTOs.updateServiceRequest;
import com.example.appointment.User.dto.CreateUserRequest;
import com.example.appointment.User.dto.UpdateUserRequest;
import com.example.appointment.User.dto.UserResponse;
import com.example.appointment.WorkingSchedule.Working_scheduleDTO;
import com.example.appointment.WorkingSchedule.Working_scheduleService;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final Working_scheduleService workingScheduleService;
    private final ServicesServ servicesServ;
    private final HolidayService holidayService;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;

    // User endpoints

    // User Management Endpoints
    @GetMapping("/users/show_all_users")
    public List<UserResponse> getAllUsers() {
        log.info("ADMIN endpoint accessed - /api/admin/users/show_all_users - ADMIN role required");
        log.debug("Debug log: Processing request to get all users");

        try {
            List<UserModel> users = userService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(userService::mapToResponse)
                    .toList();
            log.debug("Found {} users", userResponses.size());

            log.info("Successfully retrieved {} users", userResponses.size());
            return userResponses;
        } catch (Exception e) {
            log.error("Error retrieving all users: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/users/create_user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("ADMIN endpoint accessed - /api/admin/users/create_user - ADMIN role required");
        log.debug("Debug log: Processing request to create user with username: {}", request.username());

        try {
            UserResponse response = userService.createUser(request);
            log.info("Successfully created user with ID: {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating user with username: {} - {}", request.username(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PutMapping("/users/update_user/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        log.info("ADMIN endpoint accessed - /api/admin/users/update_user/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to update user with ID: {}", id);

        try {
            UserResponse response = userService.updateUser(id, request);
            log.info("Successfully updated user with ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating user with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/users/delete_user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/users/delete_user/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to delete user with ID: {}", id);

        try {
            userService.deleteUser(id);
            log.info("Successfully deleted user with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting user with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Service Management Endpoints
    @PostMapping("/services/create")
    public ResponseEntity<?> createService(@Valid @RequestBody CreateServiceRequest request) {
        log.info("ADMIN endpoint accessed - /api/admin/services/create - ADMIN role required");
        log.debug("Debug log: Processing request to create service with name: {}", request.name());

        try {
            ServiceResponse response = servicesServ.createService(request);
            log.info("Successfully created service with ID: {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating service with name: {} - {}", request.name(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/services/link-to-staff")
    public ResponseEntity<ServiceResponse> linkServiceToStaff(@Valid @RequestBody LinkServiceToStaffRequest request) {
        log.info("ADMIN endpoint accessed - /api/admin/services/link-to-staff - ADMIN role required");
        log.debug("Debug log: Processing request to link service ID: {} to staff", request.serviceId());

        try {
            ServiceResponse response = servicesServ.linkServiceToStaff(request);
            log.info("Successfully linked service ID: {} to staff", request.serviceId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error linking service ID: {} to staff - {}", request.serviceId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Working Schedule endpoints
    @GetMapping("/working-schedules")
    public List<Working_scheduleDTO> getAllWorkingSchedules() {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules - ADMIN role required");
        log.debug("Debug log: Processing request to get all working schedules");

        try {
            List<Working_scheduleDTO> schedules = workingScheduleService.getAllWorkingSchedules();
            log.debug("Found {} working schedules", schedules.size());

            log.info("Successfully retrieved {} working schedules", schedules.size());
            return schedules;
        } catch (Exception e) {
            log.error("Error retrieving all working schedules: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/working-schedules/{id}")
    public Working_scheduleDTO getWorkingSchedule(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to get working schedule by ID: {}", id);

        try {
            Working_scheduleDTO schedule = workingScheduleService.getWorkingScheduleById(id);
            log.debug("Found working schedule with ID: {}", id);

            log.info("Successfully retrieved working schedule with ID: {}", id);
            return schedule;
        } catch (Exception e) {
            log.error("Error retrieving working schedule with ID: {} - {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/working-schedules")
    public Working_scheduleDTO createWorkingSchedule(@RequestBody Working_scheduleDTO dto) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules - ADMIN role required");
        log.debug("Debug log: Processing request to create working schedule");

        try {
            Working_scheduleDTO createdSchedule = workingScheduleService.createWorkingSchedule(dto);
            log.debug("Created working schedule with ID: {}", createdSchedule.getId());

            log.info("Successfully created working schedule with ID: {}", createdSchedule.getId());
            return createdSchedule;
        } catch (Exception e) {
            log.error("Error creating working schedule: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/working-schedules/{id}")
    public Working_scheduleDTO updateWorkingSchedule(@PathVariable Long id,
                                                     @RequestBody Working_scheduleDTO dto) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to update working schedule with ID: {}", id);

        try {
            Working_scheduleDTO updatedSchedule = workingScheduleService.updateWorkingSchedule(id, dto);
            log.debug("Updated working schedule with ID: {}", id);

            log.info("Successfully updated working schedule with ID: {}", id);
            return updatedSchedule;
        } catch (Exception e) {
            log.error("Error updating working schedule with ID: {} - {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/working-schedules/{id}")
    public void deleteWorkingSchedule(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to delete working schedule with ID: {}", id);

        try {
            workingScheduleService.deleteWorkingSchedule(id);
            log.info("Successfully deleted working schedule with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting working schedule with ID: {} - {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Employee-schedule association endpoints
    @PostMapping("/working-schedules/{scheduleId}/employees/{employeeId}")
    public Working_scheduleDTO assignEmployeeToWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{}/employees/{} - ADMIN role required", scheduleId, employeeId);
        log.debug("Debug log: Processing request to assign employee ID: {} to working schedule ID: {}", employeeId, scheduleId);

        try {
            Working_scheduleDTO updatedSchedule = workingScheduleService.assignEmployeeToWorkingSchedule(employeeId, scheduleId);
            log.debug("Assigned employee ID: {} to working schedule ID: {}", employeeId, scheduleId);

            log.info("Successfully assigned employee ID: {} to working schedule ID: {}", employeeId, scheduleId);
            return updatedSchedule;
        } catch (Exception e) {
            log.error("Error assigning employee ID: {} to working schedule ID: {} - {}", employeeId, scheduleId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/working-schedules/{scheduleId}/employees/{employeeId}")
    public Working_scheduleDTO removeEmployeeFromWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{}/employees/{} - ADMIN role required", scheduleId, employeeId);
        log.debug("Debug log: Processing request to remove employee ID: {} from working schedule ID: {}", employeeId, scheduleId);

        try {
            Working_scheduleDTO updatedSchedule = workingScheduleService.removeEmployeeFromWorkingSchedule(employeeId, scheduleId);
            log.debug("Removed employee ID: {} from working schedule ID: {}", employeeId, scheduleId);

            log.info("Successfully removed employee ID: {} from working schedule ID: {}", employeeId, scheduleId);
            return updatedSchedule;
        } catch (Exception e) {
            log.error("Error removing employee ID: {} from working schedule ID: {} - {}", employeeId, scheduleId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/working-schedules/employees/{employeeId}")
    public Set<Working_scheduleDTO> getWorkingSchedulesForEmployee(@PathVariable Long employeeId) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/employees/{} - ADMIN role required", employeeId);
        log.debug("Debug log: Processing request to get working schedules for employee ID: {}", employeeId);

        try {
            Set<Working_scheduleDTO> schedules = workingScheduleService.getWorkingSchedulesForEmployee(employeeId);
            log.debug("Found {} working schedules for employee ID: {}", schedules.size(), employeeId);

            log.info("Successfully retrieved {} working schedules for employee ID: {}", schedules.size(), employeeId);
            return schedules;
        } catch (Exception e) {
            log.error("Error retrieving working schedules for employee ID: {} - {}", employeeId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/working-schedules/{scheduleId}/employees")
    public List<com.example.appointment.User.UserModel> getEmployeesForWorkingSchedule(@PathVariable Long scheduleId) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{}/employees - ADMIN role required", scheduleId);
        log.debug("Debug log: Processing request to get employees for working schedule ID: {}", scheduleId);

        try {
            List<com.example.appointment.User.UserModel> employees = workingScheduleService.getEmployeesForWorkingSchedule(scheduleId);
            log.debug("Found {} employees for working schedule ID: {}", employees.size(), scheduleId);

            log.info("Successfully retrieved {} employees for working schedule ID: {}", employees.size(), scheduleId);
            return employees;
        } catch (Exception e) {
            log.error("Error retrieving employees for working schedule ID: {} - {}", scheduleId, e.getMessage(), e);
            throw e;
        }
    }

    // New endpoints for staff-working schedule management
    @GetMapping("/working-schedules/staff")
    public List<Map<String, Object>> getAllStaffWithWorkingSchedules() {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/staff - ADMIN role required");
        log.debug("Debug log: Processing request to get all staff with working schedules");

        try {
            List<Map<String, Object>> staffSchedules = workingScheduleService.getAllStaffWithWorkingSchedules();
            log.debug("Found {} staff with working schedules", staffSchedules.size());

            log.info("Successfully retrieved {} staff with working schedules", staffSchedules.size());
            return staffSchedules;
        } catch (Exception e) {
            log.error("Error retrieving all staff with working schedules: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/working-schedules/{scheduleId}/staff")
    public List<com.example.appointment.User.UserModel> getStaffForWorkingSchedule(@PathVariable Long scheduleId) {
        log.info("ADMIN endpoint accessed - /api/admin/working-schedules/{}/staff - ADMIN role required", scheduleId);
        log.debug("Debug log: Processing request to get staff for working schedule ID: {}", scheduleId);

        try {
            List<com.example.appointment.User.UserModel> staff = workingScheduleService.getStaffForWorkingSchedule(scheduleId);
            log.debug("Found {} staff for working schedule ID: {}", staff.size(), scheduleId);

            log.info("Successfully retrieved {} staff for working schedule ID: {}", staff.size(), scheduleId);
            return staff;
        } catch (Exception e) {
            log.error("Error retrieving staff for working schedule ID: {} - {}", scheduleId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/services/all")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = servicesServ.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        ServiceResponse service = servicesServ.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @PostMapping("/services/update/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id ,@Valid @RequestBody updateServiceRequest request ){

        ServiceResponse response=servicesServ.updateService(id, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/services/delete/{id}")
    @Transactional
    public ResponseEntity<?> deleteService(@PathVariable Long id ){
        log.info("ADMIN endpoint accessed - /api/admin/services/delete/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to delete service with ID: {}", id);

        try{
            boolean res= servicesServ.deleteService(id);
            log.info("Successfully deleted service with ID: {}", id);
            return ResponseEntity.ok(res);
        }catch(Exception e){
            log.error("Error deleting service with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/services/employees/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSeviceEmployes(@PathVariable Long id){
        log.info("ADMIN endpoint accessed - /api/admin/services/employees/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to get employees for service with ID: {}", id);

        try {
            Service serv = servicesServ.getServWithEmployes(id);
            log.debug("Found {} employees for service with ID: {}", serv.getEmployees().size(), id);

            log.info("Successfully retrieved employees for service with ID: {}", id);
            return ResponseEntity.ok(serv.getEmployees());
        } catch (Exception e) {
            log.error("Error retrieving employees for service with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/services/appointments/{id}")
    public ResponseEntity<?> getSeviceAppointments(@PathVariable Long id){
        log.info("ADMIN endpoint accessed - /api/admin/services/appointments/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to get appointments for service with ID: {}", id);

        try {
            Service serv = servicesServ.getServWithAppointments(id);
            log.debug("Found {} appointments for service with ID: {}", serv.getAppointments().size(), id);

            log.info("Successfully retrieved appointments for service with ID: {}", id);
            return ResponseEntity.ok(serv.getAppointments());
        } catch (Exception e) {
            log.error("Error retrieving appointments for service with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/holidays/create")
    public ResponseEntity<?> createHoliday(@Valid @RequestBody CreateHolidayRequest request){
        log.info("ADMIN endpoint accessed - /api/admin/holidays/create - ADMIN role required");
        log.debug("Debug log: Processing request to create holiday with date: {}", request.holidayDate());

        try{
            HolidayDTO response=holidayService.createHoliday(request);
            log.info("Successfully created holiday with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (Exception e){
            log.error("Error creating holiday with date: {} - {}", request.holidayDate(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/holidays/update/{id}")
    public ResponseEntity<?> updateHoliday(@PathVariable Long id ,@Valid @RequestBody UpdateHolidayRequest request){
        log.info("ADMIN endpoint accessed - /api/admin/holidays/update/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to update holiday with ID: {} and date: {}", id, request.holidayDate());

        try{
            HolidayDTO response=holidayService.updateHoliday(id,request);
            log.info("Successfully updated holiday with ID: {}", id);
            return ResponseEntity.ok(response);
        }
        catch (Exception e){
            log.error("Error updating holiday with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/holidays/delete/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id){
        log.info("ADMIN endpoint accessed - /api/admin/holidays/delete/{} - ADMIN role required", id);
        log.debug("Debug log: Processing request to delete holiday with ID: {}", id);

        try{
            boolean res=holidayService.deleteHoliday(id);
            log.info("Successfully deleted holiday with ID: {}", id);
            return ResponseEntity.ok(res);
        }
        catch (Exception e ){
            log.error("Error deleting holiday with ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/holidays/showAll")
    public ResponseEntity<?> showAllHolidays(){
        log.info("ADMIN endpoint accessed - /api/admin/holidays/showAll - ADMIN role required");
        log.debug("Debug log: Processing request to get all holidays");

        try {
            List<Holiday> holidays= holidayService.getAllHolidays();
            log.debug("Found {} holidays", holidays.size());

            log.info("Successfully retrieved {} holidays", holidays.size());
            return ResponseEntity.ok(holidays);
        } catch (Exception e) {
            log.error("Error retrieving all holidays: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Admin Appointment Management Endpoints
    @GetMapping("/appointments/show_all")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/show_all - ADMIN role required");
        log.debug("Debug log: Processing request to get all appointments");

        try {
            List<Appointment> appointments = appointmentService.getAllAppointments();
            log.debug("Found {} total appointments", appointments.size());

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertAppointmentToDTO)
                    .toList();

            log.info("Successfully retrieved {} appointments", appointmentDTOs.size());
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving all appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/appointments/by_status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable Appointment.AppointmentStatus status) {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/by_status/{} - ADMIN role required", status);
        log.debug("Debug log: Processing request to get appointments by status: {}", status);

        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
            log.debug("Found {} appointments with status: {}", appointments.size(), status);

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertAppointmentToDTO)
                    .toList();

            log.info("Successfully retrieved {} appointments with status: {}", appointmentDTOs.size(), status);
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving appointments with status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments() {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/pending - ADMIN role required");
        log.debug("Debug log: Processing request to get pending appointments");

        try {
            List<Appointment> appointments = appointmentService.getPendingAppointments();
            log.debug("Found {} pending appointments", appointments.size());

            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(this::convertAppointmentToDTO)
                    .toList();

            log.info("Successfully retrieved {} pending appointments", appointmentDTOs.size());
            return ResponseEntity.ok(appointmentDTOs);
        } catch (Exception e) {
            log.error("Error retrieving pending appointments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/appointments/{id}/accept")
    public ResponseEntity<AppointmentDTO> acceptAppointment(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/{}/accept - ADMIN role required", id);
        log.debug("Debug log: Processing request to accept appointment ID: {}", id);

        try {
            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.SCHEDULED);
            if (updatedAppointment != null) {
                log.info("Successfully accepted appointment ID: {} - status changed to SCHEDULED", id);
                AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
                UserModel costomer=updatedAppointment.getCustomer();
                notificationService.createNotification(costomer , NotificationType.ACCEPT , "the  appointment " + updatedAppointment.getId() + " is accepted");

                return ResponseEntity.ok(dto);
            } else {
                log.warn("Failed to accept appointment ID: {} - appointment not found", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error accepting appointment ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/appointments/{id}/reject")
    public ResponseEntity<AppointmentDTO> rejectAppointment(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/{}/reject - ADMIN role required", id);
        log.debug("Debug log: Processing request to reject appointment ID: {}", id);

        try {
            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.REJECTED);
            if (updatedAppointment != null) {
                log.info("Successfully rejected appointment ID: {} - status changed to REJECTED", id);
                AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
                UserModel costomer=updatedAppointment.getCustomer();
                notificationService.createNotification(costomer , NotificationType.REJECT , "the  appointment " + updatedAppointment.getId() + " is rejected");
                return ResponseEntity.ok(dto);
            } else {
                log.warn("Failed to reject appointment ID: {} - appointment not found", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error rejecting appointment ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/{}/cancel - ADMIN role required", id);
        log.debug("Debug log: Processing request to cancel appointment ID: {}", id);

        try {
            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.CANCELLED);
            if (updatedAppointment != null) {
                log.info("Successfully cancelled appointment ID: {} - status changed to CANCELLED", id);
                AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
                return ResponseEntity.ok(dto);
            } else {
                log.warn("Failed to cancel appointment ID: {} - appointment not found", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error cancelling appointment ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable Long id) {
        log.info("ADMIN endpoint accessed - /api/admin/appointments/{}/complete - ADMIN role required", id);
        log.debug("Debug log: Processing request to complete appointment ID: {}", id);

        try {
            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.COMPLETED);
            if (updatedAppointment != null) {
                log.info("Successfully completed appointment ID: {} - status changed to COMPLETED", id);
                AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
                return ResponseEntity.ok(dto);
            } else {
                log.warn("Failed to complete appointment ID: {} - appointment not found", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error completing appointment ID: {} - {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    private AppointmentDTO convertAppointmentToDTO(Appointment appointment) {
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
