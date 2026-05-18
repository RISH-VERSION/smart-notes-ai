package com.notesapp.smartnotesapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
    	
    	   // 🔥 ADD THESE
        System.out.println("❌ AuthEntryPoint triggered!");
        System.out.println("❌ Request URI: " + request.getRequestURI());
        System.out.println("❌ Auth header: " + request.getHeader("Authorization"));
        System.out.println("❌ Exception: " + authException.getMessage());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("""
            {
                "error": "Unauthorized",
                "message": "Invalid or missing JWT token",
                "status": 401
            }
        """);
    }
}
