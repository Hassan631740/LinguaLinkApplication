package com.lingualink.service;

import com.lingualink.entity.Interpreter;
import com.lingualink.repository.InterpreterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InterpreterService {
    private final InterpreterRepository interpreterRepository;

    public InterpreterService(InterpreterRepository interpreterRepository) {
        this.interpreterRepository = interpreterRepository;
    }

    // Create
    public Interpreter createInterpreter(Interpreter interpreter) {
        return interpreterRepository.save(interpreter);
    }

    // Read
    public List<Interpreter> getAllInterpreters() {
        return interpreterRepository.findAll();
    }

    public Optional<Interpreter> getInterpreterById(Long id) {
        return interpreterRepository.findById(id);
    }

    public Optional<Interpreter> getInterpreterByUserId(Long userId) {
        return interpreterRepository.findByUser_Id(userId);
    }

    // Update
    public Interpreter updateInterpreter(Long id, Interpreter interpreterDetails) {
        Interpreter interpreter = interpreterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interpreter not found with id: " + id));
        interpreter.setUser(interpreterDetails.getUser());
        interpreter.setLanguages(interpreterDetails.getLanguages());
        interpreter.setRatePerHour(interpreterDetails.getRatePerHour());
        interpreter.setExperienceYears(interpreterDetails.getExperienceYears());
        interpreter.setBio(interpreterDetails.getBio());
        return interpreterRepository.save(interpreter);
    }

    // Delete
    public void deleteInterpreter(Long id) {
        interpreterRepository.deleteById(id);
    }
}

