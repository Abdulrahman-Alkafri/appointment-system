package com.example.appointment.Holiday;


import com.example.appointment.Holiday.DTOs.CreateHolidayRequest;
import com.example.appointment.Holiday.DTOs.HolidayDTO;
import com.example.appointment.Holiday.DTOs.UpdateHolidayRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {
    @Autowired
    private HolidayRepository holidayRepository;

    public HolidayDTO createHoliday(CreateHolidayRequest request) throws Exception {

     Holiday holiday=new Holiday();
     holiday.setHolidayDate(request.holidayDate());

     holiday=holidayRepository.save(holiday);



     return holiday.convertToHolidayDTO();

    }

    @Transactional
    public HolidayDTO updateHoliday(Long id , UpdateHolidayRequest request) throws Exception {
     Optional <Holiday> holiday=holidayRepository.findById(id);
     if(holiday.isEmpty()){
         throw new Exception("holiday : "+id+" not found");
     }
     Holiday holiday1=holiday.get();
     holiday1.setHolidayDate(request.holidayDate());
     holidayRepository.save(holiday1);


     return holiday1.convertToHolidayDTO();
    }

    @Transactional
    public boolean deleteHoliday(Long id) throws Exception  {

        holidayRepository.deleteById(id);

         return true;

    }

    public List<Holiday> getAllHolidays(){
        List<Holiday> holidays=holidayRepository.findAll();

        return holidays;
    }

}


