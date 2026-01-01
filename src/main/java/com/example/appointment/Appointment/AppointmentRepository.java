package com.example.appointment.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByCustomerIdAndStatusNot(Long customerId, Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.id = :id AND a.customer.id = :customerId")
    Optional<Appointment> findByIdAndCustomerId(@Param("id") Long id, @Param("customerId") Long customerId);

    // Method to get all appointments including cancelled ones
    List<Appointment> findByCustomerId(Long customerId);
}
