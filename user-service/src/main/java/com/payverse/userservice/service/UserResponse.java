package com.payverse.userservice.dto;

import com.payverse.userservice.model.UserRole;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long id,
                        String email,
                        String phone,
                        UserRole role,
                        LocalDateTime createdAt) {

        this.id = id;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    // getters and setters
}