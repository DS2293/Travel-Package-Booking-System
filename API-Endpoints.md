# API Endpoints - Travel Package Booking System

**Base URL:** `http://localhost:8080`

---

## 1. User Service

### Authentication
- `POST /api/auth/register` (User Registration)
- `POST /api/auth/login` (User Login)

### Profile Management
- `GET /api/users/profile` (View My Profile)
- `PUT /api/users/profile` (Update My Profile)

### Admin User Management
- `GET /api/users` (Admin Views All Users)
- `GET /api/users/{id}` (Admin Views User Details)
- `PUT /api/users/{id}` (Admin Updates User)
- `DELETE /api/users/{id}` (Admin Deletes User)
- `PUT /api/users/{id}/approve` (Admin Approves Agent)
- `GET /api/users/pending-approval` (Admin Views Pending Agents)

---

## 2. Package Service

### Package Browsing
- `GET /api/packages` (Browse Available Packages)
- `GET /api/packages/{id}` (View Package Details)

### Agent Package Management
- `POST /api/packages` (Agent Creates Package)
- `PUT /api/packages/{id}` (Agent Updates Package)
- `DELETE /api/packages/{id}` (Agent Deletes Package)
- `GET /api/packages/agent/{agentId}` (Agent Views My Packages)

---

## 3. Booking Service

### User Booking Management
- `POST /api/bookings` (User Books Package)
- `GET /api/bookings/user/{userId}` (User Views My Bookings)
- `PUT /api/bookings/{id}` (User Updates Booking)
- `PUT /api/bookings/{id}/cancel` (User Cancels Booking)

### Agent Booking Overview
- `GET /api/bookings/package/{packageId}` (Agent Views Package Bookings)

### Admin Booking Management
- `GET /api/bookings` (Admin Views All Bookings)
- `GET /api/bookings/{id}` (Admin Views Booking Details)

---

## 4. Payment Service

### User Payment Management
- `POST /api/payments` (User Makes Payment)
- `GET /api/payments/user/{userId}` (User Views Payment History)
- `GET /api/payments/booking/{bookingId}` (User Views Booking Payment)

### Admin Payment Management
- `GET /api/payments` (Admin Views All Payments)
- `PUT /api/payments/{id}/status` (Admin Updates Payment Status)

---

## 5. Insurance Service

### Insurance Management
- `GET /api/insurance/types` (View Insurance Options)
- `POST /api/insurance/quote` (Get Insurance Quote)
- `POST /api/insurance` (Purchase Insurance)
- `GET /api/insurance/user/{userId}` (User Views My Policies)

### Claims Management
- `POST /api/insurance/{id}/claim` (File Insurance Claim)
- `GET /api/insurance/claims` (Admin Views All Claims)

---

## 6. Review Service

### Review Management
- `POST /api/reviews` (User Writes Review)
- `GET /api/reviews/package/{packageId}` (View Package Reviews)
- `GET /api/reviews/user/{userId}` (User Views My Reviews)
- `PUT /api/reviews/{id}` (User Updates Review)

### Admin Review Management
- `GET /api/reviews` (Admin Views All Reviews)
- `DELETE /api/reviews/{id}` (Admin Deletes Review)

---

## 7. Assistance Service

### User Support
- `POST /api/assistance` (User Requests Help)
- `GET /api/assistance/user/{userId}` (User Views My Requests)
- `PUT /api/assistance/{id}` (User Updates Request)

### Admin Support Management
- `GET /api/assistance` (Admin Views All Requests)
- `PUT /api/assistance/{id}/assign` (Admin Assigns Support Staff)
- `PUT /api/assistance/{id}/resolve` (Admin Resolves Request)

---

## Authentication Required
All endpoints except registration and login require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Access Levels
- **Public**: Registration, Login, Browse Packages, View Reviews
- **User**: Profile management, Booking, Payment, Insurance, Reviews, Support requests
- **Agent**: Package management, View package bookings
- **Admin**: All user/agent operations, System management

---

*Total Active Endpoints: 35*