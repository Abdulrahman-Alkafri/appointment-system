package com.example.appointment.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private String message;
    private String email;
    private Long userId;
    private String role;

    public TokenValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}
