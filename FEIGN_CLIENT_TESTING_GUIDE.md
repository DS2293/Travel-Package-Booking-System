# Feign Client Integration Testing Guide

## Overview
This guide provides comprehensive testing procedures for the enhanced cross-service communication using Feign clients in the Travel Package Booking System.

## Service Dependencies

### Review Service Feign Clients
- **UserServiceClient**: Fetches user profile information for reviews
- **PackageServiceClient**: Retrieves package details for review context
- **BookingServiceClient**: Validates user has booked the package before allowing reviews

### Assistance Service Feign Clients  
- **UserServiceClient**: Gets user details for assistance request context
- **BookingServiceClient**: Fetches booking information for travel-related assistance

## Testing Procedures

### 1. Pre-Test Setup

#### Start All Services
```powershell
# Start services in this order:
# 1. Eureka Server (port 8761)
cd "Backend\eureka-server"
mvn spring-boot:run

# 2. User Service (port 8081)  
cd "Backend\user-service"
mvn spring-boot:run

# 3. Package Service (port 8082)
cd "Backend\package-service"  
mvn spring-boot:run

# 4. Booking Service (port 8083)
cd "Backend\booking-service"
mvn spring-boot:run

# 5. Payment Service (port 8084)
cd "Backend\payment-service"
mvn spring-boot:run

# 6. Insurance Service (port 8085)
cd "Backend\insurance-service"
mvn spring-boot:run

# 7. Assistance Service (port 8086)
cd "Backend\assistance-service"
mvn spring-boot:run

# 8. Review Service (port 8087)
cd "Backend\review-service"
mvn spring-boot:run

# 9. API Gateway (port 8080)
cd "Backend\api-gateway"
mvn spring-boot:run
```

#### Verify Service Registration
```powershell
# Check Eureka dashboard
Start-Process "http://localhost:8761"

# Verify all services are registered:
# - user-service
# - package-service  
# - booking-service
# - payment-service
# - insurance-service
# - assistance-service
# - review-service
# - api-gateway
```

### 2. Cross-Service Integration Tests

#### Test 1: Review Service Enhanced Responses

**Create Test Data:**
```powershell
# 1. Register a user
$userResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{
  "name": "Test User",
  "email": "test@example.com", 
  "password": "password123",
  "contactNumber": "+1-555-0123",
  "role": "customer"
}'

# 2. Login to get JWT token
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{
  "email": "test@example.com",
  "password": "password123"
}'

$token = $loginResponse.data.token

# 3. Create a package (as admin/agent)
$packageResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/packages" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body '{
  "name": "Test Package",
  "description": "Test Description",
  "destination": "Test Destination", 
  "duration": "5 days",
  "price": 1000.00,
  "availableSlots": 10
}'

# 4. Create a booking
$bookingResponse = Invoke-RestMethod -Uri "http://localhost:8083/api/bookings" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body '{
  "userId": 1,
  "packageId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05", 
  "numberOfTravelers": 2
}'
```

**Test Enhanced Review Creation:**
```powershell
# Create review (should include user and package details)
$reviewResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body '{
  "userID": 1,
  "packageID": 1,
  "rating": 5,
  "comment": "Excellent package!"
}'

# Verify response includes enhanced data:
# - userName: "Test User" 
# - userEmail: "test@example.com"
# - packageName: "Test Package"
# - packageDestination: "Test Destination"
# - packagePrice: 1000.00
```

**Test Review Validation:**
```powershell
# Try to create review without booking (should fail)
$invalidReviewResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body '{
  "userID": 1,
  "packageID": 999,
  "rating": 5,
  "comment": "Should fail - no booking exists"
}' -ErrorAction SilentlyContinue

# Should return error: "You can only review packages you have booked"
```

#### Test 2: Assistance Service Enhanced Responses

**Create Assistance Request:**
```powershell
$assistanceResponse = Invoke-RestMethod -Uri "http://localhost:8086/api/assistance" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body '{
  "userID": 1,
  "requestType": "EMERGENCY_CONTACT",
  "priority": "HIGH", 
  "subject": "Lost luggage",
  "message": "My luggage was lost at the airport"
}'

# Verify response includes enhanced data:
# - userName: "Test User"
# - userEmail: "test@example.com"  
# - userContactNumber: "+1-555-0123"
# - bookingId: 1
# - packageName: "Test Package"
# - packageDestination: "Test Destination"
# - travelStartDate, travelEndDate, bookingStatus
```

### 3. Service Availability Testing

#### Test Fallback Mechanisms

**Stop User Service:**
```powershell
# Stop user-service to test fallback
# Kill the user-service process

# Test review creation - should work but without user details
$reviewResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews" -Method Get

# Response should include:
# - Core review data
# - userName: null (service unavailable)
# - userEmail: null
# - Package details should still work if package-service is running
```

**Stop Package Service:**
```powershell
# Stop package-service to test fallback

# Test review retrieval - should work but without package details
$reviewResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews/package/1" -Method Get

# Response should include:
# - Core review data
# - User details (if user-service is running)
# - packageName: null (service unavailable)
# - packageDestination: null
```

### 4. Performance Testing

#### Test Response Times
```powershell
# Measure enhanced vs standard endpoint response times
$startTime = Get-Date

# Enhanced endpoint (with cross-service calls)
$enhancedResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews/1"

$enhancedTime = (Get-Date) - $startTime

# Compare with direct service calls
$startTime = Get-Date
$userResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/users/1"
$packageResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/packages/1"
$directTime = (Get-Date) - $startTime

Write-Output "Enhanced endpoint time: $enhancedTime"
Write-Output "Direct calls time: $directTime"
```

#### Load Testing
```powershell
# Test concurrent requests to enhanced endpoints
$jobs = @()
for ($i = 1; $i -le 10; $i++) {
    $jobs += Start-Job -ScriptBlock {
        Invoke-RestMethod -Uri "http://localhost:8087/api/reviews" -Method Get
    }
}

$results = $jobs | Receive-Job -Wait
Write-Output "Completed $($results.Count) concurrent requests"
```

### 5. Error Handling Tests

#### Test Invalid Service Responses
```powershell
# Test with invalid user ID in review
$errorResponse = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body '{
  "userID": 999,
  "packageID": 1,
  "rating": 5,
  "comment": "User does not exist"
}' -ErrorAction SilentlyContinue

# Should handle gracefully and log warning
```

#### Test Network Timeouts
```powershell
# Configure short timeout and test
$timeout = New-TimeSpan -Seconds 1
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8087/api/reviews/1" -TimeoutSec 1
} catch {
    Write-Output "Timeout handled gracefully: $($_.Exception.Message)"
}
```

## Expected Results

### ✅ Success Criteria

1. **Enhanced Data Integration**
   - Reviews include user and package information
   - Assistance requests include user and booking context
   - No N+1 query problems

2. **Fallback Mechanisms**
   - Services work even when dependencies are unavailable
   - Graceful degradation with null values for unavailable data
   - No cascade failures

3. **Performance**
   - Enhanced endpoints response time < 2 seconds under normal load
   - Service handles 10+ concurrent requests without issues
   - Proper error logging and monitoring

4. **Data Validation**
   - Review creation validates booking existence
   - Cross-service data consistency
   - Proper error messages for invalid requests

### ❌ Failure Indicators

1. **Service Failures**
   - Cascade failures when one service is down
   - Timeout exceptions without fallback
   - Data inconsistency between services

2. **Performance Issues**  
   - Response times > 5 seconds
   - Memory leaks or connection pool exhaustion
   - Service unavailable errors under load

3. **Data Issues**
   - Null pointer exceptions
   - Stale or incorrect cross-service data
   - Missing validation checks

## Troubleshooting

### Common Issues

1. **Feign Client Not Found**
   ```
   Error: No qualifying bean of type 'UserServiceClient'
   Solution: Ensure @EnableFeignClients annotation is present
   ```

2. **Connection Refused**
   ```
   Error: Connection refused to http://localhost:8081
   Solution: Verify target service is running and accessible
   ```

3. **Circuit Breaker Open**
   ```
   Error: Circuit breaker is open
   Solution: Wait for circuit breaker to reset or restart services
   ```

### Debug Commands

```powershell
# Check service health
Invoke-RestMethod -Uri "http://localhost:8081/actuator/health"
Invoke-RestMethod -Uri "http://localhost:8087/actuator/health"

# Check Eureka registration
Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps"

# View application logs
Get-Content -Tail 50 -Wait "Backend\review-service\logs\application.log"
```

---

## Conclusion

The Feign client integration provides seamless cross-service communication with proper fallback mechanisms. Regular testing ensures system reliability and performance under various conditions.

**Last Updated:** December 2024
