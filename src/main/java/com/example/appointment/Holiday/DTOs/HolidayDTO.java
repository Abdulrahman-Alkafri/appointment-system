package com.example.appointment.Holiday.DTOs;

import com.example.appointment.Holiday.Holiday;
import com.example.appointment.Holiday.HolidayService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HolidayDTO {

    private Long id;
    private LocalDate holidayDate;

    public Holiday convertToHolidayEntity(){

      Holiday  holiday=new Holiday();

      holiday.setId(this.id);
      holiday.setHolidayDate(this.holidayDate);

      return holiday;
    }

}
