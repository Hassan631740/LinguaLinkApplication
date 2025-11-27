package com.lingualink;

import com.lingualink.config.DotEnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LinguaLinkApplication {
    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        DotEnvConfig.loadEnvFile();
        SpringApplication.run(LinguaLinkApplication.class, args);
    }
}
