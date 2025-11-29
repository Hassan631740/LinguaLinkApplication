package com.lingualink.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageConfig {
    /**
     * Storage type: "local" for local/dev, "s3" for production
     */
    private String type = "local";

    /**
     * Local storage configuration
     */
    private Local local = new Local();

    /**
     * S3 storage configuration
     */
    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Local {
        /**
         * Base directory for local file storage
         */
        private String basePath = "uploads";

        /**
         * Base URL for accessing uploaded files (e.g., http://localhost:8080/uploads)
         */
        private String baseUrl = "http://localhost:8080/uploads";
    }

    @Getter
    @Setter
    public static class S3 {
        /**
         * AWS S3 bucket name
         */
        private String bucketName;

        /**
         * AWS region (e.g., us-east-1)
         */
        private String region = "us-east-1";

        /**
         * AWS access key ID
         */
        private String accessKeyId;

        /**
         * AWS secret access key
         */
        private String secretAccessKey;

        /**
         * Base URL for accessing files from S3 (e.g., https://bucket.s3.region.amazonaws.com)
         */
        private String baseUrl;
    }
}

