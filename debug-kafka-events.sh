#!/bin/bash

echo "🔍 Kafka Debugging Script for Officer Module Events"
echo "=================================================="

# Check if Kafka container is running
echo "1. Checking Kafka container status..."
docker ps | grep kafka

echo -e "\n2. Listing available Kafka topics..."
docker exec -it $(docker ps -q --filter "name=kafka") kafka-topics --list --bootstrap-server localhost:9092

echo -e "\n3. Checking consumer groups..."
docker exec -it $(docker ps -q --filter "name=kafka") kafka-consumer-groups --bootstrap-server localhost:9092 --list

echo -e "\n4. Checking officer-service-group details..."
docker exec -it $(docker ps -q --filter "name=kafka") kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group officer-service-group

echo -e "\n5. Testing loan-application topic (last 10 messages)..."
timeout 5s docker exec -it $(docker ps -q --filter "name=kafka") kafka-console-consumer --bootstrap-server localhost:9092 --topic loan-application --from-beginning --max-messages 10 || echo "No messages or timeout"

echo -e "\n6. Testing documents-upload topic (last 10 messages)..."
timeout 5s docker exec -it $(docker ps -q --filter "name=kafka") kafka-console-consumer --bootstrap-server localhost:9092 --topic documents-upload --from-beginning --max-messages 10 || echo "No messages or timeout"

echo -e "\n7. Testing borrower-created topic (last 10 messages)..."
timeout 5s docker exec -it $(docker ps -q --filter "name=kafka") kafka-console-consumer --bootstrap-server localhost:9092 --topic borrower-created --from-beginning --max-messages 10 || echo "No messages or timeout"

echo -e "\n🎯 Debugging Tips:"
echo "- If topics don't exist, they need to be created"
echo "- If no messages appear, check if borrower-service is publishing"
echo "- If officer-service-group shows no activity, check if officer-service is consuming"
echo "- Check service logs for any Kafka connection errors"
