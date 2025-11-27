package com.lingualink.service;

import com.lingualink.entity.Interpreter;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.InterpreterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
    @Transactional(readOnly = true)
    public List<Interpreter> getAllInterpreters() {
        return interpreterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Interpreter getInterpreterById(Long id) {
        return interpreterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", id));
    }

    @Transactional(readOnly = true)
    public Optional<Interpreter> getInterpreterByUserId(Long userId) {
        return interpreterRepository.findByUser_Id(userId);
    }

    // Update - Full update
    public Interpreter updateInterpreter(Long id, Interpreter interpreterDetails) {
        Interpreter interpreter = getInterpreterById(id);
        interpreter.setUser(interpreterDetails.getUser());
        interpreter.setLanguages(interpreterDetails.getLanguages());
        interpreter.setRatePerHour(interpreterDetails.getRatePerHour());
        interpreter.setExperienceYears(interpreterDetails.getExperienceYears());
        interpreter.setBio(interpreterDetails.getBio());
        return interpreterRepository.save(interpreter);
    }

    // Update - Partial update
    public Interpreter patchInterpreter(Long id, Interpreter interpreterDetails) {
        Interpreter interpreter = getInterpreterById(id);
        
        if (interpreterDetails.getUser() != null) {
            interpreter.setUser(interpreterDetails.getUser());
        }
        if (interpreterDetails.getLanguages() != null) {
            interpreter.setLanguages(interpreterDetails.getLanguages());
        }
        if (interpreterDetails.getRatePerHour() != null) {
            interpreter.setRatePerHour(interpreterDetails.getRatePerHour());
        }
        if (interpreterDetails.getExperienceYears() != null) {
            interpreter.setExperienceYears(interpreterDetails.getExperienceYears());
        }
        if (interpreterDetails.getBio() != null) {
            interpreter.setBio(interpreterDetails.getBio());
        }
        return interpreterRepository.save(interpreter);
    }

    // Delete
    public void deleteInterpreter(Long id) {
        Interpreter interpreter = getInterpreterById(id);
        interpreterRepository.delete(interpreter);
    }
}


