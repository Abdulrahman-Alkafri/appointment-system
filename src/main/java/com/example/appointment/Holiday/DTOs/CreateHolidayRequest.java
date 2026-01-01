package com.example.appointment.Holiday.DTOs;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateHolidayRequest(

        @NotNull
        LocalDate holidayDate

) {
}
