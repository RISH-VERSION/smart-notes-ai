package com.notesapp.smartnotesapp.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.notesapp.smartnotesapp.entity.UserEntity;
import com.notesapp.smartnotesapp.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        // ✅ Step 1: Try to find by Username first
        // ✅ Step 2: Fallback to finding by Email (Crucial for OAuth2/JWT users)
        UserEntity user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + usernameOrEmail));

        // ✅ Return the Spring Security UserDetails object
        return User.withUsername(user.getEmail()) 
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // Or .roles("USER") if you prefer
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
