# Loanster - Architecture Documentation

## 🏗️ System Overview

Loanster is a cloud-native loan management platform built with a microservices architecture. The system enables borrowers to apply for loans, upload documents, and track application status, while loan officers can review, approve/reject applications, and manage documents.

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Frontend (React)                                │
│                           Port: 3000 (Development)                           │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │ HTTP/REST
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        API Gateway (Spring Cloud)                            │
│                                Port: 4000                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  • JWT Authentication & Authorization                                 │   │
│  │  • Request Routing & Load Balancing                                   │   │
│  │  • Centralized Security Filtering                                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└───────┬──────────────┬───────────────┬──────────────┬────────────────────────┘
        │              │               │              │
        │              │               │              │
        ▼              ▼               ▼              ▼
┌───────────────┐ ┌──────────────┐ ┌─────────────┐ ┌──────────────────┐
│  Auth Service │ │   Borrower   │ │   Officer   │ │  Loan Score      │
│   Port: 4005  │ │   Service    │ │   Service   │ │   Service        │
│               │ │  Port: 4001  │ │ Port: 4002  │ │   Port: 4003     │
│ ┌───────────┐ │ │              │ │             │ │                  │
│ │  JWT      │ │ │              │ │             │ │                  │
│ │  Token    │ │ │              │ │             │ │                  │
│ │  Mgmt     │ │ │              │ │             │ │                  │
│ └───────────┘ │ │              │ │             │ │                  │
│               │ │              │ │             │ │                  │
│  H2 Database  │ │ PostgreSQL   │ │ PostgreSQL  │ │  PostgreSQL      │
└───────────────┘ └──────┬───────┘ └──────┬──────┘ └────────┬─────────┘
                         │                 │                  │
                         │                 │                  │
                         ▼                 ▼                  ▼
                  ┌──────────────────────────────────────────────┐
                  │          Apache Kafka (Event Bus)             │
                  │              Port: 9092                        │
                  │                                                │
                  │  Topics:                                       │
                  │  • borrower-created                            │
                  │  • loan-application                            │
                  │  • documents-upload                            │
                  │  • loan-status                                 │
                  │  • documents-status                            │
                  └────────────────────────────────────────────────┘
```

## 🔄 Data Flow Architecture

### 1. Borrower Registration Flow

```
┌──────────┐     ┌─────────┐     ┌──────────────┐     ┌───────────────┐
│ Frontend │────▶│   API   │────▶│   Borrower   │────▶│     Kafka     │
│  (React) │     │ Gateway │     │   Service    │     │ Topic:        │
└──────────┘     └─────────┘     └──────────────┘     │ borrower-     │
                                         │              │ created       │
                                         │              └───────┬───────┘
                                         ▼                      │
                                  ┌─────────────┐              │
                                  │ PostgreSQL  │              │
                                  │  (Borrower  │              │
                                  │    Data)    │              │
                                  └─────────────┘              │
                                                               │
                     ┌─────────────────────────────────────────┴──────┐
                     │                                                 │
                     ▼                                                 ▼
            ┌─────────────────┐                            ┌──────────────────┐
            │ Officer Service │                            │ Loan Score       │
            │  (Consumes)     │                            │   Service        │
            │                 │                            │  (Consumes)      │
            │ • Creates local │                            │                  │
            │   borrower      │                            │ • Caches         │
            │   record        │                            │   borrower data  │
            └─────────────────┘                            └──────────────────┘
```

### 2. Loan Application Flow

```
┌──────────┐     ┌─────────┐     ┌──────────────┐     ┌───────────────┐
│ Borrower │────▶│   API   │────▶│   Borrower   │────▶│     Kafka     │
│  Portal  │     │ Gateway │     │   Service    │     │ Topic:        │
│ (React)  │     │         │     │              │     │ loan-         │
└──────────┘     └─────────┘     │ • Calculates │     │ application   │
                                  │   monthly    │     └───────┬───────┘
                                  │   payment    │             │
                                  │ • Saves to   │             │
                                  │   DB         │             │
                                  └──────────────┘             │
                                                               │
                     ┌─────────────────────────────────────────┴──────┐
                     │                                                 │
                     ▼                                                 ▼
            ┌─────────────────┐                            ┌──────────────────┐
            │ Officer Service │                            │ Loan Score       │
            │  (Consumes)     │                            │   Service        │
            │                 │                            │  (Consumes)      │
            │ • Creates local │                            │                  │
            │   application   │                            │ • Calculates     │
            │   record        │                            │   credit score   │
            │ • Sets status   │                            │ • Stores score   │
            │   to PENDING    │                            │   in DB          │
            └─────────────────┘                            └──────────────────┘
```

### 3. Document Upload Flow

```
┌──────────┐     ┌─────────┐     ┌──────────────┐     ┌───────────────┐
│ Borrower │────▶│   API   │────▶│   Borrower   │────▶│     Kafka     │
│  Portal  │     │ Gateway │     │   Service    │     │ Topic:        │
│ (React)  │     │ (JWT)   │     │              │     │ documents-    │
└──────────┘     └─────────┘     │ • Saves file │     │ upload        │
                                  │   to disk    │     └───────┬───────┘
                                  │ • Stores     │             │
                                  │   metadata   │             │
                                  │   in DB      │             │
                                  └──────────────┘             │
                                                               │
                                                               ▼
                                                     ┌─────────────────┐
                                                     │ Officer Service │
                                                     │  (Consumes)     │
                                                     │                 │
                                                     │ • Creates local │
                                                     │   document      │
                                                     │   record        │
                                                     │ • Sets status   │
                                                     │   to PENDING    │
                                                     └─────────────────┘
```

### 4. Loan Application Review Flow (Officer)

```
┌──────────┐     ┌─────────┐     ┌──────────────┐     ┌───────────────┐
│ Officer  │────▶│   API   │────▶│   Officer    │────▶│     Kafka     │
│  Portal  │     │ Gateway │     │   Service    │     │ Topic:        │
│ (React)  │     │(Officer │     │              │     │ loan-status   │
└──────────┘     │  JWT)   │     │ • Updates    │     └───────┬───────┘
                 └─────────┘     │   status in  │             │
                      ▲          │   DB         │             │
                      │          │              │             ▼
                      │          │ • Calls Loan │    ┌─────────────────┐
                      │          │   Score      │    │ Borrower Service│
                      │          │   Service    │    │  (Consumes)     │
                      │          │   (REST)     │    │                 │
                      │          └──────┬───────┘    │ • Updates local │
                      │                 │            │   application   │
                      │                 │            │   status        │
                      │                 ▼            │ • Notifies      │
                      │          ┌──────────────┐   │   borrower      │
                      │          │ Loan Score   │   └─────────────────┘
                      │          │   Service    │
                      │          │              │
                      │◀─────────│ Returns      │
                      │          │ Credit Score │
                      │          │ via REST     │
                      │          └──────────────┘
                      │
                 Resilience4j
                 Circuit Breaker
                 • Retry (3x)
                 • Timeout (3s)
                 • Fallback
```

### 5. Document Review Flow (Officer)

```
┌──────────┐     ┌─────────┐     ┌──────────────┐     ┌───────────────┐
│ Officer  │────▶│   API   │────▶│   Officer    │────▶│     Kafka     │
│  Portal  │     │ Gateway │     │   Service    │     │ Topic:        │
│ (React)  │     │(Officer │     │              │     │ documents-    │
└──────────┘     │  JWT)   │     │ • Updates    │     │ status        │
                 └─────────┘     │   document   │     └───────┬───────┘
                                 │   status     │             │
                                 │ • Adds       │             │
                                 │   rejection  │             ▼
                                 │   reason if  │    ┌─────────────────┐
                                 │   rejected   │    │ Borrower Service│
                                 └──────────────┘    │  (Consumes)     │
                                                     │                 │
                                                     │ • Updates local │
                                                     │   document      │
                                                     │   status        │
                                                     └─────────────────┘
```

## 🎯 Microservices Details

### 1. API Gateway (Port: 4000)
**Technology:** Spring Cloud Gateway with WebFlux

**Responsibilities:**
- Centralized entry point for all client requests
- JWT token validation and authorization
- Request routing to appropriate microservices
- Security filtering (JWT, Officer role validation)

**Routes:**
```yaml
/api/borrowers/**        → Borrower Service (JWT required)
/admin/**                → Officer Service (Officer role required)
/borrower/**             → Officer Service (JWT required)
/api/auth/**             → Auth Service (Public)
/api/health/**           → Officer Service (Public)
```

### 2. Auth Service (Port: 4005)
**Technology:** Spring Boot with JWT

**Responsibilities:**
- User authentication (borrowers and officers)
- JWT token generation and validation
- User registration
- Password management

**Database:** H2 (In-memory for development)

**Endpoints:**
- `POST /api/auth/register` - Register new borrower
- `POST /api/auth/login` - Login (returns JWT)
- `POST /api/auth/validate` - Validate JWT token

### 3. Borrower Service (Port: 4001)
**Technology:** Spring Boot with Kafka Producer & Consumer

**Responsibilities:**
- Borrower profile management
- Loan application creation
- Document upload management
- Loan payment calculations
- Event publishing to Kafka
- Consuming status updates from Officer Service

**Database:** PostgreSQL

**Kafka Events Produced:**
- `borrower-created` - When new borrower registers
- `loan-application` - When borrower applies for loan
- `documents-upload` - When borrower uploads document

**Kafka Events Consumed:**
- `loan-status` - Loan application status updates from Officer
- `documents-status` - Document status updates from Officer

**Key Endpoints:**
```
POST   /api/borrowers                    - Create borrower
GET    /api/borrowers/{id}               - Get borrower details
POST   /api/borrowers/{id}/applications  - Create loan application
GET    /api/borrowers/{id}/applications  - Get all applications
POST   /api/borrowers/{id}/documents     - Upload document
GET    /api/borrowers/{id}/documents     - Get all documents
```

### 4. Officer Service (Port: 4002)
**Technology:** Spring Boot with Kafka Consumer & Producer, Resilience4j

**Responsibilities:**
- Review and approve/reject loan applications
- Review and approve/reject documents
- Manage borrower data (read-only replica)
- Call Loan Score Service for credit scoring
- Event publishing for status updates
- Consuming events from Borrower Service

**Database:** PostgreSQL

**Kafka Events Consumed:**
- `borrower-created` - Creates local borrower record
- `loan-application` - Creates local application record
- `documents-upload` - Creates local document record

**Kafka Events Produced:**
- `loan-status` - When officer updates loan status
- `documents-status` - When officer updates document status

**External Dependencies:**
- Loan Score Service (REST API with Circuit Breaker)

**Resilience Patterns:**
```properties
Circuit Breaker:
- Sliding window: 10 calls
- Failure threshold: 50%
- Wait duration: 5s

Retry:
- Max attempts: 3
- Wait duration: 1s

Timeout:
- Duration: 3s
```

**Key Endpoints:**
```
Admin Routes (Officer only):
GET    /admin/borrowers                  - List all borrowers
GET    /admin/borrowers/{id}             - Get borrower details
GET    /admin/applications               - List all applications
GET    /admin/applications/{id}          - Get application details
PUT    /admin/applications/{id}/status   - Update application status
GET    /admin/documents                  - List all documents
PUT    /admin/documents/{id}/status      - Update document status

Borrower Routes (Any authenticated user):
GET    /borrower/applications/{id}       - Get application by ID
GET    /borrower/applications            - Get all applications
```

### 5. Loan Score Service (Port: 4003)
**Technology:** Spring Boot with Kafka Consumer

**Responsibilities:**
- Calculate credit scores for loan applications
- Store scoring history
- Consume borrower and application events
- Provide REST API for score retrieval

**Database:** PostgreSQL

**Kafka Events Consumed:**
- `borrower-created` - Caches borrower data
- `loan-application` - Calculates and stores credit score

**Scoring Algorithm:**
```
Base Score: 500

Employment Status Weights:
- EMPLOYED: +100
- SELF_EMPLOYED: +75
- RETIRED: +50
- STUDENT: +25
- UNEMPLOYED: -50

Additional Factors:
- Annual Income: +0.001 per dollar
- Loan Amount Ratio: -0.5 per dollar
- Interest Rate: -10 per percent
- Employment Years: +5 per year
- Loan Term: -2 per month

Score Ranges:
- Excellent: 750+
- Good: 650-749
- Fair: 550-649
- Poor: 400-549
- Very Poor: <400
```

**Key Endpoints:**
```
GET    /api/scores/borrower/{borrowerId}              - Get all scores for borrower
GET    /api/scores/application/{applicationId}        - Get score for application
POST   /api/scores/calculate                          - Calculate score (internal)
```

## 🔐 Security Architecture

### JWT Token Flow
```
1. User Login → Auth Service
2. Auth Service validates credentials
3. Auth Service generates JWT token
4. Frontend stores JWT in localStorage/sessionStorage
5. All subsequent requests include JWT in Authorization header
6. API Gateway validates JWT before routing
7. If valid, request forwarded to target service
8. If invalid, returns 401 Unauthorized
```

### Role-Based Access Control (RBAC)
```
Borrower Role:
- Access own profile and applications
- Create loan applications
- Upload documents
- View own application status

Officer Role:
- View all borrowers and applications
- Approve/reject applications
- Approve/reject documents
- Access admin endpoints
```

## 📨 Event-Driven Communication

### Kafka Topics and Message Flow

#### 1. borrower-created
```protobuf
message BorrowerCreatedEvent {
  int64 borrower_id
  string first_name
  string last_name
  string email
  string phone_number
  string date_of_birth
  string ssn
  string address
  double annual_income
  string employment_status
  string employer_name
  int32 employment_years
  string event_timestamp
}
```
**Producers:** Borrower Service  
**Consumers:** Officer Service, Loan Score Service

#### 2. loan-application
```protobuf
message LoanApplicationEvent {
  int64 application_id
  int64 borrower_id
  double loan_amount
  int32 loan_term_months
  string loan_purpose
  double interest_rate
  double monthly_payment
  string status
  string event_timestamp
}
```
**Producers:** Borrower Service  
**Consumers:** Officer Service, Loan Score Service

#### 3. documents-upload
```protobuf
message DocumentUploadEvent {
  int64 document_id
  int64 borrower_id
  int64 loan_application_id
  string document_type
  string file_name
  string file_path
  int64 file_size
  string status
  string event_timestamp
}
```
**Producers:** Borrower Service  
**Consumers:** Officer Service

#### 4. loan-status
```protobuf
message LoanStatusUpdateEvent {
  int64 application_id
  int64 borrower_id
  string old_status
  string new_status
  string updated_by
  string rejection_reason (optional)
  string event_timestamp
}
```
**Producers:** Officer Service  
**Consumers:** Borrower Service

#### 5. documents-status
```protobuf
message DocumentStatusUpdateEvent {
  int64 document_id
  int64 borrower_id
  int64 loan_application_id
  string old_status
  string new_status
  string updated_by
  string rejection_reason (optional)
  string event_timestamp
}
```
**Producers:** Officer Service  
**Consumers:** Borrower Service

### Event Serialization
- **Format:** Protocol Buffers (Protobuf)
- **Benefits:**
  - Compact binary format
  - Type-safe
  - Backward/forward compatibility
  - Language-agnostic
  - Fast serialization/deserialization

## 🔄 Synchronous Communication

### Officer Service → Loan Score Service (REST)

```
┌────────────────┐                    ┌──────────────────┐
│ Officer Service│───────REST────────▶│ Loan Score       │
│                │                    │   Service        │
│  Circuit       │◀──────Response─────│                  │
│  Breaker       │                    │  /api/scores/    │
│  • Retry       │                    │  application/    │
│  • Timeout     │                    │  {id}            │
│  • Fallback    │                    │                  │
└────────────────┘                    └──────────────────┘
```

**Request Flow:**
1. Officer reviews loan application
2. Officer Service calls Loan Score Service REST API
3. Circuit breaker wraps the call:
   - Monitors for failures
   - Retries on temporary failures (3 attempts)
   - Times out after 3 seconds
   - Opens circuit after 50% failure rate
   - Returns fallback response if circuit open
4. Returns credit score to officer
5. Officer makes decision based on score

**Fallback Strategy:**
- Returns cached score if available
- Returns default score estimation
- Allows officer to proceed without score

## 🗄️ Database Architecture

### Borrower Service Database
```sql
Tables:
- borrowers          (borrower profiles)
- loan_applications  (loan requests)
- documents          (uploaded documents metadata)

Relationships:
- borrowers 1:N loan_applications
- borrowers 1:N documents
- loan_applications 1:N documents
```

### Officer Service Database
```sql
Tables:
- borrowers          (replicated from events)
- loan_applications  (replicated from events)
- documents          (replicated from events)

Note: Read-only replicas created from Kafka events
```

### Loan Score Service Database
```sql
Tables:
- borrower_scores    (cached borrower data)
- application_scores (calculated credit scores)

Relationships:
- borrower_scores 1:N application_scores
```

### Auth Service Database
```sql
Tables:
- users              (authentication data)
- roles              (user roles)

Note: Uses H2 in-memory database (development)
```

## 🐳 Containerization

### Docker Compose Services
```yaml
services:
  - kafka              (Port: 9092)
  - zookeeper          (Port: 2181)
  - postgres-borrower  (Port: 5432)
  - postgres-officer   (Port: 5000)
  - postgres-score     (Port: 5001)
  - auth-service       (Port: 4005)
  - borrower-service   (Port: 4001)
  - officer-service    (Port: 4002)
  - loan-score-service (Port: 4003)
  - api-gateway        (Port: 4000)
  - frontend           (Port: 3000)
```

## 🚀 Running the System

### Prerequisites
- Docker & Docker Compose
- Java 21
- Node.js (for frontend)

### Start All Services
```bash
# Start infrastructure (Kafka, PostgreSQL)
docker-compose up -d kafka postgres-borrower postgres-officer postgres-score

# Build and start backend services
cd auth-service && ./mvnw clean install && ./mvnw spring-boot:run &
cd borrower-service && ./mvnw clean install && ./mvnw spring-boot:run &
cd officer-service && ./mvnw clean install && ./mvnw spring-boot:run &
cd loan-score-service && ./mvnw clean install && ./mvnw spring-boot:run &
cd api-gateway && ./mvnw clean install && ./mvnw spring-boot:run &

# Start frontend
cd frontend && npm install && npm start
```

### Access Points
- **Frontend:** http://localhost:3000
- **API Gateway:** http://localhost:4000
- **Auth Service:** http://localhost:4005
- **Borrower Service:** http://localhost:4001
- **Officer Service:** http://localhost:4002
- **Loan Score Service:** http://localhost:4003

## 🧪 Testing

### Unit Testing
- **Framework:** JUnit 5, Mockito
- **Coverage:** Service layer, business logic
- **Location:** `src/test/java` in each service

### Running Tests
```bash
# Test all services
./mvnw test

# Test with coverage
./mvnw test jacoco:report
```

## 📊 Monitoring & Observability

### Health Checks
Each service exposes health endpoints:
```
GET /api/health
GET /actuator/health
```

### Logging
- **Framework:** SLF4J with Logback
- **Levels:** DEBUG, INFO, WARN, ERROR
- **Pattern:** `%d{yyyy-MM-dd HH:mm:ss} - %msg%n`

### Metrics (Actuator)
```
/actuator/metrics
/actuator/info
```

## 🔧 Configuration Management

### Environment Variables
```bash
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
POSTGRES_HOST=postgres-borrower
POSTGRES_PORT=5432
POSTGRES_DB=loan-borrower-db
POSTGRES_USER=admin_loan
POSTGRES_PASSWORD=password
```

### Application Properties
Each service has `application.properties` with:
- Server port
- Database connection
- Kafka configuration
- Service-specific settings

## 🌟 Key Features

1. **Microservices Architecture** - Independent, scalable services
2. **Event-Driven Communication** - Asynchronous integration via Kafka
3. **Circuit Breaker Pattern** - Resilience4j for fault tolerance
4. **JWT Authentication** - Secure token-based auth
5. **API Gateway** - Centralized routing and security
6. **Protobuf Serialization** - Efficient binary messaging
7. **Docker Containerization** - Easy deployment and orchestration
8. **CQRS Pattern** - Separate read models in Officer Service
9. **Database Per Service** - Data isolation and independence
10. **Comprehensive Testing** - Unit tests with JUnit & Mockito

## 📈 Scalability Considerations

### Horizontal Scaling
- Each microservice can be scaled independently
- Kafka provides distributed message processing
- Load balancer can be added in front of API Gateway

### Database Scaling
- PostgreSQL with connection pooling
- Read replicas for read-heavy operations
- Potential for sharding by borrower ID

### Kafka Scaling
- Multiple partitions for parallel processing
- Consumer groups for load distribution
- Replication for fault tolerance

## 🔮 Future Enhancements

1. **Service Mesh** - Istio for advanced traffic management
2. **Distributed Tracing** - Zipkin/Jaeger for request tracing
3. **Centralized Logging** - ELK stack for log aggregation
4. **API Rate Limiting** - Prevent abuse
5. **GraphQL Gateway** - More flexible API queries
6. **Event Sourcing** - Full audit trail of changes
7. **SAGA Pattern** - Distributed transaction management
8. **Kubernetes Deployment** - Production-grade orchestration

## 📝 License

This project is for educational and portfolio purposes.

---

**Author:** Mohamed Fatnassi  
**Project:** Loanster - Microservices Loan Management Platform  
**Year:** 2025
