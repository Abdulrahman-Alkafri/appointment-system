package com.example.appointment.Holiday;


import com.example.appointment.Holiday.DTOs.HolidayDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Holiday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;


    public HolidayDTO convertToHolidayDTO(){
        HolidayDTO holidayDTO=new HolidayDTO();

        holidayDTO.setHolidayDate(this.holidayDate);
        holidayDTO.setId(this.id);

        return holidayDTO;

    }

}
