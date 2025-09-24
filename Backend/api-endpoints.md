# Travel Package Booking System - Complete API Endpoints Documentation

## Overview
This document provides a comprehensive list of all API endpoints in the Travel Package Booking System, organized by service with access control information and sample test data. The system now includes enhanced cross-service communication using Feign clients for improved data integration.

## Base URL
All endpoints are accessed through the API Gateway: `http://localhost:8080`

## Service Architecture
- **API Gateway**: 8080 (Entry point for all requests)
- **User Service**: 8081 (Authentication and user management)
- **Package Service**: 8082 (Travel package management)
- **Booking Service**: 8083 (Booking management with cross-service data)
- **Payment Service**: 8084 (Payment processing with booking integration)
- **Insurance Service**: 8085 (Insurance management with booking integration)
- **Assistance Service**: 8086 (Customer assistance with booking integration)
- **Review Service**: 8087 (Review management with user and package integration)

## Cross-Service Integration
The system utilizes Feign clients for inter-microservice communication, providing:
- Enhanced data responses with related service information
- Reduced frontend API calls
- Improved data consistency and user experience

## Authentication
- **JWT Token Required**: Include in Authorization header as `Bearer <token>`
- **User Headers**: Gateway adds `X-User-ID` and `X-User-Role` headers automatically

---

## 1. USER SERVICE (`/api/users`)

### Public Endpoints (No Authentication Required)

#### POST `/api/auth/register` - User Registration
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "contactNumber": "+1-555-0123",
  "role": "customer"
}
```

#### POST `/api/auth/login` - User Login
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

### User/Authenticated Endpoints

#### GET `/api/users/profile` - Get Current User Profile
- **Access**: Authenticated users only
- **Returns**: Current user's profile data

#### PUT `/api/users/profile` - Update Own Profile
```json
{
  "name": "John Updated",
  "email": "john.updated@example.com",
  "contactNumber": "+1-555-9999"
}
```

### Admin Only Endpoints

#### GET `/api/users` - Get All Users
- **Access**: Admin only
- **Query Params**: `role`, `approval`, `page`, `size`

#### GET `/api/users/{id}` - Get User by ID
- **Access**: Admin only

#### GET `/api/users/email/{email}` - Get User by Email
- **Access**: Admin only

#### GET `/api/users/role/{role}` - Get Users by Role
- **Access**: Admin only

#### GET `/api/users/pending-approval` - Get Pending Agent Approvals
- **Access**: Admin only

#### POST `/api/users` - Create User (Admin)
```json
{
  "name": "Admin Created User",
  "email": "admin.created@example.com",
  "password": "AdminPass123",
  "contactNumber": "+1-555-7777",
  "role": "agent"
}
```

#### PUT `/api/users/{id}` - Update User (Admin)
```json
{
  "name": "Admin Updated User",
  "email": "admin.updated@example.com",
  "contactNumber": "+1-555-8888",
  "role": "agent"
}
```

#### PUT `/api/users/{id}/approve` - Approve Agent
- **Access**: Admin only
- **Body**: Empty

#### DELETE `/api/users/{id}` - Delete User
- **Access**: Admin only

---

## 2. PACKAGE SERVICE (`/api/packages`)

### Public Endpoints

#### GET `/api/packages` - Get All Packages
- **Query Params**: `destination`, `minPrice`, `maxPrice`, `page`, `size`

#### GET `/api/packages/{id}` - Get Package by ID

#### GET `/api/packages/{id}/with-details` - Get Package with Booking Details

### Agent/Admin Endpoints

#### POST `/api/packages` - Create Package
```json
{
  "title": "Tropical Paradise Getaway",
  "description": "A 7-day tropical vacation with pristine beaches and luxury accommodations",
  "duration": "7 days",
  "price": 2499.99,
  "includedServices": "Round-trip flights, 5-star hotel, All meals, Water sports",
  "agentId": 1,
  "image": "https://example.com/maldives.jpg"
}
```

#### PUT `/api/packages/{id}` - Update Package
```json
{
  "title": "Updated Paradise Getaway",
  "description": "Updated description with new features",
  "duration": "7 days",
  "price": 2699.99,
  "includedServices": "Round-trip flights, 5-star hotel, All meals, Water sports, Sunset cruise",
  "agentId": 1,
  "image": "https://example.com/maldives-updated.jpg"
}
```

#### DELETE `/api/packages/{id}` - Delete Package
- **Access**: Agent (own packages) / Admin (all packages)

#### GET `/api/packages/agent/{agentId}` - Get Agent's Packages
- **Access**: Agent (own) / Admin (all)

#### GET `/api/packages/agent/{agentId}/statistics` - Get Agent Package Statistics
- **Access**: Agent (own) / Admin (all)

---

## 3. BOOKING SERVICE (`/api/bookings`)

### User Endpoints

#### POST `/api/bookings` - Create Booking
```json
{
  "userId": 1,
  "packageId": 1,
  "startDate": "2024-06-15",
  "endDate": "2024-06-22",
  "status": "PENDING"
}
```

#### GET `/api/bookings/user/{userId}` - Get User Bookings
- **Access**: User (own) / Admin (all)

#### GET `/api/bookings/user/{userId}/with-details` - Get User Bookings with Package Details
- **Access**: User (own) / Admin (all)

#### PUT `/api/bookings/{id}` - Update Booking
```json
{
  "startDate": "2024-06-20",
  "endDate": "2024-06-27",
  "status": "CONFIRMED"
}
```

#### PUT `/api/bookings/{id}/cancel` - Cancel Booking
- **Access**: User (own booking) / Admin (all)

### Agent Endpoints

#### GET `/api/bookings/package/{packageId}` - Get Package Bookings
- **Access**: Agent (own packages) / Admin (all)

#### GET `/api/bookings/agent/{agentId}/dashboard` - Get Agent Dashboard Data
- **Access**: Agent (own) / Admin (all)

### Admin Endpoints

#### GET `/api/bookings` - Get All Bookings
- **Query Params**: `status`, `packageId`, `userId`, `page`, `size`

#### GET `/api/bookings/{id}` - Get Booking by ID

#### DELETE `/api/bookings/{id}` - Delete Booking
- **Access**: Admin only

---

## 4. PAYMENT SERVICE (`/api/payments`)

### User Endpoints

#### POST `/api/payments` - Create Payment
```json
{
  "userId": 1,
  "bookingId": 1,
  "amount": 2499.99,
  "paymentMethod": "CREDIT_CARD",
  "status": "PENDING",
  "description": "Payment for Tropical Paradise Getaway booking",
  "cardLastFour": "1111"
}
```

#### GET `/api/payments/user/{userId}` - Get User Payments
- **Access**: User (own) / Admin (all)

#### GET `/api/payments/user/{userId}/with-details` - Get User Payments with Booking Details
- **Access**: User (own) / Admin (all)

#### GET `/api/payments/booking/{bookingId}` - Get Payment by Booking
- **Access**: User (if own booking) / Admin (all)

#### POST `/api/payments/process` - Process Payment
```json
{
  "userId": 1,
  "bookingId": 1,
  "amount": 2499.99,
  "paymentMethod": "CREDIT_CARD",
  "status": "PENDING",
  "description": "Payment for Tropical Paradise Getaway booking",
  "cardLastFour": "1111"
}
```

#### PUT `/api/payments/{id}` - Update Payment
```json
{
  "amount": 2699.99,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "description": "Updated payment information",
  "transactionId": "TXN123456789",
  "cardLastFour": "1111"
}
```

#### PUT `/api/payments/{id}/refund` - Refund Payment
- **Access**: User (own payments) / Admin (all)
- **Body**: Empty (no request body needed)
- **Note**: This endpoint automatically processes the refund

#### GET `/api/payments/{id}/with-details` - Get Payment with Details
- **Enhanced**: Returns payment with integrated booking and user details

### Admin Endpoints

#### GET `/api/payments` - Get All Payments
- **Query Params**: `status`, `method`, `userId`, `bookingId`, `page`, `size`

#### GET `/api/payments/{id}` - Get Payment by ID

#### PUT `/api/payments/{id}/status` - Update Payment Status (Admin)
```json
{
  "status": "COMPLETED",
  "transactionId": "TXN123456789",
  "description": "Payment processed successfully by admin"
}
```
- **Access**: Admin only
- **Note**: Allows admin to update payment status with optional transaction ID and description

#### DELETE `/api/payments/{id}` - Delete Payment
- **Access**: Admin only

---

## 5. INSURANCE SERVICE (`/api/insurance`)

### User Endpoints

#### GET `/api/insurance/types` - Get Insurance Types
- **Access**: Public

#### POST `/api/insurance/quote` - Get Insurance Quote
```json
{
  "userId": 1,
  "bookingId": 1,
  "policyType": "COMPREHENSIVE",
  "premium": 199.99,
  "coverageAmount": 50000,
  "startDate": "2024-06-15",
  "endDate": "2024-06-22",
  "status": "QUOTE"
}
```

#### POST `/api/insurance` - Purchase Insurance
```json
{
  "userId": 1,
  "bookingId": 1,
  "policyType": "COMPREHENSIVE",
  "policyNumber": "INS-2024-001234",
  "premium": 199.99,
  "coverageAmount": 50000,
  "startDate": "2024-06-15",
  "endDate": "2024-06-22",
  "status": "ACTIVE",
  "coverageDetails": "Comprehensive coverage including medical emergency, trip cancellation, and baggage loss"
}
```

#### GET `/api/insurance/user/{userId}` - Get User Insurance Policies
- **Access**: User (own) / Admin (all)

#### GET `/api/insurance/booking/{bookingId}` - Get Insurance by Booking
- **Access**: User (if own booking) / Admin (all)

#### POST `/api/insurance/{id}/claim` - File Insurance Claim
```json
{
  "status": "CLAIMED",
  "coverageDetails": "Medical emergency claim - Hospital bills and treatment costs covered under policy terms"
}
```

### Admin Endpoints

#### GET `/api/insurance` - Get All Insurance Policies
- **Query Params**: `type`, `status`, `userId`, `bookingId`, `page`, `size`

#### GET `/api/insurance/{id}` - Get Insurance Policy by ID

#### PUT `/api/insurance/{id}/status` - Update Insurance Status
```json
{
  "status": "ACTIVE",
  "coverageDetails": "Policy activated successfully with comprehensive coverage"
}
```

#### GET `/api/insurance/claims` - Get All Claims
- **Access**: Admin only

#### PUT `/api/insurance/claims/{id}/process` - Process Insurance Claim
```json
{
  "status": "CLAIMED",
  "coverageDetails": "Claim approved with partial coverage - Medical expenses covered up to policy limits"
}
```

---

## 6. ASSISTANCE SERVICE (`/api/assistance`)

### User Endpoints

#### POST `/api/assistance` - Create Assistance Request
```json
{
  "userID": 1,
  "subject": "Lost passport during travel",
  "message": "I lost my passport at the airport and need immediate assistance for replacement procedures. Currently at Maldives International Airport and need urgent help.",
  "issueDescription": "Lost passport - need replacement assistance",
  "priority": "high",
  "status": "pending"
}
```

#### GET `/api/assistance/user/{userId}` - Get User Assistance Requests
- **Access**: User (own) / Admin (all)
- **Enhanced**: Returns assistance requests with integrated user and booking details via Feign clients

#### PUT `/api/assistance/{id}` - Update Assistance Request
```json
{
  "message": "Updated description with more details - Currently at hotel lobby waiting for assistance",
  "priority": "urgent",
  "issueDescription": "Lost passport - located hotel contact for embassy assistance"
}
```

#### PUT `/api/assistance/{id}/status` - Update Assistance Request Status
```json
{
  "status": "in_progress"
}
```
- **Access**: User (own requests) / Admin (all)

### Admin Endpoints

#### GET `/api/assistance` - Get All Assistance Requests
- **Query Params**: `status`, `priority`, `type`, `userId`, `page`, `size`
- **Enhanced**: Returns assistance requests with comprehensive user and booking information

#### GET `/api/assistance/{id}` - Get Assistance Request by ID
- **Enhanced**: Returns request with integrated user profile and booking details

#### PUT `/api/assistance/{id}/resolve` - Resolve Assistance Request
```json
{
  "resolutionNote": "Passport replacement process initiated. New passport will be ready in 3 business days. Coordinated with embassy and provided temporary travel documents"
}
```

#### DELETE `/api/assistance/{id}` - Delete Assistance Request
- **Access**: Admin only

---

## 7. REVIEW SERVICE (`/api/reviews`)

### User Endpoints

#### POST `/api/reviews` - Create Review
```json
{
  "userID": 1,
  "packageID": 1,
  "rating": 5,
  "comment": "This was an incredible experience. The hotel was luxurious, food was excellent, and the staff was very helpful. Highly recommended!"
}
```

#### GET `/api/reviews/user/{userId}` - Get User Reviews
- **Access**: User (own) / Admin (all)
- **Enhanced**: Returns reviews with integrated user and package details via Feign clients

#### PUT `/api/reviews/{id}` - Update Review
```json
{
  "rating": 4,
  "comment": "Updated review after some reflection. Still a great experience overall."
}
```

### Agent Endpoints

#### POST `/api/reviews/{id}/reply` - Add Agent Reply to Review
```json
{
  "agentReply": "Thank you for your wonderful feedback! We're thrilled to hear you enjoyed your stay. We look forward to welcoming you back on your next adventure!"
}
```
- **Access**: Agent (for own packages) / Admin (all)

### Public Endpoints

#### GET `/api/reviews/package/{packageId}` - Get Package Reviews
- **Query Params**: `rating`, `page`, `size`, `sortBy`
- **Enhanced**: Returns reviews with integrated user details (name only) and agent replies

#### GET `/api/reviews/{id}` - Get Review by ID
- **Enhanced**: Returns review with integrated user, package, and booking details

### Admin Endpoints

#### GET `/api/reviews` - Get All Reviews
- **Query Params**: `rating`, `packageId`, `userId`, `flagged`, `page`, `size`
- **Enhanced**: Returns reviews with comprehensive cross-service data integration

#### DELETE `/api/reviews/{id}` - Delete Review
- **Access**: Admin only

---

## Enhanced Cross-Service Integration

### Overview
The Travel Package Booking System utilizes Feign clients for seamless inter-microservice communication. This provides enriched data responses and reduces the number of API calls required from the frontend.

### Key Integration Points

#### 1. Review Service Enhancements
- **User Integration**: Reviews include user profile information (name, email) via User Service Feign client
- **Package Integration**: Reviews include package details (name, destination, price) via Package Service Feign client
- **Booking Validation**: System validates that user has actually booked the package before allowing reviews via Booking Service Feign client

#### 2. Assistance Service Enhancements
- **User Integration**: Assistance requests include complete user profile information for better support
- **Booking Integration**: Requests include booking details (package info, travel dates, booking status) for context-aware assistance

#### 3. Enhanced Response Examples

**Enhanced Review Response:**
```json
{
  "success": true,
  "data": {
    "reviewID": 1,
    "rating": 5,
    "comment": "Wonderful experience...",
    "agentReply": "Thank you for your feedback!",
    "userID": 1,
    "packageID": 1,
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "packageName": "Tropical Paradise Getaway",
    "packageDestination": "Maldives",
    "packagePrice": 2499.99,
    "timestamp": "2024-06-20T10:30:00Z"
  }
}
```

**Enhanced Assistance Request Response:**
```json
{
  "success": true,
  "data": {
    "requestID": 1,
    "subject": "Lost passport during travel",
    "message": "Lost passport at airport...",
    "issueDescription": "Emergency passport replacement needed",
    "priority": "high",
    "status": "in_progress",
    "userID": 1,
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "userContactNumber": "+1-555-0123",
    "bookingId": 1,
    "packageName": "Tropical Paradise Getaway",
    "packageDestination": "Maldives",
    "travelStartDate": "2024-06-15",
    "travelEndDate": "2024-06-22",
    "bookingStatus": "CONFIRMED",
    "timestamp": "2024-06-18T08:00:00Z"
  }
}
```
      "travelEndDate": "2024-06-22",
      "bookingStatus": "CONFIRMED"
    }
  }
}
```

### Fallback Mechanisms
All Feign clients implement fallback mechanisms to ensure service availability:
- If a dependent service is unavailable, the response includes the core data with a note about unavailable related information
- Circuit breaker pattern prevents cascade failures
- Graceful degradation maintains system functionality

---

## Error Response Format

All endpoints return errors in the following format:

```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/endpoint"
}
```

## Success Response Format

All successful responses follow this format:

```json
{
  "success": true,
  "data": {
    // Response data object or array
  },
  "message": "Operation completed successfully"
}
```

---

## Testing Notes

1. **Authentication**: Always include JWT token in Authorization header for protected endpoints
2. **Content-Type**: Use `application/json` for all POST/PUT requests
3. **Date Format**: Use ISO 8601 format (YYYY-MM-DD) for dates
4. **File Uploads**: Use multipart/form-data for endpoints with file uploads
5. **Pagination**: Most list endpoints support `page` and `size` query parameters
6. **Filtering**: Use query parameters for filtering results

## Environment Setup

- **API Gateway**: http://localhost:8080
- **Eureka Server**: http://localhost:8761
- **Database**: MySQL on localhost:3306
- **Credentials**: username=root, password=mysql

---

*Last Updated: September 2025 - Fixed JSON samples to match actual database schema*

