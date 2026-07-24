package com.payverse.userservice.service;

import com.payverse.userservice.dto.CreateUserRequest;
import com.payverse.userservice.model.User;
import com.payverse.userservice.model.UserRole;

import java.util.List;

public interface UserService {

    User register(CreateUserRequest request);

    List<User> getAllUsers();

    User getUserById(Long id);

    void deleteUser(Long id);

    long getUserCount();

    boolean userExists(Long id);

    List<User> getUsersByRole(UserRole role);

List<User> searchUsersByEmail(String keyword);

List<User> getLatestUsers();

User getUserByEmail(String email);
}