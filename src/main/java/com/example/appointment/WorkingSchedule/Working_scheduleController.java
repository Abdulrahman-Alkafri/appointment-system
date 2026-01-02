package com.example.appointment.WorkingSchedule;

import com.example.appointment.User.UserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.Map;

@RestController
@RequestMapping("/api/working-schedules")
@RequiredArgsConstructor
@Slf4j
public class Working_scheduleController {

    private final Working_scheduleService workingScheduleService;

    // Public endpoint to get all working schedules
    @GetMapping("/get")
    public List<Working_scheduleDTO> getAllWorkingSchedules() {
        log.info("PUBLIC endpoint accessed - /api/working-schedules/get");
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

    // Public endpoint to get a specific working schedule
    @GetMapping("/get_by/{id}")
    public Working_scheduleDTO getWorkingSchedule(@PathVariable Long id) {
        log.info("PUBLIC endpoint accessed - /api/working-schedules/get_by/{}", id);
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

    // Admin and Staff endpoints for managing working schedules
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO createWorkingSchedule(@RequestBody Working_scheduleDTO dto) {
        log.info("ADMIN endpoint accessed - /api/working-schedules/create - ADMIN role required");
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

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO updateWorkingSchedule(@PathVariable Long id,
                                                     @RequestBody Working_scheduleDTO dto) {
        log.info("ADMIN endpoint accessed - /api/working-schedules/update/{} - ADMIN role required", id);
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

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public void deleteWorkingSchedule(@PathVariable Long id) {
        log.info("ADMIN/STAFF endpoint accessed - /api/working-schedules/delete/{} - ADMIN or STAFF role required", id);
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
    @PostMapping("/{scheduleId}/link/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO assignEmployeeToWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        log.info("ADMIN endpoint accessed - /api/working-schedules/{}/link/employees/{} - ADMIN role required", scheduleId, employeeId);
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

    @DeleteMapping("/{scheduleId}/unlink/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Working_scheduleDTO removeEmployeeFromWorkingSchedule(@PathVariable Long scheduleId, @PathVariable Long employeeId) {
        log.info("ADMIN endpoint accessed - /api/working-schedules/{}/unlink/employees/{} - ADMIN role required", scheduleId, employeeId);
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

    @GetMapping("/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public Set<Working_scheduleDTO> getWorkingSchedulesForEmployee(@PathVariable Long employeeId) {
        log.info("ADMIN/STAFF endpoint accessed - /api/working-schedules/employees/{} - ADMIN or STAFF role required", employeeId);
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

    @GetMapping("/{scheduleId}/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public List<UserModel> getEmployeesForWorkingSchedule(@PathVariable Long scheduleId) {
        log.info("ADMIN/STAFF endpoint accessed - /api/working-schedules/{}/employees - ADMIN or STAFF role required", scheduleId);
        log.debug("Debug log: Processing request to get employees for working schedule ID: {}", scheduleId);

        try {
            List<UserModel> employees = workingScheduleService.getEmployeesForWorkingSchedule(scheduleId);
            log.debug("Found {} employees for working schedule ID: {}", employees.size(), scheduleId);

            log.info("Successfully retrieved {} employees for working schedule ID: {}", employees.size(), scheduleId);
            return employees;
        } catch (Exception e) {
            log.error("Error retrieving employees for working schedule ID: {} - {}", scheduleId, e.getMessage(), e);
            throw e;
        }
    }

    // New endpoints for staff-specific working schedule management
    @GetMapping("/get/staff_in_working/")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> getAllStaffWithWorkingSchedules() {
        log.info("ADMIN endpoint accessed - /api/working-schedules/get/staff_in_working/ - ADMIN role required");
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

    @GetMapping("/{scheduleId}/staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public List<UserModel> getStaffForWorkingSchedule(@PathVariable Long scheduleId) {
        log.info("ADMIN/STAFF endpoint accessed - /api/working-schedules/{}/staff - ADMIN or STAFF role required", scheduleId);
        log.debug("Debug log: Processing request to get staff for working schedule ID: {}", scheduleId);

        try {
            List<UserModel> staff = workingScheduleService.getStaffForWorkingSchedule(scheduleId);
            log.debug("Found {} staff for working schedule ID: {}", staff.size(), scheduleId);

            log.info("Successfully retrieved {} staff for working schedule ID: {}", staff.size(), scheduleId);
            return staff;
        } catch (Exception e) {
            log.error("Error retrieving staff for working schedule ID: {} - {}", scheduleId, e.getMessage(), e);
            throw e;
        }
    }
}