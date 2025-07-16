# Borrower Service - Testing Guide

This document provides comprehensive testing guidelines for the Borrower Service microservice, part of the Loan Application Management System.

## Project Overview

The Borrower Service is a Spring Boot microservice that handles:
- Borrower management (CRUD operations)
- Loan application processing
- Document upload and management
- Loan calculations
- Kafka event publishing/consuming

## Test Structure

The testing suite includes:

### Unit Tests
- **Controller Tests**: Test REST endpoints using MockMvc
- **Service Tests**: Test business logic with mocked dependencies
- **Repository Tests**: Test data persistence with @DataJpaTest
- **Utility Tests**: Test calculation and mapping logic

### Integration Tests
- **Full Application Tests**: Test complete workflows end-to-end
- **Database Integration**: Test with real H2 in-memory database
- **Kafka Integration**: Test event publishing (mocked for unit tests)

## Test Files Created

```
src/test/java/
├── com/pm/borrowerservice/
│   ├── controller/
│   │   └── BorrowerControllerTest.java
│   ├── service/
│   │   ├── BorrowerServiceTest.java
│   │   ├── LoanApplicationServiceTest.java
│   │   ├── LoanCalculatorServiceTest.java
│   │   └── DocumentServiceTest.java
│   ├── repository/
│   │   └── LoanApplicationRepositoryTest.java
│   └── integration/
│       └── BorrowerServiceIntegrationTest.java
└── resources/
    └── application-test.properties
```

## Running Tests

### Prerequisites
1. Java 17 or higher
2. Maven 3.6+
3. All project dependencies should be available

### Run All Tests
```bash
mvn test
```

### Run Specific Test Categories

#### Unit Tests Only
```bash
mvn test -Dtest="*Test"
```

#### Integration Tests Only
```bash
mvn test -Dtest="*IntegrationTest"
```

#### Controller Tests
```bash
mvn test -Dtest="*ControllerTest"
```

#### Service Tests
```bash
mvn test -Dtest="*ServiceTest"
```

### Run Individual Test Classes
```bash
# Test controller layer
mvn test -Dtest=BorrowerControllerTest

# Test service layer
mvn test -Dtest=BorrowerServiceTest

# Test loan calculations
mvn test -Dtest=LoanCalculatorServiceTest
```

### Run with Coverage
```bash
mvn test jacoco:report
```

## Test Scenarios Covered

### 1. Borrower Management
- ✅ Create borrower with valid data
- ✅ Create borrower with invalid data (validation errors)
- ✅ Retrieve borrower by ID
- ✅ Retrieve borrower by user ID
- ✅ Get all borrowers
- ✅ Delete borrower
- ✅ Handle duplicate email/SSN validation

### 2. Loan Applications
- ✅ Apply for loan with valid data
- ✅ Calculate monthly and total payments
- ✅ Retrieve loan applications by borrower
- ✅ Filter loan applications by status
- ✅ Handle borrower not found scenarios
- ✅ Validate loan application constraints

### 3. Document Management
- ✅ Upload documents with valid files
- ✅ Validate file types and sizes
- ✅ Associate documents with loan applications
- ✅ Retrieve documents by borrower
- ✅ Delete documents
- ✅ Handle file validation errors

### 4. Loan Calculations
- ✅ Calculate monthly payments with various interest rates
- ✅ Handle zero interest rate scenarios
- ✅ Calculate total payments
- ✅ Test edge cases (very small/large amounts)

### 5. Repository Operations
- ✅ Custom finder methods
- ✅ Relationship integrity
- ✅ Unique constraint validation
- ✅ CRUD operations

### 6. Integration Scenarios
- ✅ End-to-end workflows
- ✅ Database persistence
- ✅ Transaction management
- ✅ Error handling across layers

## Test Configuration

### Test Properties
The tests use `application-test.properties` with:
- H2 in-memory database
- Disabled Kafka for unit tests
- Test-specific file upload paths
- Debug logging enabled

### Test Dependencies
Key testing dependencies in `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## Common Test Patterns

### 1. Controller Tests
```java
@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BorrowerService borrowerService;
    
    @Test
    void createBorrower_ShouldReturnCreatedBorrower() throws Exception {
        // Test implementation
    }
}
```

### 2. Service Tests
```java
@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {
    @Mock
    private BorrowerRepository borrowerRepository;
    
    @InjectMocks
    private BorrowerService borrowerService;
    
    @Test
    void createBorrower_ShouldReturnBorrowerDto() {
        // Test implementation
    }
}
```

### 3. Repository Tests
```java
@DataJpaTest
@ActiveProfiles("test")
class LoanApplicationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private LoanApplicationRepository repository;
}
```

## Troubleshooting

### Common Issues

1. **Lombok Compilation Errors**
   - Ensure Lombok is properly configured
   - Run `mvn clean compile` before running tests

2. **H2 Database Issues**
   - Check test properties configuration
   - Ensure unique test data per test

3. **Mock Configuration**
   - Verify all required dependencies are mocked
   - Check mock interactions and verifications

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests with verbose output
mvn test -X

# Skip tests during build
mvn package -DskipTests

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

## Test Data Examples

### Valid Borrower Request
```json
{
  "userId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "dateOfBirth": "1990-01-01",
  "ssn": "123-45-6789",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "annualIncome": 75000.0,
  "employmentStatus": "Employed",
  "employerName": "Tech Corp",
  "employmentYears": 5
}
```

### Valid Loan Application Request
```json
{
  "loanType": "Personal",
  "loanAmount": 10000,
  "loanTermMonths": 36,
  "interestRate": 5.5,
  "purpose": "Home improvement"
}
```

## Continuous Integration

The tests are designed to run in CI/CD pipelines with:
- No external dependencies (H2 database)
- Mocked Kafka integration
- Fast execution times
- Clear error reporting

## Next Steps

1. Add performance tests for high-volume scenarios
2. Implement contract testing with Pact
3. Add security testing for authentication/authorization
4. Create load tests for API endpoints
5. Add mutation testing for test quality assessment

## Support

For issues with tests:
1. Check the test logs for detailed error messages
2. Ensure all dependencies are properly configured
3. Verify test data setup and cleanup
4. Review mock configurations and interactions
