# Auth Service

A Spring Boot authentication service with JWT token-based authentication.

## Features

- User registration with role-based access
- User login with JWT token generation
- Token validation
- Two user roles: OFFICER and BORROWER

## User Roles

- **OFFICER**: Administrative role with full access
- **BORROWER**: Standard user role

## API Endpoints

### Register User
```
POST /register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "role": "BORROWER",
  "fullName": "John Doe",
  "dateOfBirth": "1990-01-01"
}
```

Response:
```json
{
  "message": "User registered successfully",
  "userId": "uuid-here"
}
```

### Login
```
POST /login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "token": "jwt-token-here"
}
```

### Validate Token
```
GET /validate
Authorization: Bearer <jwt-token>
```

## User Model

The User entity includes:
- `id`: UUID primary key
- `email`: Unique email address
- `password`: Encrypted password
- `role`: User role (OFFICER or BORROWER)
- `fullName`: User's full name
- `dateOfBirth`: User's date of birth

## Configuration

Update `application.properties` with your JWT secret:
```properties
jwt.secret=your-256-bit-secret-key-here-make-it-long-enough-for-hmac-sha256
```

## Running the Application

1. Ensure you have Java 17+ installed
2. Run: `./mvnw spring-boot:run`
3. The service will start on port 4005

## Database

The application uses H2 in-memory database by default. The `data.sql` file creates initial test data:
- Email: testuser@test.com
- Password: (encoded)
- Role: OFFICER
- Full Name: Test User
- Date of Birth: 1990-01-01 