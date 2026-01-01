package com.example.appointment.Holiday.DTOs;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateHolidayRequest(
        @NotNull
        LocalDate holidayDate
) {

}
