#!/bin/bash

# Test script for Circuit Breaker functionality
# This script tests the officer service circuit breaker when loan score service is down

echo "=== Circuit Breaker Test Script ==="
echo ""

OFFICER_SERVICE_URL="http://localhost:4002"
LOAN_SCORE_SERVICE_URL="http://localhost:4003"
APPLICATION_ID="1"

# Function to test endpoint
test_endpoint() {
    local url=$1
    local description=$2
    echo "Testing: $description"
    echo "URL: $url"
    
    response=$(curl -s -w "HTTP_STATUS:%{http_code}" "$url" 2>/dev/null)
    http_code=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    echo "HTTP Status: $http_code"
    if [ "$http_code" = "200" ]; then
        echo "✅ Success"
        echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
    elif [ "$http_code" = "404" ]; then
        echo "❌ Not Found (404) - Service might be down"
    elif [ "$http_code" = "503" ]; then
        echo "⚠️  Service Unavailable (503) - Circuit breaker activated"
        echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
    else
        echo "❌ Failed with status $http_code"
        echo "Response: $body"
    fi
    echo ""
}

# Check if services are running
echo "1. Checking if services are running..."
echo ""

test_endpoint "$OFFICER_SERVICE_URL/actuator/health" "Officer Service Health"
test_endpoint "$LOAN_SCORE_SERVICE_URL/actuator/health" "Loan Score Service Health"

echo "2. Testing loan score retrieval..."
echo ""

test_endpoint "$OFFICER_SERVICE_URL/admin/loans/$APPLICATION_ID/score" "Get Loan Score (Normal)"

echo "3. Testing circuit breaker endpoint..."
echo ""

test_endpoint "$OFFICER_SERVICE_URL/admin/scores/test-circuit-breaker/$APPLICATION_ID" "Test Circuit Breaker"

echo "4. Testing service status..."
echo ""

test_endpoint "$OFFICER_SERVICE_URL/admin/scores/service-status" "Service Status Check"

echo "=== Test Complete ==="
echo ""
echo "To test circuit breaker when service is down:"
echo "1. Stop the loan score service"
echo "2. Run this script again"
echo "3. Observe the circuit breaker fallback responses"
