package com.example.appointment.Appointment;

import com.example.appointment.Holiday.Holiday;
import com.example.appointment.Holiday.HolidayRepository;
import com.example.appointment.Services.Service;
import com.example.appointment.Services.ServiceRepository;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserRepository;
import com.example.appointment.WorkingSchedule.Working_schedule;
import com.example.appointment.WorkingSchedule.Working_scheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private Working_scheduleRepository workingScheduleRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private UserModel customer;
    private UserModel employee;
    private Service service;
    private Working_schedule workingSchedule;
    private Holiday holiday;

    @BeforeEach
    void setUp() {
        customer = new UserModel();
        customer.setId(1L);
        customer.setUsername("customer1");
        customer.setEmail("customer1@example.com");

        employee = new UserModel();
        employee.setId(2L);
        employee.setUsername("employee1");
        employee.setEmail("employee1@example.com");

        service = new Service();
        service.setId(1L);
        service.setName("Haircut");
        service.setDuration(60); // 60 minutes

        workingSchedule = new Working_schedule();
        workingSchedule.setId(1L);
        workingSchedule.setDay(DayOfWeek.MONDAY);
        workingSchedule.setStartTime(LocalTime.of(9, 0)); // 9:00 AM
        workingSchedule.setEndTime(LocalTime.of(17, 0)); // 5:00 PM
        workingSchedule.setEmployees(Set.of(employee));

        holiday = new Holiday();
        holiday.setId(1L);
        holiday.setHolidayDate(LocalDate.of(2025, 12, 25)); // Christmas
    }

    @Test
    void testReserveAppointment_Success() {
        Long customerId = 1L;
        Long serviceId = 1L;
        LocalDateTime appointmentDateTime = LocalDateTime.of(2025, 10, 10, 12, 0); // 2025-10-10 12:00:00

        // Mock service repository
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // Mock working schedule repository
        when(workingScheduleRepository.findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY))
                .thenReturn(List.of(workingSchedule));

        // Mock user repository
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Mock holiday repository
        when(holidayRepository.findAll()).thenReturn(List.of());

        // Mock appointment repository
        when(appointmentRepository.findByEmployeeIdAndDateRange(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of()); // No conflicts

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(1L);
            return appointment;
        });

        // Execute
        Appointment result = appointmentService.reserveAppointment(customerId, serviceId, appointmentDateTime);

        // Verify
        assertNotNull(result);
        assertEquals(customer, result.getCustomer());
        assertEquals(employee, result.getEmployee());
        assertEquals(service, result.getService());
        assertEquals(appointmentDateTime, result.getFrom());
        assertEquals(appointmentDateTime.plusMinutes(service.getDuration()), result.getTo());
        assertEquals(Appointment.AppointmentStatus.SCHEDULED, result.getStatus());

        // Verify interactions
        verify(serviceRepository).findById(serviceId);
        verify(workingScheduleRepository).findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY);
        verify(userRepository).findById(customerId);
        verify(holidayRepository).findAll();
        verify(appointmentRepository).findByEmployeeIdAndDateRange(eq(employee.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void testReserveAppointment_WhenDateIsHoliday_ThrowsException() {
        Long customerId = 1L;
        Long serviceId = 1L;
        LocalDateTime appointmentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0); // Christmas 2025-12-25

        // Mock holiday repository
        when(holidayRepository.findAll()).thenReturn(List.of(holiday));

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.reserveAppointment(customerId, serviceId, appointmentDateTime);
        });

        assertEquals("Appointment date cannot be on a holiday: 2025-12-25", exception.getMessage());

        // Verify that other repositories were not called
        verifyNoInteractions(serviceRepository, workingScheduleRepository, userRepository, appointmentRepository);
    }

    @Test
    void testReserveAppointment_WhenServiceNotFound_ThrowsException() {
        Long customerId = 1L;
        Long serviceId = 999L; // Non-existent service
        LocalDateTime appointmentDateTime = LocalDateTime.of(2025, 10, 10, 12, 0);

        // Mock service repository
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        // Mock holiday repository
        when(holidayRepository.findAll()).thenReturn(List.of());

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.reserveAppointment(customerId, serviceId, appointmentDateTime);
        });

        assertEquals("Service not found with id: " + serviceId, exception.getMessage());

        // Verify interactions
        verify(serviceRepository).findById(serviceId);
        verify(holidayRepository).findAll();
        verifyNoMoreInteractions(serviceRepository, holidayRepository);
    }

    @Test
    void testReserveAppointment_WhenNoEmployeesAvailable_ThrowsException() {
        Long customerId = 1L;
        Long serviceId = 1L;
        LocalDateTime appointmentDateTime = LocalDateTime.of(2025, 10, 10, 12, 0);

        // Mock service repository
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // Mock working schedule repository - return empty list
        when(workingScheduleRepository.findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY))
                .thenReturn(List.of());

        // Mock user repository
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Mock holiday repository
        when(holidayRepository.findAll()).thenReturn(List.of());

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.reserveAppointment(customerId, serviceId, appointmentDateTime);
        });

        assertEquals("No employees available for service " + serviceId + " on FRIDAY", exception.getMessage());

        // Verify interactions
        verify(serviceRepository).findById(serviceId);
        verify(workingScheduleRepository).findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY);
        verify(userRepository).findById(customerId);
        verify(holidayRepository).findAll();
    }

    @Test
    void testReserveAppointment_WhenNoAvailableEmployee_ThrowsException() {
        Long customerId = 1L;
        Long serviceId = 1L;
        LocalDateTime appointmentDateTime = LocalDateTime.of(2025, 10, 10, 12, 0);

        // Mock service repository
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // Mock working schedule repository
        when(workingScheduleRepository.findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY))
                .thenReturn(List.of(workingSchedule));

        // Mock user repository
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Mock holiday repository
        when(holidayRepository.findAll()).thenReturn(List.of());

        // Mock appointment repository - return a conflicting appointment
        when(appointmentRepository.findByEmployeeIdAndDateRange(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Appointment())); // Conflict exists

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.reserveAppointment(customerId, serviceId, appointmentDateTime);
        });

        assertEquals("No available employee for service " + serviceId + " at time " + appointmentDateTime, exception.getMessage());

        // Verify interactions
        verify(serviceRepository).findById(serviceId);
        verify(workingScheduleRepository).findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY);
        verify(userRepository).findById(customerId);
        verify(holidayRepository).findAll();
        verify(appointmentRepository).findByEmployeeIdAndDateRange(eq(employee.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testReserveAppointment_WhenCustomerNotFound_ThrowsException() {
        Long customerId = 999L; // Non-existent customer
        Long serviceId = 1L;
        LocalDateTime appointmentDateTime = LocalDateTime.of(2025, 10, 10, 12, 0);

        // Mock service repository
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        // Mock working schedule repository
        when(workingScheduleRepository.findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY))
                .thenReturn(List.of(workingSchedule));

        // Mock user repository - customer not found
        when(userRepository.findById(customerId)).thenReturn(Optional.empty());

        // Mock holiday repository
        when(holidayRepository.findAll()).thenReturn(List.of());

        // Mock appointment repository
        when(appointmentRepository.findByEmployeeIdAndDateRange(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of()); // No conflicts

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.reserveAppointment(customerId, serviceId, appointmentDateTime);
        });

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());

        // Verify interactions
        verify(serviceRepository).findById(serviceId);
        verify(workingScheduleRepository).findByServiceIdAndDay(serviceId, DayOfWeek.FRIDAY);
        verify(userRepository).findById(customerId);
        verify(holidayRepository).findAll();
        verify(appointmentRepository).findByEmployeeIdAndDateRange(eq(employee.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}