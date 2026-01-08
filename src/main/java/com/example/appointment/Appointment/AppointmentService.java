package com.example.appointment.Appointment;


import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Holiday.HolidayRepository;
import com.example.appointment.Notifications.NotificationService;
import com.example.appointment.Services.ServiceRepository;
import com.example.appointment.User.UserModel;
import com.example.appointment.WorkingSchedule.Working_schedule;
import com.example.appointment.WorkingSchedule.Working_scheduleRepository;

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

public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final Working_scheduleRepository workingScheduleRepository;
    private final HolidayRepository holidayRepository;
    private final NotificationService notificationService;
    private final AppointmentSchedulerService appointmentSchedulerService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                            ServiceRepository serviceRepository,
                            Working_scheduleRepository workingScheduleRepository,
                            HolidayRepository holidayRepository,
                            NotificationService notificationService,
                            @org.springframework.context.annotation.Lazy AppointmentSchedulerService appointmentSchedulerService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.workingScheduleRepository = workingScheduleRepository;
        this.holidayRepository = holidayRepository;
        this.notificationService = notificationService;
        this.appointmentSchedulerService = appointmentSchedulerService;
    }

    public List<Appointment> getAppointmentsByCustomerId(Long customerId) {
        return appointmentRepository.findByCustomerIdAndStatusNot(customerId, Appointment.AppointmentStatus.CANCELLED);
    }

    public List<Appointment> getAllAppointmentsByCustomerId(Long customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    public Optional<Appointment> getAppointmentByIdAndCustomerId(Long appointmentId, Long customerId) {
        return appointmentRepository.findByIdAndCustomerId(appointmentId, customerId);
    }

    public List<Appointment> getAppointmentsByEmployeeId(Long employeeId) {
        return appointmentRepository.findByEmployeeIdAndStatusNot(employeeId, Appointment.AppointmentStatus.CANCELLED);
    }

    public List<Appointment> getAllAppointmentsByEmployeeId(Long employeeId) {
        return appointmentRepository.findByEmployeeId(employeeId);
    }

    public Optional<Appointment> getAppointmentByIdAndEmployeeId(Long appointmentId, Long employeeId) {
        return appointmentRepository.findByIdAndEmployeeId(appointmentId, employeeId);
    }

    public List<Appointment> getAppointmentsByEmployeeIdAndStatus(Long employeeId, Appointment.AppointmentStatus status) {
        return appointmentRepository.findByEmployeeIdAndStatus(employeeId, status);
    }

    // Admin methods
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllAppointments();
    }

    public List<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    public List<Appointment> getPendingAppointments() {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.PENDING);
    }

    public Appointment updateAppointmentStatus(Long appointmentId, Appointment.AppointmentStatus newStatus) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            Appointment.AppointmentStatus oldStatus = appointment.getStatus();

            // Handle scheduling/cancellation of appointment jobs
            if (newStatus == Appointment.AppointmentStatus.SCHEDULED && oldStatus != Appointment.AppointmentStatus.SCHEDULED) {
                // When accepting an appointment, schedule the completion job
                appointmentSchedulerService.scheduleAppointmentJob(appointment);
            } else if (newStatus == Appointment.AppointmentStatus.CANCELLED && oldStatus != Appointment.AppointmentStatus.CANCELLED) {
                // When cancelling an appointment, cancel the scheduled job
                appointmentSchedulerService.cancelScheduledJob(appointmentId);
            }

            appointment.setStatus(newStatus);
            Appointment savedAppointment = appointmentRepository.save(appointment);

            if (newStatus == Appointment.AppointmentStatus.SCHEDULED && oldStatus != Appointment.AppointmentStatus.SCHEDULED) {
                String message = "Your appointment has been accepted.";
                notificationService.createNotification(appointment.getCustomer(), NotificationType.ACCEPT, message);
            } else if (newStatus == Appointment.AppointmentStatus.REJECTED && oldStatus != Appointment.AppointmentStatus.REJECTED) {
                String message = "Your appointment has been rejected.";
                notificationService.createNotification(appointment.getCustomer(), NotificationType.REJECT, message);
            } else if (newStatus == Appointment.AppointmentStatus.CANCELLED && oldStatus != Appointment.AppointmentStatus.CANCELLED) {
                String message = "Your appointment has been cancelled.";
                notificationService.createNotification(appointment.getCustomer(), NotificationType.CANCELLED, message);
            }

            return savedAppointment;
        }
        return null;
    }

    public Appointment cancelAppointment(Long appointmentId, Long customerId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndCustomerId(appointmentId, customerId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            // Cancel the scheduled job if it exists
            appointmentSchedulerService.cancelScheduledJob(appointmentId);
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
        List<Working_schedule> workingSchedules = workingScheduleRepository.findByServiceIdAndDayWithEmployees(serviceId, dayOfWeek);

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

    public AppointmentReservationResponse reserveAppointment(Long serviceId, Long customerId, LocalDateTime appointmentDateTime) {
        // Get the service to check its duration
        com.example.appointment.Services.Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) {
            return AppointmentReservationResponse.failure("Service not found");
        }

        // Check if the appointment date is a holiday
        LocalDate appointmentDate = appointmentDateTime.toLocalDate();
        List<com.example.appointment.Holiday.Holiday> holidays = holidayRepository.findByHolidayDate(appointmentDate);
        if (!holidays.isEmpty()) {
            return AppointmentReservationResponse.failure("Cannot book appointment on a holiday");
        }

        // Get available slots for the requested service and date
        List<AvailableSlotDTO> availableSlots = getAvailableSlots(serviceId, appointmentDate);

        // Check if the requested time slot exactly matches an available slot
        AvailableSlotDTO matchingSlot = availableSlots.stream()
            .filter(slot -> slot.getStartTime().equals(appointmentDateTime))
            .findFirst()
            .orElse(null);

        if (matchingSlot == null) {
            return AppointmentReservationResponse.failure("Requested time slot is not available or does not match an exact available slot");
        }

        // Use the employee from the matching slot
        Long employeeId = matchingSlot.getEmployeeId();
        if (employeeId == null) {
            return AppointmentReservationResponse.failure("No employee assigned to the requested time slot");
        }

        // Double-check: ensure no appointment (including pending) exists for this employee at this time
        LocalDateTime startOfDay = appointmentDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Appointment> existingAppointments = appointmentRepository
            .findByEmployeeIdAndDate(employeeId, startOfDay, endOfDay);

        LocalDateTime requestedEndDateTime = appointmentDateTime.plusMinutes(service.getDuration());
        for (Appointment existingAppointment : existingAppointments) {
            // Check for overlap regardless of status (including PENDING)
            LocalDateTime existingStart = existingAppointment.getFrom();
            LocalDateTime existingEnd = existingAppointment.getTo();

            // Check if there's an overlap
            if (appointmentDateTime.isBefore(existingEnd) && requestedEndDateTime.isAfter(existingStart)) {
                return AppointmentReservationResponse.failure("Requested time slot conflicts with an existing appointment (including pending ones)");
            }
        }

        // Create and save the new appointment
        Appointment appointment = new Appointment();
        appointment.setCustomer(new UserModel());
        appointment.getCustomer().setId(customerId);
        appointment.setEmployee(new UserModel());
        appointment.getEmployee().setId(employeeId);
        appointment.setFrom(appointmentDateTime);
        appointment.setTo(requestedEndDateTime);
        appointment.setService(service);
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return AppointmentReservationResponse.success(savedAppointment.getId(), employeeId);
    }

    // Helper method to check for appointment conflicts
    private boolean hasAppointmentConflict(Long employeeId, LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        // Find all appointments for this employee on the same date
        LocalDate appointmentDate = requestedStart.toLocalDate();
        LocalDateTime startOfDay = appointmentDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Appointment> existingAppointments = appointmentRepository
            .findByEmployeeIdAndDate(employeeId, startOfDay, endOfDay);

        // Check for any overlapping appointments
        for (Appointment existingAppointment : existingAppointments) {
            LocalDateTime existingStart = existingAppointment.getFrom();
            LocalDateTime existingEnd = existingAppointment.getTo();

            // Check if there's an overlap
            if (requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart)) {
                return true; // Conflict found
            }
        }

        return false; // No conflict
    }

    /**
     * Create a new appointment with the specified status
     * @param customerId The ID of the customer
     * @param employeeId The ID of the employee
     * @param appointmentDateTime The date and time of the appointment
     * @param service The service for the appointment
     * @param status The initial status of the appointment
     * @return The created appointment
     */
    public Appointment createAppointment(Long customerId, Long employeeId, LocalDateTime appointmentDateTime,
                                       com.example.appointment.Services.Service service, Appointment.AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setCustomer(new UserModel());
        appointment.getCustomer().setId(customerId);
        appointment.setEmployee(new UserModel());
        appointment.getEmployee().setId(employeeId);
        appointment.setFrom(appointmentDateTime);
        appointment.setTo(appointmentDateTime.plusMinutes(service.getDuration()));
        appointment.setService(service);
        appointment.setStatus(status);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // If the appointment is created with SCHEDULED status, schedule the completion job
        if (status == Appointment.AppointmentStatus.SCHEDULED) {
            appointmentSchedulerService.scheduleAppointmentJob(savedAppointment);
        }

        return savedAppointment;
    }

    // Getter for repository to be used by scheduler service to avoid circular dependency
    public AppointmentRepository getAppointmentRepository() {
        return appointmentRepository;
    }
}
