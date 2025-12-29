package com.example.appointment.Services;


import com.example.appointment.Appointment.Appointment;
import com.example.appointment.User.UserModel;
import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "services")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false  ,length = 128)
    private  String name;

    @Column(nullable = true  ) // able to delete this column
    private String description;

    @Column(nullable = false)
    @Size(min = 0 , message = "cannot be negative")
    private  int Cost;

    @Column(nullable = false)
    private Duration duration;

    @ManyToMany
    @JoinTable(
            name = "emp_serv",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserModel> employees = new HashSet<>();

    @OneToMany(mappedBy = "service")

    private List<Appointment> appointments;

}
