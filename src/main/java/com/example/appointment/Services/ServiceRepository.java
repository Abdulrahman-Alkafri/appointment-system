package com.example.appointment.Services;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;


public interface ServiceRepository extends JpaRepository<Service, Long> {


    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.employees WHERE s.id = :id")
    Service findWithEmpById(@Param("id") Long id);


    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.appointments WHERE s.id = :id")
    Service findWithAppointmentsById(@Param("id") Long id);
    
    Service  findServiceById(Long id);

    @Query("SELECT DISTINCT s FROM Service s " +
    "LEFT JOIN FETCH s.employees " +
    "LEFT JOIN FETCH s.appointments " +
    "WHERE s.id = :id")
    Service findServiceFullByID(@Param("id") Long id);

}
