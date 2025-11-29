package com.lingualink.config;

import com.lingualink.service.FileStorageService;
import com.lingualink.service.LocalFileStorageService;
import com.lingualink.service.S3FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class FileStorageConfig {
    
    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService(StorageConfig storageConfig) {
        log.info("Using LocalFileStorageService for file storage");
        return new LocalFileStorageService(storageConfig);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public FileStorageService s3FileStorageService(StorageConfig storageConfig) {
        log.info("Using S3FileStorageService for file storage");
        return new S3FileStorageService(storageConfig);
    }
}

