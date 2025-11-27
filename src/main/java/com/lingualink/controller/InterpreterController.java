package com.lingualink.controller;

import com.lingualink.entity.Interpreter;
import com.lingualink.service.InterpreterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interpreters")
public class InterpreterController {
    private final InterpreterService interpreterService;

    public InterpreterController(InterpreterService interpreterService) {
        this.interpreterService = interpreterService;
    }

    // Create
    @PostMapping
    public ResponseEntity<Interpreter> createInterpreter(@RequestBody Interpreter interpreter) {
        Interpreter createdInterpreter = interpreterService.createInterpreter(interpreter);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInterpreter);
    }

    // Read - Get all
    @GetMapping
    public ResponseEntity<List<Interpreter>> getAllInterpreters() {
        List<Interpreter> interpreters = interpreterService.getAllInterpreters();
        return ResponseEntity.ok(interpreters);
    }

    // Read - Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Interpreter> getInterpreterById(@PathVariable Long id) {
        return interpreterService.getInterpreterById(id)
                .map(interpreter -> ResponseEntity.ok(interpreter))
                .orElse(ResponseEntity.notFound().build());
    }

    // Read - Get by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<Interpreter> getInterpreterByUserId(@PathVariable Long userId) {
        return interpreterService.getInterpreterByUserId(userId)
                .map(interpreter -> ResponseEntity.ok(interpreter))
                .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Interpreter> updateInterpreter(@PathVariable Long id, @RequestBody Interpreter interpreterDetails) {
        try {
            Interpreter updatedInterpreter = interpreterService.updateInterpreter(id, interpreterDetails);
            return ResponseEntity.ok(updatedInterpreter);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterpreter(@PathVariable Long id) {
        try {
            interpreterService.deleteInterpreter(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

