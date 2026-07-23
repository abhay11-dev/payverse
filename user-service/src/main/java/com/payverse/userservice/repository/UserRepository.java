package com.payverse.userservice.repository;

import com.payverse.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.payverse.userservice.model.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(UserRole role);

List<User> findByRoleOrderByCreatedAtDesc(UserRole role);

List<User> findByEmailContainingIgnoreCase(String keyword);

List<User> findTop5ByOrderByCreatedAtDesc();

}