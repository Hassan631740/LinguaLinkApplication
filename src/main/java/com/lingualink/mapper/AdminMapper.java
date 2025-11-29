package com.lingualink.mapper;

import com.lingualink.dto.request.AdminRequest;
import com.lingualink.dto.response.AdminResponse;
import com.lingualink.entity.Admin;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public Admin toEntity(AdminRequest request, User user) {
        if (request == null) {
            return null;
        }
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setDepartment(request.getDepartment());
        admin.setEmployeeId(request.getEmployeeId());
        admin.setAccessLevel(request.getAccessLevel());
        admin.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        admin.setPermissions(request.getPermissions());
        admin.setNotes(request.getNotes());
        return admin;
    }

    public AdminResponse toResponse(Admin admin) {
        if (admin == null) {
            return null;
        }
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setUserId(admin.getUser() != null ? admin.getUser().getId() : null);
        response.setUserName(admin.getUser() != null ? admin.getUser().getName() : null);
        response.setUserEmail(admin.getUser() != null ? admin.getUser().getEmail() : null);
        response.setDepartment(admin.getDepartment());
        response.setEmployeeId(admin.getEmployeeId());
        response.setAccessLevel(admin.getAccessLevel());
        response.setLastLogin(admin.getLastLogin());
        response.setIsActive(admin.getIsActive());
        response.setPermissions(admin.getPermissions());
        response.setNotes(admin.getNotes());
        response.setCreatedAt(admin.getCreatedAt());
        response.setUpdatedAt(admin.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(AdminRequest request, Admin admin, User user) {
        if (request == null || admin == null) {
            return;
        }
        if (user != null) {
            admin.setUser(user);
        }
        if (request.getDepartment() != null) {
            admin.setDepartment(request.getDepartment());
        }
        if (request.getEmployeeId() != null) {
            admin.setEmployeeId(request.getEmployeeId());
        }
        if (request.getAccessLevel() != null) {
            admin.setAccessLevel(request.getAccessLevel());
        }
        if (request.getIsActive() != null) {
            admin.setIsActive(request.getIsActive());
        }
        if (request.getPermissions() != null) {
            admin.setPermissions(request.getPermissions());
        }
        if (request.getNotes() != null) {
            admin.setNotes(request.getNotes());
        }
    }
}

