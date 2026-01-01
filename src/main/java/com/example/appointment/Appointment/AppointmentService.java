package com.example.appointment.Appointment;

import com.example.appointment.Services.Service;
import com.example.appointment.User.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public List<Appointment> getAppointmentsByCustomerId(Long customerId) {
        return appointmentRepository.findByCustomerIdAndStatusNot(customerId, Appointment.AppointmentStatus.CANCELLED);
    }

    public List<Appointment> getAllAppointmentsByCustomerId(Long customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    public Optional<Appointment> getAppointmentByIdAndCustomerId(Long appointmentId, Long customerId) {
        return appointmentRepository.findByIdAndCustomerId(appointmentId, customerId);
    }

    public Appointment cancelAppointment(Long appointmentId, Long customerId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndCustomerId(appointmentId, customerId);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            return appointmentRepository.save(appointment);
        }
        return null;
    }
}
