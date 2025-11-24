package za.co.tt.controller;

import za.co.tt.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token Information Controller
 * Provides endpoints for checking token status and information
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Check token validity and get token information
     * GET /api/auth/token-info
     */
    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid authorization header"));
            }

            String token = authHeader.substring(7);
            
            // Extract token information
            String username = JwtUtil.getUsernameFromToken(token);
            Date expirationDate = JwtUtil.getExpirationDateFromToken(token);
            boolean isExpired = JwtUtil.isTokenExpired(token);
            long remainingTimeMs = JwtUtil.getRemainingTimeInMillis(token);
            
            // Calculate remaining time in human-readable format
            long remainingHours = TimeUnit.MILLISECONDS.toHours(remainingTimeMs);
            long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMs) % 60;
            
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("username", username);
            tokenInfo.put("expirationDate", expirationDate);
            tokenInfo.put("isExpired", isExpired);
            tokenInfo.put("remainingTimeMs", remainingTimeMs);
            tokenInfo.put("remainingTimeHuman", String.format("%d hours, %d minutes", remainingHours, remainingMinutes));
            tokenInfo.put("tokenExpirationHours", JwtUtil.getExpirationTimeInHours());
            
            // Add warning if token will expire soon (within 2 hours)
            boolean expiringSoon = JwtUtil.willExpireSoon(token, 2 * 60 * 60 * 1000); // 2 hours
            tokenInfo.put("expiringSoon", expiringSoon);
            
            if (expiringSoon && !isExpired) {
                tokenInfo.put("warning", "Token will expire within 2 hours");
            }

            return ResponseEntity.ok(tokenInfo);

        } catch (Exception e) {
            logger.error("Error getting token info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid token"));
        }
    }

    /**
     * Get JWT configuration information (no authentication required)
     * GET /api/auth/config
     */
    @GetMapping("/config")
    public ResponseEntity<?> getJwtConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("tokenExpirationHours", JwtUtil.getExpirationTimeInHours());
        config.put("tokenExpirationMs", JwtUtil.getExpirationTimeInMillis());
        config.put("message", "JWT tokens expire after " + JwtUtil.getExpirationTimeInHours() + " hours");
        
        return ResponseEntity.ok(config);
    }

    /**
     * Health check endpoint for authentication service
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Authentication Service");
        health.put("timestamp", new Date());
        health.put("jwtExpirationHours", JwtUtil.getExpirationTimeInHours());
        
        return ResponseEntity.ok(health);
    }
}