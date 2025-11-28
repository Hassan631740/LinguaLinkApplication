package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.UserRequest;
import com.lingualink.dto.response.UserResponse;
import com.lingualink.entity.Role;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.UserMapper;
import com.lingualink.repository.UserRepository;
import com.lingualink.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    // Create
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        return userRepository.save(user);
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    // Read
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<User> getAllUsers(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("id");
        Page<User> page = userRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsersAsResponse(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("id");
        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public PagedResponse<User> getAllUsersWithFilters(PageParams pageParams, String name, String email, String role) {
        Pageable pageable = pageParams.toPageable("id");
        // Convert String role to Role enum for query
        Role roleEnum = role != null ? Role.fromString(role) : null;
        Page<User> page = userRepository.findByFilters(name, email, roleEnum, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsersWithFiltersAsResponse(PageParams pageParams, String name, String email, String role) {
        Pageable pageable = pageParams.toPageable("id");
        // Convert String role to Role enum for query
        Role roleEnum = role != null ? Role.fromString(role) : null;
        Page<User> page = userRepository.findByFilters(name, email, roleEnum, pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        validateUserAccess(user);
        return user;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByIdAsResponse(Long id) {
        User user = getUserById(id);
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Update - Full update
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        validateUserAccess(user);
        
        // Only administrators can change roles
        if (userDetails.getRole() != null && !SecurityUtils.isAdministrator()) {
            // Keep original role - userDetails already has Role enum
            userDetails.setRole(user.getRole());
        }
        
        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("User with email " + userDetails.getEmail() + " already exists");
        }
        
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }
        if (userDetails.getRole() != null && SecurityUtils.isAdministrator()) {
            user.setRole(userDetails.getRole());
        }
        return userRepository.save(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        validateUserAccess(user);
        
        // Only administrators can change roles
        if (request.getRole() != null && !SecurityUtils.isAdministrator()) {
            // Keep original role - convert Role enum to String
            request.setRole(user.getRole() != null ? user.getRole().getValue() : null);
        }
        
        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }
        
        userMapper.updateEntityFromRequest(request, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    // Update - Partial update
    public User patchUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        validateUserAccess(user);
        
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("User with email " + userDetails.getEmail() + " already exists");
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }
        // Only administrators can change roles
        if (userDetails.getRole() != null && SecurityUtils.isAdministrator()) {
            user.setRole(userDetails.getRole());
        }
        return userRepository.save(user);
    }

    public UserResponse patchUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        validateUserAccess(user);
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
            }
        }
        
        // Only administrators can change roles
        if (request.getRole() != null && !SecurityUtils.isAdministrator()) {
            request.setRole(null); // Ignore role change
        }
        
        userMapper.updateEntityFromRequest(request, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    // Delete
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateUserAccess(User user) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Administrators can access any user
        if (SecurityUtils.isAdministrator()) {
            return;
        }
        
        // Users can only access their own profile
        if (!user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to access this user");
        }
    }
}

