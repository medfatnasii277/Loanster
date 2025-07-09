# Officer Service Admin API Documentation

## Overview
The Officer Service now includes comprehensive admin functionality to manage loan applications and documents. When status updates are made, events are automatically published to Kafka topics for other services (like the borrower service) to consume and update their local state.

**Base URL:** `http://localhost:5001`
**API Documentation:** `http://localhost:5001/swagger-ui.html`

## Admin Endpoints

### 1. Loan Application Management

#### 1.1 Get All Loan Applications
**GET** `/api/admin/loans`

Retrieves all loan applications in the system.

**Response (200 OK):**
```json
[
  {
    "applicationId": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "loanPurpose": "Home renovation",
    "interestRate": 8.5,
    "monthlyPayment": 789.45,
    "status": "PENDING",
    "appliedAtSource": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 1.2 Get Loan Application by ID
**GET** `/api/admin/loans/{applicationId}`

Retrieves a specific loan application by its ID.

**Path Parameters:**
- `applicationId` (Long): The ID of the loan application

**Response (200 OK):**
```json
{
  "applicationId": 1,
  "borrowerId": 1,
  "borrowerName": "John Doe",
  "loanAmount": 25000.00,
  "loanTermMonths": 36,
  "loanPurpose": "Home renovation",
  "interestRate": 8.5,
  "monthlyPayment": 789.45,
  "status": "PENDING",
  "appliedAtSource": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 1.3 Get Loan Applications by Status
**GET** `/api/admin/loans/status/{status}`

Retrieves all loan applications with a specific status.

**Path Parameters:**
- `status` (String): The status to filter by (case-insensitive)

**Available Statuses:** `PENDING`, `UNDER_REVIEW`, `DOCUMENTS_REQUIRED`, `APPROVED`, `REJECTED`, `CANCELLED`, `FUNDED`

**Response (200 OK):**
```json
[
  {
    "applicationId": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "loanPurpose": "Home renovation",
    "interestRate": 8.5,
    "monthlyPayment": 789.45,
    "status": "PENDING",
    "appliedAtSource": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 1.4 Update Loan Application Status
**PUT** `/api/admin/loans/{applicationId}/status`

Updates the status of a loan application and publishes an event to the `loan-status` Kafka topic.

**Path Parameters:**
- `applicationId` (Long): The ID of the loan application to update

**Request Body:**
```json
{
  "newStatus": "APPROVED",
  "rejectionReason": "Optional reason for rejection",
  "updatedBy": "admin_user_1"
}
```

**Available Loan Statuses:**
- `PENDING` - Pending Review
- `UNDER_REVIEW` - Under Review
- `DOCUMENTS_REQUIRED` - Documents Required
- `APPROVED` - Approved
- `REJECTED` - Rejected
- `CANCELLED` - Cancelled
- `FUNDED` - Funded

**Response (200 OK):**
```json
{
  "message": "Loan status updated successfully"
}
```

### 2. Document Management

#### 2.1 Get All Documents
**GET** `/api/admin/documents`

Retrieves all documents in the system.

**Response (200 OK):**
```json
[
  {
    "documentId": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanApplicationId": 1,
    "documentType": "INCOME_PROOF",
    "fileName": "paystub.pdf",
    "filePath": "/uploads/paystub.pdf",
    "fileSize": 1024000,
    "contentType": "application/pdf",
    "status": "PENDING",
    "uploadedAtSource": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2.2 Get Document by ID
**GET** `/api/admin/documents/{documentId}`

Retrieves a specific document by its ID.

**Path Parameters:**
- `documentId` (Long): The ID of the document

**Response (200 OK):**
```json
{
  "documentId": 1,
  "borrowerId": 1,
  "borrowerName": "John Doe",
  "loanApplicationId": 1,
  "documentType": "INCOME_PROOF",
  "fileName": "paystub.pdf",
  "filePath": "/uploads/paystub.pdf",
  "fileSize": 1024000,
  "contentType": "application/pdf",
  "status": "PENDING",
  "uploadedAtSource": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 2.3 Get Documents by Status
**GET** `/api/admin/documents/status/{status}`

Retrieves all documents with a specific status.

**Path Parameters:**
- `status` (String): The status to filter by (case-insensitive)

**Available Statuses:** `PENDING`, `VERIFIED`, `REJECTED`, `EXPIRED`

**Response (200 OK):**
```json
[
  {
    "documentId": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanApplicationId": 1,
    "documentType": "INCOME_PROOF",
    "fileName": "paystub.pdf",
    "filePath": "/uploads/paystub.pdf",
    "fileSize": 1024000,
    "contentType": "application/pdf",
    "status": "PENDING",
    "uploadedAtSource": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2.4 Get Documents for Loan Application
**GET** `/api/admin/loans/{applicationId}/documents`

Retrieves all documents associated with a specific loan application.

**Path Parameters:**
- `applicationId` (Long): The ID of the loan application

**Response (200 OK):**
```json
[
  {
    "documentId": 1,
    "borrowerId": 1,
    "borrowerName": "John Doe",
    "loanApplicationId": 1,
    "documentType": "INCOME_PROOF",
    "fileName": "paystub.pdf",
    "filePath": "/uploads/paystub.pdf",
    "fileSize": 1024000,
    "contentType": "application/pdf",
    "status": "PENDING",
    "uploadedAtSource": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2.5 Update Document Status
**PUT** `/api/admin/documents/{documentId}/status`

Updates the status of a document and publishes an event to the `documents-status` Kafka topic.

**Path Parameters:**
- `documentId` (Long): The ID of the document to update

**Request Body:**
```json
{
  "newStatus": "VERIFIED",
  "rejectionReason": "Optional reason for rejection",
  "updatedBy": "admin_user_1"
}
```

**Available Document Statuses:**
- `PENDING` - Pending Review
- `VERIFIED` - Verified
- `REJECTED` - Rejected
- `EXPIRED` - Expired

**Response (200 OK):**
```json
{
  "message": "Document status updated successfully"
}
```

### 3. Status Information

#### 3.1 Get Available Loan Statuses
**GET** `/api/admin/status/loan-statuses`

Retrieves all available loan application statuses.

**Response (200 OK):**
```json
[
  "PENDING",
  "UNDER_REVIEW",
  "DOCUMENTS_REQUIRED",
  "APPROVED",
  "REJECTED",
  "CANCELLED",
  "FUNDED"
]
```

#### 3.2 Get Available Document Statuses
**GET** `/api/admin/status/document-statuses`

Retrieves all available document statuses.

**Response (200 OK):**
```json
[
  "PENDING",
  "VERIFIED",
  "REJECTED",
  "EXPIRED"
]
```

## Example Requests

### Approve a Loan
```bash
curl -X PUT http://localhost:5001/api/admin/loans/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "APPROVED",
    "updatedBy": "admin_user_1"
  }'
```

### Reject a Loan with Reason
```bash
curl -X PUT http://localhost:5001/api/admin/loans/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "REJECTED",
    "rejectionReason": "Insufficient income to support the requested loan amount",
    "updatedBy": "admin_user_1"
  }'
```

### Verify a Document
```bash
curl -X PUT http://localhost:5001/api/admin/documents/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "VERIFIED",
    "updatedBy": "admin_user_1"
  }'
```

### Reject a Document with Reason
```bash
curl -X PUT http://localhost:5001/api/admin/documents/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "REJECTED",
    "rejectionReason": "Document is blurry and unreadable",
    "updatedBy": "admin_user_1"
  }'
```

### Get Pending Loan Applications
```bash
curl -X GET http://localhost:5001/api/admin/loans/status/pending
```

### Get Documents for a Loan Application
```bash
curl -X GET http://localhost:5001/api/admin/loans/1/documents
```

## Kafka Events

### Loan Status Update Event
When a loan status is updated, the following event is published to the `loan-status` topic:

```protobuf
message LoanStatusUpdateEvent {
  int64 application_id = 1;
  int64 borrower_id = 2;
  string old_status = 3;
  string new_status = 4;
  string updated_by = 5;
  string updated_at = 6;
  string rejection_reason = 7;  // Optional, only for rejected loans
  string event_id = 8;
  string event_timestamp = 9;
}
```

### Document Status Update Event
When a document status is updated, the following event is published to the `documents-status` topic:

```protobuf
message DocumentStatusUpdateEvent {
  int64 document_id = 1;
  int64 borrower_id = 2;
  int64 loan_application_id = 3;  // Optional, may be 0 if not associated with loan
  string old_status = 4;
  string new_status = 5;
  string updated_by = 6;
  string updated_at = 7;
  string rejection_reason = 8;  // Optional, only for rejected documents
  string event_id = 9;
  string event_timestamp = 10;
}
```

## Kafka Topics

The following Kafka topics are used for status update events:

- `loan-status` - Published when loan application status is updated
- `documents-status` - Published when document status is updated

## Error Handling

### Common Error Responses

**404 Not Found:**
```json
{
  "error": "Loan application not found with ID: 999"
}
```

**400 Bad Request:**
```json
{
  "error": "Invalid status value. Must be one of: PENDING, UNDER_REVIEW, DOCUMENTS_REQUIRED, APPROVED, REJECTED, CANCELLED, FUNDED"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Failed to update loan status"
}
```

## Testing

Use the provided test file `officer-service-test-admin.http` to test all admin endpoints:

```bash
# Test loan status update
curl -X PUT http://localhost:5001/api/admin/loans/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "APPROVED",
    "updatedBy": "admin_user_1"
  }'

# Test document status update
curl -X PUT http://localhost:5001/api/admin/documents/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "VERIFIED",
    "updatedBy": "admin_user_1"
  }'

# Get all loan applications
curl -X GET http://localhost:5001/api/admin/loans

# Get all documents
curl -X GET http://localhost:5001/api/admin/documents
```

## Integration with Borrower Service

The borrower service can consume these events to update its local state:

1. **Loan Status Events**: Update loan application status in borrower service database
2. **Document Status Events**: Update document status in borrower service database

This ensures data consistency across services without direct database coupling.

## Configuration

The following properties can be configured in `application.properties`:

```properties
# Kafka Topics
kafka.topics.loan-status=loan-status
kafka.topics.document-status=documents-status

# Kafka Configuration
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer
```

## Service Layer

The admin functionality is supported by the following services:

- **LoanApplicationService**: Handles loan application operations and status updates
- **DocumentService**: Handles document operations and status updates
- **KafkaEventProducerService**: Publishes events to Kafka topics

## Repository Layer

The following repositories provide data access:

- **LoanApplicationRepository**: CRUD operations for loan applications
- **DocumentRepository**: CRUD operations for documents
- **BorrowerRepository**: CRUD operations for borrowers 