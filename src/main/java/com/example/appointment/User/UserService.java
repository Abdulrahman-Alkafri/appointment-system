package com.example.appointment.User;
import com.example.appointment.Common.enums.UserRole;
import com.example.appointment.User.dto.CreateUserRequest;
import com.example.appointment.User.dto.UpdateUserRequest;
import com.example.appointment.User.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public UserModel getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserModel getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new RuntimeException("Email already exists");
        if (userRepository.existsByUsername(req.username()))
            throw new RuntimeException("Username already exists");

        UserModel user = new UserModel();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhoneNumber(req.phoneNumber());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        UserModel user = getUserByIdOrThrow(id);
        
        // Only allow editing STAFF users
        if (user.getRole() != UserRole.STAFF) {
            throw new RuntimeException("Can only update users with STAFF role");
        }
        
        // Validate email uniqueness if changed
        if (!user.getEmail().equals(req.email()) && userRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validate username uniqueness if changed
        if (!user.getUsername().equals(req.username()) && userRepository.existsByUsername(req.username())) {
            throw new RuntimeException("Username already exists");
        }
        
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhoneNumber(req.phoneNumber());
        
        // Only allow changing role to STAFF
        if (req.role() != UserRole.STAFF) {
            throw new RuntimeException("Can only assign STAFF role to users");
        }
        user.setRole(req.role());

        userRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUser(Long id) {
        UserModel user = getUserByIdOrThrow(id);
        
        // Prevent admin from deleting themselves
        UserModel currentUser = getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Cannot delete your own account");
        }
        
        userRepository.deleteById(id);
    }

    public UserResponse mapToResponse(UserModel user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    public java.util.Optional<UserModel> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserModel> findByRole(UserRole role){

        return userRepository.findUserModelByRole(role);


    }


}
