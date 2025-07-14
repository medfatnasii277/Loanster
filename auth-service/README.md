# Auth Service

A robust authentication and authorization microservice built with Spring Boot for the LoanApp ecosystem. This service provides secure user registration, authentication, and JWT-based authorization with role-based access control.

## 🏗️ Architecture Overview

This is a stateless authentication microservice designed for a microservices architecture. It provides:

- **User Registration & Authentication**: Secure user onboarding and login
- **JWT Token Management**: Stateless authentication with JSON Web Tokens
- **Role-Based Access Control**: Support for BORROWER and OFFICER roles
- **Secure Password Handling**: BCrypt password encryption
- **API Security**: CORS configuration and security headers

## 🚀 Features

### Core Authentication Features
- ✅ User registration with email validation
- ✅ Secure password authentication with BCrypt
- ✅ JWT token generation with configurable expiration
- ✅ Token validation and role-based authorization
- ✅ Duplicate email prevention
- ✅ Role-based endpoint protection

### Security Features
- 🔐 Password encryption using BCrypt
- 🔑 JWT tokens with HMAC SHA256 signing
- 🛡️ CORS configuration for cross-origin requests
- 🚫 Protection against common security vulnerabilities
- ⏰ Configurable token expiration (default: 10 hours)

### API Documentation
- 📚 OpenAPI 3.0 (Swagger) documentation
- 🔍 Interactive API explorer at `/swagger-ui.html`

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL (Production), H2 (Testing)
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Documentation**: SpringDoc OpenAPI

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database (for production)
- Git

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd auth-service
```

### 2. Database Setup
Configure your PostgreSQL database and update the connection details in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/loanapp_auth
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. JWT Secret Configuration
Set your JWT secret key in `application.properties`:

```properties
jwt.secret=your_base64_encoded_secret_key
```

**Important**: Generate a secure Base64-encoded secret key for production:
```bash
echo -n "your-256-bit-secret-key-here" | base64
```

### 4. Build and Run
```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The service will start on port `4005` by default.

## 🌐 API Endpoints

### Authentication Endpoints

#### Register User
```http
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

**Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "userId": "uuid-here"
}
```

#### Login User
```http
POST /login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "jwt.token.here",
  "user": {
    "id": "uuid-here",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "BORROWER",
    "dateOfBirth": "1990-01-01"
  }
}
```

### Token Validation Endpoints

#### Validate Token
```http
GET /validate
Authorization: Bearer <jwt-token>
```

#### Validate Borrower Token
```http
GET /validate/borrower
Authorization: Bearer <jwt-token>
```

#### Validate Officer Token
```http
GET /validate/officer
Authorization: Bearer <jwt-token>
```

**Response**: `200 OK` for valid tokens, `401 Unauthorized` for invalid/expired tokens.

## 👥 User Roles

The system supports two user roles:

- **BORROWER**: Users who can apply for loans
- **OFFICER**: Staff members who can review and process loans

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run integration tests only
mvn test -Dtest=AuthIntegrationTest
```

### Test Structure
- **Unit Tests**: Testing individual components in isolation
- **Integration Tests**: End-to-end testing with real Spring context
- **Controller Tests**: Testing REST endpoints with MockMvc

### Test Coverage
The test suite covers:
- ✅ All service layer methods
- ✅ Controller endpoints and error handling
- ✅ JWT token generation and validation
- ✅ Role-based access control
- ✅ Input validation and edge cases
- ✅ Database operations
- ✅ Security configurations

## 🔒 Security Considerations

### Password Security
- Passwords are encrypted using BCrypt with default strength (10 rounds)
- Plain text passwords are never stored in the database
- Password validation enforces minimum length requirements

### JWT Security
- Tokens are signed using HMAC SHA256
- Configurable expiration time (default: 10 hours)
- Tokens include user email and role claims
- Invalid/expired tokens are properly rejected

### API Security
- CORS is configured to allow cross-origin requests
- Input validation on all endpoints
- Proper HTTP status codes for all scenarios
- Security headers are configured

## 📊 Monitoring & Health Checks

### Application Health
```http
GET /actuator/health
```

### Application Information
```http
GET /actuator/info
```

## 🐳 Docker Support

### Build Docker Image
```bash
docker build -t auth-service:latest .
```

### Run with Docker
```bash
docker run -p 4005:4005 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  -e JWT_SECRET=your_secret \
  auth-service:latest
```

## 🚀 Deployment

### Environment Variables
Set these environment variables for production:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/loanapp_auth
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=secure_base64_encoded_secret
SPRING_PROFILES_ACTIVE=prod
```

### Production Configuration
Create `application-prod.properties` for production-specific settings:

```properties
# Database
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Logging
logging.level.com.pm.authservice=INFO
logging.level.org.springframework.security=WARN

# Security
server.error.include-stacktrace=never
server.error.include-message=never
```

## 📈 Performance Considerations

- **Stateless Design**: No server-side session storage
- **Connection Pooling**: Configured for optimal database performance
- **Caching**: Consider adding Redis for token blacklisting in production
- **Load Balancing**: Service is stateless and can be horizontally scaled

## 🔧 Configuration

### Key Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Server port | 4005 |
| `jwt.secret` | JWT signing secret | Required |
| `spring.datasource.url` | Database URL | Required |
| `spring.jpa.hibernate.ddl-auto` | Database schema handling | create-drop |

## 🤝 Integration with Other Services

This auth service is designed to integrate with other microservices in the LoanApp ecosystem:

- **Loan Service**: Validates user tokens and roles for loan operations
- **User Management Service**: Shares user data and profile information
- **Notification Service**: Authenticates requests for sending notifications

### Token Usage in Other Services
Other services should validate tokens by calling:
```http
GET /validate/borrower  # For borrower-only endpoints
GET /validate/officer   # For officer-only endpoints
GET /validate          # For any authenticated user
```

## 🐛 Troubleshooting

### Common Issues

1. **Invalid JWT Secret**
   - Ensure the secret is Base64 encoded and at least 256 bits
   - Check environment variables are properly set

2. **Database Connection Issues**
   - Verify PostgreSQL is running and accessible
   - Check connection string format and credentials

3. **Token Validation Failures**
   - Ensure Authorization header format: `Bearer <token>`
   - Check token expiration time
   - Verify JWT secret matches between services

### Logging
Enable debug logging for troubleshooting:
```properties
logging.level.com.pm.authservice=DEBUG
logging.level.org.springframework.security=DEBUG
```

## 📞 Support

For issues and questions:
- Check the troubleshooting section above
- Review the API documentation at `/swagger-ui.html`
- Examine application logs for error details
- Verify configuration settings

## 🔄 Version History

### v1.0.0 (Current)
- Initial release with core authentication features
- JWT token management
- Role-based access control
- Comprehensive test suite
- Docker support
- API documentation

---

**Note**: This microservice is part of the LoanApp ecosystem and should be deployed alongside other required services for full functionality.
