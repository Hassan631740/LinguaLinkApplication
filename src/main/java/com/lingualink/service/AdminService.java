package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.AdminRequest;
import com.lingualink.dto.response.AdminResponse;
import com.lingualink.entity.Admin;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceConflictException;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.AdminMapper;
import com.lingualink.repository.AdminRepository;
import com.lingualink.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final UserRepository userRepository;

    public AdminService(AdminRepository adminRepository, AdminMapper adminMapper, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
        this.userRepository = userRepository;
    }

    // Create
    public Admin createAdmin(Admin admin) {
        if (admin.getUser() != null && adminRepository.existsByUser_Id(admin.getUser().getId())) {
            throw new ResourceConflictException("Admin", "userId", admin.getUser().getId().toString());
        }
        if (admin.getEmployeeId() != null && adminRepository.existsByEmployeeId(admin.getEmployeeId())) {
            throw new ResourceConflictException("Admin", "employeeId", admin.getEmployeeId());
        }
        return adminRepository.save(admin);
    }

    public AdminResponse createAdmin(AdminRequest request) {
        if (adminRepository.existsByUser_Id(request.getUserId())) {
            throw new ResourceConflictException("Admin", "userId", request.getUserId().toString());
        }
        if (request.getEmployeeId() != null && adminRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new ResourceConflictException("Admin", "employeeId", request.getEmployeeId());
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        Admin admin = adminMapper.toEntity(request, user);
        Admin savedAdmin = adminRepository.save(admin);
        return adminMapper.toResponse(savedAdmin);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Admin> getAllAdmins(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("id");
        Page<Admin> page = adminRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Admin> getAllAdminsWithFilters(PageParams pageParams, String department,
                                                        String accessLevel, Boolean isActive) {
        Pageable pageable = pageParams.toPageable("id");
        Page<Admin> page = adminRepository.findByFilters(department, accessLevel, isActive, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminResponse> getAllAdminsWithFiltersAsResponse(PageParams pageParams, String department,
                                                                          String accessLevel, Boolean isActive) {
        Pageable pageable = pageParams.toPageable("id");
        Page<Admin> page = adminRepository.findByFilters(department, accessLevel, isActive, pageable);
        List<AdminResponse> content = page.getContent().stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdminsAsResponse() {
        List<Admin> admins = getAllAdmins();
        return admins.stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", id));
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminByIdAsResponse(Long id) {
        Admin admin = getAdminById(id);
        return adminMapper.toResponse(admin);
    }

    @Transactional(readOnly = true)
    public Admin getAdminByUserId(Long userId) {
        return adminRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "userId", userId));
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminByUserIdAsResponse(Long userId) {
        Admin admin = getAdminByUserId(userId);
        return adminMapper.toResponse(admin);
    }

    @Transactional(readOnly = true)
    public List<Admin> getAdminsByDepartment(String department) {
        return adminRepository.findByDepartment(department);
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAdminsByDepartmentAsResponse(String department) {
        List<Admin> admins = getAdminsByDepartment(department);
        return admins.stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Admin> getAdminsByAccessLevel(String accessLevel) {
        return adminRepository.findByAccessLevel(accessLevel);
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAdminsByAccessLevelAsResponse(String accessLevel) {
        List<Admin> admins = getAdminsByAccessLevel(accessLevel);
        return admins.stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Update - Full update
    public Admin updateAdmin(Long id, Admin adminDetails) {
        Admin admin = getAdminById(id);
        admin.setUser(adminDetails.getUser());
        admin.setDepartment(adminDetails.getDepartment());
        admin.setEmployeeId(adminDetails.getEmployeeId());
        admin.setAccessLevel(adminDetails.getAccessLevel());
        admin.setIsActive(adminDetails.getIsActive());
        admin.setPermissions(adminDetails.getPermissions());
        admin.setNotes(adminDetails.getNotes());
        return adminRepository.save(admin);
    }

    public AdminResponse updateAdmin(Long id, AdminRequest request) {
        Admin admin = getAdminById(id);
        
        // Check for conflicts if user ID or employee ID is being changed
        if (request.getUserId() != null && !admin.getUser().getId().equals(request.getUserId())) {
            if (adminRepository.existsByUser_Id(request.getUserId())) {
                throw new ResourceConflictException("Admin", "userId", request.getUserId().toString());
            }
        }
        if (request.getEmployeeId() != null && !request.getEmployeeId().equals(admin.getEmployeeId())) {
            if (adminRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new ResourceConflictException("Admin", "employeeId", request.getEmployeeId());
            }
        }
        
        User user = request.getUserId() != null
                ? userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()))
                : admin.getUser();
        adminMapper.updateEntityFromRequest(request, admin, user);
        Admin savedAdmin = adminRepository.save(admin);
        return adminMapper.toResponse(savedAdmin);
    }

    // Update - Partial update
    public Admin patchAdmin(Long id, Admin adminDetails) {
        Admin admin = getAdminById(id);
        
        if (adminDetails.getUser() != null) {
            admin.setUser(adminDetails.getUser());
        }
        if (adminDetails.getDepartment() != null) {
            admin.setDepartment(adminDetails.getDepartment());
        }
        if (adminDetails.getEmployeeId() != null) {
            admin.setEmployeeId(adminDetails.getEmployeeId());
        }
        if (adminDetails.getAccessLevel() != null) {
            admin.setAccessLevel(adminDetails.getAccessLevel());
        }
        if (adminDetails.getIsActive() != null) {
            admin.setIsActive(adminDetails.getIsActive());
        }
        if (adminDetails.getPermissions() != null) {
            admin.setPermissions(adminDetails.getPermissions());
        }
        if (adminDetails.getNotes() != null) {
            admin.setNotes(adminDetails.getNotes());
        }
        return adminRepository.save(admin);
    }

    public AdminResponse patchAdmin(Long id, AdminRequest request) {
        Admin admin = getAdminById(id);
        
        // Check for conflicts if user ID or employee ID is being changed
        if (request.getUserId() != null && !admin.getUser().getId().equals(request.getUserId())) {
            if (adminRepository.existsByUser_Id(request.getUserId())) {
                throw new ResourceConflictException("Admin", "userId", request.getUserId().toString());
            }
        }
        if (request.getEmployeeId() != null && !request.getEmployeeId().equals(admin.getEmployeeId())) {
            if (adminRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new ResourceConflictException("Admin", "employeeId", request.getEmployeeId());
            }
        }
        
        User user = request.getUserId() != null
                ? userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()))
                : null;
        adminMapper.updateEntityFromRequest(request, admin, user);
        Admin savedAdmin = adminRepository.save(admin);
        return adminMapper.toResponse(savedAdmin);
    }

    // Delete
    public void deleteAdmin(Long id) {
        Admin admin = getAdminById(id);
        adminRepository.delete(admin);
    }

    // Update last login
    public void updateLastLogin(Long id) {
        Admin admin = getAdminById(id);
        admin.setLastLogin(java.time.LocalDateTime.now());
        adminRepository.save(admin);
    }

    public void updateLastLoginByUserId(Long userId) {
        Admin admin = getAdminByUserId(userId);
        admin.setLastLogin(java.time.LocalDateTime.now());
        adminRepository.save(admin);
    }
}

