package com.lingualink.controller;

import com.lingualink.dto.JwtAuthenticationResponse;
import com.lingualink.dto.LoginRequest;
import com.lingualink.dto.RegisterRequest;
import com.lingualink.entity.Role;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceConflictException;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.security.JwtTokenService;
import com.lingualink.security.UserPrincipal;
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

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API endpoints for user registration and login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                         PasswordEncoder passwordEncoder, JwtTokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    @PostMapping("/register")
    public ResponseEntity<JwtAuthenticationResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if user already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceConflictException("User", "email", registerRequest.getEmail());
        }

        // Create new user
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // Default to CLIENT role if not specified
        Role role = registerRequest.getRole() != null ? 
                Role.fromString(registerRequest.getRole()) : Role.CLIENT;
        user.setRole(role);

        User savedUser = userService.createUser(user);

        // Generate JWT token with authorities
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        List<String> authorities = userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.toList());
        String jwt = tokenService.generateTokenFromUsername(savedUser.getEmail(), authorities);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/users/" + savedUser.getId())
                .body(new JwtAuthenticationResponse(jwt, savedUser.getEmail(), 
                        savedUser.getRole() != null ? savedUser.getRole().getValue() : null));
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
        
        // Generate JWT token from authenticated user
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenService.generateToken(userPrincipal);

        // Get user details
        User user = userService.getUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getEmail()));

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, user.getEmail(), 
                user.getRole() != null ? user.getRole().getValue() : null));
    }
}
