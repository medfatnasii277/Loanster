# Kafka Consumer Implementation - Borrower Service

## Overview

The borrower service now acts as a Kafka consumer to automatically receive and process status updates from the officer service. This ensures that when an officer changes the status of a loan application or document, the change is immediately reflected in the borrower's view.

## Architecture

```
Officer Service (Admin)    →    Kafka Topics    →    Borrower Service (Consumer)
    |                              |                        |
    ├─ Updates loan status    →    loan-status         →    Updates local loan status
    └─ Updates document status →   documents-status    →    Updates local document status
```

## Security Features

### Borrower-Specific Filtering
- Each event includes a `borrower_id` field
- Consumer validates that the event belongs to the correct borrower
- Events for other borrowers are ignored (security isolation)
- Prevents cross-borrower data leakage

### Event Validation
- Protobuf schema validation ensures data integrity
- Missing entities are logged but don't crash the consumer
- Malformed events are caught and logged

## Consumer Configuration

### Kafka Settings
- **Consumer Group**: `borrower-service-group`
- **Topics**: `loan-status`, `documents-status`
- **Offset Reset**: `earliest` (processes all events from the beginning)
- **Auto Commit**: Disabled (manual acknowledgment for reliability)

### Event Processing
- **Transactional**: Each event is processed in a database transaction
- **Idempotent**: Safe to reprocess the same event multiple times
- **Error Handling**: Exceptions are logged, failed events can be retried

## Event Flow

### Loan Status Update
1. Officer updates loan status via `/api/admin/loans/{id}/status`
2. Officer service publishes `LoanStatusUpdateEvent` to `loan-status` topic
3. Borrower service consumes event
4. Security check: Verify borrower ID matches
5. Update local loan application status
6. Save changes to database

### Document Status Update
1. Officer updates document status via `/api/admin/documents/{id}/status`
2. Officer service publishes `DocumentStatusUpdateEvent` to `documents-status` topic
3. Borrower service consumes event
4. Security check: Verify borrower ID matches
5. Update local document status
6. Save changes to database

## Event Schema

### LoanStatusUpdateEvent
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

### DocumentStatusUpdateEvent
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

## Status Synchronization

### Status Overwriting
- Status updates **overwrite** the previous status (no concatenation)
- Each status change creates a complete new state
- History is maintained through audit fields

### Audit Trail
- `statusUpdatedBy`: Records which officer made the change
- `statusUpdatedAt`: Records when the change was made
- `rejectionReason`: Records reason for rejection (if applicable)

## Error Handling

### Consumer Resilience
- **Invalid Events**: Logged and skipped
- **Missing Entities**: Logged as warnings, processing continues
- **Security Violations**: Logged as warnings, event ignored
- **Parse Errors**: Logged as errors, event skipped

### Dead Letter Queue (Future Enhancement)
In production, failed events should be sent to a dead letter queue for manual review and reprocessing.

## Testing

### Unit Testing
Test the consumer service with mock events to verify:
- Correct status updates
- Security filtering
- Error handling
- Transaction rollback on failures

### Integration Testing
Use the provided HTTP test file to verify end-to-end functionality:
1. Create test data in borrower service
2. Update status via officer service
3. Verify status change in borrower service
4. Check that only correct borrower's data is updated

## Monitoring

### Key Metrics to Monitor
- **Event Processing Rate**: Events processed per second
- **Error Rate**: Percentage of events that fail processing
- **Lag**: Delay between event production and consumption
- **Security Violations**: Number of cross-borrower access attempts

### Logging
- **INFO**: Successful event processing
- **WARN**: Security violations, missing entities
- **ERROR**: Processing failures, parse errors
- **DEBUG**: Detailed event data (for troubleshooting)

## Configuration Properties

```properties
# Kafka Consumer Settings
spring.kafka.consumer.group-id=borrower-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.listener.ack-mode=manual_immediate

# Topic Names
kafka.topics.loan-status=loan-status
kafka.topics.document-status=documents-status

# Logging
logging.level.com.pm.borrowerservice.service.OfficerEventConsumerService=INFO
```

## Benefits

### Real-Time Updates
- Status changes are reflected immediately in borrower service
- No polling or manual synchronization required
- Consistent state across services

### Security
- Borrower-specific event filtering
- No access to other borrowers' data
- Audit trail of all status changes

### Reliability
- Transactional processing
- Error handling and logging
- Event replay capability
- Idempotent operations

### Scalability
- Kafka provides horizontal scaling
- Consumer groups enable parallel processing
- Decoupled architecture allows independent scaling

This implementation ensures that borrowers always see the most up-to-date status of their loan applications and documents, while maintaining security and reliability.
