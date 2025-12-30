package com.example.appointment.User;
import com.example.appointment.User.dto.CreateUserRequest;
import com.example.appointment.User.dto.UpdateUserRequest;
import com.example.appointment.User.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

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

    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        UserModel user = getUserByIdOrThrow(id);
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhoneNumber(req.phoneNumber());
        user.setRole(req.role());

        userRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new RuntimeException("User not found");
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
}
