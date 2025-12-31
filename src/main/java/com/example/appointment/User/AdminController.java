package com.example.appointment.User;

import com.example.appointment.Services.*;
import com.example.appointment.Services.ServiceDTOs.CreateServiceRequest;
import com.example.appointment.Services.ServiceDTOs.updateServiceRequest;
import com.example.appointment.User.dto.CreateUserRequest;
import com.example.appointment.User.dto.UpdateUserRequest;
import com.example.appointment.User.dto.UserResponse;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ServicesServ servicesServ;

    // User Management Endpoints
    @GetMapping("/users/show_all_users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userService::mapToResponse)
                .toList();
    }

    @PostMapping("/users/create_user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/users/update_user/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/delete_user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Service Management Endpoints
    @PostMapping("/services/create")
    public ResponseEntity<?> createService(@Valid @RequestBody CreateServiceRequest request) {
    //  return ResponseEntity.ok(request);
        try{
            ServiceResponse response = servicesServ.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
      
    }
      catch(Exception e) {
        return ResponseEntity.ok(e.getMessage());
      }
 
    }

    @PostMapping("/services/link-to-staff")
    public ResponseEntity<ServiceResponse> linkServiceToStaff(@Valid @RequestBody LinkServiceToStaffRequest request) {
        ServiceResponse response = servicesServ.linkServiceToStaff(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/services/all")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = servicesServ.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        ServiceResponse service = servicesServ.getServiceById(id);
        return ResponseEntity.ok(service);
    }
      
    @PostMapping("/services/update/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id ,@Valid @RequestBody updateServiceRequest request ){
      
        ServiceResponse response=servicesServ.updateService(id, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/services/delete/{id}")
    @Transactional
    public ResponseEntity<?> deleteService(@PathVariable Long id ){
     try{
       boolean res= servicesServ.deleteService(id);
       return ResponseEntity.ok(res);
     }catch(Exception e){
      return ResponseEntity.badRequest().body(e);
     }
    }

    @GetMapping("/services/employees/{id}")
    @Transactional(readOnly = true) 
    public ResponseEntity<?> getSeviceEmployes(@PathVariable Long id){
        Service serv = servicesServ.getServWithEmployes(id);
        return ResponseEntity.ok(serv.getEmployees());
    }

    @GetMapping("/services/appointments/{id}")
    public ResponseEntity<?> getSeviceAppointments(@PathVariable Long id){
        Service serv = servicesServ.getServWithAppointments(id);
        return ResponseEntity.ok(serv.getAppointments());
    }

}
