package com.payverse.userservice.controller;

import com.payverse.userservice.dto.BaseResponse;
import com.payverse.userservice.dto.CreateUserRequest;
import com.payverse.userservice.model.User;
import com.payverse.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.payverse.userservice.model.UserRole;
import com.payverse.userservice.dto.AuthResponse;
import com.payverse.userservice.dto.LoginRequest;

import com.payverse.userservice.repository.UserRepository;
import com.payverse.userservice.security.JwtTokenProvider;

import com.payverse.userservice.exception.InvalidCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {



private final UserRepository userRepository;
private final BCryptPasswordEncoder passwordEncoder;
private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

   public AuthController(
        UserService userService,
        UserRepository userRepository,
        BCryptPasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider) {

    this.userService = userService;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
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


    @PostMapping("/login")
public BaseResponse<AuthResponse> login(
        @Valid @RequestBody LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new InvalidCredentialsException("Invalid email or password"));

System.out.println("Email from request: " + request.getEmail());
System.out.println("Raw password: " + request.getPassword());
System.out.println("Stored hash: " + user.getPassword());

boolean matches = passwordEncoder.matches(
        request.getPassword(),
        user.getPassword());

System.out.println("Password matches: " + matches);

if (!matches) {
    throw new InvalidCredentialsException("Invalid email or password");
}

    if (!passwordEncoder.matches(
            request.getPassword(),
            user.getPassword())) {

        throw new InvalidCredentialsException("Invalid email or password");
    }

    String token = jwtTokenProvider.generateToken(user.getEmail());

    AuthResponse response =
            new AuthResponse(token, "Bearer");

    return BaseResponse.success(
            response,
            "Login successful");
}


}