package com.notesapp.smartnotesapp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notesapp.smartnotesapp.dto.UserRequest;
import com.notesapp.smartnotesapp.entity.UserEntity;
import com.notesapp.smartnotesapp.repository.UserRepository;

@Service
public class UserService {

    //  Constructor injection
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //  Transactional — safe database operation
    @Transactional
    public void registerUser(UserRequest request) {

        //  Check duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        //  Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        //  Always encode password — never store plain text
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }
    
    @Transactional
    public void processOAuthPostLogin(String email, String name) {
        if (userRepository.existsByEmail(email)) {
            return; // Returning Google user — nothing to do
        }

        // Derive a clean username from the name Google provides
        // e.g. "John Doe" → "johndoe", fallback to email prefix
        String baseUsername = (name != null && !name.isBlank())
            ? name.toLowerCase().replaceAll("\\s+", "").replaceAll("[^a-z0-9]", "")
            : email.split("@")[0];

        // Truncate to 50 chars to match your @Column(length = 50)
        String username = baseUsername.length() > 50
            ? baseUsername.substring(0, 50)
            : baseUsername;

        // Ensure username is unique — append suffix if taken
        String finalUsername = username;
        int suffix = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + suffix++;
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setUsername(finalUsername);
        newUser.setPassword(passwordEncoder.encode("OAUTH2_NO_PASSWORD_" + email));
        userRepository.save(newUser);
    }
}