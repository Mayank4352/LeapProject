package com.ticketing.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class EnvironmentConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
                    props.load(fis);
                }
                
                // Add properties to Spring environment with high priority
                event.getEnvironment().getPropertySources().addFirst(
                    new PropertiesPropertySource("envFile", props)
                );
                
                System.out.println("Loaded .env file successfully with " + props.size() + " properties");
            } else {
                System.out.println(".env file not found at: " + envPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }
}
