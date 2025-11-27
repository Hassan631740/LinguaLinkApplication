package com.lingualink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("message", "create event - implement me"));
    }

    @GetMapping
    public ResponseEntity<?> listEvents() {
        return ResponseEntity.ok(Map.of("message", "list events - implement me"));
    }
}
