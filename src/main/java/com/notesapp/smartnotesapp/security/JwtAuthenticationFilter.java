package com.notesapp.smartnotesapp.security;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // 1. ✅ BYPASS: Skip JWT logic for public and OAuth2 endpoints
        // This prevents "Invalid credentials" errors during Google login redirect
        if (path.startsWith("/api/users/register") || 
            path.startsWith("/api/users/login") || 
            path.startsWith("/login/oauth2") || 
            path.startsWith("/oauth2") ||
            path.startsWith("/actuator")) {
            
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // 2. ✅ GUARD: If no Bearer token is present, just continue the chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = null;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (JwtException e) {
            // Return clean JSON for expired or tampered tokens
            sendErrorResponse(response, "Invalid or expired token");
            return;
        }

        // 3. ✅ AUTHENTICATE: Standard JWT validation
     // 3. ✅ AUTHENTICATE: Standard JWT validation
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("🔍 Extracted username: " + username);
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("🔍 UserDetails username: " + userDetails.getUsername());
            
            boolean valid = jwtUtil.isTokenValid(token, userDetails);
            System.out.println("🔍 isTokenValid: " + valid);
            System.out.println("🔍 Token expired? " + new java.util.Date().after(
                io.jsonwebtoken.Jwts.parser()
                    .verifyWith(jwtUtil.getKey())  // we'll add getKey() below
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
            ));

            if (valid) {
                System.out.println("✅ Authenticated: " + username);
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("❌ Token invalid for user: " + username);
            }
        } else {
            System.out.println("❌ username null OR already authenticated");
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(String.format("""
            {
                "error": "Unauthorized",
                "message": "%s",
                "status": 401
            }
            """, message));
    }
}