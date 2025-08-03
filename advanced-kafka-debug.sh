#!/bin/bash

echo "🔍 Advanced Kafka Debugging for Officer Events"
echo "=============================================="

# Function to run kafka commands
run_kafka_cmd() {
    docker exec -it $(docker ps --filter "name=kafka" --format "{{.Names}}" | head -1) "$@" 2>/dev/null
}

echo "1. Checking Kafka container..."
KAFKA_CONTAINER=$(docker ps --filter "name=kafka" --format "{{.Names}}" | head -1)
if [ -z "$KAFKA_CONTAINER" ]; then
    echo "❌ No Kafka container found. Please start your Docker services."
    exit 1
fi
echo "✅ Found Kafka container: $KAFKA_CONTAINER"

echo -e "\n2. Listing all topics..."
run_kafka_cmd kafka-topics --list --bootstrap-server localhost:9092

echo -e "\n3. Checking topic configurations..."
echo "📊 borrower-created topic:"
run_kafka_cmd kafka-topics --describe --topic borrower-created --bootstrap-server localhost:9092

echo "📊 loan-application topic:"
run_kafka_cmd kafka-topics --describe --topic loan-application --bootstrap-server localhost:9092

echo "📊 documents-upload topic:"
run_kafka_cmd kafka-topics --describe --topic documents-upload --bootstrap-server localhost:9092

echo -e "\n4. Checking consumer group details..."
run_kafka_cmd kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group officer-service-group

echo -e "\n5. Testing message consumption from each topic..."
echo "📥 Checking borrower-created messages (5 seconds timeout):"
timeout 5s run_kafka_cmd kafka-console-consumer --bootstrap-server localhost:9092 --topic borrower-created --from-beginning --max-messages 5 2>/dev/null || echo "No messages or topic doesn't exist"

echo "📥 Checking loan-application messages (5 seconds timeout):"
timeout 5s run_kafka_cmd kafka-console-consumer --bootstrap-server localhost:9092 --topic loan-application --from-beginning --max-messages 5 2>/dev/null || echo "No messages or topic doesn't exist"

echo "📥 Checking documents-upload messages (5 seconds timeout):"
timeout 5s run_kafka_cmd kafka-console-consumer --bootstrap-server localhost:9092 --topic documents-upload --from-beginning --max-messages 5 2>/dev/null || echo "No messages or topic doesn't exist"

echo -e "\n6. Service connectivity test..."
echo "📡 Checking if borrower-service can connect to Kafka..."
BORROWER_CONTAINER=$(docker ps --filter "name=borrower" --format "{{.Names}}" | head -1)
if [ ! -z "$BORROWER_CONTAINER" ]; then
    echo "Borrower service container: $BORROWER_CONTAINER"
    docker logs $BORROWER_CONTAINER --tail 20 | grep -i kafka || echo "No recent Kafka logs in borrower service"
else
    echo "❌ Borrower service container not found"
fi

echo "📡 Checking if officer-service can connect to Kafka..."
OFFICER_CONTAINER=$(docker ps --filter "name=officer" --format "{{.Names}}" | head -1)
if [ ! -z "$OFFICER_CONTAINER" ]; then
    echo "Officer service container: $OFFICER_CONTAINER"
    docker logs $OFFICER_CONTAINER --tail 20 | grep -i kafka || echo "No recent Kafka logs in officer service"
else
    echo "❌ Officer service container not found"
fi

echo -e "\n🔍 Manual Test Commands:"
echo "To manually publish a test message to loan-application topic:"
echo "docker exec -it $KAFKA_CONTAINER kafka-console-producer --bootstrap-server localhost:9092 --topic loan-application"
echo ""
echo "To manually consume from loan-application topic:"
echo "docker exec -it $KAFKA_CONTAINER kafka-console-consumer --bootstrap-server localhost:9092 --topic loan-application --from-beginning"
echo ""
echo "To check service logs:"
echo "docker logs $BORROWER_CONTAINER | grep -i 'publishing.*loan.*application'"
echo "docker logs $OFFICER_CONTAINER | grep -i 'received.*loan.*application'"
