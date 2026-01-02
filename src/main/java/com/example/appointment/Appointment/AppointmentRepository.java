package com.example.appointment.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.customer.id = :customerId AND a.status != :status")
    List<Appointment> findByCustomerIdAndStatusNot(@Param("customerId") Long customerId, @Param("status") Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.id = :id AND a.customer.id = :customerId")
    Optional<Appointment> findByIdAndCustomerId(@Param("id") Long id, @Param("customerId") Long customerId);

    // Methods for staff/appointment operations
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.employee.id = :employeeId AND a.status != :status")
    List<Appointment> findByEmployeeIdAndStatusNot(@Param("employeeId") Long employeeId, @Param("status") Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.employee.id = :employeeId")
    List<Appointment> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.id = :id AND a.employee.id = :employeeId")
    Optional<Appointment> findByIdAndEmployeeId(@Param("id") Long id, @Param("employeeId") Long employeeId);

    // Method to get all appointments including cancelled ones
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.customer.id = :customerId")
    List<Appointment> findByCustomerId(@Param("customerId") Long customerId);

    // Method to find appointments by service ID and date range - from jalal
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.service.id = :serviceId AND a.from >= :startOfDay AND a.from < :endOfDay")
    List<Appointment> findByServiceIdAndDate(@Param("serviceId") Long serviceId,
                                           @Param("startOfDay") LocalDateTime startOfDay,
                                           @Param("endOfDay") LocalDateTime endOfDay);

    // Method to find appointments by employee ID and date range
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.employee.id = :employeeId AND a.from >= :startOfDay AND a.from < :endOfDay AND a.status != 'CANCELLED'")
    List<Appointment> findByEmployeeIdAndDate(@Param("employeeId") Long employeeId,
                                           @Param("startOfDay") LocalDateTime startOfDay,
                                           @Param("endOfDay") LocalDateTime endOfDay);

    // Admin methods - get all appointments
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee")
    List<Appointment> findAllAppointments();

    // Admin methods - get appointments by status
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.status = :status")
    List<Appointment> findByStatus(@Param("status") Appointment.AppointmentStatus status);

    // Admin method - get pending appointments
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.employee WHERE a.status = :status")
    List<Appointment> findByStatusOnly(@Param("status") Appointment.AppointmentStatus status);
}
