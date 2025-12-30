package com.example.appointment.User;

import com.example.appointment.User.dto.CreateUserRequest;
import com.example.appointment.User.dto.UpdateUserRequest;
import com.example.appointment.User.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/show_all_users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userService::mapToResponse)
                .toList();
    }

    @PostMapping("/create_user")
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/update_user/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/delete_user/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
