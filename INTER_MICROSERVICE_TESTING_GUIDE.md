# Inter-Microservice Communication Testing Guide

## Overview
This guide documents the enhanced inter-microservice communication implemented using Feign clients in the Travel Package Booking System. The system now supports cross-service data fetching to provide enriched responses and reduce frontend complexity.

## Architecture Overview

### Services and Ports
- **API Gateway**: 8080
- **User Service**: 8081 
- **Package Service**: 8082
- **Booking Service**: 8083
- **Payment Service**: 8084
- **Insurance Service**: 8085

### Feign Client Relationships
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Booking       │───▶│   Package       │───▶│   User          │
│   Service       │    │   Service       │    │   Service       │
│   (8083)        │    │   (8082)        │    │   (8081)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       ▲
         │                       │                       │
         ▼                       ▼                       │
┌─────────────────┐    ┌─────────────────┐               │
│   Payment       │    │   Insurance     │───────────────┘
│   Service       │    │   Service       │
│   (8084)        │    │   (8085)        │
└─────────────────┘    └─────────────────┘
```

## Enhanced Endpoints

### 1. Booking Service Enhanced Endpoints

#### GET /api/bookings/user/{userId}/with-details
**Purpose**: Get user bookings with complete package information
**Response Structure**:
```json
{
  "success": true,
  "data": [
    {
      "booking": {
        "bookingId": 1,
        "userId": 1,
        "packageId": 1,
        "startDate": "2025-01-15",
        "endDate": "2025-01-22",
        "status": "confirmed"
      },
      "package": {
        "packageId": 1,
        "title": "Paris Adventure",
        "price": 1500.00,
        "duration": "7 days",
        "agentId": 2
      }
    }
  ]
}
```

#### GET /api/bookings/agent/{agentId}/dashboard
**Purpose**: Get complete dashboard data for agents
**Response Structure**:
```json
{
  "success": true,
  "data": {
    "packages": [...],
    "bookings": [...],
    "totalBookings": 15,
    "confirmedBookings": 10,
    "pendingBookings": 5
  }
}
```

### 2. Package Service Enhanced Endpoints

#### GET /api/packages/{id}/with-details
**Purpose**: Get package with booking statistics
**Response Structure**:
```json
{
  "success": true,
  "data": {
    "package": {...},
    "bookings": [...],
    "totalBookings": 5,
    "confirmedBookings": 3
  }
}
```

#### GET /api/packages/agent/{agentId}/with-stats
**Purpose**: Get agent packages with comprehensive statistics
**Response Structure**:
```json
{
  "success": true,
  "data": {
    "agent": {...},
    "packages": [...],
    "totalPackages": 8,
    "totalBookings": 25,
    "totalConfirmedBookings": 18
  }
}
```

### 3. Payment Service Enhanced Endpoints

#### GET /api/payments/user/{userId}/with-details
**Purpose**: Get user payments with booking information
**Response Structure**:
```json
{
  "success": true,
  "data": [
    {
      "payment": {...},
      "booking": {...}
    }
  ]
}
```

#### GET /api/payments/{id}/with-details
**Purpose**: Get payment with complete booking and user information

### 4. Insurance Service Enhanced Endpoints

#### GET /api/insurance/user/{userId}/with-details
**Purpose**: Get user insurance policies with booking details

#### GET /api/insurance/quotes/booking/{bookingId}
**Purpose**: Generate insurance quotes for a booking
**Response Structure**:
```json
{
  "success": true,
  "data": [
    {
      "policyType": "BASIC",
      "premium": 50.00,
      "coverageAmount": 10000.00,
      "benefits": ["Trip cancellation", "Medical emergencies", "Lost luggage"]
    },
    {
      "policyType": "PREMIUM",
      "premium": 100.00,
      "coverageAmount": 25000.00,
      "benefits": ["Trip cancellation", "Medical emergencies", "Lost luggage", "Trip delay", "Flight cancellation"]
    }
  ]
}
```

## Frontend Integration

### 1. UserDashboard Enhancements
- Uses `bookingService.getUserBookingsWithDetails()` for enriched booking data
- Integrates with enhanced payment processing
- Real-time data refresh after transactions

### 2. AgentDashboard Enhancements  
- Uses `bookingService.getAgentDashboardData()` for comprehensive dashboard
- Single API call replaces multiple service calls
- Improved performance and data consistency

### 3. AdminDashboard
- Already integrated with user service
- Backend integration for agent approvals
- Real-time user management

## Testing Scenarios

### 1. Cross-Service Data Fetching
```bash
# Test user bookings with package details
curl -X GET "http://localhost:8080/api/bookings/user/1/with-details" \
  -H "Authorization: Bearer <token>"

# Test agent dashboard data
curl -X GET "http://localhost:8080/api/bookings/agent/2/dashboard" \
  -H "Authorization: Bearer <token>"
```

### 2. Insurance Quote Generation
```bash
# Get insurance quotes for a booking
curl -X GET "http://localhost:8080/api/insurance/quotes/booking/1" \
  -H "Authorization: Bearer <token>"
```

### 3. Payment Processing with Cross-Service Updates
```bash
# Process payment (will update booking status)
curl -X POST "http://localhost:8080/api/payments/process" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": 1,
    "bookingId": 1,
    "amount": 1500.00,
    "paymentMethod": "CREDIT_CARD"
  }'
```

## Error Handling

### Circuit Breaker Pattern
- Feign clients implement fallback mechanisms
- Service degradation instead of complete failure
- Graceful error responses when services are unavailable

### Fallback Responses
```json
{
  "success": true,
  "data": {
    "booking": {...},
    "package": null,  // Service unavailable
    "error": "Package service temporarily unavailable"
  }
}
```

## Performance Considerations

### 1. Caching Strategy
- Implement Redis caching for frequently accessed data
- Cache user profiles, package details, and booking statistics

### 2. Async Processing
- Use @Async for non-critical cross-service calls
- Implement message queues for event-driven updates

### 3. Database Optimization
- Separate databases per service
- Optimize queries for cross-service data fetching

## Monitoring and Logging

### 1. Service Health Checks
```bash
# Check service availability
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Package Service
curl http://localhost:8083/actuator/health  # Booking Service
```

### 2. Distributed Tracing
- Implement Zipkin/Jaeger for request tracing
- Monitor cross-service call latency
- Identify bottlenecks in service communication

## Deployment Considerations

### 1. Service Discovery
- Eureka Server integration
- Dynamic service registration and discovery
- Load balancing across service instances

### 2. Configuration Management
- Centralized configuration with Spring Cloud Config
- Environment-specific Feign client URLs
- Circuit breaker configuration

## Next Steps

### 1. Additional Enhancements
- Implement event-driven architecture with message queues
- Add comprehensive caching layer
- Implement advanced monitoring and alerting

### 2. Security Enhancements
- Service-to-service authentication
- Request/response encryption
- API rate limiting per service

### 3. Performance Optimization
- Implement connection pooling for Feign clients
- Add request/response compression
- Database query optimization for cross-service data

## Conclusion

The inter-microservice communication implementation significantly reduces frontend complexity while maintaining service independence. The enhanced endpoints provide rich, contextual data that improves user experience and system performance.

Key benefits:
- ✅ Reduced frontend API calls
- ✅ Improved data consistency  
- ✅ Better error handling
- ✅ Enhanced user experience
- ✅ Maintainable service architecture
