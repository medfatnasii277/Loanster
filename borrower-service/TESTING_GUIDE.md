# Testing Guide for Borrower Service API

## Overview
This guide provides comprehensive testing instructions for the Borrower Service API using various tools and mock data examples.

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL database (or use H2 for testing)
- cURL, Postman, or any HTTP client

## Quick Start Testing

### 1. Start the Application
```bash
# Build and run the application
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:4001`

### 2. Verify Application Health
```bash
curl http://localhost:4001/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 3. Access Swagger UI
Open your browser and navigate to: `http://localhost:4001/swagger-ui.html`

## Complete Testing Workflow

### Step 1: Create Test Borrowers

#### Create First Borrower
```bash
curl -X POST http://localhost:4001/api/borrowers \
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
  }'
```

#### Create Second Borrower
```bash
curl -X POST http://localhost:4001/api/borrowers \
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
  }'
```

#### Create Third Borrower
```bash
curl -X POST http://localhost:4001/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Mike",
    "lastName": "Johnson",
    "email": "mike.johnson@example.com",
    "phoneNumber": "+1555123456",
    "dateOfBirth": "1988-12-10",
    "ssn": "456-78-9012",
    "address": "789 Pine Road",
    "city": "Chicago",
    "state": "IL",
    "zipCode": "60601",
    "annualIncome": 65000.0,
    "employmentStatus": "SELF_EMPLOYED",
    "employerName": "Freelance Consultant",
    "employmentYears": 3
  }'
```

### Step 2: Retrieve Borrowers

#### Get All Borrowers
```bash
curl -X GET http://localhost:4001/api/borrowers
```

#### Get Specific Borrower
```bash
curl -X GET http://localhost:4001/api/borrowers/1
```

### Step 3: Test Loan Calculator

#### Calculate Personal Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/loans/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25000.00,
    "interestRate": 8.5,
    "termMonths": 36
  }'
```

#### Calculate Auto Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/loans/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15000.00,
    "interestRate": 6.5,
    "termMonths": 24
  }'
```

#### Calculate Business Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/loans/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50000.00,
    "interestRate": 7.5,
    "termMonths": 60
  }'
```

### Step 4: Apply for Loans

#### Apply for Personal Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/1/loans \
  -H "Content-Type: application/json" \
  -d '{
    "loanType": "PERSONAL",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom"
  }'
```

#### Apply for Auto Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/1/loans \
  -H "Content-Type: application/json" \
  -d '{
    "loanType": "AUTO",
    "loanAmount": 15000.00,
    "loanTermMonths": 24,
    "interestRate": 6.5,
    "purpose": "Car purchase",
    "notes": "Buying a used car"
  }'
```

#### Apply for Business Loan
```bash
curl -X POST http://localhost:4001/api/borrowers/2/loans \
  -H "Content-Type: application/json" \
  -d '{
    "loanType": "BUSINESS",
    "loanAmount": 50000.00,
    "loanTermMonths": 60,
    "interestRate": 7.5,
    "purpose": "Business expansion",
    "notes": "Expanding to new markets"
  }'
```

### Step 5: Retrieve Loan Applications

#### Get All Loans for Borrower
```bash
curl -X GET http://localhost:4001/api/borrowers/1/loans
```

#### Get Specific Loan Application
```bash
curl -X GET http://localhost:4001/api/borrowers/1/loans/1
```

#### Get Loans by Status
```bash
curl -X GET http://localhost:4001/api/borrowers/1/loans/status/PENDING
```

### Step 6: Test Document Upload

#### Create Test PDF File
First, create a simple test PDF file or use any existing PDF:

```bash
# Create a simple text file for testing (if you don't have a PDF)
echo "This is a test document for loan application" > test_document.txt
```

#### Upload Identity Document
```bash
curl -X POST http://localhost:4001/api/borrowers/1/documents \
  -F "file=@test_document.txt" \
  -F "documentType=ID_PROOF" \
  -F "description=Driver's license for identity verification"
```

#### Upload Income Proof
```bash
curl -X POST http://localhost:4001/api/borrowers/1/documents \
  -F "file=@test_document.txt" \
  -F "documentType=INCOME_PROOF" \
  -F "description=Latest paystub for income verification" \
  -F "loanApplicationId=1"
```

### Step 7: Retrieve Documents

#### Get All Documents for Borrower
```bash
curl -X GET http://localhost:4001/api/borrowers/1/documents
```

### Step 8: Clean Up (Optional)

#### Delete Document
```bash
curl -X DELETE http://localhost:4001/api/borrowers/1/documents/1
```

#### Delete Borrower
```bash
curl -X DELETE http://localhost:4001/api/borrowers/3
```

## Mock Data Sets

### Complete Borrower Dataset
```json
[
  {
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
  },
  {
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
  },
  {
    "firstName": "Mike",
    "lastName": "Johnson",
    "email": "mike.johnson@example.com",
    "phoneNumber": "+1555123456",
    "dateOfBirth": "1988-12-10",
    "ssn": "456-78-9012",
    "address": "789 Pine Road",
    "city": "Chicago",
    "state": "IL",
    "zipCode": "60601",
    "annualIncome": 65000.0,
    "employmentStatus": "SELF_EMPLOYED",
    "employerName": "Freelance Consultant",
    "employmentYears": 3
  },
  {
    "firstName": "Sarah",
    "lastName": "Wilson",
    "email": "sarah.wilson@example.com",
    "phoneNumber": "+1444333222",
    "dateOfBirth": "1992-08-25",
    "ssn": "111-22-3333",
    "address": "321 Elm Street",
    "city": "Houston",
    "state": "TX",
    "zipCode": "77001",
    "annualIncome": 95000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Energy Corp",
    "employmentYears": 6
  },
  {
    "firstName": "David",
    "lastName": "Brown",
    "email": "david.brown@example.com",
    "phoneNumber": "+1777888999",
    "dateOfBirth": "1983-03-12",
    "ssn": "555-66-7777",
    "address": "654 Maple Drive",
    "city": "Phoenix",
    "state": "AZ",
    "zipCode": "85001",
    "annualIncome": 70000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Healthcare Inc",
    "employmentYears": 4
  }
]
```

### Complete Loan Application Dataset
```json
[
  {
    "loanType": "PERSONAL",
    "loanAmount": 25000.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom"
  },
  {
    "loanType": "AUTO",
    "loanAmount": 15000.00,
    "loanTermMonths": 24,
    "interestRate": 6.5,
    "purpose": "Car purchase",
    "notes": "Buying a used car"
  },
  {
    "loanType": "BUSINESS",
    "loanAmount": 50000.00,
    "loanTermMonths": 60,
    "interestRate": 7.5,
    "purpose": "Business expansion",
    "notes": "Expanding to new markets"
  },
  {
    "loanType": "MORTGAGE",
    "loanAmount": 200000.00,
    "loanTermMonths": 360,
    "interestRate": 4.5,
    "purpose": "Home purchase",
    "notes": "Buying first home"
  },
  {
    "loanType": "STUDENT",
    "loanAmount": 30000.00,
    "loanTermMonths": 120,
    "interestRate": 5.5,
    "purpose": "Education",
    "notes": "Graduate school tuition"
  }
]
```

### Loan Calculation Test Cases
```json
[
  {
    "name": "Personal Loan - Low Amount",
    "amount": 5000.00,
    "interestRate": 10.0,
    "termMonths": 12
  },
  {
    "name": "Personal Loan - Medium Amount",
    "amount": 25000.00,
    "interestRate": 8.5,
    "termMonths": 36
  },
  {
    "name": "Auto Loan",
    "amount": 15000.00,
    "interestRate": 6.5,
    "termMonths": 24
  },
  {
    "name": "Business Loan",
    "amount": 50000.00,
    "interestRate": 7.5,
    "termMonths": 60
  },
  {
    "name": "Mortgage Loan",
    "amount": 200000.00,
    "interestRate": 4.5,
    "termMonths": 360
  },
  {
    "name": "Student Loan",
    "amount": 30000.00,
    "interestRate": 5.5,
    "termMonths": 120
  },
  {
    "name": "High Interest Loan",
    "amount": 10000.00,
    "interestRate": 15.0,
    "termMonths": 48
  },
  {
    "name": "Zero Interest Loan",
    "amount": 5000.00,
    "interestRate": 0.0,
    "termMonths": 12
  }
]
```

## Testing with Postman

### Import Postman Collection
1. Open Postman
2. Click "Import"
3. Copy and paste the following collection:

```json
{
  "info": {
    "name": "Borrower Service API Tests",
    "description": "Complete test collection for Borrower Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:4001/actuator/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["actuator", "health"]
        }
      }
    },
    {
      "name": "Create Borrower - John Doe",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"email\": \"john.doe@example.com\",\n  \"phoneNumber\": \"+1234567890\",\n  \"dateOfBirth\": \"1990-01-15\",\n  \"ssn\": \"123-45-6789\",\n  \"address\": \"123 Main Street\",\n  \"city\": \"New York\",\n  \"state\": \"NY\",\n  \"zipCode\": \"10001\",\n  \"annualIncome\": 75000.0,\n  \"employmentStatus\": \"EMPLOYED\",\n  \"employerName\": \"Tech Corp\",\n  \"employmentYears\": 5\n}"
        },
        "url": {
          "raw": "http://localhost:4001/api/borrowers",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers"]
        }
      }
    },
    {
      "name": "Create Borrower - Jane Smith",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"firstName\": \"Jane\",\n  \"lastName\": \"Smith\",\n  \"email\": \"jane.smith@example.com\",\n  \"phoneNumber\": \"+1987654321\",\n  \"dateOfBirth\": \"1985-05-20\",\n  \"ssn\": \"987-65-4321\",\n  \"address\": \"456 Oak Avenue\",\n  \"city\": \"Los Angeles\",\n  \"state\": \"CA\",\n  \"zipCode\": \"90210\",\n  \"annualIncome\": 85000.0,\n  \"employmentStatus\": \"EMPLOYED\",\n  \"employerName\": \"Design Studio\",\n  \"employmentYears\": 8\n}"
        },
        "url": {
          "raw": "http://localhost:4001/api/borrowers",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers"]
        }
      }
    },
    {
      "name": "Get All Borrowers",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:4001/api/borrowers",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers"]
        }
      }
    },
    {
      "name": "Get Borrower by ID",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1"]
        }
      }
    },
    {
      "name": "Calculate Loan - Personal",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"amount\": 25000.00,\n  \"interestRate\": 8.5,\n  \"termMonths\": 36\n}"
        },
        "url": {
          "raw": "http://localhost:4001/api/borrowers/loans/calculate",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "loans", "calculate"]
        }
      }
    },
    {
      "name": "Apply for Loan - Personal",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"loanType\": \"PERSONAL\",\n  \"loanAmount\": 25000.00,\n  \"loanTermMonths\": 36,\n  \"interestRate\": 8.5,\n  \"purpose\": \"Home renovation\",\n  \"notes\": \"Planning to renovate kitchen and bathroom\"\n}"
        },
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/loans",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "loans"]
        }
      }
    },
    {
      "name": "Apply for Loan - Auto",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"loanType\": \"AUTO\",\n  \"loanAmount\": 15000.00,\n  \"loanTermMonths\": 24,\n  \"interestRate\": 6.5,\n  \"purpose\": \"Car purchase\",\n  \"notes\": \"Buying a used car\"\n}"
        },
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/loans",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "loans"]
        }
      }
    },
    {
      "name": "Get All Loans for Borrower",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/loans",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "loans"]
        }
      }
    },
    {
      "name": "Get Loan by Status",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/loans/status/PENDING",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "loans", "status", "PENDING"]
        }
      }
    },
    {
      "name": "Upload Document",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": []
            },
            {
              "key": "documentType",
              "value": "ID_PROOF",
              "type": "text"
            },
            {
              "key": "description",
              "value": "Driver's license for identity verification",
              "type": "text"
            }
          ]
        },
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/documents",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "documents"]
        }
      }
    },
    {
      "name": "Get Documents for Borrower",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:4001/api/borrowers/1/documents",
          "protocol": "http",
          "host": ["localhost"],
          "port": "4001",
          "path": ["api", "borrowers", "1", "documents"]
        }
      }
    }
  ]
}
```

### Running Postman Tests
1. Import the collection
2. Set up environment variables if needed
3. Run the requests in sequence
4. Use Postman's test scripts to validate responses

## Automated Testing Script

### Bash Script for Complete Testing
Create a file named `test-api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:4001"
API_BASE="$BASE_URL/api/borrowers"

echo "🚀 Starting Borrower Service API Tests"
echo "======================================"

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s "$BASE_URL/actuator/health" | jq '.'
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

JOHN_ID=$(echo $JOHN_RESPONSE | jq -r '.id')
echo "John Doe created with ID: $JOHN_ID"

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

JANE_ID=$(echo $JANE_RESPONSE | jq -r '.id')
echo "Jane Smith created with ID: $JANE_ID"
echo ""

# Test 3: Get All Borrowers
echo "3. Getting all borrowers..."
curl -s "$API_BASE" | jq '.'
echo ""

# Test 4: Loan Calculator
echo "4. Testing loan calculator..."
curl -s -X POST "$API_BASE/loans/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 25000.00,
    "interestRate": 8.5,
    "termMonths": 36
  }' | jq '.'
echo ""

# Test 5: Apply for Loans
echo "5. Applying for loans..."

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

PERSONAL_LOAN_ID=$(echo $PERSONAL_LOAN_RESPONSE | jq -r '.id')
echo "Personal loan created with ID: $PERSONAL_LOAN_ID"

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

AUTO_LOAN_ID=$(echo $AUTO_LOAN_RESPONSE | jq -r '.id')
echo "Auto loan created with ID: $AUTO_LOAN_ID"

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

BUSINESS_LOAN_ID=$(echo $BUSINESS_LOAN_RESPONSE | jq -r '.id')
echo "Business loan created with ID: $BUSINESS_LOAN_ID"
echo ""

# Test 6: Get Loans
echo "6. Getting loan applications..."

echo "All loans for John:"
curl -s "$API_BASE/$JOHN_ID/loans" | jq '.'

echo "All loans for Jane:"
curl -s "$API_BASE/$JANE_ID/loans" | jq '.'

echo "Pending loans for John:"
curl -s "$API_BASE/$JOHN_ID/loans/status/PENDING" | jq '.'
echo ""

# Test 7: Document Upload (if file exists)
echo "7. Testing document upload..."
if [ -f "test_document.txt" ]; then
  echo "Uploading test document for John..."
  curl -s -X POST "$API_BASE/$JOHN_ID/documents" \
    -F "file=@test_document.txt" \
    -F "documentType=ID_PROOF" \
    -F "description=Test document for identity verification" | jq '.'
  
  echo "Getting documents for John:"
  curl -s "$API_BASE/$JOHN_ID/documents" | jq '.'
else
  echo "No test_document.txt found. Skipping document upload test."
fi
echo ""

echo "✅ All tests completed!"
echo "📊 Test Summary:"
echo "   - Health Check: ✅"
echo "   - Borrower Creation: ✅"
echo "   - Loan Calculator: ✅"
echo "   - Loan Applications: ✅"
echo "   - Document Upload: $(if [ -f "test_document.txt" ]; then echo "✅"; else echo "⏭️"; fi)"
echo ""
echo "🌐 API Documentation: $BASE_URL/swagger-ui.html"
echo "📈 Health Dashboard: $BASE_URL/actuator/health"
```

Make the script executable and run it:
```bash
chmod +x test-api.sh
./test-api.sh
```

## Error Testing

### Test Invalid Data
```bash
# Test invalid email
curl -X POST http://localhost:4001/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "invalid-email",
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
  }'

# Test invalid SSN format
curl -X POST http://localhost:4001/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15",
    "ssn": "123456789",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "annualIncome": 75000.0,
    "employmentStatus": "EMPLOYED",
    "employerName": "Tech Corp",
    "employmentYears": 5
  }'

# Test non-existent borrower
curl -X GET http://localhost:4001/api/borrowers/999

# Test invalid loan amount
curl -X POST http://localhost:4001/api/borrowers/1/loans \
  -H "Content-Type: application/json" \
  -d '{
    "loanType": "PERSONAL",
    "loanAmount": 500.00,
    "loanTermMonths": 36,
    "interestRate": 8.5,
    "purpose": "Home renovation",
    "notes": "Planning to renovate kitchen and bathroom"
  }'
```

## Performance Testing

### Load Testing with Apache Bench
```bash
# Test borrower creation endpoint
ab -n 100 -c 10 -p borrower_data.json -T application/json http://localhost:4001/api/borrowers/

# Test loan calculation endpoint
ab -n 1000 -c 20 -p loan_calc_data.json -T application/json http://localhost:4001/api/borrowers/loans/calculate/

# Test get borrowers endpoint
ab -n 1000 -c 20 http://localhost:4001/api/borrowers/
```

Create the test data files:

`borrower_data.json`:
```json
{
  "firstName": "Test",
  "lastName": "User",
  "email": "test@example.com",
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
}
```

`loan_calc_data.json`:
```json
{
  "amount": 25000.00,
  "interestRate": 8.5,
  "termMonths": 36
}
```

## Conclusion

This testing guide provides comprehensive coverage of the Borrower Service API functionality. The mock data sets and testing scripts can be used to:

1. **Validate API functionality** - Ensure all endpoints work correctly
2. **Test error handling** - Verify proper validation and error responses
3. **Performance testing** - Measure API response times and throughput
4. **Integration testing** - Test complete workflows from borrower creation to loan application
5. **Regression testing** - Ensure new changes don't break existing functionality

Remember to:
- Run tests in a clean environment
- Validate all response codes and data
- Test both positive and negative scenarios
- Monitor application logs during testing
- Use the Swagger UI for interactive testing 