package com.example.appointment.WorkingSchedule;

import com.example.appointment.User.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface Working_scheduleRepository extends JpaRepository<Working_schedule,Long> {

    // Query to find working schedules by employee ID and day - from jalal
    @Query("SELECT ws FROM Working_schedule ws JOIN ws.employees e WHERE e.id = :employeeId AND ws.day = :day")
    List<Working_schedule> findByEmployeeIdAndDay(@Param("employeeId") Long employeeId, @Param("day") DayOfWeek day);

    // Query to find working schedules by employee ID - from jalal
    @Query("SELECT ws FROM Working_schedule ws JOIN ws.employees e WHERE e.id = :employeeId")
    List<Working_schedule> findByEmployeeId(@Param("employeeId") Long employeeId);

    // Query to find working schedules by service ID and day - from jalal
    @Query("SELECT ws FROM Working_schedule ws JOIN ws.employees e JOIN e.services serv WHERE serv.id = :serviceId AND ws.day = :day")
    List<Working_schedule> findByServiceIdAndDay(@Param("serviceId") Long serviceId, @Param("day") DayOfWeek day);

    // Query to find working schedules by service ID - from jalal
    @Query("SELECT ws FROM Working_schedule ws JOIN ws.employees e JOIN e.services serv WHERE serv.id = :serviceId")
    List<Working_schedule> findByServiceId(@Param("serviceId") Long serviceId);

    // Query to find working schedules by service ID and day with employees fetched - to fix lazy loading issue
    @Query("SELECT DISTINCT ws FROM Working_schedule ws JOIN FETCH ws.employees e JOIN e.services serv WHERE serv.id = :serviceId AND ws.day = :day")
    List<Working_schedule> findByServiceIdAndDayWithEmployees(@Param("serviceId") Long serviceId, @Param("day") DayOfWeek day);
}
