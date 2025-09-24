# 🚀 Travel Package Booking System - Complete Startup Guide

## Prerequisites

### Required Software
- **Java 17+** (OpenJDK or Oracle JDK)
- **Maven 3.6+**
- **MySQL 8.0+** 
- **Node.js 18+** with npm
- **Git** (for version control)

### MySQL Database Setup
1. **Install MySQL Server** (if not already installed)
2. **Start MySQL Service**
3. **Create databases** (they will be auto-created, but you can create them manually):
   ```sql
   CREATE DATABASE travel_booking_users;
   CREATE DATABASE travel_package_db;
   CREATE DATABASE travel_booking_db;
   CREATE DATABASE travel_payment_db;
   CREATE DATABASE travel_insurance_db;
   CREATE DATABASE travel_assistance_db;
   CREATE DATABASE travel_review_db;
   ```
4. **Verify credentials**: 
   - Username: `root`
   - Password: `mysql`

---

## 🏃‍♂️ Quick Start (Recommended Order)

### Step 1: Start Eureka Server
```powershell
cd "Backend\eureka-server"
mvn spring-boot:run
```
**Wait for**: Console message "Started EurekaServerApplication"
**Verify**: Open http://localhost:8761 - should see Eureka dashboard

### Step 2: Start User Service
```powershell
cd "Backend\user-service"
mvn spring-boot:run
```
**Wait for**: "User Service started successfully on port 8081"

### Step 3: Start Package Service
```powershell
cd "Backend\package-service"
mvn spring-boot:run
```
**Wait for**: Service registration with Eureka

### Step 4: Start Booking Service
```powershell
cd "Backend\booking-service"
mvn spring-boot:run
```
**Wait for**: Service registration with Eureka

### Step 5: Start Payment Service
```powershell
cd "Backend\payment-service"
mvn spring-boot:run
```
**Wait for**: Service registration with Eureka

### Step 6: Start Insurance Service
```powershell
cd "Backend\insurance-service"
mvn spring-boot:run
```
**Wait for**: Service registration with Eureka

### Step 7: Start API Gateway (LAST)
```powershell
cd "Backend\api-gateway"
mvn spring-boot:run
```
**Wait for**: "API Gateway started successfully on port 8080"

### Step 8: Start Frontend
```powershell
cd Frontend
npm install
npm run dev
```
**Access**: http://localhost:5173

---

## 🔧 Service Configuration Summary

| Service | Port | Database | Eureka | Feign Clients |
|---------|------|----------|---------|---------------|
| Eureka Server | 8761 | - | Server | - |
| User Service | 8081 | travel_booking_users | ✅ | - |
| Package Service | 8082 | travel_package_db | ✅ | booking, user |
| Booking Service | 8083 | travel_booking_db | ✅ | package, user, payment |
| Payment Service | 8084 | travel_payment_db | ✅ | booking, user |
| Insurance Service | 8085 | travel_insurance_db | ✅ | booking, user |
| Assistance Service | 8086 | travel_assistance_db | ✅ | - |
| Review Service | 8087 | travel_review_db | ✅ | - |
| API Gateway | 8080 | - | ✅ | - |

---

## 🔐 Authentication & Authorization

### JWT Configuration
- **Secret Key**: Shared across all services
- **Token Expiration**: 24 hours (86400 seconds)
- **Role-Based Access**: Admin, Agent, User roles

### Admin Routes (Requires Admin Role)
- `GET /api/users` - List all users
- `DELETE /api/users/{id}` - Delete user
- `PUT /api/users/{id}/approve` - Approve agent
- `GET /api/users/pending-approvals` - Get pending agent approvals

### Agent/Admin Routes
- `POST /api/packages` - Create package
- `PUT /api/packages/{id}` - Update package
- `DELETE /api/packages/{id}` - Delete package

---

## 🌐 API Gateway Routes

All frontend requests go through API Gateway at **http://localhost:8080**

### Public Routes (No Authentication)
- `POST /api/auth/login`
- `POST /api/auth/register`

### Protected Routes (JWT Required)
- `/api/users/**` → User Service
- `/api/packages/**` → Package Service  
- `/api/bookings/**` → Booking Service
- `/api/payments/**` → Payment Service
- `/api/insurance/**` → Insurance Service
- `/api/assistance/**` → Assistance Service
- `/api/reviews/**` → Review Service

---

## 🗄️ Database Structure

### MySQL Databases Created
```sql
-- User management
travel_booking_users      (User Service)

-- Core business data  
travel_package_db         (Package Service)
travel_booking_db         (Booking Service)

-- Financial data
travel_payment_db         (Payment Service)
travel_insurance_db       (Insurance Service)

-- Support services
travel_assistance_db      (Assistance Service)
travel_review_db          (Review Service)
```

---

## 📊 Health Monitoring

### Eureka Dashboard
- **URL**: http://localhost:8761
- **Purpose**: View all registered services
- **Check**: All services should appear in "Instances currently registered"

### Individual Service Health
- User Service: http://localhost:8081/actuator/health
- Package Service: http://localhost:8082/actuator/health
- Booking Service: http://localhost:8083/actuator/health
- Payment Service: http://localhost:8084/actuator/health
- Insurance Service: http://localhost:8085/actuator/health

### API Gateway Health
- **URL**: http://localhost:8080/actuator/health
- **Service Discovery**: Shows health of all downstream services

---

## 🎯 Testing the System

### 1. Register Admin User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@test.com", 
    "password": "password123",
    "role": "admin"
  }'
```

### 2. Login and Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "password123"
  }'
```

### 3. Test Admin Route
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <your_jwt_token>"
```

---

## 🔧 Troubleshooting

### Common Issues

1. **"Failed to configure DataSource"**
   - **Solution**: Ensure MySQL is running and accessible
   - **Check**: Connection string, username, password

2. **"Service not registered with Eureka"**
   - **Solution**: Start Eureka Server first, wait for full startup
   - **Check**: http://localhost:8761 for service registration

3. **"CORS Error in Frontend"**
   - **Solution**: CORS is configured at API Gateway only
   - **Check**: All requests go through http://localhost:8080

4. **"JWT Token Invalid"**
   - **Solution**: Ensure same JWT secret across all services
   - **Check**: application.yml files have matching jwt.secret

### Service Startup Order
**CRITICAL**: Always start services in this order:
1. MySQL Database
2. Eureka Server (8761)
3. User Service (8081) 
4. Other Microservices (8082-8087)
5. API Gateway (8080) - **LAST**
6. Frontend (5173)

---

## 📋 Features Implemented

### ✅ Core Features
- User registration and authentication
- Travel package management
- Booking system with payment processing
- Insurance policy management
- Admin dashboard for user management
- Agent approval workflow

### ✅ Technical Features
- JWT-based authentication
- Role-based authorization
- Inter-microservice communication via Feign
- Service discovery with Eureka
- Centralized routing via API Gateway
- CORS handling at gateway level
- MySQL database integration
- Real-time data refresh

---

## 🎉 System Ready for Production

The Travel Package Booking System is now fully configured with:
- **Secure JWT Authentication** at API Gateway level
- **MySQL Database Integration** across all services
- **Service Discovery** with Eureka Server
- **Inter-service Communication** with Feign clients
- **Centralized CORS** configuration
- **Role-based Access Control** for admin functionality

**Start all services in the recommended order and enjoy your complete microservices travel booking system!**
