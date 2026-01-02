package com.example.appointment.WorkingSchedule;

import com.example.appointment.User.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.Map;

@RestController
@RequestMapping("/api/working-schedules")
@RequiredArgsConstructor
public class Working_scheduleController {

    private final Working_scheduleService workingScheduleService;

    // Public endpoint to get all working schedules
    @GetMapping("/get")
    public List<Working_scheduleDTO> getAllWorkingSchedules() {
        return workingScheduleService.getAllWorkingSchedules();
    }

    // Public endpoint to get a specific working schedule
    @GetMapping("/get_by/{id}")
    public Working_scheduleDTO getWorkingSchedule(@PathVariable Long id) {
        return workingScheduleService.getWorkingScheduleById(id);
    }

    // Admin and Staff endpoints for managing working schedules
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO createWorkingSchedule(@RequestBody Working_scheduleDTO dto) {
        return workingScheduleService.createWorkingSchedule(dto);
    }

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO updateWorkingSchedule(@PathVariable Long id,
                                                     @RequestBody Working_scheduleDTO dto) {
        return workingScheduleService.updateWorkingSchedule(id, dto);
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public void deleteWorkingSchedule(@PathVariable Long id) {
        workingScheduleService.deleteWorkingSchedule(id);
    }

    // Employee-schedule association endpoints
    @PostMapping("/{scheduleId}/link/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO assignEmployeeToWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        return workingScheduleService.assignEmployeeToWorkingSchedule(employeeId, scheduleId);
    }

    @DeleteMapping("/{scheduleId}/unlink/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO removeEmployeeFromWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        return workingScheduleService.removeEmployeeFromWorkingSchedule(employeeId, scheduleId);
    }

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public Set<Working_scheduleDTO> getWorkingSchedulesForEmployee(@PathVariable Long employeeId) {
        return workingScheduleService.getWorkingSchedulesForEmployee(employeeId);
    }

    @GetMapping("/{scheduleId}/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public List<UserModel> getEmployeesForWorkingSchedule(@PathVariable Long scheduleId) {
        return workingScheduleService.getEmployeesForWorkingSchedule(scheduleId);
    }

    // New endpoints for staff-specific working schedule management
    @GetMapping("/get/staff_in_working/")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getAllStaffWithWorkingSchedules() {
        return workingScheduleService.getAllStaffWithWorkingSchedules();
    }

    @GetMapping("/{scheduleId}/staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public List<UserModel> getStaffForWorkingSchedule(@PathVariable Long scheduleId) {
        return workingScheduleService.getStaffForWorkingSchedule(scheduleId);
    }
}