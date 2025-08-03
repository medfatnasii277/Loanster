# Loan Score Service

A microservice responsible for calculating loan scores based on borrower and loan application data using Kafka event streaming.

## 🎯 Overview

The Loan Score Service consumes Kafka events from the borrower service and calculates comprehensive loan scores using a weighted algorithm. It provides RESTful APIs for the officer service to retrieve loan scores and risk assessments.

## 🏗️ Architecture

- **Event-Driven**: Consumes Kafka events for loan applications and borrower data
- **Scoring Engine**: Weight-based algorithm for loan risk assessment
- **REST API**: Provides loan scores to other services via HTTP
- **Database**: PostgreSQL for storing calculated scores and history

## 📊 Scoring Algorithm

### Weight-Based Calculation

The loan score is calculated using multiple factors with configurable weights:

#### 1. Employment Status (-50 to +100 points)
- **Unemployed**: -50 points (high risk)
- **Employed**: +100 points (stable income)
- **Self-employed**: +75 points (moderate stability)
- **Student**: +25 points (potential but low current income)
- **Retired**: +50 points (fixed income)

#### 2. Annual Income (Income × 0.001)
- Higher income = higher score
- $50,000 income = +50 points
- $100,000 income = +100 points

#### 3. Loan Amount vs Income Ratio (Ratio × -0.5)
- Lower ratio = better score
- If loan amount > annual income, penalty applies
- Ratio of 2.0 = -100 points penalty

#### 4. Interest Rate (Rate × -10)
- Higher interest rate = risk penalty
- 5% rate = -50 points
- 10% rate = -100 points

#### 5. Employment Years (Years × +5)
- Longer employment = stability bonus
- 5 years = +25 points
- 10 years = +50 points (capped at 100)

#### 6. Loan Term (Months × -2)
- Longer term = slight penalty (extended risk)
- 12 months = -24 points
- 36 months = -72 points

### Score Grades
- **EXCELLENT**: 750+ (Low risk, prime candidate)
- **GOOD**: 650-749 (Moderate risk, good candidate)
- **FAIR**: 550-649 (Higher risk, requires review)
- **POOR**: <550 (High risk, likely rejection)

### Risk Assessment
- **LOW**: Score 650+, employed, debt-to-income < 0.3
- **MEDIUM**: Score 450-649 or moderate risk factors
- **HIGH**: Score <450 or high debt-to-income ratio

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+

### Environment Variables
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://loan-score-service-db:5432/loan-score-db
SPRING_DATASOURCE_USERNAME=score_loan
SPRING_DATASOURCE_PASSWORD=password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Build & Run
```bash
# Clean and compile
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Build Docker image
docker build -t loan-score-service .
```

### Docker Compose
```yaml
services:
  loan-score-service:
    image: loan-score-service
    ports:
      - "4003:4003"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://loan-score-service-db:5432/loan-score-db
      - SPRING_DATASOURCE_USERNAME=score_loan
      - SPRING_DATASOURCE_PASSWORD=password
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - loan-score-service-db
      - kafka

  loan-score-service-db:
    image: postgres:15
    environment:
      - POSTGRES_DB=loan-score-db
      - POSTGRES_USER=score_loan
      - POSTGRES_PASSWORD=password
    volumes:
      - loan_score_db_data:/var/lib/postgresql/data
```

## 📡 API Endpoints

### Health Check
```http
GET /health
```

### Loan Scoring
```http
GET /api/scores/loan/{applicationId}
GET /api/scores/borrower/{borrowerId}
GET /api/scores/stats
```

### Filtering
```http
GET /api/scores/grade/{grade}        # EXCELLENT, GOOD, FAIR, POOR
GET /api/scores/risk/{risk}          # LOW, MEDIUM, HIGH
```

### Example Response
```json
{
  "id": 1,
  "applicationId": 12345,
  "borrowerId": 67890,
  "totalScore": 725,
  "scoreGrade": "GOOD",
  "riskAssessment": "LOW",
  "debtToIncomeRatio": 0.25,
  "scoringReason": "Score Breakdown:\n• Employment (employed): +100 points\n• Annual Income ($75000): +75 points\n• Loan Amount Ratio: -25 points\n• Interest Rate (5.5%): -55 points\n• Employment Years (8): +40 points\n• Loan Term (36 months): -72 points\n\nTotal Score: 725",
  "calculatedAt": "2025-08-03T10:00:00Z"
}
```

## 🔧 Configuration

### Scoring Weights (application.properties)
```properties
# Employment weights
loan.scoring.weights.employment.unemployed=-50
loan.scoring.weights.employment.employed=100
loan.scoring.weights.employment.self-employed=75

# Income multiplier
loan.scoring.weights.income.multiplier=0.001

# Penalties and bonuses
loan.scoring.weights.interest-rate.penalty=-10
loan.scoring.weights.employment-years.bonus=5
loan.scoring.weights.loan-term.penalty=-2

# Score thresholds
loan.scoring.thresholds.excellent=750
loan.scoring.thresholds.good=650
loan.scoring.thresholds.fair=550
```

## 🔄 Kafka Integration

### Consumed Topics
- `loan-application-events`: New loan applications
- `borrower-events`: Borrower profile updates
- `document-events`: Document uploads (for future enhancements)

### Event Processing
1. **Loan Application Event**: Triggers score calculation when combined with borrower data
2. **Borrower Event**: Updates borrower information and recalculates existing scores
3. **Automatic Scoring**: Scores are calculated and stored automatically upon event consumption

## 🗄️ Database Schema

### Tables
- **borrowers**: Borrower information from Kafka events
- **loan_applications**: Loan application data from Kafka events
- **loan_scores**: Calculated scores with detailed breakdown

### Key Features
- **Automatic Timestamps**: CreatedAt and UpdatedAt managed automatically
- **Unique Constraints**: One score per loan application
- **Audit Trail**: Complete scoring history and reasoning

## 📈 Monitoring & Observability

### Health Checks
- Database connectivity
- Kafka consumer status
- Application health

### Metrics
- Scores calculated per minute
- Score distribution by grade
- Risk assessment distribution
- Processing time metrics

### Swagger Documentation
Available at: `http://localhost:4003/swagger-ui.html`

## 🔒 Security Notes

- No authentication implemented (handled by API Gateway)
- Database credentials via environment variables
- Input validation on all endpoints
- SQL injection protection via JPA

## 🧪 Testing

```bash
# Run unit tests
./mvnw test

# Integration tests
./mvnw integration-test

# Test with sample data
# See api-tests.http for example requests
```

## 🚧 Future Enhancements

1. **ML Integration**: Machine learning models for more sophisticated scoring
2. **Credit Bureau**: Integration with external credit reporting agencies
3. **Historical Trends**: Time-series analysis of borrower behavior
4. **Custom Rules**: Business rule engine for specialized scoring criteria
5. **Batch Processing**: Bulk score recalculation capabilities

## 📝 Contributing

1. Follow SOLID principles and DRY patterns
2. Add comprehensive tests for new features
3. Update documentation for API changes
4. Use meaningful commit messages
5. Ensure backward compatibility

## 📄 License

[Your License Here]
