package com.pm.loanscoreservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for loan score service.
 * Provides health status and service information.
 */
@RestController
@RequestMapping("/api/health")
@Slf4j
@Tag(name = "Health Check", description = "Health check and service status endpoints")
public class HealthController {

    /**
     * Basic health check endpoint.
     */
    @GetMapping
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "loan-score-service");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        log.debug("Health check requested");
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed status endpoint.
     */
    @GetMapping("/status")
    @Operation(summary = "Detailed status", description = "Get detailed service status information")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "loan-score-service");
        status.put("status", "RUNNING");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0.0");
        status.put("description", "Loan Score Calculation Service");
        status.put("port", 4003);
        
        Map<String, Object> features = new HashMap<>();
        features.put("kafka_consumer", "ENABLED");
        features.put("score_calculation", "ENABLED");
        features.put("rest_api", "ENABLED");
        features.put("database", "ENABLED");
        
        status.put("features", features);
        
        log.debug("Status check requested");
        return ResponseEntity.ok(status);
    }
}
