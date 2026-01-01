package com.example.appointment.Appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testReserveAppointment_Success() throws Exception {
        // Arrange
        ReserveAppointmentRequest request = new ReserveAppointmentRequest(1L, 1L, LocalDateTime.of(2025, 10, 10, 12, 0));
        Appointment mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setFrom(LocalDateTime.of(2025, 10, 10, 12, 0));
        mockAppointment.setTo(LocalDateTime.of(2025, 10, 10, 13, 0));

        when(appointmentService.reserveAppointment(any(Long.class), any(Long.class), any(LocalDateTime.class)))
                .thenReturn(mockAppointment);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.from").value("2025-10-10T12:00:00"))
                .andExpect(jsonPath("$.to").value("2025-10-10T13:00:00"));
    }
}