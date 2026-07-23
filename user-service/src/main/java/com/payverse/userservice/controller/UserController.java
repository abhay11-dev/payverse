package com.payverse.userservice.controller;

import com.payverse.userservice.dto.BaseResponse;
import com.payverse.userservice.dto.CreateUserRequest;
import com.payverse.userservice.model.User;
import com.payverse.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.payverse.userservice.model.UserRole;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public BaseResponse<User> register(
            @Valid @RequestBody CreateUserRequest request) {

        User user = userService.register(request);

        return BaseResponse.success(
                user,
                "User registered successfully"
        );
    }

    @GetMapping
    public BaseResponse<List<User>> getAllUsers() {

        List<User> users = userService.getAllUsers();

        return BaseResponse.success(
                users,
                "Users fetched successfully"
        );
    }

    @GetMapping("/{id}")
public BaseResponse<User> getUserById(
        @PathVariable Long id) {

    User user = userService.getUserById(id);

    return BaseResponse.success(
            user,
            "User fetched successfully"
    );
}


@DeleteMapping("/{id}")
public BaseResponse<Object> deleteUser(
        @PathVariable Long id) {

    userService.deleteUser(id);

    return BaseResponse.success(
            null,
            "User deleted successfully"
    );
}

@GetMapping("/count")
public BaseResponse<Long> getUserCount() {

    return BaseResponse.success(
            userService.getUserCount(),
            "User count fetched successfully"
    );
}

@GetMapping("/exists/{id}")
public BaseResponse<Boolean> userExists(
        @PathVariable Long id) {

    return BaseResponse.success(
            userService.userExists(id),
            "User existence checked successfully"
    );
}


@GetMapping("/role/{role}")
public BaseResponse<List<User>> getUsersByRole(
        @PathVariable UserRole role) {

    return BaseResponse.success(
            userService.getUsersByRole(role),
            "Users fetched successfully"
    );
}


@GetMapping("/search")
public BaseResponse<List<User>> searchUsers(
        @RequestParam String keyword) {

    return BaseResponse.success(
            userService.searchUsersByEmail(keyword),
            "Users fetched successfully"
    );
}

@GetMapping("/latest")
public BaseResponse<List<User>> latestUsers() {

    return BaseResponse.success(
            userService.getLatestUsers(),
            "Latest users fetched successfully"
    );
}

}