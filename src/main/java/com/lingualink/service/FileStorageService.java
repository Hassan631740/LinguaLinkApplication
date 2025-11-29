package com.lingualink.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Service interface for file storage operations.
 * Supports both local file system and cloud storage (S3).
 */
public interface FileStorageService {
    /**
     * Uploads a file and returns the URL/path to access it.
     *
     * @param file the file to upload
     * @param folder the folder/category (e.g., "avatars", "icons", "certificates")
     * @param fileName the desired file name (without extension)
     * @return the URL or path to access the uploaded file
     * @throws Exception if upload fails
     */
    String uploadFile(MultipartFile file, String folder, String fileName) throws Exception;

    /**
     * Deletes a file from storage.
     *
     * @param fileUrl the URL or path of the file to delete
     * @throws Exception if deletion fails
     */
    void deleteFile(String fileUrl) throws Exception;

    /**
     * Checks if a file exists.
     *
     * @param fileUrl the URL or path of the file
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String fileUrl);

    /**
     * Gets the input stream for reading a file.
     *
     * @param fileUrl the URL or path of the file
     * @return InputStream for reading the file
     * @throws Exception if file cannot be read
     */
    InputStream getFileInputStream(String fileUrl) throws Exception;
}

