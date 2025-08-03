# Kafka Event Issues - Officer Module Not Receiving Documents & Loan Applications

## 🔍 Root Causes Identified

### 1. **Missing Kafka Topic Configurations in Borrower Service**
**Problem**: The borrower service `KafkaEventProducerService` was trying to inject topic names via `@Value` annotations, but the properties were missing from `application.properties`.

**Missing Properties**:
```properties
kafka.topics.borrower-created=borrower-created
kafka.topics.loan-application=loan-application  
kafka.topics.documents-upload=documents-upload
```

**Impact**: 
- ✅ Borrower events worked (likely due to different creation mechanism)
- ❌ Loan application events failed silently
- ❌ Document upload events failed silently

**Fix Applied**: Added missing topic configurations to borrower service `application.properties`

### 2. **Missing Kafka Bootstrap Servers Configuration**
**Problem**: The borrower service was missing the `spring.kafka.bootstrap-servers` configuration.

**Fix Applied**: Added `spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`

### 3. **Environment-Specific Bootstrap Server Configuration**
**Problem**: 
- Officer service was hardcoded to `kafka:9092` (Docker container name)
- Borrower service was defaulting to `localhost:9092`

**Fix Applied**: Made officer service configuration flexible: `${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}`

## 🛠️ Changes Made

### Borrower Service (`borrower-service/src/main/resources/application.properties`)
```properties
# Added Kafka Configuration
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

# Added missing outbound topic configurations
kafka.topics.borrower-created=borrower-created
kafka.topics.loan-application=loan-application
kafka.topics.documents-upload=documents-upload
```

### Officer Service (`officer-service/src/main/resources/application.properties`)
```properties
# Made bootstrap servers configuration flexible
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
```

## 🧪 Testing & Verification

### Manual Testing Script
Run the debugging script: `./debug-kafka-events.sh`

This will:
1. Check Kafka container status
2. List available topics
3. Show consumer group details
4. Display recent messages from each topic

### Expected Behavior After Fix
1. **Loan Application Submission**: Should publish events to `loan-application` topic
2. **Document Upload**: Should publish events to `documents-upload` topic  
3. **Officer Service**: Should receive and process both event types

### Service Restart Required
After configuration changes, restart both services:
```bash
# If using Docker Compose
docker-compose restart borrower-service officer-service

# If running individually
# Restart borrower-service and officer-service
```

## 🔍 Monitoring & Debugging

### Check Service Logs
```bash
# Borrower service logs (for publishing events)
docker logs borrower-service | grep -i kafka

# Officer service logs (for consuming events)  
docker logs officer-service | grep -i kafka
```

### Verify Event Publishing
Look for these log messages in borrower service:
- "Publishing loan application event for application ID: {}"
- "Successfully published loan application event..."
- "Publishing document upload event for document ID: {}"
- "Successfully published document upload event..."

### Verify Event Consumption
Look for these log messages in officer service:
- "Received loan application event from topic: loan-application"
- "Successfully processed loan application event..."
- "Received document upload event from topic: documents-upload"
- "Successfully processed document upload event..."

## 🚨 Potential Additional Issues

If problems persist, check:

1. **Kafka Topic Creation**: Topics might need to be manually created
2. **Network Connectivity**: Services might not be able to reach Kafka broker
3. **Serialization**: Protobuf serialization/deserialization issues
4. **Consumer Group Offsets**: Officer service might be reading from wrong offset
5. **Service Startup Order**: Kafka should be started before the services

## 🎯 Next Steps

1. **Restart Services**: Apply the configuration changes
2. **Run Debug Script**: Execute `./debug-kafka-events.sh` to verify setup
3. **Test End-to-End**: Submit loan application and upload document
4. **Monitor Logs**: Check both services for Kafka-related messages
5. **Verify Officer Dashboard**: Check if loan applications and documents appear in officer interface
