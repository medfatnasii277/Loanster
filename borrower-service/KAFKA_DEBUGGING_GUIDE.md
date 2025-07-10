# Kafka Consumer Debugging Checklist

## Issues Found and Fixed:

### ✅ 1. Missing Kafka Bootstrap Servers
- **Problem**: `spring.kafka.bootstrap-servers` was missing from application.properties
- **Solution**: Added `spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`
- **Impact**: Without this, the consumer can't connect to Kafka at all

### ✅ 2. Missing @EnableKafka Annotation  
- **Problem**: Main application class didn't have `@EnableKafka`
- **Solution**: Added `@EnableKafka` to `BorrowerServiceApplication.java`
- **Impact**: Spring Boot won't initialize Kafka listeners without this

## Additional Debugging Steps:

### Step 1: Verify Kafka Connection
Run this to check if borrower service can connect to Kafka:
```bash
# Start borrower service and check logs for Kafka connection
tail -f logs/borrower-service.log | grep -i kafka
```

Look for logs like:
- `Consumer group coordinator is available`
- `Successfully joined group with generation`
- `Successfully synced group in generation`

### Step 2: Test Kafka Topic Existence
Verify the topics exist in Kafka:
```bash
# List Kafka topics
docker exec -it <kafka-container> kafka-topics --list --bootstrap-server localhost:9092

# Should show:
# loan-status
# documents-status
```

### Step 3: Verify Officer Service is Publishing
Check officer service logs when you update status:
```bash
tail -f logs/officer-service.log | grep -i "publish"
```

Should see logs like:
```
Successfully published loan status update event for application ID: 1 to topic: loan-status
```

### Step 4: Check Consumer Group Status
```bash
# Check if borrower service is part of consumer group
docker exec -it <kafka-container> kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group borrower-service-group
```

### Step 5: Manual Message Test
Send a test message to verify consumer is working:
```bash
# Send test message to loan-status topic
docker exec -it <kafka-container> kafka-console-producer --bootstrap-server localhost:9092 --topic loan-status
```

### Step 6: Verify Protobuf Classes
Ensure protobuf classes are generated correctly:
```bash
ls -la borrower-service/target/generated-sources/protobuf/java/com/pm/borrowerservice/events/
```

Should contain:
- LoanStatusUpdateEvent.java
- DocumentStatusUpdateEvent.java

## Common Issues and Solutions:

### Issue: Consumer Not Joining Group
**Symptoms**: No "joining group" messages in logs
**Causes**: 
- Kafka bootstrap servers wrong/missing
- Network connectivity issues
- Consumer group ID conflicts

**Solutions**:
- Verify `spring.kafka.bootstrap-servers` points to correct Kafka instance
- Check Docker network connectivity
- Try different consumer group ID

### Issue: Consumer Joins but No Messages
**Symptoms**: Consumer joins group but no message processing logs
**Causes**:
- Topic names don't match between producer and consumer
- Producer not actually publishing
- Messages consumed but processing fails

**Solutions**:
- Verify topic names match exactly
- Check producer logs for successful publishing
- Look for exception logs in consumer

### Issue: Protobuf Parse Errors
**Symptoms**: "Error processing...event" in logs
**Causes**:
- Protobuf schema mismatch between services
- Wrong serialization format

**Solutions**:
- Ensure both services use same .proto file
- Verify ByteArraySerializer/Deserializer usage

### Issue: Entity Not Found
**Symptoms**: "Loan application with ID X not found"
**Causes**:
- Different databases between services
- Entity IDs don't match
- Consumer processing before entity creation

**Solutions**:
- Verify both services use same database/ID strategy
- Check entity creation timing
- Add retry logic for eventual consistency

## Debug Configuration

Add these properties to borrower service for detailed debugging:

```properties
# Enable Kafka debug logging
logging.level.org.apache.kafka=DEBUG
logging.level.org.springframework.kafka=DEBUG
logging.level.com.pm.borrowerservice.service.OfficerEventConsumerService=DEBUG

# Kafka consumer debug settings
spring.kafka.consumer.properties.enable.auto.commit=false
spring.kafka.consumer.properties.session.timeout.ms=30000
spring.kafka.consumer.properties.heartbeat.interval.ms=10000
```

## Test Commands

### 1. Test Officer Service Status Update
```bash
curl -X PUT http://localhost:8083/api/admin/loans/1/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "UNDER_REVIEW", "updatedBy": "officer123"}'
```

### 2. Check Borrower Service Received Update
```bash
curl http://localhost:4001/api/borrowers/1/loan-applications
```

### 3. Monitor Kafka Messages
```bash
# Monitor loan-status topic
docker exec -it <kafka-container> kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic loan-status \
  --from-beginning
```

## Expected Log Flow

When working correctly, you should see this sequence:

1. **Officer Service**: "Updating loan status for application ID: 1"
2. **Officer Service**: "Successfully published loan status update event"
3. **Borrower Service**: "Received loan status update event: applicationId=1"
4. **Borrower Service**: "Successfully updated loan application 1 status"

If any step is missing, that's where the issue lies.
