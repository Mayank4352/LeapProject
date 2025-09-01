package com.ticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        System.out.println("=== DatabaseConfig called ===");
        System.out.println("DATABASE_URL: " + databaseUrl);
        System.out.println("Raw URI info:");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            // Convert PostgreSQL URL to JDBC URL
            try {
                URI uri = new URI(databaseUrl);
                String host = uri.getHost();
                int rawPort = uri.getPort();
                int port = rawPort == -1 ? 5432 : rawPort; // Default to 5432 if port is -1
                String database = uri.getPath().substring(1); // Remove leading '/'
                String username = uri.getUserInfo().split(":")[0];
                String password = uri.getUserInfo().split(":")[1];
                
                System.out.println("Raw port from URI: " + rawPort);
                System.out.println("Final port used: " + port);
                System.out.println("Host: " + host);
                System.out.println("Database: " + database);
                System.out.println("Username: " + username);
                
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                System.out.println("Final JDBC URL: " + jdbcUrl);
                
                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + databaseUrl);
                e.printStackTrace();
                throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
            }
        } else {
            System.out.println("Using individual env variables for local development");
            // Use individual environment variables for local development
            String host = System.getenv().getOrDefault("DB_HOST", "localhost");
            String port = System.getenv().getOrDefault("DB_PORT", "5432");
            String database = System.getenv().getOrDefault("DB_NAME", "ticketing_db");
            String username = System.getenv().getOrDefault("DB_USERNAME", "postgres");
            String password = System.getenv().getOrDefault("DB_PASSWORD", "password");
            
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
            
            System.out.println("Using individual env vars - JDBC URL: " + jdbcUrl);
            System.out.println("Username: " + username);
            
            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }
    }
}
