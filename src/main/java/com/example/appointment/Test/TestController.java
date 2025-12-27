package com.example.appointment.Test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/public/hello")
    public ResponseEntity<Map<String, Object>> publicHello() {
        log.info("PUBLIC endpoint accessed - /api/test/public/hello");
        log.debug("Debug log: Processing public hello request");

        return ResponseEntity.ok(Map.of(
            "message", "Hello from PUBLIC endpoint!",
            "timestamp", LocalDateTime.now(),
            "authenticated", false
        ));
    }

    @GetMapping("/private/hello")
    public ResponseEntity<Map<String, Object>> privateHello() {
        log.info("PRIVATE endpoint accessed - /api/test/private/hello - Authentication required");
        log.debug("Debug log: Processing private hello request");
        log.warn("Warning: This is a protected endpoint");

        return ResponseEntity.ok(Map.of(
            "message", "Hello from PRIVATE endpoint!",
            "timestamp", LocalDateTime.now(),
            "authenticated", true
        ));
    }

    @GetMapping("/admin/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminHello() {
        log.info("ADMIN endpoint accessed - /api/test/admin/hello - ADMIN role required");
        log.debug("Debug log: Processing admin hello request");
        log.warn("Warning: Admin-only endpoint accessed");

        return ResponseEntity.ok(Map.of(
            "message", "Hello from ADMIN endpoint!",
            "timestamp", LocalDateTime.now(),
            "role", "ADMIN"
        ));
    }

    @GetMapping("/staff/hello")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Map<String, Object>> staffHello() {
        log.info("STAFF endpoint accessed - /api/test/staff/hello - STAFF role required");
        log.debug("Debug log: Processing staff hello request");

        return ResponseEntity.ok(Map.of(
            "message", "Hello from STAFF endpoint!",
            "timestamp", LocalDateTime.now(),
            "role", "STAFF"
        ));
    }

    @GetMapping("/customer/hello")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> customerHello() {
        log.info("CUSTOMER endpoint accessed - /api/test/customer/hello - CUSTOMER role required");
        log.debug("Debug log: Processing customer hello request");

        return ResponseEntity.ok(Map.of(
            "message", "Hello from CUSTOMER endpoint!",
            "timestamp", LocalDateTime.now(),
            "role", "CUSTOMER"
        ));
    }

    @PostMapping("/logs")
    public ResponseEntity<Map<String, String>> generateLogs(@RequestBody(required = false) Map<String, String> payload) {
        String level = payload != null ? payload.getOrDefault("level", "info") : "info";
        String message = payload != null ? payload.getOrDefault("message", "Test log message") : "Test log message";

        log.info("=== Generating test logs at level: {} ===", level);

        switch (level.toLowerCase()) {
            case "error":
                log.error("ERROR LOG: {}", message);
                break;
            case "warn":
                log.warn("WARN LOG: {}", message);
                break;
            case "debug":
                log.debug("DEBUG LOG: {}", message);
                break;
            case "trace":
                log.trace("TRACE LOG: {}", message);
                break;
            case "all":
                log.trace("TRACE LOG: {}", message);
                log.debug("DEBUG LOG: {}", message);
                log.info("INFO LOG: {}", message);
                log.warn("WARN LOG: {}", message);
                log.error("ERROR LOG: {}", message);
                break;
            default:
                log.info("INFO LOG: {}", message);
        }

        log.info("=== Test logs generated successfully ===");

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "level", level,
            "message", "Logs generated successfully. Check Loki for output."
        ));
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> triggerError() {
        log.error("ERROR endpoint triggered - Simulating application error");
        log.error("Stack trace simulation", new RuntimeException("Simulated error for testing"));

        return ResponseEntity.status(500).body(Map.of(
            "error", "Simulated error",
            "message", "Check logs in Loki for error details"
        ));
    }
}
