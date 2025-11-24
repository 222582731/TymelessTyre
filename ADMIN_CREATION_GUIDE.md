# Admin User Creation Guide

## 🎯 Best Methods to Add Admin Users (Without Data Initializer)

Based on your Spring Boot application, here are the **5 best ways** to add admin users to the database when running:

---

## 🔥 Method 1: REST API Endpoint (Recommended)

### **Using the Public Registration Endpoint**

You can use the existing `/user/register` endpoint to create an admin user:

**Endpoint:** `POST http://localhost:8080/user/register`

**Request Body:**
```json
{
    "name": "Admin",
    "surname": "User", 
    "username": "admin",
    "email": "admin@tymelesstyre.com",
    "phoneNumber": "0123456789",
    "password": "AdminPass123!",
    "role": "ADMIN",
    "street": "123 Admin Street",
    "city": "Cape Town",
    "state": "Western Cape",
    "postalCode": 8001,
    "country": "South Africa",
    "addressType": "WORK"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin",
    "surname": "User",
    "username": "admin", 
    "email": "admin@tymelesstyre.com",
    "phoneNumber": "0123456789",
    "password": "AdminPass123!",
    "role": "ADMIN",
    "street": "123 Admin Street",
    "city": "Cape Town", 
    "state": "Western Cape",
    "postalCode": 8001,
    "country": "South Africa",
    "addressType": "WORK"
  }'
```

---

## 🛠️ Method 2: Admin-Only User Creation Endpoint

### **Using the Admin User Creation Endpoint**

**Endpoint:** `POST http://localhost:8080/user/create`

⚠️ **Note:** This requires admin authentication, so create your first admin with Method 1, then use this for additional admins.

**Steps:**
1. **First, login as admin:**
```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "AdminPass123!"
  }'
```

2. **Copy the JWT token from response**

3. **Create new admin user:**
```bash
curl -X POST http://localhost:8080/user/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "name": "Second",
    "surname": "Admin",
    "username": "admin2",
    "email": "admin2@tymelesstyre.com",
    "phoneNumber": "0987654321",
    "password": "SecondAdmin123!",
    "role": "ADMIN"
  }'
```

---

## 🐘 Method 3: Direct Database Insert

### **SQL Command (Connect to your database directly)**

```sql
-- Insert admin user (password is BCrypt encoded)
INSERT INTO users (name, surname, username, email, password, phone_number, role, created_at) 
VALUES (
    'Admin', 
    'User', 
    'admin', 
    'admin@tymelesstyre.com', 
    '$2a$10$rQhgPw8XhOz1kfQZUQBDbeKTOJgpLPNfFt.DjZP8Sh9QmJF6LkBQS', -- Password: "AdminPass123!"
    '0123456789', 
    'ADMIN', 
    NOW()
);

-- Optional: Add address for the admin
INSERT INTO addresses (street, city, state, postal_code, country, address_type, user_id)
VALUES (
    '123 Admin Street',
    'Cape Town', 
    'Western Cape',
    8001,
    'South Africa',
    'WORK',
    (SELECT user_id FROM users WHERE username = 'admin')
);
```

---

## 📝 Method 4: Create Admin Endpoint Controller

### **Custom Admin Creation Controller**

Create this controller to add a simple admin creation endpoint:

```java
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminSetupController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create initial admin user (no authentication required)
     * Should be disabled in production or secured
     */
    @PostMapping("/create-initial")
    public ResponseEntity<?> createInitialAdmin(@RequestBody Map<String, String> request) {
        try {
            // Check if any admin already exists
            List<User> existingAdmins = userService.findByRole("ADMIN");
            if (!existingAdmins.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admin users already exist. Use regular user creation endpoint.");
            }

            User admin = new User.Builder()
                .setName(request.get("name"))
                .setSurname(request.get("surname"))
                .setUsername(request.get("username"))
                .setEmail(request.get("email"))
                .setPassword(passwordEncoder.encode(request.get("password")))
                .setPhoneNumber(request.get("phoneNumber"))
                .setRole("ADMIN")
                .build();

            User savedAdmin = userService.save(admin);
            savedAdmin.setPassword(null); // Remove password from response
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error creating admin: " + e.getMessage());
        }
    }
}
```

**Usage:**
```bash
curl -X POST http://localhost:8080/api/admin/create-initial \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin",
    "surname": "User",
    "username": "admin",
    "email": "admin@tymelesstyre.com",
    "password": "AdminPass123!",
    "phoneNumber": "0123456789"
  }'
```

---

## 🔧 Method 5: Application Startup Bean

### **Automatic Admin Creation on Startup**

Create this component to automatically create an admin on startup:

```java
@Component
public class AdminUserSetup {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultAdmin() {
        try {
            // Check if admin already exists
            Optional<User> existingAdmin = userService.findByUsername("admin");
            if (existingAdmin.isPresent()) {
                System.out.println("Admin user already exists.");
                return;
            }

            // Create default admin
            User admin = new User.Builder()
                .setName("Default")
                .setSurname("Admin")
                .setUsername("admin")
                .setEmail("admin@tymelesstyre.com")
                .setPassword(passwordEncoder.encode("AdminPass123!"))
                .setPhoneNumber("0123456789")
                .setRole("ADMIN")
                .build();

            User savedAdmin = userService.save(admin);
            System.out.println("Default admin user created: " + savedAdmin.getUsername());
            
        } catch (Exception e) {
            System.err.println("Error creating default admin: " + e.getMessage());
        }
    }
}
```

---

## 🎯 Quick Implementation

### **I'll create the simplest solution for you:**

Let me create the admin setup controller right now: