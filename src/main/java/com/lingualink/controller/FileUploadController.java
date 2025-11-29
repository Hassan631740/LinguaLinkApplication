package com.lingualink.controller;

import com.lingualink.dto.response.FileInfoResponse;
import com.lingualink.dto.response.FileUploadResponse;
import com.lingualink.entity.Event;
import com.lingualink.entity.Interpreter;
import com.lingualink.entity.User;
import com.lingualink.exception.FileUploadException;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.EventRepository;
import com.lingualink.repository.InterpreterRepository;
import com.lingualink.repository.UserRepository;
import com.lingualink.security.SecurityUtils;
import com.lingualink.service.FileStorageService;
import com.lingualink.util.FileValidationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@Tag(name = "File Upload", description = "File upload API endpoints for avatars, icons, and certificates")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final InterpreterRepository interpreterRepository;
    private final ObjectMapper objectMapper;

    public FileUploadController(FileStorageService fileStorageService,
                               UserRepository userRepository,
                               EventRepository eventRepository,
                               InterpreterRepository interpreterRepository,
                               ObjectMapper objectMapper) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.interpreterRepository = interpreterRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/avatars/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Upload user avatar", description = "Uploads an avatar image for a user. Users can only upload their own avatar unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<FileUploadResponse> uploadAvatar(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Avatar image file") @RequestParam("file") MultipartFile file) {
        try {
            // Validate access
            validateUserAccess(userId);

            // Validate file
            FileValidationUtil.validateAvatarFile(file);

            // Get user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            // Delete old avatar if exists
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(user.getAvatarUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete old avatar: {}", e.getMessage());
                }
            }

            // Upload new avatar
            String fileName = "avatar-" + userId;
            String fileUrl = fileStorageService.uploadFile(file, "avatars", fileName);

            // Update user entity
            user.setAvatarUrl(fileUrl);
            userRepository.save(user);

            FileUploadResponse response = FileUploadResponse.builder()
                    .fileUrl(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .message("Avatar uploaded successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new FileUploadException(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading avatar for user {}: {}", userId, e.getMessage(), e);
            throw new FileUploadException("Failed to upload avatar: " + e.getMessage());
        }
    }

    @PostMapping("/icons/events/{eventId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Upload event icon", description = "Uploads a theme icon/image for an event. Only event organizers or administrators can upload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Icon uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<FileUploadResponse> uploadEventIcon(
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @Parameter(description = "Icon image file") @RequestParam("file") MultipartFile file) {
        try {
            // Get event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

            // Validate access - only organizer or admin can upload
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new AccessDeniedException("User not authenticated");
            }
            if (!SecurityUtils.isAdministrator() && !event.getOrganizer().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Only event organizer or administrator can upload event icon");
            }

            // Validate file
            FileValidationUtil.validateIconFile(file);

            // Delete old icon if exists
            if (event.getIconUrl() != null && !event.getIconUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(event.getIconUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete old icon: {}", e.getMessage());
                }
            }

            // Upload new icon
            String fileName = "icon-" + eventId;
            String fileUrl = fileStorageService.uploadFile(file, "icons", fileName);

            // Update event entity
            event.setIconUrl(fileUrl);
            eventRepository.save(event);

            FileUploadResponse response = FileUploadResponse.builder()
                    .fileUrl(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .message("Event icon uploaded successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new FileUploadException(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading icon for event {}: {}", eventId, e.getMessage(), e);
            throw new FileUploadException("Failed to upload event icon: " + e.getMessage());
        }
    }

    @PostMapping("/certificates/interpreters/{interpreterId}")
    @PreAuthorize("hasAnyRole('INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Upload interpreter certificate", description = "Uploads a certificate file for an interpreter. Interpreters can only upload their own certificates unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found")
    })
    public ResponseEntity<FileUploadResponse> uploadCertificate(
            @Parameter(description = "Interpreter ID") @PathVariable Long interpreterId,
            @Parameter(description = "Certificate file (PDF or image)") @RequestParam("file") MultipartFile file) {
        try {
            // Get interpreter
            Interpreter interpreter = interpreterRepository.findById(interpreterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", interpreterId));

            // Validate access - only interpreter owner or admin can upload
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new AccessDeniedException("User not authenticated");
            }
            if (!SecurityUtils.isAdministrator() && !interpreter.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Only interpreter owner or administrator can upload certificates");
            }

            // Validate file
            FileValidationUtil.validateCertificateFile(file);

            // Upload certificate
            String fileName = "cert-" + interpreterId + "-" + System.currentTimeMillis();
            String fileUrl = fileStorageService.uploadFile(file, "certificates", fileName);

            // Add to interpreter's certificate URLs (JSON array)
            List<String> certificateUrls = getCertificateUrls(interpreter);
            certificateUrls.add(fileUrl);
            try {
                interpreter.setCertificateUrls(objectMapper.writeValueAsString(certificateUrls));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.error("Failed to serialize certificate URLs: {}", e.getMessage());
                throw new FileUploadException("Failed to save certificate: " + e.getMessage());
            }
            interpreterRepository.save(interpreter);

            FileUploadResponse response = FileUploadResponse.builder()
                    .fileUrl(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .message("Certificate uploaded successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new FileUploadException(e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading certificate for interpreter {}: {}", interpreterId, e.getMessage(), e);
            throw new FileUploadException("Failed to upload certificate: " + e.getMessage());
        }
    }

    @DeleteMapping("/certificates/interpreters/{interpreterId}")
    @PreAuthorize("hasAnyRole('INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Delete interpreter certificate", description = "Deletes a certificate file for an interpreter by URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Certificate deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Interpreter or certificate not found")
    })
    public ResponseEntity<Void> deleteCertificate(
            @Parameter(description = "Interpreter ID") @PathVariable Long interpreterId,
            @Parameter(description = "Certificate file URL to delete") @RequestParam("fileUrl") String fileUrl) {
        try {
            // Get interpreter
            Interpreter interpreter = interpreterRepository.findById(interpreterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", interpreterId));

            // Validate access
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new AccessDeniedException("User not authenticated");
            }
            if (!SecurityUtils.isAdministrator() && !interpreter.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("Only interpreter owner or administrator can delete certificates");
            }

            // Get certificate URLs
            List<String> certificateUrls = getCertificateUrls(interpreter);
            if (!certificateUrls.contains(fileUrl)) {
                throw new ResourceNotFoundException("Certificate", "url", fileUrl);
            }

            // Delete file from storage
            fileStorageService.deleteFile(fileUrl);

            // Remove from list
            certificateUrls.remove(fileUrl);
            try {
                interpreter.setCertificateUrls(
                        certificateUrls.isEmpty() ? null : objectMapper.writeValueAsString(certificateUrls)
                );
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.error("Failed to serialize certificate URLs: {}", e.getMessage());
                throw new FileUploadException("Failed to update certificates: " + e.getMessage());
            }
            interpreterRepository.save(interpreter);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting certificate for interpreter {}: {}", interpreterId, e.getMessage(), e);
            throw new FileUploadException("Failed to delete certificate: " + e.getMessage());
        }
    }

    // ========== READ Operations ==========

    @GetMapping("/avatars/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Get user avatar", description = "Retrieves avatar information for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or no avatar")
    })
    public ResponseEntity<FileInfoResponse> getAvatar(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            throw new ResourceNotFoundException("Avatar", "userId", userId);
        }

        FileInfoResponse response = FileInfoResponse.builder()
                .fileUrl(user.getAvatarUrl())
                .folder("avatars")
                .entityId(userId)
                .entityType("user")
                .exists(fileStorageService.fileExists(user.getAvatarUrl()))
                .uploadedAt(user.getUpdatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/icons/events/{eventId}")
    @Operation(summary = "Get event icon", description = "Retrieves icon information for an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Icon information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found or no icon")
    })
    public ResponseEntity<FileInfoResponse> getEventIcon(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getIconUrl() == null || event.getIconUrl().isEmpty()) {
            throw new ResourceNotFoundException("Icon", "eventId", eventId);
        }

        FileInfoResponse response = FileInfoResponse.builder()
                .fileUrl(event.getIconUrl())
                .folder("icons")
                .entityId(eventId)
                .entityType("event")
                .exists(fileStorageService.fileExists(event.getIconUrl()))
                .uploadedAt(event.getUpdatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/certificates/interpreters/{interpreterId}")
    @Operation(summary = "Get interpreter certificates", description = "Retrieves all certificate URLs for an interpreter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found")
    })
    public ResponseEntity<List<FileInfoResponse>> getCertificates(
            @Parameter(description = "Interpreter ID") @PathVariable Long interpreterId) {
        Interpreter interpreter = interpreterRepository.findById(interpreterId)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", interpreterId));

        List<String> certificateUrls = getCertificateUrls(interpreter);
        List<FileInfoResponse> responses = certificateUrls.stream()
                .map(url -> FileInfoResponse.builder()
                        .fileUrl(url)
                        .folder("certificates")
                        .entityId(interpreterId)
                        .entityType("interpreter")
                        .exists(fileStorageService.fileExists(url))
                        .uploadedAt(interpreter.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // ========== UPDATE Operations ==========

    @PutMapping("/avatars/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Update user avatar", description = "Replaces an existing avatar for a user. Users can only update their own avatar unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<FileUploadResponse> updateAvatar(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Avatar image file") @RequestParam("file") MultipartFile file) {
        // Same as upload - PUT replaces the file
        return uploadAvatar(userId, file);
    }

    @PutMapping("/icons/events/{eventId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Update event icon", description = "Replaces an existing icon for an event. Only event organizers or administrators can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Icon updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<FileUploadResponse> updateEventIcon(
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @Parameter(description = "Icon image file") @RequestParam("file") MultipartFile file) {
        // Same as upload - PUT replaces the file
        return uploadEventIcon(eventId, file);
    }

    // ========== DELETE Operations ==========

    @DeleteMapping("/avatars/{userId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Delete user avatar", description = "Deletes the avatar for a user. Users can only delete their own avatar unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Avatar deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found or no avatar")
    })
    public ResponseEntity<Void> deleteAvatar(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        // Validate access
        validateUserAccess(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            throw new ResourceNotFoundException("Avatar", "userId", userId);
        }

        // Delete file from storage
        try {
            fileStorageService.deleteFile(user.getAvatarUrl());
        } catch (Exception e) {
            log.warn("Failed to delete avatar file: {}", e.getMessage());
        }

        // Clear avatar URL
        user.setAvatarUrl(null);
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/icons/events/{eventId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Delete event icon", description = "Deletes the icon for an event. Only event organizers or administrators can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Icon deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found or no icon")
    })
    public ResponseEntity<Void> deleteEventIcon(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // Validate access
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        if (!SecurityUtils.isAdministrator() && !event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only event organizer or administrator can delete event icon");
        }

        if (event.getIconUrl() == null || event.getIconUrl().isEmpty()) {
            throw new ResourceNotFoundException("Icon", "eventId", eventId);
        }

        // Delete file from storage
        try {
            fileStorageService.deleteFile(event.getIconUrl());
        } catch (Exception e) {
            log.warn("Failed to delete icon file: {}", e.getMessage());
        }

        // Clear icon URL
        event.setIconUrl(null);
        eventRepository.save(event);

        return ResponseEntity.noContent().build();
    }

    // ========== ADMIN Operations ==========

    @GetMapping("/admin/avatars")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "List all avatars (Admin)", description = "Retrieves a list of all users with avatars (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatars retrieved successfully")
    })
    public ResponseEntity<List<FileInfoResponse>> getAllAvatars() {
        List<User> usersWithAvatars = userRepository.findAll().stream()
                .filter(user -> user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty())
                .collect(Collectors.toList());

        List<FileInfoResponse> responses = usersWithAvatars.stream()
                .map(user -> FileInfoResponse.builder()
                        .fileUrl(user.getAvatarUrl())
                        .folder("avatars")
                        .entityId(user.getId())
                        .entityType("user")
                        .exists(fileStorageService.fileExists(user.getAvatarUrl()))
                        .uploadedAt(user.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/icons")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "List all event icons (Admin)", description = "Retrieves a list of all events with icons (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Icons retrieved successfully")
    })
    public ResponseEntity<List<FileInfoResponse>> getAllIcons() {
        List<Event> eventsWithIcons = eventRepository.findAll().stream()
                .filter(event -> event.getIconUrl() != null && !event.getIconUrl().isEmpty())
                .collect(Collectors.toList());

        List<FileInfoResponse> responses = eventsWithIcons.stream()
                .map(event -> FileInfoResponse.builder()
                        .fileUrl(event.getIconUrl())
                        .folder("icons")
                        .entityId(event.getId())
                        .entityType("event")
                        .exists(fileStorageService.fileExists(event.getIconUrl()))
                        .uploadedAt(event.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/certificates")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "List all certificates (Admin)", description = "Retrieves a list of all interpreter certificates (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully")
    })
    public ResponseEntity<List<FileInfoResponse>> getAllCertificates() {
        List<Interpreter> interpreters = interpreterRepository.findAll();
        List<FileInfoResponse> responses = new ArrayList<>();

        for (Interpreter interpreter : interpreters) {
            List<String> certificateUrls = getCertificateUrls(interpreter);
            for (String url : certificateUrls) {
                responses.add(FileInfoResponse.builder()
                        .fileUrl(url)
                        .folder("certificates")
                        .entityId(interpreter.getId())
                        .entityType("interpreter")
                        .exists(fileStorageService.fileExists(url))
                        .uploadedAt(interpreter.getUpdatedAt())
                        .build());
            }
        }

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/admin/avatars/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete user avatar (Admin)", description = "Deletes the avatar for any user (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Avatar deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or no avatar")
    })
    public ResponseEntity<Void> deleteAvatarAsAdmin(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            throw new ResourceNotFoundException("Avatar", "userId", userId);
        }

        try {
            fileStorageService.deleteFile(user.getAvatarUrl());
        } catch (Exception e) {
            log.warn("Failed to delete avatar file: {}", e.getMessage());
        }

        user.setAvatarUrl(null);
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/icons/events/{eventId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete event icon (Admin)", description = "Deletes the icon for any event (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Icon deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found or no icon")
    })
    public ResponseEntity<Void> deleteEventIconAsAdmin(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getIconUrl() == null || event.getIconUrl().isEmpty()) {
            throw new ResourceNotFoundException("Icon", "eventId", eventId);
        }

        try {
            fileStorageService.deleteFile(event.getIconUrl());
        } catch (Exception e) {
            log.warn("Failed to delete icon file: {}", e.getMessage());
        }

        event.setIconUrl(null);
        eventRepository.save(event);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/certificates/interpreters/{interpreterId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete interpreter certificate (Admin)", description = "Deletes a certificate for any interpreter (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Certificate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Interpreter or certificate not found")
    })
    public ResponseEntity<Void> deleteCertificateAsAdmin(
            @Parameter(description = "Interpreter ID") @PathVariable Long interpreterId,
            @Parameter(description = "Certificate file URL to delete") @RequestParam("fileUrl") String fileUrl) {
        Interpreter interpreter = interpreterRepository.findById(interpreterId)
                .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", interpreterId));

        List<String> certificateUrls = getCertificateUrls(interpreter);
        if (!certificateUrls.contains(fileUrl)) {
            throw new ResourceNotFoundException("Certificate", "url", fileUrl);
        }

        try {
            fileStorageService.deleteFile(fileUrl);
        } catch (Exception e) {
            log.warn("Failed to delete certificate file: {}", e.getMessage());
        }

        certificateUrls.remove(fileUrl);
        try {
            interpreter.setCertificateUrls(
                    certificateUrls.isEmpty() ? null : objectMapper.writeValueAsString(certificateUrls)
            );
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to serialize certificate URLs: {}", e.getMessage());
            throw new FileUploadException("Failed to delete certificate: " + e.getMessage());
        }
        interpreterRepository.save(interpreter);

        return ResponseEntity.noContent().build();
    }

    // ========== Helper Methods ==========

    /**
     * Helper method to get certificate URLs as a list.
     */
    private List<String> getCertificateUrls(Interpreter interpreter) {
        try {
            if (interpreter.getCertificateUrls() == null || interpreter.getCertificateUrls().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(interpreter.getCertificateUrls(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse certificate URLs for interpreter {}: {}", interpreter.getId(), e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Validates that the current user can access/modify the specified user.
     */
    private void validateUserAccess(Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        if (!SecurityUtils.isAdministrator() && !userId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only manage your own files");
        }
    }
}

