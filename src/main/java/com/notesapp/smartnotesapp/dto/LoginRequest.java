package com.notesapp.smartnotesapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // ✅ Lombok generates getters, setters, toString, equals
public class LoginRequest {

    // ✅ Username validation
    @NotBlank(message = "Username is required")
    private String username;

    // ✅ Password — matches UserRequest validation
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    // ✅ No manual getters/setters — Lombok handles it!
}
