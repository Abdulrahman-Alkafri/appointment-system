package com.example.appointment.Services;



public record ServiceResponse(
        Long id,
        String name,
        String description,
        Integer cost,
        Integer duration
) {}

