package com.lingualink.mapper;

import com.lingualink.dto.request.InterpreterRequest;
import com.lingualink.dto.response.InterpreterResponse;
import com.lingualink.entity.Interpreter;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class InterpreterMapper {

    public Interpreter toEntity(InterpreterRequest request, User user) {
        if (request == null) {
            return null;
        }
        Interpreter interpreter = new Interpreter();
        interpreter.setUser(user);
        interpreter.setLanguages(request.getLanguages());
        interpreter.setRatePerHour(request.getRatePerHour());
        interpreter.setExperienceYears(request.getExperienceYears());
        interpreter.setBio(request.getBio());
        return interpreter;
    }

    public InterpreterResponse toResponse(Interpreter interpreter) {
        if (interpreter == null) {
            return null;
        }
        InterpreterResponse response = new InterpreterResponse();
        response.setId(interpreter.getId());
        response.setUserId(interpreter.getUser() != null ? interpreter.getUser().getId() : null);
        response.setUserName(interpreter.getUser() != null ? interpreter.getUser().getName() : null);
        response.setUserEmail(interpreter.getUser() != null ? interpreter.getUser().getEmail() : null);
        response.setLanguages(interpreter.getLanguages());
        response.setRatePerHour(interpreter.getRatePerHour());
        response.setExperienceYears(interpreter.getExperienceYears());
        response.setBio(interpreter.getBio());
        response.setCreatedAt(interpreter.getCreatedAt());
        response.setUpdatedAt(interpreter.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(InterpreterRequest request, Interpreter interpreter, User user) {
        if (request == null || interpreter == null) {
            return;
        }
        if (user != null) {
            interpreter.setUser(user);
        }
        if (request.getLanguages() != null) {
            interpreter.setLanguages(request.getLanguages());
        }
        if (request.getRatePerHour() != null) {
            interpreter.setRatePerHour(request.getRatePerHour());
        }
        if (request.getExperienceYears() != null) {
            interpreter.setExperienceYears(request.getExperienceYears());
        }
        if (request.getBio() != null) {
            interpreter.setBio(request.getBio());
        }
    }
}

