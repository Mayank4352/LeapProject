package com.ticketing.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController implements HealthIndicator {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Test database connectivity
            try (Connection connection = dataSource.getConnection()) {
                connection.isValid(1); // 1 second timeout
                response.put("status", "UP");
                response.put("database", "UP");
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "DOWN");
            response.put("error", e.getMessage());
        }
        
        response.put("application", "Ticketing System");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @Override
    public Health health() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                connection.isValid(1);
                return Health.up()
                        .withDetail("database", "UP")
                        .withDetail("application", "Ticketing System")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
