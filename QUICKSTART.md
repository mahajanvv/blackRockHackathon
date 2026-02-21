# Quick Start Guide

## 5-Minute Setup

### Option 1: Docker Compose (Recommended)

```bash
cd /Users/mahajanvv/MyProjects/BlackRock

# Build and start
docker-compose -f compose.yaml up -d

# Wait for startup
sleep 5

# Test the API
curl http://localhost:5477/blackrock/challenge/v1/performance
```

### Option 2: Local Development

```bash
cd /Users/mahajanvv/MyProjects/BlackRock

# Build
mvn clean package -DskipTests

# Run
java -jar target/auto-saving-retirement-system-1.0.0.jar
```

API will be available at `http://localhost:5477`

## Testing the API

### 1. Parse Transactions

```bash
curl -X POST http://localhost:5477/blackrock/challenge/v1/transactions:parse \
  -H "Content-Type: application/json" \
  -d '{
    "timestamps": [1000, 2000, 3000],
    "amounts": [150.50, 250.75, 99.99]
  }'
```

### 2. Filter with Temporal Rules

```bash
curl -X POST http://localhost:5477/blackrock/challenge/v1/transactions:filter \
  -H "Content-Type: application/json" \
  -d '{
    "transactions": [
      {
        "timestamp": 1500,
        "originalAmount": 150.0,
        "ceiling": 200.0,
        "remanent": 50.0,
        "valid": true
      }
    ],
    "qPeriods": [
      {
        "startDate": 1000,
        "endDate": 2000,
        "amount": 75.0,
        "periodType": "q"
      }
    ],
    "pPeriods": [],
    "kPeriods": [
      {
        "startDate": 1000,
        "endDate": 3000,
        "kPeriodId": "group_1",
        "periodType": "k"
      }
    ]
  }'
```

### 3. Calculate NPS Returns

```bash
curl -X POST http://localhost:5477/blackrock/challenge/v1/returns:nps \
  -H "Content-Type: application/json" \
  -d '{
    "wage": 1500000.0,
    "inflation": 0.03,
    "age": 30,
    "transactions": [
      {"timestamp": 1000, "amount": 100.0},
      {"timestamp": 2000, "amount": 150.0}
    ],
    "q": [{"startDate": 1000, "endDate": 2500, "amount": 75.0}],
    "p": [{"startDate": 1500, "endDate": 3000, "amount": 25.0}],
    "k": [{"startDate": 1000, "endDate": 10000, "kPeriodId": "2024-Q1"}]
  }'
```

### 4. Get Performance Metrics

```bash
curl http://localhost:5477/blackrock/challenge/v1/performance
```

**Response**:
```json
{
  "time": "2026-02-21 16:07:55.498",
  "threads": 16,
  "memory": "18.07"
}
```

## Running Tests

### Unit Tests Only

```bash
mvn test -Dtest=TransactionParseServiceTest
mvn test -Dtest=TransactionValidatorServiceTest
mvn test -Dtest=TemporalFilterServiceTest
mvn test -Dtest=FinancialProjectionServiceTest
```

### Integration Tests

```bash
mvn test -Dtest=ParseControllerIntegrationTest
```

### Performance Tests (Scale Testing)

```bash
mvn test -Dtest=TemporalFilterPerformanceTest
```

### All Tests

```bash
mvn test
```

## Cleanup

```bash
# Stop Docker containers
docker-compose -f compose.yaml down

# Remove volumes (if needed)
docker-compose -f compose.yaml down -v
```

## Troubleshooting

**Port already in use?**
```bash
# Find and kill process
lsof -i :5477
kill -9 <PID>
```

**Need more memory?**
```bash
# Edit compose.yaml
environment:
  - JAVA_OPTS=-Xmx4g -Xms1g
```

**Check logs**
```bash
docker-compose logs -f blackrock-retirement-api
```

## Next Steps

1. Read the full `README.md` for architecture details
2. Review test files for usage examples
3. Explore the API documentation via endpoints
4. Scale test with large datasets
