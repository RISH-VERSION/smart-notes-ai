package com.notesapp.smartnotesapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notesapp.smartnotesapp.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // ✅ Find user by username — used in CustomUserDetailsService
    Optional<UserEntity> findByUsername(String username);

    // ✅ Find user by email — useful for future forgot password feature
    Optional<UserEntity> findByEmail(String email);

    // ✅ Check duplicate username before register
    boolean existsByUsername(String username);

    // ✅ Check duplicate email before register
    boolean existsByEmail(String email);
}
