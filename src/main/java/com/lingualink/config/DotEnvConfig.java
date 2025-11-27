package com.lingualink.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotEnvConfig implements ApplicationListener<ContextRefreshedEvent> {

    private static boolean loaded = false;

    static {
        loadEnvFile();
    }

    public static void loadEnvFile() {
        if (!loaded) {
            try {
                Dotenv dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                
                // Set system properties from .env file
                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // Only set if not already set as system property
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                });
                loaded = true;
            } catch (Exception e) {
                System.err.println("Warning: Could not load .env file: " + e.getMessage());
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ensure .env is loaded
        loadEnvFile();
    }
}

