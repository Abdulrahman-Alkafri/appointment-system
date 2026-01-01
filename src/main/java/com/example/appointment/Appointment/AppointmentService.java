package com.example.appointment.Appointment;


import com.example.appointment.Services.ServiceRepository;
import com.example.appointment.User.UserModel;
import com.example.appointment.WorkingSchedule.Working_schedule;
import com.example.appointment.WorkingSchedule.Working_scheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final Working_scheduleRepository workingScheduleRepository;

    public List<Appointment> getAppointmentsByCustomerId(Long customerId) {
        return appointmentRepository.findByCustomerIdAndStatusNot(customerId, Appointment.AppointmentStatus.CANCELLED);
    }

    public List<Appointment> getAllAppointmentsByCustomerId(Long customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    public Optional<Appointment> getAppointmentByIdAndCustomerId(Long appointmentId, Long customerId) {
        return appointmentRepository.findByIdAndCustomerId(appointmentId, customerId);
    }

    public Appointment cancelAppointment(Long appointmentId, Long customerId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndCustomerId(appointmentId, customerId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            return appointmentRepository.save(appointment);
        }
        return null;
    }

    // Added method to get available time slots for a service on a specific date - from jalal
    public List<AvailableSlotDTO> getAvailableSlots(Long serviceId, LocalDate date) {
        // Get the service
        com.example.appointment.Services.Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) {
            return new ArrayList<>();
        }

        // Get the day of week for the requested date
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Get all working schedules for employees who provide this service on the requested day
        List<Working_schedule> workingSchedules = workingScheduleRepository.findByServiceIdAndDay(serviceId, dayOfWeek);

        // Get all existing appointments for this service on the requested date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Appointment> existingAppointments = appointmentRepository
            .findByServiceIdAndDate(serviceId, startOfDay, endOfDay);

        // Calculate available time slots based on working schedules and existing appointments
        List<AvailableSlotDTO> availableSlots = new ArrayList<>();

        for (Working_schedule schedule : workingSchedules) {
            LocalTime startTime = schedule.getStartTime();
            LocalTime endTime = schedule.getEndTime();
            Long employeeId = schedule.getEmployees().stream()
                .findFirst()
                .map(UserModel::getId)
                .orElse(null);

            if (employeeId != null) {
                // Get appointments for this specific employee on the requested date
                List<Appointment> employeeAppointments = existingAppointments.stream()
                    .filter(app -> app.getEmployee().getId().equals(employeeId))
                    .collect(Collectors.toList());

                // Calculate available slots by subtracting booked appointments from working hours
                List<AvailableSlotDTO> employeeSlots = calculateAvailableSlots(
                    date, startTime, endTime, employeeAppointments, service.getDuration(), employeeId);

                availableSlots.addAll(employeeSlots);
            }
        }

        return availableSlots;
    }

    // Helper method to calculate available time slots - from jalal
    private List<AvailableSlotDTO> calculateAvailableSlots(
            LocalDate date,
            LocalTime scheduleStart,
            LocalTime scheduleEnd,
            List<Appointment> bookedAppointments,
            int serviceDuration,
            Long employeeId) {

        List<AvailableSlotDTO> availableSlots = new ArrayList<>();

        // Sort booked appointments by start time
        bookedAppointments.sort((a, b) -> a.getFrom().compareTo(b.getFrom()));

        LocalTime currentTime = scheduleStart;

        // Process each booked appointment to find available slots before it
        for (Appointment appointment : bookedAppointments) {
            LocalTime appointmentStart = appointment.getFrom().toLocalTime();
            LocalTime appointmentEnd = appointment.getTo().toLocalTime();

            // Check if there's available time before this appointment
            if (currentTime.isBefore(appointmentStart)) {
                // Add available slots from current time to appointment start
                while (!currentTime.isAfter(appointmentStart.minusMinutes(serviceDuration)) &&
                       currentTime.plusMinutes(serviceDuration).compareTo(appointmentStart) <= 0) {
                    LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
                    LocalDateTime slotEnd = slotStart.plusMinutes(serviceDuration);

                    if (slotEnd.compareTo(LocalDateTime.of(date, appointmentStart)) <= 0) {
                        UserModel employee = new UserModel();
                        employee.setId(employeeId);
                        String employeeName = "Employee " + employeeId; // In a real implementation, you'd fetch the actual name

                        availableSlots.add(new AvailableSlotDTO(slotStart, slotEnd, employeeId, employeeName));
                        currentTime = currentTime.plusMinutes(serviceDuration);
                    } else {
                        break;
                    }
                }
            }

            // Move current time to after this appointment
            currentTime = appointmentEnd;
        }

        // Add remaining available slots after the last appointment
        while (!currentTime.isAfter(scheduleEnd.minusMinutes(serviceDuration)) &&
               currentTime.plusMinutes(serviceDuration).compareTo(scheduleEnd) <= 0) {
            LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(serviceDuration);

            if (slotEnd.compareTo(LocalDateTime.of(date, scheduleEnd)) <= 0) {
                UserModel employee = new UserModel();
                employee.setId(employeeId);
                String employeeName = "Employee " + employeeId; // In a real implementation, you'd fetch the actual name

                availableSlots.add(new AvailableSlotDTO(slotStart, slotEnd, employeeId, employeeName));
                currentTime = currentTime.plusMinutes(serviceDuration);
            } else {
                break;
            }
        }

        return availableSlots;
    }
}
