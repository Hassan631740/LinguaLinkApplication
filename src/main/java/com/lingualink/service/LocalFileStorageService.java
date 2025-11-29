package com.lingualink.service;

import com.lingualink.config.StorageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
public class LocalFileStorageService implements FileStorageService {
    private final StorageConfig.Local localConfig;
    private final String basePath;

    public LocalFileStorageService(StorageConfig storageConfig) {
        this.localConfig = storageConfig.getLocal();
        this.basePath = localConfig.getBasePath();
        initializeStorageDirectory();
    }

    private void initializeStorageDirectory() {
        try {
            Path uploadPath = Paths.get(basePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", basePath);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", basePath, e);
            throw new RuntimeException("Failed to initialize storage directory", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder, String fileName) throws Exception {
        try {
            // Create folder structure: basePath/folder/
            Path folderPath = Paths.get(basePath, folder);
            Files.createDirectories(folderPath);

            // Generate unique file name: fileName-originalExtension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = fileName + "-" + UUID.randomUUID().toString() + extension;

            // Save file
            Path targetPath = folderPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL path: /uploads/folder/uniqueFileName
            String fileUrl = "/" + basePath + "/" + folder + "/" + uniqueFileName;
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", fileName, e);
            throw new Exception("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        try {
            // Remove leading slash if present and convert to file path
            String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path path = Paths.get(filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully: {}", fileUrl);
            } else {
                log.warn("File not found for deletion: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new Exception("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path path = Paths.get(filePath);
            return Files.exists(path);
        } catch (Exception e) {
            log.error("Error checking file existence: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public InputStream getFileInputStream(String fileUrl) throws Exception {
        try {
            String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new Exception("File not found: " + fileUrl);
            }
            return new FileInputStream(path.toFile());
        } catch (IOException e) {
            log.error("Failed to get file input stream: {}", fileUrl, e);
            throw new Exception("Failed to read file: " + e.getMessage(), e);
        }
    }
}

