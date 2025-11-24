package za.co.tt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.tt.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Admin Management Service - All admin operations in one place
 * Handles auto-creation, manual creation, and admin user management
 */
@Service
public class AdminService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Auto-create default admin on startup if none exists
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultAdminOnStartup() {
        try {
            System.out.println("🔍 Checking for admin users...");
            
            if (hasAdminUsers()) {
                System.out.println("✅ Admin users already exist. Skipping auto-creation.");
                return;
            }

            System.out.println("⚠️  No admin users found. Creating default admin...");
            createDefaultAdmin();
            
        } catch (Exception e) {
            System.err.println("❌ Error during admin setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if any admin users exist
     */
    public boolean hasAdminUsers() {
        return userService.findByUsername("admin").isPresent() || 
               !userService.findByRole("ADMIN").isEmpty();
    }

    /**
     * Create default admin user
     */
    public User createDefaultAdmin() {
        User defaultAdmin = new User.Builder()
            .setName("Ofentse")
            .setSurname("Lebaka")
            .setUsername("ofentselabaka")
            .setEmail("lebaka@tymelesstyre.com")
            .setPassword(passwordEncoder.encode("Lebaka123!"))
            .setPhoneNumber("0213456789")
            .setRole("ADMIN")
            .build();

        User savedAdmin = userService.save(defaultAdmin);
        
        System.out.println("🎉 DEFAULT ADMIN USER CREATED!");
        System.out.println("📧 Username: " + savedAdmin.getUsername());
        System.out.println("📧 Email: " + savedAdmin.getEmail());
        System.out.println("🔑 Password: Lebaka123!");
  
        
        return savedAdmin;
    }

    /**
     * Create custom admin user
     */
    public User createAdmin(String name, String surname, String username, 
                           String email, String password, String phoneNumber) {
        
        // Validation
        if (userService.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' already exists");
        }
        if (userService.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' already registered");
        }

        User admin = new User.Builder()
            .setName(name)
            .setSurname(surname)
            .setUsername(username)
            .setEmail(email)
            .setPassword(passwordEncoder.encode(password))
            .setPhoneNumber(phoneNumber != null ? phoneNumber : "0000000000")
            .setRole("ADMIN")
            .build();

        User savedAdmin = userService.save(admin);
        System.out.println("✅ Admin user created: " + savedAdmin.getUsername());
        
        return savedAdmin;
    }

    /**
     * Create admin user by another admin (requires authentication)
     */
    public User createAdminByAdmin(String creatorUsername, String name, String surname, 
                                  String username, String email, String password, String phoneNumber) {
        
        // Verify creator is an admin
        User creator = userService.findByUsername(creatorUsername)
            .orElseThrow(() -> new IllegalArgumentException("Creator user not found"));
        
        if (!"ADMIN".equals(creator.getRole())) {
            throw new SecurityException("Only admin users can create other admins");
        }
        
        // Create the new admin
        User newAdmin = createAdmin(name, surname, username, email, password, phoneNumber);
        
        System.out.println("✅ Admin '" + newAdmin.getUsername() + "' created by admin '" + creatorUsername + "'");
        return newAdmin;
    }

    /**
     * Get admin user by username
     */
    public Optional<User> getAdminByUsername(String username) {
        return userService.findByUsername(username)
            .filter(user -> "ADMIN".equals(user.getRole()));
    }

    /**
     * Verify if user is admin
     */
    public boolean isAdmin(String username) {
        return userService.findByUsername(username)
            .map(user -> "ADMIN".equals(user.getRole()))
            .orElse(false);
    }

    /**
     * Get all admin users
     */
    public List<User> getAllAdmins() {
        return userService.findByRole("ADMIN");
    }
}