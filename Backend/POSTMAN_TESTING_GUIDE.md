# Postman Testing Guide - Travel Package Booking System

This guide provides step-by-step instructions for testing the JWT authentication system using Postman.

## 🚀 Setup Instructions

### 1. Service Startup Sequence
Start services in this order:
```bash
# 1. Start Eureka Server (Port 8761)
cd Backend/eureka-server
mvn spring-boot:run

# 2. Start User Service (Port 8081)  
cd Backend/user-service
mvn spring-boot:run

# 3. Start API Gateway (Port 8080)
cd Backend/api-gateway
mvn spring-boot:run
```

### 2. Verify Services are Running
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway Health**: http://localhost:8080/health
- **User Service Health**: http://localhost:8081/actuator/health

## 📋 Postman Collection Setup

### Base URL Configuration
- **API Gateway**: `http://localhost:8080`
- All requests should go through the API Gateway

### Environment Variables
Create a Postman environment with these variables:
- `base_url`: `http://localhost:8080`
- `auth_token`: (will be set automatically from login response)

## 🔐 Authentication Endpoints

### 1. User Registration

**POST** `{{base_url}}/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "role": "customer",
  "contactNumber": "+1-555-1234"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiY3VzdG9tZXIiLCJ1c2VySWQiOjEsIm5hbWUiOiJKb2huIERvZSIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzI2NzQ5NjAwLCJleHAiOjE3MjY4MzYwMDB9.xyz123",
  "type": "Bearer",
  "user": {
    "userId": 11,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "customer",
    "contactNumber": "+1-555-1234",
    "approval": "approved",
    "registrationDate": "2024-09-19T12:00:00"
  },
  "message": "Registration successful"
}
```

**Test Script (Tests tab):**
```javascript
pm.test("Registration successful", function () {
    pm.response.to.have.status(200);
    
    const jsonData = pm.response.json();
    pm.expect(jsonData.token).to.not.be.empty;
    pm.expect(jsonData.user.email).to.eql("john.doe@example.com");
    
    // Save token for future requests
    pm.environment.set("auth_token", jsonData.token);
});
```

### 2. Agent Registration (Requires Approval)

**POST** `{{base_url}}/api/auth/register`

**Body (JSON):**
```json
{
  "name": "Agent Smith",
  "email": "agent.smith@travel.com",
  "password": "agent123",
  "role": "agent",
  "contactNumber": "+1-555-5678"
}
```

**Expected Response (200 OK):**
```json
{
  "token": null,
  "type": "Bearer",
  "user": {
    "userId": 12,
    "name": "Agent Smith",
    "email": "agent.smith@travel.com",
    "role": "agent",
    "contactNumber": "+1-555-5678",
    "approval": "pending",
    "registrationDate": "2024-09-19T12:00:00"
  },
  "message": "Registration successful. Waiting for admin approval."
}
```

### 3. User Login

**POST** `{{base_url}}/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON) - Default Admin:**
```json
{
  "email": "admin@travel.com",
  "password": "admin123"
}
```

**Body (JSON) - Default Customer:**
```json
{
  "email": "john.smith@email.com",
  "password": "password123"
}
```

**Body (JSON) - Default Agent:**
```json
{
  "email": "alex.agent@travel.com",
  "password": "agent123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VySWQiOjEwLCJuYW1lIjoiQWRtaW4gVXNlciIsInN1YiI6ImFkbWluQHRyYXZlbC5jb20iLCJpYXQiOjE3MjY3NDk2MDAsImV4cCI6MTcyNjgzNjAwMH0.xyz123",
  "type": "Bearer",
  "user": {
    "userId": 10,
    "name": "Admin User",
    "email": "admin@travel.com",
    "role": "admin",
    "contactNumber": "+1-555-0301",
    "approval": "approved",
    "registrationDate": "2024-09-19T00:00:00"
  },
  "message": "Login successful"
}
```

**Test Script (Tests tab):**
```javascript
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
    
    const jsonData = pm.response.json();
    pm.expect(jsonData.token).to.not.be.empty;
    pm.expect(jsonData.user.email).to.not.be.empty;
    
    // Save token for future requests
    pm.environment.set("auth_token", jsonData.token);
});
```

## 👤 User Management Endpoints (Protected)

### Authorization Header Setup
For all protected endpoints, add this header:
```
Authorization: Bearer {{auth_token}}
```

### 4. Get All Users (Admin Only)

**GET** `{{base_url}}/api/users`

**Headers:**
```
Authorization: Bearer {{auth_token}}
```

**Expected Response (200 OK):**
```json
[
  {
    "userId": 1,
    "name": "John Smith",
    "email": "john.smith@email.com",
    "role": "customer",
    "contactNumber": "+1-555-0101",
    "approval": "approved",
    "registrationDate": "2024-09-19T00:00:00"
  },
  {
    "userId": 10,
    "name": "Admin User",
    "email": "admin@travel.com",
    "role": "admin",
    "contactNumber": "+1-555-0301",
    "approval": "approved",
    "registrationDate": "2024-09-19T00:00:00"
  }
]
```

### 5. Get User by ID

**GET** `{{base_url}}/api/users/1`

**Headers:**
```
Authorization: Bearer {{auth_token}}
```

**Expected Response (200 OK):**
```json
{
  "userId": 1,
  "name": "John Smith",
  "email": "john.smith@email.com",
  "role": "customer",
  "contactNumber": "+1-555-0101",
  "approval": "approved",
  "registrationDate": "2024-09-19T00:00:00"
}
```

### 6. Get Users by Role (Admin Only)

**GET** `{{base_url}}/api/users/role/customer`

**Headers:**
```
Authorization: Bearer {{auth_token}}
```

**Expected Response (200 OK):**
```json
[
  {
    "userId": 1,
    "name": "John Smith",
    "email": "john.smith@email.com",
    "role": "customer",
    "contactNumber": "+1-555-0101",
    "approval": "approved",
    "registrationDate": "2024-09-19T00:00:00"
  }
]
```

### 7. Update User Profile

**PUT** `{{base_url}}/api/users/1`

**Headers:**
```
Authorization: Bearer {{auth_token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "John Smith Updated",
  "email": "john.smith@email.com",
  "role": "customer",
  "contactNumber": "+1-555-9999",
  "approval": "approved"
}
```

**Expected Response (200 OK):**
```json
{
  "userId": 1,
  "name": "John Smith Updated",
  "email": "john.smith@email.com",
  "role": "customer",
  "contactNumber": "+1-555-9999",
  "approval": "approved",
  "registrationDate": "2024-09-19T00:00:00"
}
```

### 8. Get Pending Approvals (Admin Only)

**GET** `{{base_url}}/api/users/pending-approvals`

**Headers:**
```
Authorization: Bearer {{auth_token}}
```

**Expected Response (200 OK):**
```json
[
  {
    "userId": 12,
    "name": "Agent Smith",
    "email": "agent.smith@travel.com",
    "role": "agent",
    "contactNumber": "+1-555-5678",
    "approval": "pending",
    "registrationDate": "2024-09-19T12:00:00"
  }
]
```

### 9. Approve User (Admin Only)

**PUT** `{{base_url}}/api/users/12/approve`

**Headers:**
```
Authorization: Bearer {{auth_token}}
```

**Expected Response (200 OK):**
```json
{
  "userId": 12,
  "name": "Agent Smith",
  "email": "agent.smith@travel.com",
  "role": "agent",
  "contactNumber": "+1-555-5678",
  "approval": "approved",
  "registrationDate": "2024-09-19T12:00:00"
}
```

### 10. Create User (Admin Only)

**POST** `{{base_url}}/api/users`

**Headers:**
```
Authorization: Bearer {{auth_token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "New Customer",
  "email": "new.customer@example.com",
  "role": "customer",
  "contactNumber": "+1-555-7777",
  "approval": "approved"
}
```

**Expected Response (200 OK):**
```json
{
  "userId": 13,
  "name": "New Customer",
  "email": "new.customer@example.com",
  "role": "customer",
  "contactNumber": "+1-555-7777",
  "approval": "approved",
  "registrationDate": "2024-09-19T12:30:00"
}
```

## ❌ Error Scenarios Testing

### 11. Unauthorized Access (No Token)

**GET** `{{base_url}}/api/users`

**Headers:** (No Authorization header)

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2024-09-19T12:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/users"
}
```

### 12. Invalid Credentials

**POST** `{{base_url}}/api/auth/login`

**Body (JSON):**
```json
{
  "email": "admin@travel.com",
  "password": "wrongpassword"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Authentication failed",
  "message": "Authentication failed: Invalid email or password"
}
```

### 13. Role-Based Access Denied

Login as a customer, then try to access admin endpoint:

**GET** `{{base_url}}/api/users/pending-approvals`

**Headers:**
```
Authorization: Bearer {{customer_token}}
```

**Expected Response (403 Forbidden):**
```json
{
  "timestamp": "2024-09-19T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/users/pending-approvals"
}
```

### 14. Duplicate Email Registration

**POST** `{{base_url}}/api/auth/register`

**Body (JSON):**
```json
{
  "name": "Another John",
  "email": "john.smith@email.com",
  "password": "password123",
  "role": "customer",
  "contactNumber": "+1-555-8888"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Registration failed",
  "message": "Registration failed: Email already exists"
}
```

## 🧪 Test Collection Structure

### Postman Collection Organization:
```
Travel Package Booking System
├── 🔐 Authentication
│   ├── Register Customer
│   ├── Register Agent
│   └── Login
├── 👤 User Management (Protected)
│   ├── Get All Users (Admin)
│   ├── Get User by ID
│   ├── Get Users by Role (Admin)
│   ├── Update User Profile
│   ├── Get Pending Approvals (Admin)
│   ├── Approve User (Admin)
│   └── Create User (Admin)
└── ❌ Error Scenarios
    ├── Unauthorized Access
    ├── Invalid Credentials
    ├── Role-Based Access Denied
    └── Duplicate Email Registration
```

## 🔧 Advanced Testing

### JWT Token Validation
Use this JavaScript snippet in Tests to decode JWT:
```javascript
function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}

pm.test("Token contains correct user info", function () {
    const token = pm.response.json().token;
    const decoded = parseJwt(token);
    
    pm.expect(decoded.role).to.not.be.empty;
    pm.expect(decoded.userId).to.be.a('number');
    pm.expect(decoded.exp).to.be.greaterThan(Date.now() / 1000);
});
```

### Pre-request Script for Auto-Login
Add this to collection pre-request script:
```javascript
// Auto-login if token is expired or missing
const token = pm.environment.get("auth_token");
if (!token) {
    pm.sendRequest({
        url: pm.environment.get("base_url") + "/api/auth/login",
        method: 'POST',
        header: {
            'Content-Type': 'application/json'
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                "email": "admin@travel.com",
                "password": "admin123"
            })
        }
    }, function (err, response) {
        if (!err && response.code === 200) {
            const jsonData = response.json();
            pm.environment.set("auth_token", jsonData.token);
        }
    });
}
```

## 📊 Expected Test Results Summary

- ✅ **Customer Registration**: Auto-approved, receives token
- ✅ **Agent Registration**: Pending approval, no token
- ✅ **Admin Login**: Full access token
- ✅ **Protected Endpoints**: Require valid JWT token
- ✅ **Role-Based Access**: Admin-only endpoints blocked for non-admins
- ✅ **Error Handling**: Proper HTTP status codes and error messages
- ✅ **Service Discovery**: All requests routed through API Gateway
- ✅ **JWT Security**: Tokens expire in 24 hours

## 🚀 Quick Start Checklist

1. ☐ Start Eureka Server (8761)
2. ☐ Start User Service (8081)  
3. ☐ Start API Gateway (8080)
4. ☐ Verify Eureka Dashboard shows both services
5. ☐ Import Postman collection
6. ☐ Set environment variables
7. ☐ Test authentication flow
8. ☐ Test protected endpoints
9. ☐ Test error scenarios
10. ☐ Verify JWT token functionality

This comprehensive guide covers all authentication scenarios and provides sample data for thorough testing of your JWT-secured microservices architecture! 