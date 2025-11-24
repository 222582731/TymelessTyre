# 🔧 Frontend Admin Management API Documentation

This document provides complete integration guide for frontend applications to manage admin users in the TymelessTyre system.

## 📋 Table of Contents
- [Authentication](#authentication)
- [Endpoints Overview](#endpoints-overview)
- [API Reference](#api-reference)
- [Frontend Integration Examples](#frontend-integration-examples)
- [Error Handling](#error-handling)
- [Security Considerations](#security-considerations)

---

## 🔐 Authentication

All admin management endpoints (except status check) require JWT authentication with ADMIN role.

### Getting JWT Token
```javascript
// Login to get JWT token
const loginResponse = await fetch('/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'AdminPass123!'
  })
});

const { token } = await loginResponse.json();
localStorage.setItem('jwtToken', token);
```

### Using JWT Token
```javascript
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
  'Content-Type': 'application/json'
};
```

---

## 🌐 Endpoints Overview

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| `POST` | `/api/admin-setup/create-admin` | ✅ ADMIN | Create new admin user |
| `GET` | `/api/admin-setup/admins` | ✅ ADMIN | Fetch all admin users |
| `GET` | `/api/admin-setup/status` | ❌ | Check admin setup status |
| `GET` | `/api/admin-setup/instructions` | ❌ | Get API documentation |

---

## 📚 API Reference

### 1. Create New Admin User

**Endpoint:** `POST /api/admin-setup/create-admin`  
**Authentication:** Required (ADMIN role)  
**Description:** Creates a new admin user. Only existing admins can create new admins.

#### Request
```http
POST /api/admin-setup/create-admin
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "John",
  "surname": "Admin",
  "username": "johnadmin",
  "email": "john@tymelesstyre.com",
  "password": "SecurePass123!",
  "phoneNumber": "0123456789"
}
```

#### Request Fields
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | ✅ | First name of the admin |
| `surname` | string | ✅ | Last name of the admin |
| `username` | string | ✅ | Unique username (must not exist) |
| `email` | string | ✅ | Unique email address (must not exist) |
| `password` | string | ✅ | Strong password for the admin |
| `phoneNumber` | string | ❌ | Phone number (optional) |

#### Success Response (201 Created)
```json
{
  "message": "Admin user created successfully",
  "admin": {
    "userId": 123,
    "name": "John",
    "surname": "Admin",
    "username": "johnadmin",
    "email": "john@tymelesstyre.com",
    "phoneNumber": "0123456789",
    "role": "ADMIN",
    "createdAt": "2025-10-21T10:30:00",
    "updatedAt": "2025-10-21T10:30:00"
  },
  "createdBy": "existing-admin"
}
```

#### Error Responses
```json
// 400 Bad Request - Missing field
{
  "error": "Missing required field: email"
}

// 400 Bad Request - Duplicate username
{
  "error": "Username 'johnadmin' already exists"
}

// 400 Bad Request - Duplicate email
{
  "error": "Email 'john@tymelesstyre.com' already registered"
}

// 401 Unauthorized - Invalid/missing token
{
  "error": "Access denied"
}

// 403 Forbidden - Not admin
{
  "error": "Only admin users can create other admins"
}
```

---

### 2. Fetch All Admin Users

**Endpoint:** `GET /api/admin-setup/admins`  
**Authentication:** Required (ADMIN role)  
**Description:** Retrieves list of all admin users in the system.

#### Request
```http
GET /api/admin-setup/admins
Authorization: Bearer <jwt-token>
```

#### Success Response (200 OK)
```json
{
  "admins": [
    {
      "userId": 1,
      "name": "Default",
      "surname": "Admin",
      "username": "admin",
      "email": "admin@tymelesstyre.com",
      "phoneNumber": "0123456789",
      "role": "ADMIN",
      "createdAt": "2025-10-21T09:00:00",
      "updatedAt": "2025-10-21T09:00:00"
    },
    {
      "userId": 123,
      "name": "John",
      "surname": "Admin",
      "username": "johnadmin",
      "email": "john@tymelesstyre.com",
      "phoneNumber": "0123456789",
      "role": "ADMIN",
      "createdAt": "2025-10-21T10:30:00",
      "updatedAt": "2025-10-21T10:30:00"
    }
  ],
  "count": 2,
  "requestedBy": "admin"
}
```

#### Error Responses
```json
// 401 Unauthorized
{
  "error": "Access denied"
}

// 403 Forbidden
{
  "error": "Access denied"
}
```

---

### 3. Check Admin Setup Status

**Endpoint:** `GET /api/admin-setup/status`  
**Authentication:** Not required  
**Description:** Check if admin users exist in the system.

#### Request
```http
GET /api/admin-setup/status
```

#### Success Response (200 OK)
```json
{
  "hasAdmins": true,
  "canCreateInitial": false,
  "message": "Admin users already exist. Use regular endpoints for additional admins."
}
```

---

### 4. Get API Instructions

**Endpoint:** `GET /api/admin-setup/instructions`  
**Authentication:** Not required  
**Description:** Get complete API documentation and usage examples.

#### Request
```http
GET /api/admin-setup/instructions
```

#### Success Response (200 OK)
```json
{
  "title": "Admin User Setup Instructions",
  "endpoints": {
    "createAdmin": {
      "method": "POST",
      "url": "/api/admin-setup/create-admin",
      "description": "Create new admin user (requires admin authentication)",
      "auth": "Bearer JWT token with ADMIN role",
      "requiredFields": ["name", "surname", "username", "email", "password"],
      "optionalFields": ["phoneNumber"]
    },
    "listAdmins": {
      "method": "GET",
      "url": "/api/admin-setup/admins",
      "description": "List all admin users",
      "auth": "Bearer JWT token with ADMIN role"
    }
  },
  "example": {
    "name": "John",
    "surname": "Admin",
    "username": "johnadmin",
    "email": "john@tymelesstyre.com",
    "password": "SecurePass123!",
    "phoneNumber": "0123456789"
  },
  "authentication": {
    "note": "For authenticated endpoints, include header:",
    "header": "Authorization: Bearer <your-jwt-token>",
    "getToken": "Use /user/login endpoint to get JWT token"
  }
}
```

---

## 💻 Frontend Integration Examples

### React.js Implementation

#### 1. Admin Service Class
```javascript
// services/adminService.js
class AdminService {
  constructor(baseURL = 'http://localhost:8080') {
    this.baseURL = baseURL;
  }

  getAuthHeaders() {
    const token = localStorage.getItem('jwtToken');
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  }

  async createAdmin(adminData) {
    const response = await fetch(`${this.baseURL}/api/admin-setup/create-admin`, {
      method: 'POST',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(adminData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Failed to create admin');
    }

    return await response.json();
  }

  async fetchAdmins() {
    const response = await fetch(`${this.baseURL}/api/admin-setup/admins`, {
      headers: this.getAuthHeaders()
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Failed to fetch admins');
    }

    return await response.json();
  }

  async checkStatus() {
    const response = await fetch(`${this.baseURL}/api/admin-setup/status`);
    return await response.json();
  }
}

export default new AdminService();
```

#### 2. React Component
```jsx
// components/AdminManagement.jsx
import React, { useState, useEffect } from 'react';
import adminService from '../services/adminService';

function AdminManagement() {
  const [admins, setAdmins] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const [formData, setFormData] = useState({
    name: '',
    surname: '',
    username: '',
    email: '',
    password: '',
    phoneNumber: ''
  });

  useEffect(() => {
    loadAdmins();
  }, []);

  const loadAdmins = async () => {
    try {
      setLoading(true);
      const response = await adminService.fetchAdmins();
      setAdmins(response.admins);
      setError('');
    } catch (err) {
      setError(`Failed to load admins: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const result = await adminService.createAdmin(formData);
      setSuccess(`Admin '${result.admin.username}' created successfully!`);
      
      // Reset form
      setFormData({
        name: '', surname: '', username: '',
        email: '', password: '', phoneNumber: ''
      });
      
      // Reload admin list
      await loadAdmins();
    } catch (err) {
      setError(`Failed to create admin: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-management">
      <h2>Admin Management</h2>

      {/* Error/Success Messages */}
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Create Admin Form */}
      <div className="create-admin-section">
        <h3>Create New Admin</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>First Name:</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Last Name:</label>
            <input
              type="text"
              name="surname"
              value={formData.surname}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Username:</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Email:</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Password:</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              required
              minLength="8"
            />
          </div>

          <div className="form-group">
            <label>Phone Number:</label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleInputChange}
            />
          </div>

          <button 
            type="submit" 
            disabled={loading}
            className="btn btn-primary"
          >
            {loading ? 'Creating...' : 'Create Admin'}
          </button>
        </form>
      </div>

      {/* Admin List */}
      <div className="admin-list-section">
        <h3>Existing Admins ({admins.length})</h3>
        
        {loading && <div>Loading...</div>}
        
        <div className="admin-grid">
          {admins.map(admin => (
            <div key={admin.userId} className="admin-card">
              <h4>{admin.name} {admin.surname}</h4>
              <p><strong>Username:</strong> {admin.username}</p>
              <p><strong>Email:</strong> {admin.email}</p>
              <p><strong>Phone:</strong> {admin.phoneNumber || 'N/A'}</p>
              <p><strong>Created:</strong> {new Date(admin.createdAt).toLocaleDateString()}</p>
            </div>
          ))}
        </div>

        {admins.length === 0 && !loading && (
          <p>No admin users found.</p>
        )}
      </div>
    </div>
  );
}

export default AdminManagement;
```

#### 3. CSS Styles
```css
/* styles/AdminManagement.css */
.admin-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.alert {
  padding: 12px;
  border-radius: 4px;
  margin: 10px 0;
}

.alert-error {
  background-color: #fee;
  color: #c00;
  border: 1px solid #fcc;
}

.alert-success {
  background-color: #efe;
  color: #060;
  border: 1px solid #cfc;
}

.create-admin-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.admin-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border: 1px solid #eee;
}

.admin-card h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.admin-card p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}
```

### Vanilla JavaScript Implementation

```javascript
// js/adminManager.js
class AdminManager {
  constructor() {
    this.baseURL = 'http://localhost:8080';
    this.init();
  }

  init() {
    this.loadAdmins();
    this.setupEventListeners();
  }

  getAuthHeaders() {
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
    };
  }

  setupEventListeners() {
    const form = document.getElementById('create-admin-form');
    const refreshBtn = document.getElementById('refresh-admins');

    form?.addEventListener('submit', (e) => this.handleCreateAdmin(e));
    refreshBtn?.addEventListener('click', () => this.loadAdmins());
  }

  async loadAdmins() {
    try {
      this.showLoading(true);
      
      const response = await fetch(`${this.baseURL}/api/admin-setup/admins`, {
        headers: this.getAuthHeaders()
      });

      if (!response.ok) {
        throw new Error('Failed to fetch admins');
      }

      const data = await response.json();
      this.displayAdmins(data.admins);
      
    } catch (error) {
      this.showError(`Error loading admins: ${error.message}`);
    } finally {
      this.showLoading(false);
    }
  }

  async handleCreateAdmin(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const adminData = Object.fromEntries(formData.entries());

    try {
      this.showLoading(true);
      
      const response = await fetch(`${this.baseURL}/api/admin-setup/create-admin`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(adminData)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to create admin');
      }

      const result = await response.json();
      this.showSuccess(`Admin '${result.admin.username}' created successfully!`);
      
      e.target.reset(); // Clear form
      this.loadAdmins(); // Refresh list
      
    } catch (error) {
      this.showError(`Error creating admin: ${error.message}`);
    } finally {
      this.showLoading(false);
    }
  }

  displayAdmins(admins) {
    const container = document.getElementById('admin-list');
    
    if (!container) return;

    if (admins.length === 0) {
      container.innerHTML = '<p>No admin users found.</p>';
      return;
    }

    container.innerHTML = admins.map(admin => `
      <div class="admin-card">
        <h4>${admin.name} ${admin.surname}</h4>
        <p><strong>Username:</strong> ${admin.username}</p>
        <p><strong>Email:</strong> ${admin.email}</p>
        <p><strong>Phone:</strong> ${admin.phoneNumber || 'N/A'}</p>
        <p><strong>Created:</strong> ${new Date(admin.createdAt).toLocaleDateString()}</p>
      </div>
    `).join('');
  }

  showLoading(show) {
    const loader = document.getElementById('loading');
    if (loader) {
      loader.style.display = show ? 'block' : 'none';
    }
  }

  showError(message) {
    this.showMessage(message, 'error');
  }

  showSuccess(message) {
    this.showMessage(message, 'success');
  }

  showMessage(message, type) {
    const container = document.getElementById('messages');
    if (!container) return;

    const div = document.createElement('div');
    div.className = `alert alert-${type}`;
    div.textContent = message;
    
    container.appendChild(div);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
      div.remove();
    }, 5000);
  }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new AdminManager();
});
```

### HTML Template

```html
<!-- admin-management.html -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Management - TymelessTyre</title>
    <link rel="stylesheet" href="styles/AdminManagement.css">
</head>
<body>
    <div class="admin-management">
        <h1>Admin Management</h1>
        
        <!-- Messages Container -->
        <div id="messages"></div>
        
        <!-- Loading Indicator -->
        <div id="loading" style="display: none;">Loading...</div>

        <!-- Create Admin Form -->
        <div class="create-admin-section">
            <h2>Create New Admin</h2>
            <form id="create-admin-form">
                <div class="form-group">
                    <label for="name">First Name:</label>
                    <input type="text" id="name" name="name" required>
                </div>

                <div class="form-group">
                    <label for="surname">Last Name:</label>
                    <input type="text" id="surname" name="surname" required>
                </div>

                <div class="form-group">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username" required>
                </div>

                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" required>
                </div>

                <div class="form-group">
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" required minlength="8">
                </div>

                <div class="form-group">
                    <label for="phoneNumber">Phone Number:</label>
                    <input type="tel" id="phoneNumber" name="phoneNumber">
                </div>

                <button type="submit" class="btn btn-primary">Create Admin</button>
            </form>
        </div>

        <!-- Admin List -->
        <div class="admin-list-section">
            <h2>
                Existing Admins 
                <button id="refresh-admins" class="btn btn-secondary">Refresh</button>
            </h2>
            <div id="admin-list" class="admin-grid"></div>
        </div>
    </div>

    <script src="js/adminManager.js"></script>
</body>
</html>
```

---

## ⚠️ Error Handling

### Common HTTP Status Codes

| Status Code | Meaning | Typical Cause |
|-------------|---------|---------------|
| `200` | OK | Request successful |
| `201` | Created | Admin created successfully |
| `400` | Bad Request | Missing/invalid fields, duplicate username/email |
| `401` | Unauthorized | Invalid/missing JWT token |
| `403` | Forbidden | User doesn't have ADMIN role |
| `500` | Internal Server Error | Server-side error |

### Error Response Format
```json
{
  "error": "Descriptive error message"
}
```

### Frontend Error Handling Best Practices

```javascript
async function handleApiCall(apiCall) {
  try {
    const response = await apiCall();
    return response;
  } catch (error) {
    if (error.message.includes('401')) {
      // Token expired or invalid
      localStorage.removeItem('jwtToken');
      window.location.href = '/login';
    } else if (error.message.includes('403')) {
      // Access denied
      showError('You do not have permission to perform this action');
    } else if (error.message.includes('400')) {
      // Bad request
      showError(error.message);
    } else {
      // Generic error
      showError('An unexpected error occurred. Please try again.');
    }
  }
}
```

---

## 🔒 Security Considerations

### 1. JWT Token Management
- Store JWT securely (localStorage or httpOnly cookies)
- Implement token expiration handling
- Clear token on logout
- Validate token before API calls

### 2. Input Validation
- Validate all form inputs on frontend
- Use strong password requirements
- Sanitize user inputs
- Implement email format validation

### 3. HTTPS Only
- Always use HTTPS in production
- Never send credentials over HTTP
- Implement CORS properly

### 4. Password Security
- Enforce strong password policies
- Never display passwords in UI
- Use password input types
- Consider password strength indicators

### 5. Error Messages
- Don't expose sensitive information in errors
- Log security events
- Implement rate limiting for failed attempts

---

## 🚀 Quick Start Checklist

- [ ] Implement authentication flow
- [ ] Set up JWT token storage
- [ ] Create admin service class
- [ ] Build admin management UI
- [ ] Add form validation
- [ ] Implement error handling
- [ ] Test all endpoints
- [ ] Add loading states
- [ ] Implement responsive design
- [ ] Add security headers

---

## 📞 Support

For additional help or questions about the admin management API:

1. Check the `/api/admin-setup/instructions` endpoint
2. Review error messages for specific guidance
3. Ensure JWT token is valid and user has ADMIN role
4. Verify all required fields are provided
5. Check network connectivity and CORS settings

---

**Last Updated:** October 21, 2025  
**API Version:** 1.0  
**Backend Framework:** Spring Boot with Spring Security