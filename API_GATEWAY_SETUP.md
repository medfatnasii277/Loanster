# API Gateway Setup with Authentication

This document describes the API Gateway setup that provides authentication for the Borrower Service using the Auth Service.

## Overview

The API Gateway acts as a central entry point for all requests and implements authentication middleware that:
1. Intercepts all requests to `/api/borrowers/**` endpoints
2. Validates JWT tokens with the Auth Service
3. Ensures the token has the `BORROWER` role
4. Returns 401 Unauthorized for invalid/missing tokens

## Architecture

```
Client Request → API Gateway (Port 4000) → Auth Service Validation (Port 4005) → Borrower Service (Port 4001)
```

### Services Configuration

- **API Gateway**: Port 4000
- **Auth Service**: Container `loan-auth-service`, Port 4005
- **Borrower Service**: Container `loan-borrower-service`, Port 4001

## Changes Made

### 1. API Gateway Configuration (`api-gateway/src/main/resources/application.yml`)

- Updated routes to point to borrower service instead of patient service
- Configured auth service URL to `loan-auth-service:4005`
- Configured borrower service URL to `loan-borrower-service:4001`
- Added authentication filter for borrower endpoints

### 2. JWT Validation Filter (`api-gateway/src/main/java/com/pm/apigateway/filters/JwtValidationGatewayFilterFactory.java`)

- Updated to call `/validate/borrower` endpoint
- Added error handling for unauthorized responses
- Validates Bearer token format

### 3. Auth Service Enhancements

#### JWT Utility (`auth-service/src/main/java/com/pm/authservice/util/JwtUtil.java`)
- Added `extractRole()` method to get role from token
- Added `validateTokenAndCheckRole()` method for role-based validation

#### Auth Controller (`auth-service/src/main/java/com/pm/authservice/controller/AuthController.java`)
- Added `/validate/borrower` endpoint for BORROWER role validation

#### Auth Service (`auth-service/src/main/java/com/pm/authservice/service/AuthService.java`)
- Added `validateTokenWithRole()` method

### 4. Borrower Service Configuration
- Kept original port 4001 (no changes needed)

## API Endpoints

### Public Endpoints (No Authentication Required)
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token
- `GET /auth/validate` - Validate token (any role)
- `GET /auth/validate/borrower` - Validate token with BORROWER role

### Protected Endpoints (Require BORROWER Role)
- `GET /api/borrowers` - Get all borrowers
- `POST /api/borrowers` - Create borrower
- `GET /api/borrowers/{id}` - Get borrower by ID
- `DELETE /api/borrowers/{id}` - Delete borrower
- `POST /api/borrowers/{borrowerId}/loans` - Apply for loan
- `GET /api/borrowers/{borrowerId}/loans` - Get borrower's loans
- And all other borrower service endpoints...

## Authentication Flow

1. **User Registration/Login**
   ```bash
   # Register a BORROWER user
   POST /auth/register
   {
     "email": "borrower@example.com",
     "password": "password123",
     "role": "BORROWER",
     "fullName": "John Doe",
     "dateOfBirth": "1990-01-01"
   }
   
   # Login to get token
   POST /auth/login
   {
     "email": "borrower@example.com",
     "password": "password123"
   }
   ```

2. **Access Protected Endpoints**
   ```bash
   # Include Bearer token in Authorization header
   GET /api/borrowers
   Authorization: Bearer <jwt_token>
   ```

## Testing

Use the provided test script to verify the authentication flow:

```bash
./test-auth-flow.sh
```

This script will:
1. Register a new BORROWER user
2. Login to get a JWT token
3. Test accessing borrower endpoints with the token
4. Test accessing borrower endpoints without token (should fail)
5. Test direct auth service validation

## Docker Configuration

Ensure your Docker containers are configured with the correct service names and ports:

```yaml
services:
  api-gateway:
    ports:
      - "4000:4000"
  
  loan-auth-service:
    ports:
      - "4005:4005"
  
  loan-borrower-service:
    ports:
      - "4001:4001"
```

## Error Responses

### 401 Unauthorized
- Missing Authorization header
- Invalid Bearer token format
- Expired or invalid JWT token
- Token without BORROWER role

### 500 Internal Server Error
- Auth service unavailable
- Network connectivity issues

## Security Considerations

1. **JWT Secret**: Ensure the JWT secret is properly configured in the auth service
2. **Token Expiration**: Tokens expire after 10 hours
3. **Role-Based Access**: Only users with BORROWER role can access borrower endpoints
4. **HTTPS**: In production, use HTTPS for all communications

## Troubleshooting

1. **401 Errors**: Check if token is valid and has BORROWER role
2. **Connection Errors**: Verify all services are running and accessible
3. **Port Conflicts**: Ensure API Gateway uses port 4000, auth service uses port 4005, and borrower service uses port 4001
4. **Docker Network**: Verify services can communicate via Docker network 