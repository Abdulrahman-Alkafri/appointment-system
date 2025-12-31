package com.example.appointment.WorkingSchedule;

import com.example.appointment.Common.enums.UserRole;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Working_scheduleService {
    private final Working_scheduleRepository workRepo;
    private final UserRepository userRepository;

    public Working_scheduleDTO createWorkingSchedule(Working_scheduleDTO dto) {
        // Validate that start time is before end time
        if (dto.getStartTime().isAfter(dto.getEndTime()) ||
            dto.getStartTime().equals(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Working_schedule workingSchedule = new Working_schedule();
        workingSchedule.setDay(dto.getDay());
        workingSchedule.setStartTime(dto.getStartTime());
        workingSchedule.setEndTime(dto.getEndTime());

        Working_schedule savedSchedule = workRepo.save(workingSchedule);
        return mapToDTO(savedSchedule);
    }

    public List<Working_scheduleDTO> getAllWorkingSchedules() {
        return workRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Working_scheduleDTO getWorkingScheduleById(Long id) {
        Working_schedule schedule = workRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Working schedule not found with id: " + id));
        return mapToDTO(schedule);
    }

    public Working_scheduleDTO updateWorkingSchedule(Long id, Working_scheduleDTO dto) {
        Working_schedule existingSchedule = workRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Working schedule not found with id: " + id));

        // Validate that start time is before end time
        if (dto.getStartTime().isAfter(dto.getEndTime()) ||
            dto.getStartTime().equals(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        existingSchedule.setDay(dto.getDay());
        existingSchedule.setStartTime(dto.getStartTime());
        existingSchedule.setEndTime(dto.getEndTime());

        Working_schedule updatedSchedule = workRepo.save(existingSchedule);
        return mapToDTO(updatedSchedule);
    }

    public void deleteWorkingSchedule(Long id) {
        if (!workRepo.existsById(id)) {
            throw new RuntimeException("Working schedule not found with id: " + id);
        }
        workRepo.deleteById(id);
    }

    @Transactional
    public Working_scheduleDTO assignEmployeeToWorkingSchedule(Long employeeId, Long scheduleId) {
        UserModel employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Validate that the user is a staff member
        if (employee.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("Only staff members can be assigned to working schedules");
        }

        Working_schedule schedule = workRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Working schedule not found with id: " + scheduleId));

        employee.getWorkingtimes().add(schedule);
        userRepository.save(employee);

        return mapToDTO(schedule);
    }

    @Transactional
    public Working_scheduleDTO removeEmployeeFromWorkingSchedule(Long employeeId, Long scheduleId) {
        UserModel employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Validate that the user is a staff member
        if (employee.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("Only staff members can be assigned to working schedules");
        }

        Working_schedule schedule = workRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Working schedule not found with id: " + scheduleId));

        employee.getWorkingtimes().remove(schedule);
        userRepository.save(employee);

        return mapToDTO(schedule);
    }

    public Set<Working_scheduleDTO> getWorkingSchedulesForEmployee(Long employeeId) {
        UserModel employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        return employee.getWorkingtimes().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toSet());
    }

    public List<UserModel> getEmployeesForWorkingSchedule(Long scheduleId) {
        Working_schedule schedule = workRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Working schedule not found with id: " + scheduleId));

        return schedule.getEmployees().stream().toList();
    }

    // New method to get all staff members with their working schedules
    public List<Map<String, Object>> getAllStaffWithWorkingSchedules() {
        List<UserModel> staffMembers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.STAFF)
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserModel staff : staffMembers) {
            Map<String, Object> staffInfo = new HashMap<>();
            staffInfo.put("id", staff.getId());
            staffInfo.put("username", staff.getUsername());
            staffInfo.put("email", staff.getEmail());
            staffInfo.put("workingSchedules", staff.getWorkingtimes().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList()));
            result.add(staffInfo);
        }

        return result;
    }

    // New method to get staff members by working schedule
    public List<UserModel> getStaffForWorkingSchedule(Long scheduleId) {
        Working_schedule schedule = workRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Working schedule not found with id: " + scheduleId));

        return schedule.getEmployees().stream()
                .filter(user -> user.getRole() == UserRole.STAFF)
                .toList();
    }

    public Working_scheduleDTO mapToDTO(Working_schedule schedule) {
        return new Working_scheduleDTO(
                schedule.getId(),
                schedule.getDay(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }

    public boolean create_working_day(DayOfWeek day, LocalTime from, LocalTime to) {
        Working_schedule workingSchedule = new Working_schedule();
        workingSchedule.setDay(day);
        workingSchedule.setEndTime(to);
        workingSchedule.setStartTime(from);
        workRepo.save(workingSchedule);
        return true;
    }
}
