package com.payverse.userservice.service.impl;

import com.payverse.userservice.dto.CreateUserRequest;
import com.payverse.userservice.exception.UserAlreadyExistsException;
import com.payverse.userservice.model.User;
import com.payverse.userservice.model.UserRole;
import com.payverse.userservice.repository.UserRepository;
import com.payverse.userservice.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.payverse.userservice.exception.UserNotFoundException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone already registered");
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id)
            .orElseThrow(() ->
                    new UserNotFoundException(
                            "User not found with id : " + id));
    }


    @Override
public void deleteUser(Long id) {

    User user = userRepository.findById(id)
            .orElseThrow(() ->
                    new UserNotFoundException(
                            "User not found with id : " + id));

    userRepository.delete(user);
}

@Override
public long getUserCount() {
    return userRepository.count();
}

@Override
public boolean userExists(Long id) {
    return userRepository.existsById(id);
}

@Override
public List<User> getUsersByRole(UserRole role) {
    return userRepository.findByRole(role);
}

@Override
public List<User> searchUsersByEmail(String keyword) {
    return userRepository.findByEmailContainingIgnoreCase(keyword);
}

@Override
public List<User> getLatestUsers() {
    return userRepository.findTop5ByOrderByCreatedAtDesc();
}
}