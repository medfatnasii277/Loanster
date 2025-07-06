#!/bin/bash

echo "=== Debug Authentication Test ==="

# Step 1: Register a fresh user
echo "1. Registering a fresh user..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:4005/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "debug-user@test.com",
    "password": "password123",
    "role": "BORROWER",
    "fullName": "Debug User",
    "dateOfBirth": "1990-01-01"
  }')
echo "Register Response: $REGISTER_RESPONSE"

# Step 2: Login to get a fresh token
echo -e "\n2. Getting fresh token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:4005/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "debug-user@test.com",
    "password": "password123"
  }')
echo "Login Response: $LOGIN_RESPONSE"

# Extract token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Extracted Token: $TOKEN"

if [ -z "$TOKEN" ]; then
  echo "ERROR: Failed to get token"
  exit 1
fi

# Step 3: Test direct auth service validation
echo -e "\n3. Testing direct auth service validation..."
AUTH_RESPONSE=$(curl -s -w "%{http_code}" http://localhost:4005/validate/borrower \
  -H "Authorization: Bearer $TOKEN")
echo "Direct Auth Response Code: $AUTH_RESPONSE"

# Step 4: Test API Gateway
echo -e "\n4. Testing API Gateway..."
GATEWAY_RESPONSE=$(curl -s -v http://localhost:4000/api/borrowers \
  -H "Authorization: Bearer $TOKEN" 2>&1)
echo "Gateway Response: $GATEWAY_RESPONSE"

echo -e "\n=== Test Complete ==="
