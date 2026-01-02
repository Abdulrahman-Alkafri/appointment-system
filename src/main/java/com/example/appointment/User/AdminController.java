package com.example.appointment.User;

import com.example.appointment.Appointment.Appointment;
import com.example.appointment.Appointment.AppointmentDTO;
import com.example.appointment.Appointment.AppointmentService;
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
public class AdminController {

    private final UserService userService;
    private final Working_scheduleService workingScheduleService;
    private final ServicesServ servicesServ;
    private final HolidayService holidayService;
    private final AppointmentService appointmentService;
    // User endpoints

    // User Management Endpoints
    @GetMapping("/users/show_all_users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userService::mapToResponse)
                .toList();
    }

    @PostMapping("/users/create_user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PutMapping("/users/update_user/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/users/delete_user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Service Management Endpoints
    @PostMapping("/services/create")
    public ResponseEntity<?> createService(@Valid @RequestBody CreateServiceRequest request) {
    //  return ResponseEntity.ok(request);
        try{
            ServiceResponse response = servicesServ.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
      catch(Exception e) {
        return ResponseEntity.ok(e.getMessage());
      }

    }

    @PostMapping("/services/link-to-staff")
    public ResponseEntity<ServiceResponse> linkServiceToStaff(@Valid @RequestBody LinkServiceToStaffRequest request) {
        ServiceResponse response = servicesServ.linkServiceToStaff(request);
        return ResponseEntity.ok(response);
    }

    // Working Schedule endpoints
    @GetMapping("/working-schedules")
    public List<Working_scheduleDTO> getAllWorkingSchedules() {
        return workingScheduleService.getAllWorkingSchedules();
    }

    @GetMapping("/working-schedules/{id}")
    public Working_scheduleDTO getWorkingSchedule(@PathVariable Long id) {
        return workingScheduleService.getWorkingScheduleById(id);
    }

    @PostMapping("/working-schedules")
    public Working_scheduleDTO createWorkingSchedule(@RequestBody Working_scheduleDTO dto) {
        return workingScheduleService.createWorkingSchedule(dto);
    }

    @PutMapping("/working-schedules/{id}")
    public Working_scheduleDTO updateWorkingSchedule(@PathVariable Long id,
                                                     @RequestBody Working_scheduleDTO dto) {
        return workingScheduleService.updateWorkingSchedule(id, dto);
    }

    @DeleteMapping("/working-schedules/{id}")
    public void deleteWorkingSchedule(@PathVariable Long id) {
        workingScheduleService.deleteWorkingSchedule(id);
    }

    // Employee-schedule association endpoints
    @PostMapping("/working-schedules/{scheduleId}/employees/{employeeId}")
    public Working_scheduleDTO assignEmployeeToWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        return workingScheduleService.assignEmployeeToWorkingSchedule(employeeId, scheduleId);
    }

    @DeleteMapping("/working-schedules/{scheduleId}/employees/{employeeId}")
    public Working_scheduleDTO removeEmployeeFromWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        return workingScheduleService.removeEmployeeFromWorkingSchedule(employeeId, scheduleId);
    }

    @GetMapping("/working-schedules/employees/{employeeId}")
    public Set<Working_scheduleDTO> getWorkingSchedulesForEmployee(@PathVariable Long employeeId) {
        return workingScheduleService.getWorkingSchedulesForEmployee(employeeId);
    }

    @GetMapping("/working-schedules/{scheduleId}/employees")
    public List<com.example.appointment.User.UserModel> getEmployeesForWorkingSchedule(@PathVariable Long scheduleId) {
        return workingScheduleService.getEmployeesForWorkingSchedule(scheduleId);
    }

    // New endpoints for staff-working schedule management
    @GetMapping("/working-schedules/staff")
    public List<Map<String, Object>> getAllStaffWithWorkingSchedules() {
        return workingScheduleService.getAllStaffWithWorkingSchedules();
    }

    @GetMapping("/working-schedules/{scheduleId}/staff")
    public List<com.example.appointment.User.UserModel> getStaffForWorkingSchedule(@PathVariable Long scheduleId) {
        return workingScheduleService.getStaffForWorkingSchedule(scheduleId);
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
     try{
       boolean res= servicesServ.deleteService(id);
       return ResponseEntity.ok(res);
     }catch(Exception e){
      return ResponseEntity.badRequest().body(e);
     }
    }

    @GetMapping("/services/employees/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSeviceEmployes(@PathVariable Long id){
        Service serv = servicesServ.getServWithEmployes(id);
        return ResponseEntity.ok(serv.getEmployees());
    }

    @GetMapping("/services/appointments/{id}")
    public ResponseEntity<?> getSeviceAppointments(@PathVariable Long id){
        Service serv = servicesServ.getServWithAppointments(id);
        return ResponseEntity.ok(serv.getAppointments());
    }

    @PostMapping("/holidays/create")
    public ResponseEntity<?> createHoliday(@Valid @RequestBody CreateHolidayRequest request){
      try{
      HolidayDTO response=holidayService.createHoliday(request);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
      }
      catch (Exception e){
       return ResponseEntity.badRequest().body(e.getMessage());
      }
    }

    @PostMapping("/holidays/update/{id}")
    public ResponseEntity<?> updateHoliday(@PathVariable Long id ,@Valid @RequestBody UpdateHolidayRequest request){
        try{
            HolidayDTO response=holidayService.updateHoliday(id,request);
            return ResponseEntity.ok(response);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/holidays/delete/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id){
     try{
      boolean res=holidayService.deleteHoliday(id);
      return    ResponseEntity.ok(res);
     }
     catch (Exception e ){
       return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/holidays/showAll")
    public ResponseEntity<?> showAllHolidays(){
      List<Holiday> holidays= holidayService.getAllHolidays();
      return ResponseEntity.ok(holidays);
    }

    // Admin Appointment Management Endpoints
    @GetMapping("/appointments/show_all")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/by_status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable Appointment.AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments() {
        List<Appointment> appointments = appointmentService.getPendingAppointments();
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertAppointmentToDTO)
                .toList();
        return ResponseEntity.ok(appointmentDTOs);
    }

    @PutMapping("/appointments/{id}/accept")
    public ResponseEntity<AppointmentDTO> acceptAppointment(@PathVariable Long id) {
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.SCHEDULED);
        if (updatedAppointment != null) {
            AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/appointments/{id}/reject")
    public ResponseEntity<AppointmentDTO> rejectAppointment(@PathVariable Long id) {
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.REJECTED);
        if (updatedAppointment != null) {
            AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id) {
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.CANCELLED);
        if (updatedAppointment != null) {
            AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable Long id) {
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, Appointment.AppointmentStatus.COMPLETED);
        if (updatedAppointment != null) {
            AppointmentDTO dto = convertAppointmentToDTO(updatedAppointment);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
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
