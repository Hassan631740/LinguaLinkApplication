package com.lingualink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("message", "register endpoint - implement me"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("token", "jwt-token-placeholder"));
    }
}
