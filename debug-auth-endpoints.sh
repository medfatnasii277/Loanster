#!/bin/bash

# Debug script for auth service endpoints
# This script helps diagnose issues with auth service endpoints

echo "=== Debugging Auth Service Endpoints ==="

# Configuration
API_GATEWAY_URL="http://localhost:4000"
AUTH_SERVICE_URL="http://localhost:4000/auth"

echo "1. Testing API Gateway Health..."
echo "Checking if API Gateway is responding..."

GATEWAY_HEALTH=$(curl -s -w "%{http_code}" "$API_GATEWAY_URL/actuator/health" -o /dev/null)
echo "Gateway Health Status: $GATEWAY_HEALTH"

echo ""
echo "2. Testing Direct Auth Service Access (if accessible)..."
echo "Trying to access auth service directly on port 4005..."

DIRECT_AUTH=$(curl -s -w "%{http_code}" "http://localhost:4005/actuator/health" -o /dev/null)
echo "Direct Auth Service Status: $DIRECT_AUTH"

echo ""
echo "3. Testing Auth Endpoints via API Gateway..."

echo "Testing /auth/register endpoint..."
REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -X POST "$AUTH_SERVICE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "role": "BORROWER",
    "fullName": "Test User",
    "dateOfBirth": "1990-01-01"
  }' -o /dev/null)

echo "Register Endpoint Status: $REGISTER_RESPONSE"

echo ""
echo "Testing /auth/login endpoint..."
LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -X POST "$AUTH_SERVICE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' -o /dev/null)

echo "Login Endpoint Status: $LOGIN_RESPONSE"

echo ""
echo "4. Testing with Verbose Output..."
echo "Detailed response from /auth/register:"

VERBOSE_RESPONSE=$(curl -v -X POST "$AUTH_SERVICE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "verbose@example.com",
    "password": "password123",
    "role": "BORROWER",
    "fullName": "Verbose User",
    "dateOfBirth": "1990-01-01"
  }' 2>&1)

echo "$VERBOSE_RESPONSE"

echo ""
echo "=== Debug Summary ==="
echo "Gateway Health: $GATEWAY_HEALTH"
echo "Direct Auth: $DIRECT_AUTH"
echo "Register Endpoint: $REGISTER_RESPONSE"
echo "Login Endpoint: $LOGIN_RESPONSE"

echo ""
echo "=== Common Issues and Solutions ==="
echo "1. If Gateway Health is not 200: API Gateway is not running properly"
echo "2. If Direct Auth is not 200: Auth service is not accessible on port 4005"
echo "3. If endpoints return 500: Check auth service logs for errors"
echo "4. If endpoints return 404: Check API Gateway routing configuration"
echo "5. If endpoints return 502/503: Check Docker network connectivity"

echo ""
echo "=== Next Steps ==="
echo "1. Check Docker container status: docker ps"
echo "2. Check API Gateway logs: docker logs <api-gateway-container>"
echo "3. Check Auth Service logs: docker logs <loan-auth-service-container>"
echo "4. Verify Docker network: docker network ls"
echo "5. Test container connectivity: docker exec <container> ping <other-container>" 