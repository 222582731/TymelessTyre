package za.co.tt.controller;

import za.co.tt.service.DatabaseCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Database cleanup and diagnostic controller
 * Provides endpoints for diagnosing and fixing database integrity issues
 */
@RestController
@RequestMapping("/api/admin/cleanup")
@CrossOrigin(origins = "http://localhost:5173")
public class DatabaseCleanupController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupController.class);

    @Autowired
    private DatabaseCleanupService cleanupService;

    /**
     * Check integrity for a specific order
     * GET /api/admin/cleanup/order/{orderId}/check
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/{orderId}/check")
    public ResponseEntity<?> checkOrderIntegrity(@PathVariable Long orderId) {
        try {
            String report = cleanupService.getIntegrityReport(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("report", report);
            response.put("hasDuplicatePayments", cleanupService.hasDuplicatePayments(orderId));
            response.put("hasDuplicateDeliveries", cleanupService.hasDuplicateDeliveries(orderId));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking order integrity for {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to check order integrity: " + e.getMessage()));
        }
    }

    /**
     * Clean up duplicates for a specific order
     * POST /api/admin/cleanup/order/{orderId}/fix
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/order/{orderId}/fix")
    public ResponseEntity<?> fixOrderDuplicates(@PathVariable Long orderId) {
        try {
            String summary = cleanupService.performFullCleanup(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("cleanupSummary", summary);
            response.put("success", true);
            
            logger.info("Admin cleanup performed for order {}", orderId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fixing order duplicates for {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fix order duplicates: " + e.getMessage()));
        }
    }

    /**
     * Get system health check for database integrity
     * GET /api/admin/cleanup/health
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "Database Cleanup Service");
            health.put("timestamp", java.time.LocalDateTime.now());
            health.put("availableOperations", java.util.List.of(
                "GET /order/{orderId}/check - Check order integrity",
                "POST /order/{orderId}/fix - Fix order duplicates"
            ));
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Error getting system health: {}", e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "System health check failed: " + e.getMessage()));
        }
    }
}