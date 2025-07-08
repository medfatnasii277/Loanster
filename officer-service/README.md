# Officer Service Implementation

## Architecture Overview

The Officer Service follows a clean, standard **Repository-Model-Service** pattern focused purely on consuming Kafka events and persisting data to PostgreSQL.

## Project Structure

```
src/main/java/com/pm/officerservice/
├── config/
│   └── KafkaConfig.java              # Kafka consumer configuration
├── controller/
│   └── HealthController.java         # Simple health check endpoint
├── listener/
│   └── EventListener.java            # Kafka event listeners
├── model/
│   ├── Borrower.java                # Borrower entity
│   ├── Document.java                # Document entity
│   └── LoanApplication.java         # Loan application entity
├── repository/
│   ├── BorrowerRepository.java      # Borrower data access
│   ├── DocumentRepository.java     # Document data access
│   └── LoanApplicationRepository.java # Loan application data access
├── service/
│   ├── BorrowerService.java         # Borrower business logic
│   ├── DocumentService.java        # Document business logic
│   └── LoanApplicationService.java # Loan application business logic
└── OfficerServiceApplication.java   # Main application class
```

## Key Features

### 1. **Event-Driven Architecture**
- Consumes Protobuf messages from Kafka topics:
  - `borrower-created`
  - `loan-application` 
  - `documents-upload`

### 2. **Data Persistence**
- Saves all received events to PostgreSQL database
- Maintains referential integrity between entities
- Handles duplicate events gracefully

### 3. **Error Handling**
- Robust error handling for Protobuf parsing
- Transaction management for data consistency
- Comprehensive logging for debugging

### 4. **Extensible Design**
- Clean separation of concerns
- Easy to add new event types
- Prepared for future officer management features

## Database Schema

### Borrowers Table
- `borrower_id` (Primary Key) - Matches ID from borrower-service
- Personal information fields
- `created_at_source` - Original creation time from borrower-service
- `created_at` / `updated_at` - Officer service timestamps

### Loan Applications Table
- `application_id` (Primary Key) - Matches ID from borrower-service
- `borrower_id` (Foreign Key)
- Loan details and financial information
- `applied_at_source` - Original application time

### Documents Table
- `document_id` (Primary Key) - Matches ID from borrower-service
- `borrower_id` (Foreign Key)
- `loan_application_id` (Foreign Key, nullable)
- File metadata and paths
- `uploaded_at_source` - Original upload time

## Configuration

### Application Properties
- **Database**: PostgreSQL on `localhost:5000/loan-officer-db`
- **Kafka**: Consumer group `officer-service-group`
- **Server**: Runs on port `5001`

### Kafka Consumer Settings
- Auto-offset reset: `earliest`
- Manual acknowledgment mode
- Byte array deserialization for Protobuf messages

## Future Extensions Ready

The current implementation is designed to easily support:

1. **Officer Management Endpoints**
   - Review and approve/reject loan applications
   - Update document statuses
   - Add officer notes and comments

2. **Bidirectional Communication**
   - Send status updates back to borrower-service
   - Kafka producer for officer decisions

3. **Advanced Querying**
   - Dashboard APIs for officers
   - Reporting and analytics endpoints
   - Search and filtering capabilities

4. **Workflow Management**
   - Task assignment to officers
   - Priority queues for applications
   - SLA tracking and notifications

## Testing

- **Health Check**: `GET /api/health`
- **Actuator**: `/actuator/health`, `/actuator/info`
- **Event Processing**: Automatic via Kafka listeners

## Dependencies

- Spring Boot 3.5.3
- Spring Data JPA
- Spring Kafka
- PostgreSQL Driver
- Protobuf 3.25.1
- Lombok for boilerplate reduction

## Running the Service

1. Ensure PostgreSQL and Kafka are running
2. Compile: `./mvnw clean compile`
3. Run: `./mvnw spring-boot:run`
4. Test: `curl http://localhost:5001/api/health`

The service will automatically start consuming events from Kafka and persisting them to the database.
