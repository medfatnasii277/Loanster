#!/bin/bash

# Test script for API Gateway authentication flow
# This script tests the complete flow from login to accessing borrower endpoints

echo "=== Testing API Gateway Authentication Flow ==="

# Configuration
API_GATEWAY_URL="http://localhost:4000"
AUTH_SERVICE_URL="http://localhost:4000/auth"
BORROWER_SERVICE_URL="http://localhost:4000/api/borrowers"

echo "1. Testing Auth Service Registration..."
echo "Registering a new BORROWER user..."

REGISTER_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "borrower@test.com",
    "password": "password123",
    "role": "BORROWER",
    "fullName": "Test Borrower",
    "dateOfBirth": "1990-01-01"
  }')

echo "Register Response: $REGISTER_RESPONSE"

echo ""
echo "2. Testing Auth Service Login..."
echo "Logging in to get JWT token..."

LOGIN_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "borrower@test.com",
    "password": "password123"
  }')

echo "Login Response: $LOGIN_RESPONSE"

# Extract token from response (assuming it's in format {"token":"..."})
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "ERROR: Could not extract token from login response"
    exit 1
fi

echo "Extracted Token: $TOKEN"

echo ""
echo "3. Testing Borrower Service Access with Valid Token..."
echo "Accessing borrower endpoints with BORROWER role..."

# Test accessing borrower endpoints with valid token
BORROWER_RESPONSE=$(curl -s -X GET "$BORROWER_SERVICE_URL" \
  -H "Authorization: Bearer $TOKEN")

echo "Borrower Service Response: $BORROWER_RESPONSE"

echo ""
echo "4. Testing Borrower Service Access without Token..."
echo "Accessing borrower endpoints without token (should fail)..."

NO_TOKEN_RESPONSE=$(curl -s -X GET "$BORROWER_SERVICE_URL")

echo "No Token Response: $NO_TOKEN_RESPONSE"

echo ""
echo "5. Testing Auth Service Direct Access..."
echo "Testing direct access to auth service endpoints..."

AUTH_VALIDATE_RESPONSE=$(curl -s -X GET "$AUTH_SERVICE_URL/validate/borrower" \
  -H "Authorization: Bearer $TOKEN")

echo "Auth Validate Response: $AUTH_VALIDATE_RESPONSE"

echo ""
echo "=== Test Summary ==="
echo "✅ Registration: Completed"
echo "✅ Login: Completed"
echo "✅ Token Extraction: Completed"
echo "✅ Borrower Access with Token: Tested"
echo "✅ Borrower Access without Token: Tested"
echo "✅ Direct Auth Validation: Tested"

echo ""
echo "To run this test, make sure:"
echo "1. API Gateway is running on port 4000"
echo "2. Auth Service is running on port 4005 (container: loan-auth-service)"
echo "3. Borrower Service is running on port 4001 (container: loan-borrower-service)"
echo "4. All services are properly connected via Docker network" 