# Status Update Fixes - Officer Service

## Issues Fixed

### 1. Status Concatenation Issue
**Problem**: When updating status multiple times, the result was being concatenated instead of being replaced.

**Root Cause**: The service was not properly overwriting the status field in the entities.

**Solution**: 
- Modified `LoanApplicationService.updateLoanStatus()` to explicitly overwrite the status field
- Modified `DocumentService.updateDocumentStatus()` to explicitly overwrite the status field
- Added explicit comments to clarify that we're overwriting, not concatenating

### 2. Missing Officer/User ID Tracking
**Problem**: No tracking of which officer/user made the status change.

**Solution**: 
- Added `statusUpdatedBy` field to `LoanApplication` entity
- Added `statusUpdatedAt` field to `LoanApplication` entity  
- Added `statusUpdatedBy` field to `Document` entity
- Added `statusUpdatedAt` field to `Document` entity
- Updated services to set these fields when status is changed
- Updated DTOs to include these fields in responses
- Enhanced validation in request DTOs

## Key Changes Made

### Entity Models Updated
1. **LoanApplication.java**
   - Added `statusUpdatedBy` column
   - Added `statusUpdatedAt` column

2. **Document.java**
   - Added `statusUpdatedBy` column  
   - Added `statusUpdatedAt` column

### Service Layer Updated
1. **LoanApplicationService.java**
   - Status update now explicitly overwrites the status field
   - Sets `statusUpdatedBy` and `statusUpdatedAt` fields
   - Publishes Kafka events with officer/user ID included

2. **DocumentService.java**
   - Status update now explicitly overwrites the status field
   - Sets `statusUpdatedBy` and `statusUpdatedAt` fields
   - Publishes Kafka events with officer/user ID included

### DTOs Enhanced
1. **LoanStatusUpdateRequest.java**
   - Enhanced validation with `@NotBlank` and `@Size` constraints
   - Better documentation of the `updatedBy` field

2. **DocumentStatusUpdateRequest.java**
   - Enhanced validation with `@NotBlank` and `@Size` constraints
   - Better documentation of the `updatedBy` field

3. **LoanApplicationResponse.java**
   - Added `statusUpdatedBy` field
   - Added `statusUpdatedAt` field

4. **DocumentResponse.java**
   - Added `statusUpdatedBy` field
   - Added `statusUpdatedAt` field

### Protobuf Events
- The existing Protobuf schema already includes `updated_by` field
- Events are published with the officer/user ID who made the change
- Kafka events include complete audit trail information

## Testing the Fixes

### Status Overwrite Test
1. Update a loan/document status to "UNDER_REVIEW" with officer "officer123"
2. Update the same loan/document status to "APPROVED" with officer "manager456"
3. Verify that:
   - Status is "APPROVED" (not "UNDER_REVIEW,APPROVED" or concatenated)
   - `statusUpdatedBy` is "manager456"
   - `statusUpdatedAt` shows the time of the second update

### Officer ID Tracking Test
1. Make status updates with different officer IDs
2. Verify that each update records the correct officer ID
3. Check Kafka events to ensure officer ID is included in published events

## Database Schema Changes Required

You may need to run database migrations to add the new columns:

```sql
-- For loan_applications table
ALTER TABLE loan_applications 
ADD COLUMN status_updated_by VARCHAR(100),
ADD COLUMN status_updated_at TIMESTAMP;

-- For documents table  
ALTER TABLE documents
ADD COLUMN status_updated_by VARCHAR(100),
ADD COLUMN status_updated_at TIMESTAMP;
```

## Kafka Event Structure

The published events now include:
- `application_id` / `document_id`
- `borrower_id`
- `old_status`
- `new_status`
- `updated_by` (Officer/User ID)
- `updated_at`
- `event_id`
- `event_timestamp`
- `rejection_reason` (optional)

This ensures full audit trail and allows consumers to know exactly which officer made each status change.

## Next Steps

1. **Test the endpoints** using the provided HTTP requests
2. **Run database migrations** if needed
3. **Verify Kafka events** are being published correctly
4. **Check that borrower-service** can consume these events properly
5. **Monitor logs** for any issues during status updates

The status concatenation issue has been resolved, and now each status update properly overwrites the previous status while maintaining a complete audit trail of who made each change.
