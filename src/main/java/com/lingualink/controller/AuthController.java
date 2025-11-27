package com.lingualink.controller;

import com.lingualink.dto.JwtAuthenticationResponse;
import com.lingualink.dto.LoginRequest;
import com.lingualink.dto.RegisterRequest;
import com.lingualink.entity.Role;
import com.lingualink.entity.User;
import com.lingualink.security.JwtTokenProvider;
import com.lingualink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API endpoints for user registration and login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                         PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if user already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // Default to CLIENT role if not specified
        String role = registerRequest.getRole() != null ? 
                Role.fromString(registerRequest.getRole()).getValue() : Role.CLIENT.getValue();
        user.setRole(role);

        User savedUser = userService.createUser(user);

        // Generate JWT token
        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtAuthenticationResponse(jwt, savedUser.getEmail(), savedUser.getRole()));
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Get user details
        User user = userService.getUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, user.getEmail(), user.getRole()));
    }
}
