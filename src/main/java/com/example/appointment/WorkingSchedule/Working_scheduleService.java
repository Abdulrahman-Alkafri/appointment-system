package com.example.appointment.WorkingSchedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
public class Working_scheduleService {
    @Autowired
      Working_scheduleRepository workRepo;

    public boolean create_working_day(DayOfWeek day, LocalTime from,LocalTime to){
        Working_schedule workingSchedule=new Working_schedule();
        workingSchedule.setDay(day);
        workingSchedule.setEndTime(to);
        workingSchedule.setStartTime(from);
        workRepo.save(workingSchedule);


        return true;
    }
}
