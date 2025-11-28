package com.lingualink.mapper;

import com.lingualink.dto.request.UserRequest;
import com.lingualink.dto.response.UserResponse;
import com.lingualink.entity.Role;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        // Convert String role from DTO to Role enum
        user.setRole(request.getRole() != null ? Role.fromString(request.getRole()) : null);
        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        // Convert Role enum to String for DTO
        response.setRole(user.getRole() != null ? user.getRole().getValue() : null);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(UserRequest request, User user) {
        if (request == null || user == null) {
            return;
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }
        if (request.getRole() != null) {
            // Convert String role from DTO to Role enum
            user.setRole(Role.fromString(request.getRole()));
        }
    }
}

