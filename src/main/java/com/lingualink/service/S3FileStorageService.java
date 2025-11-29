package com.lingualink.service;

import com.lingualink.config.StorageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
public class S3FileStorageService implements FileStorageService {
    private final StorageConfig.S3 s3Config;
    private final S3Client s3Client;
    private final String bucketName;

    public S3FileStorageService(StorageConfig storageConfig) {
        this.s3Config = storageConfig.getS3();
        this.bucketName = s3Config.getBucketName();
        
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("S3 bucket name must be configured");
        }

        // Initialize S3 client
        Region region = Region.of(s3Config.getRegion());
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                s3Config.getAccessKeyId(),
                s3Config.getSecretAccessKey()
        );

        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("S3FileStorageService initialized for bucket: {}", bucketName);
    }

    @Override
    public String uploadFile(MultipartFile file, String folder, String fileName) throws Exception {
        try {
            // Generate unique file name: folder/fileName-uuid.extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = fileName + "-" + UUID.randomUUID().toString() + extension;
            String s3Key = folder + "/" + uniqueFileName;

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return full URL
            String fileUrl = s3Config.getBaseUrl() != null && !s3Config.getBaseUrl().isEmpty()
                    ? s3Config.getBaseUrl() + "/" + s3Key
                    : "https://" + bucketName + ".s3." + s3Config.getRegion() + ".amazonaws.com/" + s3Key;

            log.info("File uploaded to S3 successfully: {}", fileUrl);
            return fileUrl;
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new Exception("Failed to upload file to S3: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file to S3: {}", fileName, e);
            throw new Exception("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        try {
            // Extract S3 key from URL
            String s3Key = extractS3KeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3 successfully: {}", fileUrl);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", fileUrl, e);
            throw new Exception("Failed to delete file from S3: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error deleting file from S3: {}", fileUrl, e);
            throw new Exception("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(fileUrl);
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence in S3: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public InputStream getFileInputStream(String fileUrl) throws Exception {
        try {
            String s3Key = extractS3KeyFromUrl(fileUrl);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (S3Exception e) {
            log.error("Failed to get file from S3: {}", fileUrl, e);
            throw new Exception("Failed to read file from S3: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error getting file from S3: {}", fileUrl, e);
            throw new Exception("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts S3 key from a URL.
     * Handles both custom base URLs and standard S3 URLs.
     */
    private String extractS3KeyFromUrl(String fileUrl) {
        // If custom base URL is configured, extract key after base URL
        if (s3Config.getBaseUrl() != null && !s3Config.getBaseUrl().isEmpty() && fileUrl.startsWith(s3Config.getBaseUrl())) {
            return fileUrl.substring(s3Config.getBaseUrl().length() + 1); // +1 for the trailing slash
        }

        // Standard S3 URL format: https://bucket.s3.region.amazonaws.com/key
        String standardS3Url = "https://" + bucketName + ".s3." + s3Config.getRegion() + ".amazonaws.com/";
        if (fileUrl.startsWith(standardS3Url)) {
            return fileUrl.substring(standardS3Url.length());
        }

        // If URL contains the bucket name, try to extract key after it
        int bucketIndex = fileUrl.indexOf(bucketName);
        if (bucketIndex != -1) {
            int keyStart = bucketIndex + bucketName.length();
            if (keyStart < fileUrl.length() && fileUrl.charAt(keyStart) == '/') {
                return fileUrl.substring(keyStart + 1);
            }
        }

        // Fallback: assume the URL is already a key (for backward compatibility)
        return fileUrl;
    }
}

