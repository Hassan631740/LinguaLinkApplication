package com.lingualink.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public class FileValidationUtil {
    // Maximum file sizes (in bytes)
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    public static final long MAX_ICON_SIZE = 2 * 1024 * 1024; // 2MB
    public static final long MAX_CERTIFICATE_SIZE = 10 * 1024 * 1024; // 10MB

    // Allowed file types
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png"
    );

    /**
     * Validates an avatar file (image only).
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateAvatarFile(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_AVATAR_SIZE, "avatar");
    }

    /**
     * Validates an icon file (image only).
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateIconFile(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_ICON_SIZE, "icon");
    }

    /**
     * Validates a certificate file (PDF or image).
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateCertificateFile(MultipartFile file) {
        validateFile(file, ALLOWED_DOCUMENT_TYPES, MAX_CERTIFICATE_SIZE, "certificate");
    }

    /**
     * Generic file validation method.
     *
     * @param file the file to validate
     * @param allowedTypes set of allowed MIME types
     * @param maxSize maximum file size in bytes
     * @param fileTypeName name of the file type for error messages
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateFile(MultipartFile file, Set<String> allowedTypes, long maxSize, String fileTypeName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileTypeName + " file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type for " + fileTypeName + ". Allowed types: " + String.join(", ", allowedTypes)
            );
        }

        if (file.getSize() > maxSize) {
            double maxSizeMB = maxSize / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                    fileTypeName + " file size exceeds maximum allowed size of " + maxSizeMB + "MB"
            );
        }
    }
}

