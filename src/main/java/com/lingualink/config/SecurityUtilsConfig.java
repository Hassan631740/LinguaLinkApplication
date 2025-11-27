package com.lingualink.config;

import com.lingualink.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityUtilsConfig {
    
    @Bean(name = "securityUtils")
    public SecurityUtils securityUtils() {
        return new SecurityUtils();
    }
}

