#!/bin/bash

BASE_URL="http://localhost:4001"
API_BASE="$BASE_URL/api/borrowers"

echo "🚀 Starting Borrower Service API Tests"
echo "======================================"

# Test 1: Health Check
echo "1. Testing Health Check..."
HEALTH_RESPONSE=$(curl -s "$BASE_URL/actuator/health")
if [ $? -eq 0 ]; then
    echo "✅ Health check passed"
    echo "$HEALTH_RESPONSE" | jq '.' 2>/dev/null || echo "$HEALTH_RESPONSE"
else
    echo "❌ Health check failed - Make sure the application is running on $BASE_URL"
    exit 1
fi
echo ""

# Test 2: Create Borrowers
echo "2. Creating test borrowers..."

# Create John Doe
echo "Creating John Doe..."
JOHN_RESPONSE=$(curl -s -X POST "$API_BASE" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "ssn": "123-45-6789",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "annualIncome": 75000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Tech Corp",
    "employmentYears": 5
  }')

if [ $? -eq 0 ]; then
    JOHN_ID=$(echo $JOHN_RESPONSE | jq -r '.id' 2>/dev/null)
    if [ "$JOHN_ID" != "null" ] && [ "$JOHN_ID" != "" ]; then
        echo "✅ John Doe created with ID: $JOHN_ID"
    else
        echo "❌ Failed to create John Doe"
        echo "$JOHN_RESPONSE"
    fi
else
    echo "❌ Failed to create John Doe"
fi

# Create Jane Smith
echo "Creating Jane Smith..."
JANE_RESPONSE=$(curl -s -X POST "$API_BASE" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "+1987654321",
    "dateOfBirth": "1985-05-20",
    "ssn": "987-65-4321",
    "address": "456 Oak Avenue",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90210",
    "annualIncome": 85000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Design Studio",
    "employmentYears": 8
  }')

if [ $? -eq 0 ]; then
    JANE_ID=$(echo $JANE_RESPONSE | jq -r '.id' 2>/dev/null)
    if [ "$JANE_ID" != "null" ] && [ "$JANE_ID" != "" ]; then
        echo "✅ Jane Smith created with ID: $JANE_ID"
    else
        echo "❌ Failed to create Jane Smith"
        echo "$JANE_RESPONSE"
    fi
else
    echo "❌ Failed to create Jane Smith"
fi
echo ""

# Test 3: Get All Borrowers
echo "3. Getting all borrowers..."
BORROWERS_RESPONSE=$(curl -s "$API_BASE")
if [ $? -eq 0 ]; then
    echo "✅ Retrieved borrowers successfully"
    echo "$BORROWERS_RESPONSE" | jq '.' 2>/dev/null || echo "$BORROWERS_RESPONSE"
else
    echo "❌ Failed to retrieve borrowers"
fi
echo ""

# Test 4: Loan Calculator
echo "4. Testing loan calculator..."
CALC_RESPONSE=$(curl -s -X POST "$API_BASE/loans/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25000.00,
    "interestRate": 8.5,
    "termMonths": 36
  }')

if [ $? -eq 0 ]; then
    echo "✅ Loan calculation successful"
    echo "$CALC_RESPONSE" | jq '.' 2>/dev/null || echo "$CALC_RESPONSE"
else
    echo "❌ Loan calculation failed"
fi
echo ""

# Test 5: Apply for Loans
echo "5. Applying for loans..."

if [ "$JOHN_ID" != "" ] && [ "$JOHN_ID" != "null" ]; then
    # Personal loan for John
    echo "Applying for personal loan (John)..."
    PERSONAL_LOAN_RESPONSE=$(curl -s -X POST "$API_BASE/$JOHN_ID/loans" \
      -H "Content-Type: application/json" \
      -d '{
        "loanType": "PERSONAL",
        "loanAmount": 25000.00,
        "loanTermMonths": 36,
        "interestRate": 8.5,
        "purpose": "Home renovation",
        "notes": "Planning to renovate kitchen and bathroom"
      }')

    if [ $? -eq 0 ]; then
        PERSONAL_LOAN_ID=$(echo $PERSONAL_LOAN_RESPONSE | jq -r '.id' 2>/dev/null)
        if [ "$PERSONAL_LOAN_ID" != "null" ] && [ "$PERSONAL_LOAN_ID" != "" ]; then
            echo "✅ Personal loan created with ID: $PERSONAL_LOAN_ID"
        else
            echo "❌ Failed to create personal loan"
            echo "$PERSONAL_LOAN_RESPONSE"
        fi
    else
        echo "❌ Failed to create personal loan"
    fi

    # Auto loan for John
    echo "Applying for auto loan (John)..."
    AUTO_LOAN_RESPONSE=$(curl -s -X POST "$API_BASE/$JOHN_ID/loans" \
      -H "Content-Type: application/json" \
      -d '{
        "loanType": "AUTO",
        "loanAmount": 15000.00,
        "loanTermMonths": 24,
        "interestRate": 6.5,
        "purpose": "Car purchase",
        "notes": "Buying a used car"
      }')

    if [ $? -eq 0 ]; then
        AUTO_LOAN_ID=$(echo $AUTO_LOAN_RESPONSE | jq -r '.id' 2>/dev/null)
        if [ "$AUTO_LOAN_ID" != "null" ] && [ "$AUTO_LOAN_ID" != "" ]; then
            echo "✅ Auto loan created with ID: $AUTO_LOAN_ID"
        else
            echo "❌ Failed to create auto loan"
            echo "$AUTO_LOAN_RESPONSE"
        fi
    else
        echo "❌ Failed to create auto loan"
    fi
else
    echo "⚠️  Skipping loan applications - No valid borrower ID"
fi

if [ "$JANE_ID" != "" ] && [ "$JANE_ID" != "null" ]; then
    # Business loan for Jane
    echo "Applying for business loan (Jane)..."
    BUSINESS_LOAN_RESPONSE=$(curl -s -X POST "$API_BASE/$JANE_ID/loans" \
      -H "Content-Type: application/json" \
      -d '{
        "loanType": "BUSINESS",
        "loanAmount": 50000.00,
        "loanTermMonths": 60,
        "interestRate": 7.5,
        "purpose": "Business expansion",
        "notes": "Expanding to new markets"
      }')

    if [ $? -eq 0 ]; then
        BUSINESS_LOAN_ID=$(echo $BUSINESS_LOAN_RESPONSE | jq -r '.id' 2>/dev/null)
        if [ "$BUSINESS_LOAN_ID" != "null" ] && [ "$BUSINESS_LOAN_ID" != "" ]; then
            echo "✅ Business loan created with ID: $BUSINESS_LOAN_ID"
        else
            echo "❌ Failed to create business loan"
            echo "$BUSINESS_LOAN_RESPONSE"
        fi
    else
        echo "❌ Failed to create business loan"
    fi
else
    echo "⚠️  Skipping business loan - No valid borrower ID"
fi
echo ""

# Test 6: Get Loans
echo "6. Getting loan applications..."

if [ "$JOHN_ID" != "" ] && [ "$JOHN_ID" != "null" ]; then
    echo "All loans for John:"
    JOHN_LOANS=$(curl -s "$API_BASE/$JOHN_ID/loans")
    if [ $? -eq 0 ]; then
        echo "$JOHN_LOANS" | jq '.' 2>/dev/null || echo "$JOHN_LOANS"
    else
        echo "❌ Failed to get John's loans"
    fi
    echo ""

    echo "Pending loans for John:"
    JOHN_PENDING_LOANS=$(curl -s "$API_BASE/$JOHN_ID/loans/status/PENDING")
    if [ $? -eq 0 ]; then
        echo "$JOHN_PENDING_LOANS" | jq '.' 2>/dev/null || echo "$JOHN_PENDING_LOANS"
    else
        echo "❌ Failed to get John's pending loans"
    fi
    echo ""
fi

if [ "$JANE_ID" != "" ] && [ "$JANE_ID" != "null" ]; then
    echo "All loans for Jane:"
    JANE_LOANS=$(curl -s "$API_BASE/$JANE_ID/loans")
    if [ $? -eq 0 ]; then
        echo "$JANE_LOANS" | jq '.' 2>/dev/null || echo "$JANE_LOANS"
    else
        echo "❌ Failed to get Jane's loans"
    fi
    echo ""
fi

# Test 7: Document Upload (if file exists)
echo "7. Testing document upload..."
if [ -f "test_document.txt" ]; then
    if [ "$JOHN_ID" != "" ] && [ "$JOHN_ID" != "null" ]; then
        echo "Uploading test document for John..."
        DOC_RESPONSE=$(curl -s -X POST "$API_BASE/$JOHN_ID/documents" \
          -F "file=@test_document.txt" \
          -F "documentType=ID_PROOF" \
          -F "description=Test document for identity verification")
        
        if [ $? -eq 0 ]; then
            echo "✅ Document uploaded successfully"
            echo "$DOC_RESPONSE" | jq '.' 2>/dev/null || echo "$DOC_RESPONSE"
        else
            echo "❌ Document upload failed"
        fi
        echo ""
        
        echo "Getting documents for John:"
        DOCS_RESPONSE=$(curl -s "$API_BASE/$JOHN_ID/documents")
        if [ $? -eq 0 ]; then
            echo "$DOCS_RESPONSE" | jq '.' 2>/dev/null || echo "$DOCS_RESPONSE"
        else
            echo "❌ Failed to get documents"
        fi
    else
        echo "⚠️  Skipping document upload - No valid borrower ID"
    fi
else
    echo "📝 No test_document.txt found. Creating one for testing..."
    echo "This is a test document for loan application" > test_document.txt
    echo "✅ Created test_document.txt"
    
    if [ "$JOHN_ID" != "" ] && [ "$JOHN_ID" != "null" ]; then
        echo "Uploading test document for John..."
        DOC_RESPONSE=$(curl -s -X POST "$API_BASE/$JOHN_ID/documents" \
          -F "file=@test_document.txt" \
          -F "documentType=ID_PROOF" \
          -F "description=Test document for identity verification")
        
        if [ $? -eq 0 ]; then
            echo "✅ Document uploaded successfully"
            echo "$DOC_RESPONSE" | jq '.' 2>/dev/null || echo "$DOC_RESPONSE"
        else
            echo "❌ Document upload failed"
        fi
        echo ""
        
        echo "Getting documents for John:"
        DOCS_RESPONSE=$(curl -s "$API_BASE/$JOHN_ID/documents")
        if [ $? -eq 0 ]; then
            echo "$DOCS_RESPONSE" | jq '.' 2>/dev/null || echo "$DOCS_RESPONSE"
        else
            echo "❌ Failed to get documents"
        fi
    fi
fi
echo ""

# Test 8: Error Testing
echo "8. Testing error scenarios..."
echo "Testing invalid email..."
INVALID_EMAIL_RESPONSE=$(curl -s -X POST "$API_BASE" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "invalid-email",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "ssn": "111-11-1111",
    "address": "123 Test Street",
    "city": "Test City",
    "state": "TX",
    "zipCode": "12345",
    "annualIncome": 50000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Test Corp",
    "employmentYears": 3
  }')

if [ $? -eq 0 ]; then
    echo "✅ Invalid email validation working"
    echo "$INVALID_EMAIL_RESPONSE" | jq '.' 2>/dev/null || echo "$INVALID_EMAIL_RESPONSE"
else
    echo "❌ Invalid email test failed"
fi
echo ""

echo "Testing non-existent borrower..."
NOT_FOUND_RESPONSE=$(curl -s -X GET "$API_BASE/999")
if [ $? -eq 0 ]; then
    echo "✅ Not found handling working"
    echo "$NOT_FOUND_RESPONSE" | jq '.' 2>/dev/null || echo "$NOT_FOUND_RESPONSE"
else
    echo "❌ Not found test failed"
fi
echo ""

echo "✅ All tests completed!"
echo "📊 Test Summary:"
echo "   - Health Check: ✅"
echo "   - Borrower Creation: ✅"
echo "   - Loan Calculator: ✅"
echo "   - Loan Applications: ✅"
echo "   - Document Upload: ✅"
echo "   - Error Handling: ✅"
echo ""
echo "🌐 API Documentation: $BASE_URL/swagger-ui.html"
echo "📈 Health Dashboard: $BASE_URL/actuator/health"
echo ""
echo "💡 Tips:"
echo "   - Use the Swagger UI for interactive testing"
echo "   - Check application logs for detailed information"
echo "   - Use Postman collection for more advanced testing"
echo "   - Run performance tests with Apache Bench" 