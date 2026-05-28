package com.notesapp.smartnotesapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notesapp.smartnotesapp.dto.UserRequest;
import com.notesapp.smartnotesapp.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Constructor injection
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserRequest user,
            BindingResult result) {

        // Validation error — return all field errors as JSON
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(err -> err.getDefaultMessage())
                    .toList();

            return ResponseEntity.badRequest().body(
                Map.of(
                    "error", "Validation Failed",
                    "messages", errors,
                    "status", 400
                )
            );
        }

        // Register user — catch duplicate username
        try {
            userService.registerUser(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                    "error", "Conflict",
                    "message", e.getMessage(),
                    "status", 409
                )
            );
        }

        // Success JSON response
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                    "message", "User registered successfully",
                    "status", 201
                ));
    }
}
