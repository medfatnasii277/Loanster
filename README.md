# LoanApp - Enterprise Loan Management System

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-2.8+-orange.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-20+-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Event-Driven Architecture](#event-driven-architecture)
- [Security](#security)
- [Deployment](#deployment)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Contributing](#contributing)
- [License](#license)

## 🏢 Overview

LoanApp is a comprehensive, enterprise-grade loan management system built with microservices architecture. It provides end-to-end loan processing capabilities including borrower management, loan applications, document handling, officer administration, and real-time event-driven communication.

### Key Capabilities

- **Multi-tenant Loan Processing**: Complete loan lifecycle management
- **Real-time Status Updates**: Event-driven architecture with Apache Kafka
- **Secure API Gateway**: JWT-based authentication and authorization
- **Document Management**: File upload and verification workflows
- **Officer Administration**: Administrative tools for loan officers
- **Audit Trail**: Complete tracking of all loan status changes
- **Scalable Architecture**: Microservices with independent scaling

## 🏗️ Architecture

```mermaid
graph TB
    Client[Web/Mobile Client] --> Gateway[API Gateway :4000]
    
    Gateway --> Auth[Auth Service :4005]
    Gateway --> Borrower[Borrower Service :4001]
    Gateway --> Officer[Officer Service :8083]
    
    Officer --> Kafka[Apache Kafka]
    Kafka --> Borrower
    
    Auth --> AuthDB[(Auth Database)]
    Borrower --> BorrowerDB[(Borrower Database)]
    Officer --> OfficerDB[(Officer Database)]
    
    subgraph "Event Topics"
        Kafka --> LoanTopic[loan-status]
        Kafka --> DocTopic[documents-status]
    end
    
    subgraph "Security Layer"
        JWT[JWT Tokens]
        RBAC[Role-Based Access]
    end
```

### Service Communication

1. **Synchronous Communication**: REST APIs via API Gateway
2. **Asynchronous Communication**: Kafka event streaming
3. **Security**: JWT tokens validated at API Gateway
4. **Data Consistency**: Event-driven eventual consistency

## 🚀 Services

### 1. API Gateway (Port 4000)
**Purpose**: Central entry point with authentication and routing

**Responsibilities**:
- Request routing to appropriate services
- JWT token validation
- Role-based access control
- Rate limiting and load balancing
- Request/response logging

**Technology**: Spring Cloud Gateway

### 2. Auth Service (Port 4005)
**Purpose**: Authentication and authorization management

**Responsibilities**:
- User registration and login
- JWT token generation and validation
- Role-based authorization (BORROWER, OFFICER, ADMIN)
- Session management
- Password security

**Technology**: Spring Boot, Spring Security, JWT

### 3. Borrower Service (Port 4001)
**Purpose**: Borrower and loan application management

**Responsibilities**:
- Borrower profile management
- Loan application processing
- Document upload and management
- Loan calculations and eligibility
- Status tracking and history
- Kafka event consumption

**Technology**: Spring Boot, JPA/Hibernate, PostgreSQL, Apache Kafka

### 4. Officer Service (Port 8083)
**Purpose**: Administrative tools for loan officers

**Responsibilities**:
- Loan application review and approval
- Document verification
- Status updates and notifications
- Audit trail management
- Kafka event publishing
- Officer dashboard and reporting

**Technology**: Spring Boot, JPA/Hibernate, PostgreSQL, Apache Kafka, Protobuf

## ✨ Features

### Core Features

#### 🏦 Loan Management
- **Application Processing**: Complete loan application lifecycle
- **Multiple Loan Types**: Personal, auto, mortgage, business loans
- **Automated Calculations**: Interest rates, monthly payments, amortization
- **Eligibility Assessment**: Credit scoring and risk evaluation
- **Status Tracking**: Real-time status updates across services

#### 👤 Borrower Management
- **Profile Management**: Comprehensive borrower profiles
- **Document Upload**: Secure document storage and verification
- **Application History**: Complete loan application history
- **Dashboard**: Personal loan dashboard with status tracking
- **Notifications**: Real-time status change notifications

#### 👨‍💼 Officer Administration
- **Application Review**: Officer tools for application assessment
- **Bulk Operations**: Batch processing of applications
- **Document Verification**: Document review and approval workflows
- **Reporting**: Comprehensive reporting and analytics
- **Audit Trail**: Complete audit logging of all actions

#### 🔒 Security & Compliance
- **Multi-factor Authentication**: JWT + Role-based access
- **Data Encryption**: End-to-end encryption for sensitive data
- **Audit Logging**: Complete audit trail for compliance
- **GDPR Compliance**: Data privacy and protection measures
- **PCI DSS**: Payment card industry compliance

### Advanced Features

#### 📡 Real-time Event Processing
- **Status Synchronization**: Real-time status updates across services
- **Event Sourcing**: Complete event history for audit purposes
- **Message Queuing**: Reliable message delivery with Kafka
- **Dead Letter Queues**: Error handling and message recovery

#### 📊 Analytics & Reporting
- **Loan Analytics**: Application trends and approval rates
- **Performance Metrics**: Service performance monitoring
- **Business Intelligence**: Executive dashboards and reports
- **Risk Analytics**: Credit risk assessment and monitoring

## 🛠️ Technology Stack

### Backend Services
- **Java 17+**: Modern Java features and performance
- **Spring Boot 3.5.3**: Enterprise application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database abstraction layer
- **Spring Cloud Gateway**: API gateway and routing

### Database & Storage
- **PostgreSQL 13+**: Primary database for all services
- **H2 Database**: In-memory database for testing
- **File Storage**: Local file system with cloud storage support

### Message Streaming
- **Apache Kafka 2.8+**: Event streaming platform
- **Protobuf**: Efficient serialization for Kafka messages
- **Schema Registry**: Schema evolution and compatibility

### Development & Operations
- **Maven 3.8+**: Build automation and dependency management
- **Docker & Docker Compose**: Containerization and orchestration
- **Swagger/OpenAPI**: API documentation and testing
- **SLF4J + Logback**: Structured logging



## 📋 Prerequisites

### Development Environment
- **Java Development Kit (JDK) 17 or higher**
- **Apache Maven 3.8 or higher**
- **Docker 20.10 or higher**
- **Docker Compose 2.0 or higher**
- **Git 2.30 or higher**

### Infrastructure Requirements
- **Memory**: Minimum 8GB RAM (16GB recommended)
- **Storage**: Minimum 10GB free disk space
- **Network**: Internet connectivity for dependency downloads

### Development Tools (Recommended)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Database Client**: pgAdmin, DBeaver, or DataGrip
- **API Testing**: VS Code with REST Client extension (see `api-tests.http`)
- **Kafka Client**: Kafka Tool, Conduktor, or CLI tools

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/your-org/loanapp.git
cd loanapp
```

### 2. Start Infrastructure Services
```bash
# You need to manually start Kafka, Zookeeper, and PostgreSQL
# Option 1: Install locally and start services
# Option 2: Use Docker containers manually

# Example: Start Kafka and Zookeeper with Docker
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest
docker run -d --name kafka -p 9092:9092 \
  --link zookeeper \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:latest

# Example: Start PostgreSQL with Docker
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_DB=loanapp \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  postgres:13
```

### 3. Build All Services
```bash
# Build all microservices
./mvnw clean install -DskipTests

# Or build individual services
cd auth-service && ./mvnw clean install
cd ../borrower-service && ./mvnw clean install
cd ../officer-service && ./mvnw clean install
cd ../api-gateway && ./mvnw clean install
```

### 4. Start Services in Order
```bash
# Terminal 1: Start Auth Service
cd auth-service
./mvnw spring-boot:run

# Terminal 2: Start Borrower Service
cd borrower-service
./mvnw spring-boot:run

# Terminal 3: Start Officer Service
cd officer-service
./mvnw spring-boot:run

# Terminal 4: Start API Gateway
cd api-gateway
./mvnw spring-boot:run
```

### 5. Verify Installation
```bash
# Check service health
curl http://localhost:4000/actuator/health  # API Gateway
curl http://localhost:4005/actuator/health  # Auth Service
curl http://localhost:4001/actuator/health  # Borrower Service
curl http://localhost:8083/actuator/health  # Officer Service
```

### 6. Run API Tests
Use the provided `api-tests.http` file in VS Code or your preferred REST client to test all endpoints:
- Authentication flows
- Borrower management
- Loan applications  
- Document uploads
- Officer operations
- Event-driven status updates

### 7. Verify Event Processing
Check that Kafka events are being processed correctly:
- Submit a loan application via borrower service
- Update its status via officer service  
- Verify the status change appears in borrower service
- Check service logs for event processing confirmation

## ⚙️ Configuration

### Environment Variables

#### Database Configuration
```bash
# PostgreSQL Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=loanapp
DB_USERNAME=postgres
DB_PASSWORD=password
```

#### Kafka Configuration
```bash
# Apache Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ZOOKEEPER_CONNECT=localhost:2181
```

#### Service Ports
```bash
# Service Ports
API_GATEWAY_PORT=4000
BORROWER_SERVICE_PORT=4001
AUTH_SERVICE_PORT=4005
OFFICER_SERVICE_PORT=8083
```

#### Security Configuration
```bash
# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

### Application Properties

Each service can be configured via `application.properties` or `application.yml`:

```yaml
# Example: borrower-service/src/main/resources/application.yml
server:
  port: 4001

spring:
  application:
    name: borrower-service
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:loanapp}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: borrower-service-group
```

## 📚 API Documentation

### Authentication Flow

#### 1. User Registration
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "role": "BORROWER"
}
```

#### 2. User Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "SecurePassword123!"
}

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john.doe",
  "role": "BORROWER"
}
```

#### 3. Authenticated Requests
```bash
GET /api/borrowers/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Borrower Service APIs

#### Create Borrower Profile
```bash
POST /api/borrowers
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "ssn": "123-45-6789",
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001"
  },
  "employmentDetails": {
    "employer": "Tech Corp",
    "position": "Software Engineer",
    "annualIncome": 75000,
    "employmentStartDate": "2020-01-01"
  }
}
```

#### Submit Loan Application
```bash
POST /api/borrowers/{borrowerId}/loan-applications
Authorization: Bearer <token>
Content-Type: application/json

{
  "loanType": "PERSONAL",
  "loanAmount": 25000,
  "loanTermMonths": 36,
  "interestRate": 6.5,
  "purpose": "Home improvement"
}
```

#### Upload Document
```bash
POST /api/borrowers/{borrowerId}/documents
Authorization: Bearer <token>
Content-Type: multipart/form-data

# Form data:
# file: [binary file content]
# documentType: "ID_PROOF"
# description: "Driver's License"
```

### Officer Service APIs

#### Update Loan Application Status
```bash
PUT /api/admin/loans/{applicationId}/status
Authorization: Bearer <officer-token>
Content-Type: application/json

{
  "newStatus": "APPROVED",
  "updatedBy": "officer123",
  "notes": "Application approved after review"
}
```

#### Update Document Status
```bash
PUT /api/admin/documents/{documentId}/status
Authorization: Bearer <officer-token>
Content-Type: application/json

{
  "newStatus": "VERIFIED",
  "updatedBy": "officer123",
  "verificationNotes": "Document verified successfully"
}
```

### Complete API Reference

For complete API documentation, use the comprehensive test suite in `api-tests.http` which includes:
- All authentication endpoints
- Complete borrower service API
- Full officer service API  
- Event-driven workflow examples
- Error handling scenarios

This file can be run directly in VS Code with the REST Client extension.

## 🔄 Event-Driven Architecture

### Kafka Topics

#### 1. loan-status
**Purpose**: Loan application status updates from Officer Service to Borrower Service

**Event Schema**:
```protobuf
message LoanStatusUpdateEvent {
  int64 application_id = 1;        // Loan application ID
  int64 borrower_id = 2;          // Borrower ID (for security filtering)
  string old_status = 3;          // Previous status
  string new_status = 4;          // New status
  string updated_by = 5;          // Officer/User ID who made the change
  string updated_at = 6;          // Timestamp of status change
  string rejection_reason = 7;    // Optional rejection reason
  string event_id = 8;           // Unique event identifier
  string event_timestamp = 9;    // Event creation timestamp
}
```

#### 2. documents-status
**Purpose**: Document status updates from Officer Service to Borrower Service

**Event Schema**:
```protobuf
message DocumentStatusUpdateEvent {
  int64 document_id = 1;          // Document ID
  int64 borrower_id = 2;          // Borrower ID (for security filtering)
  int64 loan_application_id = 3;  // Optional loan application ID
  string old_status = 4;          // Previous status
  string new_status = 5;          // New status
  string updated_by = 6;          // Officer/User ID who made the change
  string updated_at = 7;          // Timestamp of status change
  string rejection_reason = 8;    // Optional rejection reason
  string event_id = 9;           // Unique event identifier
  string event_timestamp = 10;   // Event creation timestamp
}
```

### Event Flow

```mermaid
sequenceDiagram
    participant Officer as Officer Service
    participant Kafka as Apache Kafka
    participant Borrower as Borrower Service
    participant DB as Database
    
    Officer->>Officer: Update loan status
    Officer->>Kafka: Publish LoanStatusUpdateEvent
    Kafka->>Borrower: Consume event
    Borrower->>Borrower: Validate borrower_id
    Borrower->>DB: Update local status
    Borrower->>Borrower: Log successful update
```

### Event Processing Features

- **Exactly-Once Processing**: Idempotent event handling
- **Security Filtering**: Borrower-specific event filtering
- **Error Handling**: Dead letter queues for failed events
- **Monitoring**: Real-time event processing metrics
- **Replay Capability**: Event replay for recovery scenarios

## 🔐 Security

### Authentication & Authorization

#### JWT Token-Based Authentication
- **Token Generation**: Secure JWT token generation with configurable expiration
- **Token Validation**: Stateless token validation at API Gateway
- **Role-Based Access**: BORROWER, OFFICER, ADMIN roles with specific permissions

#### Role Permissions

| Role | Permissions |
|------|-------------|
| **BORROWER** | - View own profile and loan applications<br>- Submit loan applications<br>- Upload documents<br>- View application status |
| **OFFICER** | - View all loan applications<br>- Update application status<br>- Verify documents<br>- Generate reports |
| **ADMIN** | - All officer permissions<br>- User management<br>- System configuration<br>- Audit log access |

### Data Security

#### Encryption
- **Data at Rest**: Database encryption for sensitive fields
- **Data in Transit**: HTTPS/TLS for all API communications
- **Message Encryption**: Kafka message encryption for sensitive events

#### Sensitive Data Handling
- **PII Protection**: Encryption of personally identifiable information
- **SSN Encryption**: Secure handling of social security numbers
- **Document Security**: Encrypted file storage with access logging

### Security Headers
```yaml
# Security headers automatically applied
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'self'
```

## 🚢 Deployment

### Docker Deployment

Each service includes a Dockerfile for containerization:

#### Build Docker Images
```bash
# Build individual service images
cd auth-service
docker build -t loanapp/auth-service .

cd ../borrower-service  
docker build -t loanapp/borrower-service .

cd ../officer-service
docker build -t loanapp/officer-service .

cd ../api-gateway
docker build -t loanapp/api-gateway .
```

#### Run Docker Containers
```bash
# Run auth service
docker run -d --name auth-service -p 4005:4005 \
  -e DB_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  loanapp/auth-service

# Run borrower service  
docker run -d --name borrower-service -p 4001:4001 \
  -e DB_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  loanapp/borrower-service

# Run officer service
docker run -d --name officer-service -p 8083:8083 \
  -e DB_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  loanapp/officer-service

# Run API gateway
docker run -d --name api-gateway -p 4000:4000 \
  -e AUTH_SERVICE_URL=http://host.docker.internal:4005 \
  -e BORROWER_SERVICE_URL=http://host.docker.internal:4001 \
  -e OFFICER_SERVICE_URL=http://host.docker.internal:8083 \
  loanapp/api-gateway
```

### Environment-Specific Configuration

#### Development
```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:devdb
spring.kafka.bootstrap-servers=localhost:9092
logging.level.com.pm=DEBUG
```

#### Production  
```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://prod-db:5432/loanapp
spring.kafka.bootstrap-servers=prod-kafka:9092
logging.level.com.pm=WARN
```

## 🧪 Testing

### Manual API Testing

The project includes a comprehensive test suite in `api-tests.http` that covers:

#### Test Categories
- **Authentication Tests**: User registration, login, token validation
- **Borrower Service Tests**: Profile management, loan applications, document uploads
- **Officer Service Tests**: Application review, status updates, document verification
- **API Gateway Tests**: Routing and authentication validation
- **Event Integration Tests**: Kafka event flow verification
- **Error Handling Tests**: Invalid requests and edge cases

#### Running Tests
1. **Prerequisites**: Start all services (auth, borrower, officer, api-gateway)
2. **Infrastructure**: Ensure Kafka and PostgreSQL are running
3. **Execute Tests**: Open `api-tests.http` in VS Code with REST Client extension
4. **Follow Workflow**: Execute tests in order to build test data

#### Test Workflow
```bash
# 1. Register users (borrower and officer)
POST /api/auth/register

# 2. Login to get tokens  
POST /api/auth/login

# 3. Create borrower profile
POST /api/borrowers

# 4. Submit loan application
POST /api/borrowers/{id}/loan-applications

# 5. Upload documents
POST /api/borrowers/{id}/documents

# 6. Officer reviews and updates status
PUT /api/admin/loans/{id}/status

# 7. Verify status synchronized via Kafka
GET /api/borrowers/{id}/loan-applications
```

### Integration Testing

#### Event-Driven Testing
The system can be tested end-to-end by:
1. Creating loan applications in borrower service
2. Updating status via officer service  
3. Verifying status changes appear in borrower service (via Kafka events)
4. Checking audit trails and timestamps

#### Manual Verification
```bash
# Check service health
curl http://localhost:4000/actuator/health  # API Gateway
curl http://localhost:4005/actuator/health  # Auth Service  
curl http://localhost:4001/actuator/health  # Borrower Service
curl http://localhost:8083/actuator/health  # Officer Service
```

## 📊 Monitoring

### Application Monitoring

#### Health Checks
Each service provides Spring Boot Actuator endpoints:
- **API Gateway**: http://localhost:4000/actuator/health
- **Auth Service**: http://localhost:4005/actuator/health  
- **Borrower Service**: http://localhost:4001/actuator/health
- **Officer Service**: http://localhost:8083/actuator/health

#### Application Logs
Services use structured logging with SLF4J:
```json
{
  "timestamp": "2025-07-10T10:30:00.000Z",
  "level": "INFO", 
  "service": "borrower-service",
  "userId": "john.doe",
  "action": "LOAN_APPLICATION_SUBMITTED",
  "applicationId": 12345,
  "message": "Loan application submitted successfully"
}
```

#### Event Monitoring
Monitor Kafka event processing:
- **Producer Logs**: Officer service publishing events
- **Consumer Logs**: Borrower service processing events  
- **Event Processing**: Success/failure rates and latency

### Key Metrics to Monitor

#### Application Metrics
- **Request Volume**: HTTP requests per service
- **Response Times**: API endpoint latency
- **Error Rates**: 4xx/5xx response percentages
- **Service Uptime**: Service availability

#### Business Metrics  
- **Loan Applications**: Submission and approval rates
- **Status Updates**: Event processing throughput
- **Document Uploads**: File processing success rates
- **User Activity**: Authentication and authorization events

#### Infrastructure Metrics
- **Database Connections**: Connection pool usage
- **Kafka Lag**: Consumer lag for event processing
- **Memory Usage**: JVM heap and garbage collection
- **CPU Usage**: Service resource consumption

### Logging Configuration

```properties
# application.properties logging configuration
logging.level.com.pm=INFO
logging.level.org.apache.kafka=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## 🤝 Contributing

### Development Workflow

#### 1. Fork and Clone
```bash
# Fork the repository on GitHub
git clone https://github.com/your-username/loanapp.git
cd loanapp
```

#### 2. Create Feature Branch
```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Or bug fix branch
git checkout -b bugfix/issue-number
```

### Development Guidelines
- **Code Style**: Follow Java coding conventions
- **Documentation**: Update API documentation for changes  
- **Testing**: Test manually using the `api-tests.http` file
- **Commit Messages**: Use descriptive commit messages

#### 4. Commit and Push
```bash
# Commit changes
git add .
git commit -m "feat: add loan eligibility calculation"

# Push to your fork
git push origin feature/your-feature-name
```

#### 5. Create Pull Request
- Create PR against `main` branch
- Include description of changes
- Reference related issues
- Ensure all checks pass

### Code Standards

#### Java Code Style
```java
// Follow standard Java conventions
public class LoanCalculator {
    
    private static final double DEFAULT_INTEREST_RATE = 0.05;
    
    /**
     * Calculates monthly payment for a loan.
     */
    public BigDecimal calculateMonthlyPayment(
            BigDecimal principal, 
            BigDecimal annualRate, 
            int termMonths) {
        // Implementation
    }
}
```

#### API Design Guidelines
- Use RESTful principles
- Consistent naming conventions
- Proper HTTP status codes
- Comprehensive error responses

#### Database Design Guidelines
- Normalize database schema
- Use appropriate indexes
- Foreign key constraints  
- Audit trails for sensitive data

### Testing Requirements

#### Manual Testing
- **API Testing**: Use the comprehensive `api-tests.http` file
- **Integration Testing**: Test event-driven workflows end-to-end
- **Error Handling**: Verify error scenarios and edge cases
- **Security Testing**: Test authentication and authorization

#### Verification Steps
```bash
# Test service health
curl http://localhost:4000/actuator/health

# Test authentication flow
# Use api-tests.http for complete workflow testing

# Verify Kafka integration
# Check logs for event processing confirmation
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 LoanApp Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---


---

**Built with ❤️ by the LoanApp development team**
