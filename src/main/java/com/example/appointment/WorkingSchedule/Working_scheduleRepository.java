package com.example.appointment.WorkingSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Working_scheduleRepository extends JpaRepository<Working_schedule,Long> {
}
