# Borrower Service API Documentation

## Overview
The Borrower Service is a Spring Boot REST API for managing loan applications, borrowers, and documents. It provides comprehensive functionality for loan application processing, borrower management, document upload, and loan calculations.

**Base URL:** `http://localhost:4001`
**API Documentation:** `http://localhost:4001/swagger-ui.html`
**API Docs JSON:** `http://localhost:4001/api-docs`

## Technology Stack
- **Framework:** Spring Boot 3.5.3
- **Database:** PostgreSQL
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Validation:** Bean Validation
- **File Upload:** Multipart file handling
- **Testing:** Spring Boot Test with H2 in-memory database

## API Endpoints

### 1. Borrower Management

#### 1.1 Create Borrower
**POST** `/api/borrowers`

Creates a new borrower in the system.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "ssn": "123-45-6789",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "annualIncome": 75000.0,
  "employmentStatus": "EMPLOYED",
  "employerName": "Tech Corp",
  "employmentYears": 5
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "ssn": "123-45-6789",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "annualIncome": 75000.0,
  "employmentStatus": "EMPLOYED",
  "employerName": "Tech Corp",
  "employmentYears": 5,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 1.2 Get Borrower by ID
**GET** `/api/borrowers/{id}`

Retrieves a specific borrower by their ID.

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "ssn": "123-45-6789",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "annualIncome": 75000.0,
  "employmentStatus": "EMPLOYED",
  "employerName": "Tech Corp",
  "employmentYears": 5,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 1.3 Get All Borrowers
**GET** `/api/borrowers`

Retrieves all borrowers in the system.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "ssn": "123-45-6789",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "annualIncome": 75000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Tech Corp",
    "employmentYears": 5,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "+1987654321",
    "dateOfBirth": "1985-05-20",
    "ssn": "987-65-4321",
    "address": "456 Oak Avenue",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90210",
    "annualIncome": 85000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Design Studio",
    "employmentYears": 8,
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

#### 1.4 Delete Borrower
**DELETE** `/api/borrowers/{id}`

Deletes a borrower and all associated data.

**Response (204 No Content)**

### 2. Loan Application Management

#### 2.1 Apply for Loan
**POST** `/api/borrowers/{borrowerId}/loans`

Creates a new loan application for a borrower.

**Request Body:**
```json
{
  "loanType": "PERSONAL",
  "loanAmount": 25000.00,
  "loanTermMonths": 36,
  "interestRate": 8.5,
  "purpose": "Home renovation",
  "notes": "Planning to renovate kitchen and bathroom"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "borrowerId": 1,
  "borrowerName": "John Doe",
  "loanType": "PERSONAL",
  "loanAmount": 25000.00,
  "loanTermMonths": 36,
  "interestRate": 8.5,
  "monthlyPayment": 789.45,
  "totalPayment": 28420.20,
  "status": "PENDING",
  "applicationNumber": "LOAN-1705312200000-123",
  "purpose": "Home renovation",
  "notes": "Planning to renovate kitchen and bathroom",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 2.2 Get Loan Application
**GET** `/api/borrowers/{borrowerId}/loans/{applicationId}`

Retrieves a specific loan application.

**Response (200 OK):**
```json
{
  "id": 1,
  "borrowerId": 1,
  "borrowerName": "John Doe",
  "loanType": "PERSONAL",
  "loanAmount": 25000.00,
  "loanTermMonths": 36,
  "interestRate": 8.5,
  "monthlyPayment": 789.45,
  "totalPayment": 28420.20,
  "status": "PENDING",
  "applicationNumber": "LOAN-1705312200000-123",
  "purpose": "Home renovation",
  "notes": "Planning to renovate kitchen and bathroom",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 2.3 Get All Loan Applications for Borrower
**GET** `/api/borrowers/{borrowerId}/loans`

Retrieves all loan applications for a specific borrower.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanType": "PERSONAL",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "monthlyPayment": 789.45,
    "totalPayment": 28420.20,
    "status": "PENDING",
    "applicationNumber": "LOAN-1705312200000-123",
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanType": "AUTO",
    "loanAmount": 15000.00,
    "loanTermMonths": 24,
    "interestRate": 6.5,
    "monthlyPayment": 667.50,
    "totalPayment": 16020.00,
    "status": "APPROVED",
    "applicationNumber": "LOAN-1705312300000-456",
    "purpose": "Car purchase",
    "notes": "Buying a used car",
    "approvedBy": "loan_officer_1",
    "approvedAt": "2024-01-15T11:00:00",
    "createdAt": "2024-01-15T10:45:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

#### 2.4 Get Loan Applications by Status
**GET** `/api/borrowers/{borrowerId}/loans/status/{status}`

Retrieves loan applications filtered by status.

**Available Statuses:** `PENDING`, `UNDER_REVIEW`, `DOCUMENTS_REQUIRED`, `APPROVED`, `REJECTED`, `CANCELLED`, `FUNDED`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanType": "PERSONAL",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "monthlyPayment": 789.45,
    "totalPayment": 28420.20,
    "status": "PENDING",
    "applicationNumber": "LOAN-1705312200000-123",
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### 3. Loan Calculator

#### 3.1 Calculate Loan Payment
**POST** `/api/borrowers/loans/calculate`

Calculates monthly and total payments for a loan.

**Request Body:**
```json
{
  "amount": 25000.00,
  "interestRate": 8.5,
  "termMonths": 36
}
```

**Response (200 OK):**
```json
{
  "monthlyPayment": 789.45,
  "totalPayment": 28420.20
}
```

### 4. Document Management

#### 4.1 Upload Document
**POST** `/api/borrowers/{borrowerId}/documents`

Uploads a document for a borrower.

**Content-Type:** `multipart/form-data`

**Form Parameters:**
- `file` (required): The file to upload
- `documentType` (required): Type of document (e.g., "ID_PROOF", "INCOME_PROOF", "BANK_STATEMENT")
- `description` (optional): Description of the document
- `loanApplicationId` (optional): Associated loan application ID

**Supported File Types:** PDF, DOC, DOCX, JPG, JPEG, PNG
**Maximum File Size:** 10MB

**Response (201 Created):**
```json
{
  "id": 1,
  "borrower": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe"
  },
  "loanApplication": null,
  "documentName": "driver_license.pdf",
  "documentType": "ID_PROOF",
  "filePath": "/tmp/borrower-documents/1/1705312200000_driver_license.pdf",
  "fileName": "1705312200000_driver_license.pdf",
  "fileSize": 245760,
  "contentType": "application/pdf",
  "description": "Driver's license for identity verification",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 4.2 Get Documents for Borrower
**GET** `/api/borrowers/{borrowerId}/documents`

Retrieves all documents for a specific borrower.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "borrower": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe"
    },
    "loanApplication": null,
    "documentName": "driver_license.pdf",
    "documentType": "ID_PROOF",
    "filePath": "/tmp/borrower-documents/1/1705312200000_driver_license.pdf",
    "fileName": "1705312200000_driver_license.pdf",
    "fileSize": 245760,
    "contentType": "application/pdf",
    "description": "Driver's license for identity verification",
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "borrower": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe"
    },
    "loanApplication": {
      "id": 1,
      "applicationNumber": "LOAN-1705312200000-123"
    },
    "documentName": "paystub.pdf",
    "documentType": "INCOME_PROOF",
    "filePath": "/tmp/borrower-documents/1/1705312300000_paystub.pdf",
    "fileName": "1705312300000_paystub.pdf",
    "fileSize": 512000,
    "contentType": "application/pdf",
    "description": "Latest paystub for income verification",
    "status": "VERIFIED",
    "verifiedBy": "document_verifier_1",
    "verifiedAt": "2024-01-15T11:30:00",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:30:00"
  }
]
```

#### 4.3 Delete Document
**DELETE** `/api/borrowers/{borrowerId}/documents/{documentId}`

Deletes a specific document.

**Response (204 No Content)**

## Data Models

### Borrower Entity
```java
{
  "id": Long,
  "firstName": String (2-50 chars),
  "lastName": String (2-50 chars),
  "email": String (valid email, unique),
  "phoneNumber": String (valid phone format),
  "dateOfBirth": String,
  "ssn": String (XXX-XX-XXXX format, unique),
  "address": String,
  "city": String,
  "state": String (2 chars),
  "zipCode": String (valid ZIP format),
  "annualIncome": Double (> 0),
  "employmentStatus": String,
  "employerName": String (optional),
  "employmentYears": Integer (0-50, optional),
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

### Loan Application Entity
```java
{
  "id": Long,
  "borrowerId": Long,
  "borrowerName": String,
  "loanType": String,
  "loanAmount": BigDecimal (1000-1000000),
  "loanTermMonths": Integer (12-360),
  "interestRate": BigDecimal (0-25),
  "monthlyPayment": BigDecimal,
  "totalPayment": BigDecimal,
  "status": LoanApplicationStatus,
  "applicationNumber": String (unique),
  "purpose": String (optional),
  "notes": String (optional),
  "approvedBy": String (optional),
  "approvedAt": LocalDateTime (optional),
  "rejectionReason": String (optional),
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

### Document Entity
```java
{
  "id": Long,
  "borrower": Borrower,
  "loanApplication": LoanApplication (optional),
  "documentName": String,
  "documentType": String,
  "filePath": String,
  "fileName": String,
  "fileSize": Long,
  "contentType": String,
  "description": String (optional),
  "status": DocumentStatus,
  "verifiedBy": String (optional),
  "verifiedAt": LocalDateTime (optional),
  "rejectionReason": String (optional),
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

## Enums

### LoanApplicationStatus
- `PENDING` - Pending Review
- `UNDER_REVIEW` - Under Review
- `DOCUMENTS_REQUIRED` - Documents Required
- `APPROVED` - Approved
- `REJECTED` - Rejected
- `CANCELLED` - Cancelled
- `FUNDED` - Funded

### DocumentStatus
- `PENDING` - Pending Review
- `VERIFIED` - Verified
- `REJECTED` - Rejected
- `EXPIRED` - Expired

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/borrowers",
  "details": [
    {
      "field": "email",
      "message": "Email should be valid"
    },
    {
      "field": "ssn",
      "message": "SSN should be in format XXX-XX-XXXX"
    }
  ]
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Borrower not found",
  "path": "/api/borrowers/999"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/borrowers"
}
```

## Testing with Mock Data

### 1. Using cURL

#### Create a Borrower
```bash
curl -X POST http://localhost:4001/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "ssn": "123-45-6789",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "annualIncome": 75000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Tech Corp",
    "employmentYears": 5
  }'
```

#### Apply for a Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/1/loans \
  -H "Content-Type: application/json" \
  -d '{
    "loanType": "PERSONAL",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom"
  }'
```

#### Calculate Loan Payment
```bash
curl -X POST http://localhost:4001/api/borrowers/loans/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25000.00,
    "interestRate": 8.5,
    "termMonths": 36
  }'
```

#### Upload a Document
```bash
curl -X POST http://localhost:4001/api/borrowers/1/documents \
  -F "file=@/path/to/document.pdf" \
  -F "documentType=ID_PROOF" \
  -F "description=Driver's license for identity verification"
```

### 2. Using Postman

#### Collection Import
Import the following collection into Postman:

```json
{
  "info": {
    "name": "Borrower Service API",
    "description": "API collection for testing Borrower Service endpoints"
  },
  "item": [
    {
      "name": "Create Borrower",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "http://localhost:4001/api/borrowers",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers"]
        },
        "body": {
          "mode": "raw",
          "raw": "{\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"email\": \"john.doe@example.com\",\n  \"phoneNumber\": \"+1234567890\",\n  \"dateOfBirth\": \"1990-01-15\",\n  \"ssn\": \"123-45-6789\",\n  \"address\": \"123 Main Street\",\n  \"city\": \"New York\",\n  \"state\": \"NY\",\n  \"zipCode\": \"10001\",\n  \"annualIncome\": 75000.0,\n  \"employmentStatus\": \"EMPLOYED\",\n  \"employerName\": \"Tech Corp\",\n  \"employmentYears\": 5\n}"
        }
      }
    },
    {
      "name": "Get All Borrowers",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:4001/api/borrowers",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers"]
        }
      }
    },
    {
      "name": "Apply for Loan",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/loans",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "loans"]
        },
        "body": {
          "mode": "raw",
          "raw": "{\n  \"loanType\": \"PERSONAL\",\n  \"loanAmount\": 25000.00,\n  \"loanTermMonths\": 36,\n  \"interestRate\": 8.5,\n  \"purpose\": \"Home renovation\",\n  \"notes\": \"Planning to renovate kitchen and bathroom\"\n}"
        }
      }
    },
    {
      "name": "Calculate Loan",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "http://localhost:4001/api/borrowers/loans/calculate",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "loans", "calculate"]
        },
        "body": {
          "mode": "raw",
          "raw": "{\n  \"amount\": 25000.00,\n  \"interestRate\": 8.5,\n  \"termMonths\": 36\n}"
        }
      }
    }
  ]
}
```

### 3. Mock Data Examples

#### Sample Borrowers
```json
[
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "ssn": "123-45-6789",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "annualIncome": 75000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Tech Corp",
    "employmentYears": 5
  },
  {
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "+1987654321",
    "dateOfBirth": "1985-05-20",
    "ssn": "987-65-4321",
    "address": "456 Oak Avenue",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90210",
    "annualIncome": 85000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Design Studio",
    "employmentYears": 8
  },
  {
    "firstName": "Mike",
    "lastName": "Johnson",
    "email": "mike.johnson@example.com",
    "phoneNumber": "+1555123456",
    "dateOfBirth": "1988-12-10",
    "ssn": "456-78-9012",
    "address": "789 Pine Road",
    "city": "Chicago",
    "state": "IL",
    "zipCode": "60601",
    "annualIncome": 65000.0,
    "employmentStatus": "SELF_EMPLOYED",
    "employerName": "Freelance Consultant",
    "employmentYears": 3
  }
]
```

#### Sample Loan Applications
```json
[
  {
    "loanType": "PERSONAL",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom"
  },
  {
    "loanType": "AUTO",
    "loanAmount": 15000.00,
    "loanTermMonths": 24,
    "interestRate": 6.5,
    "purpose": "Car purchase",
    "notes": "Buying a used car"
  },
  {
    "loanType": "BUSINESS",
    "loanAmount": 50000.00,
    "loanTermMonths": 60,
    "interestRate": 7.5,
    "purpose": "Business expansion",
    "notes": "Expanding to new markets"
  }
]
```

#### Sample Loan Calculations
```json
[
  {
    "amount": 25000.00,
    "interestRate": 8.5,
    "termMonths": 36
  },
  {
    "amount": 15000.00,
    "interestRate": 6.5,
    "termMonths": 24
  },
  {
    "amount": 50000.00,
    "interestRate": 7.5,
    "termMonths": 60
  },
  {
    "amount": 100000.00,
    "interestRate": 5.5,
    "termMonths": 30
  }
]
```

## Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL database

### Database Setup
1. Create a PostgreSQL database named `loan-borrower-db`
2. Update `application.properties` with your database credentials
3. The application will automatically create tables using JPA

### Running the Application
```bash
# Clone the repository
git clone <repository-url>
cd borrower-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using Docker
```bash
# Build Docker image
docker build -t borrower-service .

# Run with Docker
docker run -p 4001:4001 borrower-service
```

## Health Checks

### Application Health
**GET** `/actuator/health`

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 419430400000,
        "threshold": 10485760
      }
    }
  }
}
```

### Application Info
**GET** `/actuator/info`

**Response:**
```json
{
  "app": {
    "name": "borrower-service",
    "description": "Borrower Service - Loan Application Management System",
    "version": "0.0.1-SNAPSHOT"
  }
}
```

## Security Considerations

1. **Input Validation**: All inputs are validated using Bean Validation annotations
2. **File Upload Security**: File type and size restrictions are enforced
3. **SQL Injection Protection**: Using JPA repositories with parameterized queries
4. **Data Privacy**: SSN and sensitive data should be encrypted in production
5. **Authentication**: Add Spring Security for production use
6. **HTTPS**: Use HTTPS in production environment

## Performance Considerations

1. **Database Indexing**: Add indexes on frequently queried fields
2. **Pagination**: Implement pagination for large result sets
3. **Caching**: Add Redis caching for frequently accessed data
4. **File Storage**: Consider using cloud storage (AWS S3, Azure Blob) for documents
5. **Connection Pooling**: Configure HikariCP connection pool settings

## Monitoring and Logging

The application includes:
- Spring Boot Actuator for health checks and metrics
- Structured logging with different levels
- SQL query logging for debugging
- File upload monitoring

