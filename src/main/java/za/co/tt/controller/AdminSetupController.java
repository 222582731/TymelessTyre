package za.co.tt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import za.co.tt.domain.User;
import za.co.tt.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-setup")
@CrossOrigin(origins = "*")
public class AdminSetupController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/create-initial")
    public ResponseEntity<?> createInitialAdmin(@RequestBody Map<String, String> request) {
        try {
            if (adminService.hasAdminUsers()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "error", "Admin users already exist",
                        "message", "Use the regular user registration endpoint with role 'ADMIN'"
                    ));
            }


            String[] requiredFields = {"name", "surname", "username", "email", "password"};
            for (String field : requiredFields) {
                if (request.get(field) == null || request.get(field).trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing required field: " + field));
                }
            }

            User savedAdmin = adminService.createAdmin(
                request.get("name"),
                request.get("surname"), 
                request.get("username"),
                request.get("email"),
                request.get("password"),
                request.get("phoneNumber")
            );

            savedAdmin.setPassword(null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Initial admin user created successfully",
                "admin", savedAdmin,
                "note", "This endpoint is now disabled until all admin users are removed"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating admin: " + e.getMessage()));
        }
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> request, 
                                        Authentication authentication) {
        try {
            String creatorUsername = authentication.getName();

            String[] requiredFields = {"name", "surname", "username", "email", "password"};
            for (String field : requiredFields) {
                if (request.get(field) == null || request.get(field).trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing required field: " + field));
                }
            }

            User newAdmin = adminService.createAdminByAdmin(
                creatorUsername,
                request.get("name"),
                request.get("surname"), 
                request.get("username"),
                request.get("email"),
                request.get("password"),
                request.get("phoneNumber")
            );
            
            // Remove password from response for security
            newAdmin.setPassword(null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Admin user created successfully",
                "admin", newAdmin,
                "createdBy", creatorUsername
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error creating admin: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAdminSetupStatus() {
        try {
            boolean hasAdmins = adminService.hasAdminUsers();
            
            return ResponseEntity.ok(Map.of(
                "hasAdmins", hasAdmins,
                "canCreateInitial", !hasAdmins,
                "message", !hasAdmins 
                    ? "No admin users exist. You can create an initial admin."
                    : "Admin users already exist. Use regular endpoints for additional admins."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error checking admin status: " + e.getMessage()));
        }
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listAdmins(Authentication authentication) {
        try {
            String requesterUsername = authentication.getName();
            
            // Verify requester is admin (additional security check)
            if (!adminService.isAdmin(requesterUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
            }
            
            // Get all admin users (you'll need to add this method to AdminService)
            List<User> admins = adminService.getAllAdmins();
            
            // Remove passwords from response
            admins.forEach(admin -> admin.setPassword(null));
            
            return ResponseEntity.ok(Map.of(
                "admins", admins,
                "count", admins.size(),
                "requestedBy", requesterUsername
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error fetching admins: " + e.getMessage()));
        }
    }


    }
