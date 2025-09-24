# Travel Package Booking System - Admin API Routes Documentation

This document contains all admin-only routes across all microservices in the Travel Package Booking System.

## Table of Contents
- [Authentication & Authorization](#authentication--authorization)
- [User Service Admin Routes](#user-service-admin-routes)
- [Package Service Admin Routes](#package-service-admin-routes)
- [Booking Service Admin Routes](#booking-service-admin-routes)
- [Payment Service Admin Routes](#payment-service-admin-routes)
- [Insurance Service Admin Routes](#insurance-service-admin-routes)
- [API Gateway Configuration](#api-gateway-configuration)

---

## Authentication & Authorization

All admin routes require:
- **Bearer Token**: Valid JWT token in Authorization header
- **Admin Role**: User must have `role: "admin"` in JWT claims
- **Access via API Gateway**: All requests must go through API Gateway at `http://localhost:8080`

### Headers Required:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## User Service Admin Routes

**Base URL**: `http://localhost:8080/api/users`

### 1. Get All Users
- **Method**: `GET`
- **Endpoint**: `/api/users`
- **Access**: Admin Only
- **Description**: Retrieve all users in the system
- **Response**: List of UserDto objects

### 2. Get User by ID
- **Method**: `GET`
- **Endpoint**: `/api/users/{id}`
- **Access**: Admin Only
- **Description**: Retrieve specific user by ID
- **Parameters**: 
  - `id` (Long): User ID
- **Response**: UserDto object

### 3. Get User by Email
- **Method**: `GET`
- **Endpoint**: `/api/users/email/{email}`
- **Access**: Admin Only
- **Description**: Retrieve user by email address
- **Parameters**: 
  - `email` (String): User email
- **Response**: UserDto object

### 4. Get Users by Role
- **Method**: `GET`
- **Endpoint**: `/api/users/role/{role}`
- **Access**: Admin Only
- **Description**: Retrieve all users with specific role
- **Parameters**: 
  - `role` (String): User role (user, agent, admin)
- **Response**: List of UserDto objects

### 5. Create User
- **Method**: `POST`
- **Endpoint**: `/api/users`
- **Access**: Admin Only
- **Description**: Create a new user
- **Request Body**: UserDto object
- **Response**: Created UserDto object

### 6. Update User
- **Method**: `PUT`
- **Endpoint**: `/api/users/{id}`
- **Access**: Admin Only
- **Description**: Update existing user
- **Parameters**: 
  - `id` (Long): User ID
- **Request Body**: UserDto object
- **Response**: Updated UserDto object

### 7. Delete User
- **Method**: `DELETE`
- **Endpoint**: `/api/users/{id}`
- **Access**: Admin Only
- **Description**: Delete user from system
- **Parameters**: 
  - `id` (Long): User ID
- **Response**: Success message

### 8. Get Pending Approvals
- **Method**: `GET`
- **Endpoint**: `/api/users/pending-approvals`
- **Access**: Admin Only
- **Description**: Retrieve all users pending approval (agents waiting for admin approval)
- **Response**: List of UserDto objects

### 9. Approve User
- **Method**: `PUT`
- **Endpoint**: `/api/users/{id}/approve`
- **Access**: Admin Only
- **Description**: Approve a pending user (typically agents)
- **Parameters**: 
  - `id` (Long): User ID
- **Response**: Approved UserDto object

---

## Package Service Admin Routes

**Base URL**: `http://localhost:8080/api/packages`

### Note on Package Service
The Package Service routes are **Agent/Admin** routes, meaning both agents and admins can access them. Based on API Gateway configuration, these routes require agent or admin role:

### 1. Create Package
- **Method**: `POST`
- **Endpoint**: `/api/packages`
- **Access**: Agent/Admin Only
- **Description**: Create a new travel package
- **Request Body**: TravelPackageDto object
- **Response**: Created TravelPackageDto object

### 2. Update Package
- **Method**: `PUT`
- **Endpoint**: `/api/packages/{id}`
- **Access**: Agent/Admin Only
- **Description**: Update existing travel package
- **Parameters**: 
  - `id` (Long): Package ID
- **Request Body**: TravelPackageDto object
- **Response**: Updated TravelPackageDto object

### 3. Delete Package
- **Method**: `DELETE`
- **Endpoint**: `/api/packages/{id}`
- **Access**: Agent/Admin Only
- **Description**: Delete travel package
- **Parameters**: 
  - `id` (Long): Package ID
- **Response**: Success message

### 4. Get Agent Packages with Statistics
- **Method**: `GET`
- **Endpoint**: `/api/packages/agent/{agentId}/with-stats`
- **Access**: Agent/Admin Only
- **Description**: Get packages for specific agent with booking statistics
- **Parameters**: 
  - `agentId` (Long): Agent ID
- **Response**: Agent packages with statistics

---

## Booking Service Admin Routes

**Base URL**: `http://localhost:8080/api/bookings`

### Note on Booking Service
The Booking Service provides admin oversight capabilities through general routes that admins can access:

### 1. Get All Bookings
- **Method**: `GET`
- **Endpoint**: `/api/bookings`
- **Access**: All authenticated users (Admin can see all)
- **Description**: Retrieve all bookings (admin sees all, users see only their own)
- **Response**: List of BookingDto objects

### 2. Get Booking by ID
- **Method**: `GET`
- **Endpoint**: `/api/bookings/{id}`
- **Access**: All authenticated users (Admin can see any)
- **Description**: Retrieve specific booking by ID
- **Parameters**: 
  - `id` (Long): Booking ID
- **Response**: BookingDto object

### 3. Update Booking
- **Method**: `PUT`
- **Endpoint**: `/api/bookings/{id}`
- **Access**: All authenticated users (Admin can update any)
- **Description**: Update booking details
- **Parameters**: 
  - `id` (Long): Booking ID
- **Request Body**: BookingDto object
- **Response**: Updated BookingDto object

### 4. Delete Booking
- **Method**: `DELETE`
- **Endpoint**: `/api/bookings/{id}`
- **Access**: All authenticated users (Admin can delete any)
- **Description**: Delete booking
- **Parameters**: 
  - `id` (Long): Booking ID
- **Response**: Success message

### 5. Confirm Booking
- **Method**: `PUT`
- **Endpoint**: `/api/bookings/{id}/confirm`
- **Access**: All authenticated users (Admin can confirm any)
- **Description**: Confirm a booking
- **Parameters**: 
  - `id` (Long): Booking ID
- **Response**: Confirmed BookingDto object

---

## Payment Service Admin Routes

**Base URL**: `http://localhost:8080/api/payments`

### Note on Payment Service
Payment Service routes are accessible to all authenticated users, but admins have oversight capabilities:

### 1. Get All Payments
- **Method**: `GET`
- **Endpoint**: `/api/payments`
- **Access**: All authenticated users (Admin can see all)
- **Description**: Retrieve all payments in system
- **Response**: List of PaymentDto objects

### 2. Get Payment by ID
- **Method**: `GET`
- **Endpoint**: `/api/payments/{id}`
- **Access**: All authenticated users (Admin can see any)
- **Description**: Retrieve specific payment by ID
- **Parameters**: 
  - `id` (Long): Payment ID
- **Response**: PaymentDto object

### 3. Update Payment
- **Method**: `PUT`
- **Endpoint**: `/api/payments/{id}`
- **Access**: All authenticated users (Admin can update any)
- **Description**: Update payment details
- **Parameters**: 
  - `id` (Long): Payment ID
- **Request Body**: PaymentDto object
- **Response**: Updated PaymentDto object

### 4. Refund Payment
- **Method**: `PUT`
- **Endpoint**: `/api/payments/{id}/refund`
- **Access**: All authenticated users (Admin can refund any)
- **Description**: Process payment refund
- **Parameters**: 
  - `id` (Long): Payment ID
- **Response**: Refunded PaymentDto object

### 5. Delete Payment
- **Method**: `DELETE`
- **Endpoint**: `/api/payments/{id}`
- **Access**: All authenticated users (Admin can delete any)
- **Description**: Delete payment record
- **Parameters**: 
  - `id` (Long): Payment ID
- **Response**: Success message

---

## Insurance Service Admin Routes

**Base URL**: `http://localhost:8080/api/insurance`

### Note on Insurance Service
Insurance Service routes are accessible to all authenticated users, but admins have oversight capabilities:

### 1. Get All Insurance
- **Method**: `GET`
- **Endpoint**: `/api/insurance`
- **Access**: All authenticated users (Admin can see all)
- **Description**: Retrieve all insurance policies in system
- **Response**: List of InsuranceDto objects

### 2. Get Insurance by ID
- **Method**: `GET`
- **Endpoint**: `/api/insurance/{id}`
- **Access**: All authenticated users (Admin can see any)
- **Description**: Retrieve specific insurance policy by ID
- **Parameters**: 
  - `id` (Long): Insurance ID
- **Response**: InsuranceDto object

### 3. Update Insurance
- **Method**: `PUT`
- **Endpoint**: `/api/insurance/{id}`
- **Access**: All authenticated users (Admin can update any)
- **Description**: Update insurance policy details
- **Parameters**: 
  - `id` (Long): Insurance ID
- **Request Body**: InsuranceDto object
- **Response**: Updated InsuranceDto object

### 4. Cancel Insurance
- **Method**: `PUT`
- **Endpoint**: `/api/insurance/{id}/cancel`
- **Access**: All authenticated users (Admin can cancel any)
- **Description**: Cancel insurance policy
- **Parameters**: 
  - `id` (Long): Insurance ID
- **Response**: Cancelled InsuranceDto object

### 5. Renew Insurance
- **Method**: `PUT`
- **Endpoint**: `/api/insurance/{id}/renew`
- **Access**: All authenticated users (Admin can renew any)
- **Description**: Renew insurance policy
- **Parameters**: 
  - `id` (Long): Insurance ID
- **Response**: Renewed InsuranceDto object

---

## API Gateway Configuration

### Admin-Only Route Patterns (Enforced at Gateway Level)

The API Gateway enforces the following admin-only route patterns:

1. **GET** `/api/users` - List all users
2. **DELETE** `/api/users/{id}` - Delete user
3. **PUT** `/api/users/{id}/approve` - Approve agent
4. Routes starting with `/api/packages/admin/` (if implemented)
5. Routes starting with `/api/bookings/admin/` (if implemented)
6. Routes containing `/statistics` or `/reports` (if implemented)

### Agent/Admin Route Patterns

1. **POST** `/api/packages` - Create package
2. **PUT** `/api/packages/{id}` - Update package
3. **DELETE** `/api/packages/{id}` - Delete package
4. Routes starting with `/api/bookings/agent/`
5. Routes starting with `/api/packages/agent/`

### JWT Claims Required

For admin access, JWT token must contain:
```json
{
  "sub": "admin@example.com",
  "role": "admin",
  "userId": 1,
  "exp": 1640995200
}
```

### Error Responses

#### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header"
}
```

#### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

---

## Security Notes

1. **All admin routes are protected by JWT authentication**
2. **API Gateway validates JWT tokens and role-based access**
3. **User context is passed to downstream services via headers**
4. **CORS is configured at API Gateway level only**
5. **All requests must go through API Gateway (port 8080)**

---

## Database Configuration

All services use MySQL with the following configuration:
- **Database Host**: localhost:3306
- **Username**: root
- **Password**: mysql
- **Auto-create databases**: Enabled

Individual service databases:
- User Service: `travel_booking_users`
- Package Service: `travel_package_db`
- Booking Service: `travel_booking_db`
- Payment Service: `travel_payment_db`
- Insurance Service: `travel_insurance_db`
- Assistance Service: `travel_assistance_db`
- Review Service: `travel_review_db`

---

*Last Updated: $(date)*
*Generated from microservice controllers analysis*
