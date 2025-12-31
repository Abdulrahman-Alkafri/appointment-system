package com.example.appointment.Services;

import com.example.appointment.Common.enums.UserRole;
import com.example.appointment.Services.ServiceDTOs.CreateServiceRequest;
import com.example.appointment.Services.ServiceDTOs.updateServiceRequest;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicesServ {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceResponse createService(CreateServiceRequest request) {
        com.example.appointment.Services.Service serviceEntity = new com.example.appointment.Services.Service();
        serviceEntity.setName(request.name());
        serviceEntity.setDescription( request.description());
        serviceEntity.setCost((Integer)request.cost());
        serviceEntity.setDuration((Integer) request.duration());
        
        serviceEntity = serviceRepository.save(serviceEntity);
        ServiceResponse response =mapToResponse(serviceEntity);
        return response;
    }

    @Transactional
    public ServiceResponse linkServiceToStaff(LinkServiceToStaffRequest request) {
        // Get the service
        com.example.appointment.Services.Service serviceEntity = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + request.serviceId()));
        
        // Get the user and verify it's STAFF
        UserModel staff = userRepository.findById(request.staffUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.staffUserId()));
        
        if (staff.getRole() != UserRole.STAFF) {
            throw new RuntimeException("User with id " + request.staffUserId() + " is not a STAFF member");
        }
        
      
        serviceEntity.getEmployees().add(staff);
        
       
        serviceRepository.save(serviceEntity);
        
        return mapToResponse(serviceEntity);
    }
    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServiceResponse getServiceById(Long id) {
        com.example.appointment.Services.Service serviceEntity = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return mapToResponse(serviceEntity);
    }
     
     
   public com.example.appointment.Services.Service getServWithEmployes(Long id){
     
   com.example.appointment.Services.Service serv=serviceRepository.findWithEmpById(id);
        
        return serv;
    }

    public com.example.appointment.Services.Service getServWithAppointments(Long id){
     
        com.example.appointment.Services.Service serv=serviceRepository.findWithAppointmentsById(id);
             
             return serv;
         }
    
  public ServiceResponse updateService(Long id , updateServiceRequest request){
   
    com.example.appointment.Services.Service service=serviceRepository.findServiceById(id);
      if(request.cost()!=null){
      service.setCost(request.cost());
      }
      if(request.duration()!=null){
      service.setDuration(request.duration());
      }
      if(request.name()!=null){
      service.setName(request.name());
      }
      if(request.description()!=null){
      service.setDescription(request.description());
      }
      ServiceResponse response=mapToResponse(service);

      return response;

  }

  public boolean deleteService(Long id) throws Exception{

    com.example.appointment.Services.Service serv=serviceRepository.findServiceFullByID(id);
    if(!serv.getAppointments().isEmpty() || !serv.getEmployees().isEmpty()){
     throw new Exception("the service has appointments and employes delete them then delete the service");
     
    }
    serviceRepository.deleteById(id);
    return true;
  }

    private ServiceResponse mapToResponse(com.example.appointment.Services.Service serviceEntity) {
        return new ServiceResponse(
                serviceEntity.getId(),
                serviceEntity.getName(),
                serviceEntity.getDescription(),
                serviceEntity.getCost(),
                serviceEntity.getDuration()
        );
    }
}
