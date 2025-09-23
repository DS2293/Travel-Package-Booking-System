# Travel Package Booking System - Frontend API Integration

## Overview

The frontend has been updated to integrate with backend microservices while maintaining backward compatibility with local data. The system automatically detects API availability and falls back to local data when APIs are not accessible.

## Architecture

### API Services Structure

The application is designed to work with 7 microservices:

1. **User Service** (`http://localhost:8081/api/users`)
2. **Package Service** (`http://localhost:8082/api/packages`)
3. **Booking Service** (`http://localhost:8083/api/bookings`)
4. **Payment Service** (`http://localhost:8084/api/payments`)
5. **Review Service** (`http://localhost:8085/api/reviews`)
6. **Insurance Service** (`http://localhost:8086/api/insurance`)
7. **Assistance Service** (`http://localhost:8087/api/assistance`)

### API Integration Features

- **Automatic Fallback**: If APIs are not available, the system uses local data
- **Visual Indicator**: Header shows whether the app is in "API Mode" 🌐 or "Local Mode" 🔧
- **Loading States**: Proper loading indicators during API calls
- **Error Handling**: Graceful error handling with user-friendly messages
- **Configuration**: Environment-based configuration for different deployment scenarios
- **Axios Integration**: Uses Axios for robust HTTP client functionality
- **Request/Response Interceptors**: Automatic auth token handling and error processing
- **Request Timeout**: Configurable timeout for all API calls (default: 10 seconds)
- **Enhanced Error Messages**: Detailed error messages with status codes and context

## API Endpoints Reference

### User Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/users` | Get all users | Admin Dashboard |
| GET | `/api/users/{id}` | Get user by ID | Profile, Dashboards |
| POST | `/api/users` | Create new user | Registration |
| PUT | `/api/users/{id}` | Update user | Profile, Admin |
| DELETE | `/api/users/{id}` | Delete user | Admin Dashboard |
| POST | `/api/users/login` | User login | Authentication |
| GET | `/api/users/role/{role}` | Get users by role | Admin Dashboard |

### Package Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/packages` | Get all packages | Packages Page, Dashboards |
| GET | `/api/packages/{id}` | Get package by ID | Package Details |
| GET | `/api/packages/agent/{agentId}` | Get packages by agent | Agent Dashboard |
| POST | `/api/packages` | Create package | Agent Dashboard |
| PUT | `/api/packages/{id}` | Update package | Agent Dashboard |
| DELETE | `/api/packages/{id}` | Delete package | Agent Dashboard |
| GET | `/api/packages/search` | Search packages | Packages Page |

### Booking Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/bookings` | Get all bookings | Admin Dashboard |
| GET | `/api/bookings/{id}` | Get booking by ID | Booking Details |
| GET | `/api/bookings/user/{userId}` | Get user bookings | User Dashboard |
| GET | `/api/bookings/package/{packageId}` | Get package bookings | Analytics |
| POST | `/api/bookings` | Create booking | User Dashboard, Packages |
| PUT | `/api/bookings/{id}` | Update booking | Dashboards |
| DELETE | `/api/bookings/{id}` | Delete booking | User Dashboard |
| PUT | `/api/bookings/{id}/cancel` | Cancel booking | User Dashboard |
| PUT | `/api/bookings/{id}/confirm` | Confirm booking | Agent Dashboard |

### Payment Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/payments` | Get all payments | Admin Dashboard |
| GET | `/api/payments/{id}` | Get payment by ID | Payment Details |
| GET | `/api/payments/user/{userId}` | Get user payments | User Dashboard |
| GET | `/api/payments/booking/{bookingId}` | Get booking payment | Booking Details |
| POST | `/api/payments` | Create payment | Payment Processing |
| PUT | `/api/payments/{id}` | Update payment | Payment Management |
| POST | `/api/payments/process` | Process payment | Checkout |
| PUT | `/api/payments/{id}/refund` | Refund payment | Admin Dashboard |

### Review Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/reviews` | Get all reviews | Reviews Page |
| GET | `/api/reviews/{id}` | Get review by ID | Review Details |
| GET | `/api/reviews/user/{userId}` | Get user reviews | Profile |
| GET | `/api/reviews/package/{packageId}` | Get package reviews | Package Details |
| POST | `/api/reviews` | Create review | Reviews Page |
| PUT | `/api/reviews/{id}` | Update review | Review Management |
| DELETE | `/api/reviews/{id}` | Delete review | Review Management |
| POST | `/api/reviews/{id}/reply` | Add agent reply | Agent Dashboard |

### Insurance Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/insurance` | Get all insurance | Admin Dashboard |
| GET | `/api/insurance/{id}` | Get insurance by ID | Insurance Details |
| GET | `/api/insurance/user/{userId}` | Get user insurance | User Dashboard |
| GET | `/api/insurance/booking/{bookingId}` | Get booking insurance | Booking Details |
| POST | `/api/insurance` | Create insurance | Booking Process |
| PUT | `/api/insurance/{id}` | Update insurance | Insurance Management |
| PUT | `/api/insurance/{id}/cancel` | Cancel insurance | User Dashboard |
| GET | `/api/insurance/types` | Get insurance types | Booking Process |

### Assistance Service Endpoints

| Method | Endpoint | Description | Used By |
|--------|----------|-------------|---------|
| GET | `/api/assistance` | Get all requests | Admin Dashboard |
| GET | `/api/assistance/{id}` | Get request by ID | Request Details |
| GET | `/api/assistance/user/{userId}` | Get user requests | Assistance Page |
| POST | `/api/assistance` | Create request | Assistance Page |
| PUT | `/api/assistance/{id}` | Update request | Admin Dashboard |
| DELETE | `/api/assistance/{id}` | Delete request | Admin Dashboard |
| PUT | `/api/assistance/{id}/status` | Update status | Admin Dashboard |
| PUT | `/api/assistance/{id}/resolve` | Resolve request | Admin Dashboard |

## Data Models

### Expected Request/Response Formats

#### User Model
```json
{
  "userId": 1,
  "name": "John Smith",
  "email": "john.smith@email.com",
  "password": "password123",
  "role": "customer",
  "contactNumber": "+1-555-0101",
  "approval": "approved",
  "registrationDate": "2024-01-01T00:00:00Z"
}
```

#### Travel Package Model
```json
{
  "packageId": 1,
  "title": "Paris Adventure",
  "description": "Explore the City of Light...",
  "duration": "5 days",
  "price": 1299.99,
  "includedServices": "Hotel, Flights, Guided Tours...",
  "agentId": 7,
  "image": "https://example.com/image.jpg"
}
```

#### Booking Model
```json
{
  "bookingId": 1,
  "userId": 1,
  "packageId": 1,
  "startDate": "2024-03-15",
  "endDate": "2024-03-20",
  "status": "confirmed",
  "paymentId": 1
}
```

#### Payment Model
```json
{
  "paymentId": 1,
  "userId": 1,
  "bookingId": 1,
  "amount": 1299.99,
  "status": "completed",
  "paymentMethod": "credit_card"
}
```

#### Review Model
```json
{
  "reviewId": 1,
  "userId": 1,
  "packageId": 1,
  "rating": 5,
  "comment": "Amazing experience!",
  "timestamp": "2024-03-25T10:30:00Z",
  "agentReply": "Thank you for your feedback!"
}
```

#### Insurance Model
```json
{
  "insuranceId": 1,
  "userId": 1,
  "bookingId": 1,
  "coverageDetails": "Comprehensive travel insurance...",
  "provider": "TravelSafe Insurance",
  "status": "active"
}
```

#### Assistance Request Model
```json
{
  "requestId": 1,
  "userId": 2,
  "issueDescription": "Need help with visa application",
  "priority": "high",
  "status": "pending",
  "resolutionTime": null,
  "timestamp": "2024-03-10T10:30:00Z"
}
```

## Testing Setup

### Direct Service Testing

The API is configured with direct service URLs for easy testing:

- **User Service**: `http://localhost:8081/api/users`
- **Package Service**: `http://localhost:8082/api/packages`
- **Booking Service**: `http://localhost:8083/api/bookings`
- **Payment Service**: `http://localhost:8084/api/payments`
- **Review Service**: `http://localhost:8085/api/reviews`
- **Insurance Service**: `http://localhost:8086/api/insurance`
- **Assistance Service**: `http://localhost:8087/api/assistance`

### Simple Configuration

- **Timeout**: 10 seconds for all requests
- **Auto Authentication**: JWT tokens automatically added to headers
- **Error Handling**: Simple and clear error messages
- **Content-Type**: Always `application/json`

### Testing with Dummy Data

To test a specific service:

1. Start one backend service (e.g., User Service on port 8081)
2. Create a simple endpoint that returns dummy data
3. The frontend will automatically detect and use the API
4. Check the header indicator - it will show 🌐 API Mode when connected

## Usage Examples

### Using the API

```javascript
import { api } from '../api';

// Simple API call
const handleAPICall = async () => {
  const result = await api.user.getAllUsers();
  if (result.success) {
    console.log(result.data);
  } else {
    console.error(result.error);
  }
};
```

### Testing Individual Services

```javascript
// Test User Service
const testUserService = async () => {
  const result = await api.user.getAllUsers();
  console.log('User Service:', result);
};

// Test Package Service  
const testPackageService = async () => {
  const result = await api.package.getAllPackages();
  console.log('Package Service:', result);
};
```

## Backend Implementation Requirements

For the backend services to work with this frontend, ensure:

1. **CORS Configuration**: Enable CORS for frontend origin
2. **Response Format**: All endpoints should return `{ data: ..., success: boolean, error?: string }`
3. **Error Handling**: Proper HTTP status codes and error messages
4. **Field Naming**: Use camelCase or match the expected field names
5. **Authentication**: Implement JWT or session-based authentication if required

## Testing

### Quick Testing Steps
1. Start any backend service on its designated port
2. The frontend automatically detects and connects to available APIs
3. Check the header - 🌐 means API connected, 🔧 means using local data
4. Test endpoints by using the app features

## Troubleshooting

### Common Issues

1. **CORS Errors**: Enable CORS in your backend service
2. **Connection Refused**: Check if backend service is running on the correct port
3. **Wrong Data Format**: Make sure your API returns `{ data: [...] }` format

### Simple Error Format

```javascript
{
  success: false,
  error: "Simple error message"
}
```

### Debug Tips

- Check browser console for API call logs
- Verify the header indicator shows correct mode (🌐 or 🔧)
- Test with simple GET endpoints first

## Future Enhancements

- Real-time updates using WebSockets
- Offline mode with data synchronization
- Advanced error retry mechanisms
- Request/response caching
- Performance monitoring and analytics 