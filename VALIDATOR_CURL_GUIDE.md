# transactions:validator API - CURL Guide

## Endpoint
**POST** `/blackrock/challenge/v1/transactions:validator`

## Purpose
Validates transactions and detects duplicates (same timestamp + amount combination).

---

## Basic Request Format

The endpoint expects a JSON object with a `transactions` array containing ParsedTransaction objects:

```json
{
  "transactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": true,
      "message": null
    }
  ]
}
```

---

## CURL Examples

### 1. Validate Multiple Valid Transactions

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
  "transactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": true,
      "message": null
    }
  ]
}'
```

**Expected Response (200 OK)**:
```json
{
  "validTransactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": true,
      "message": null
    }
  ],
  "invalidTransactions": [],
  "totalCount": 2,
  "validCount": 2,
  "invalidCount": 0
}
```

---

### 2. Detect Duplicate Transactions

When the same transaction appears twice (same timestamp + amount), the validator marks duplicates as invalid:

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
  "transactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": true,
      "message": null
    }
  ]
}'
```

**Expected Response (200 OK)**:
```json
{
  "validTransactions": [
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": true,
      "message": null
    }
  ],
  "invalidTransactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": false,
      "message": "Duplicate transaction: same timestamp and amount"
    }
  ],
  "totalCount": 3,
  "validCount": 1,
  "invalidCount": 2
}
```

---

### 3. Mixed Valid and Invalid Transactions

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
  "transactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": false,
      "message": "Negative amounts are not allowed"
    },
    {
      "timestamp": 1700000000000,
      "originalAmount": 100.0,
      "ceiling": 100.0,
      "remanent": 0.0,
      "valid": true,
      "message": null
    }
  ]
}'
```

**Expected Response (200 OK)**:
```json
{
  "validTransactions": [
    {
      "timestamp": 1697121930000,
      "originalAmount": 250.0,
      "ceiling": 300.0,
      "remanent": 50.0,
      "valid": true,
      "message": null
    },
    {
      "timestamp": 1700000000000,
      "originalAmount": 100.0,
      "ceiling": 100.0,
      "remanent": 0.0,
      "valid": true,
      "message": null
    }
  ],
  "invalidTransactions": [
    {
      "timestamp": 1677579560000,
      "originalAmount": 375.0,
      "ceiling": 400.0,
      "remanent": 25.0,
      "valid": false,
      "message": "Negative amounts are not allowed"
    }
  ],
  "totalCount": 3,
  "validCount": 2,
  "invalidCount": 1
}
```

---

### 4. Empty Transaction List

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
  "transactions": []
}'
```

**Expected Response (200 OK)**:
```json
{
  "validTransactions": [],
  "invalidTransactions": [],
  "totalCount": 0,
  "validCount": 0,
  "invalidCount": 0
}
```

---

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `validTransactions` | Array | List of transactions that passed validation |
| `invalidTransactions` | Array | List of transactions that failed validation (duplicates or invalid) |
| `totalCount` | Integer | Total number of transactions processed |
| `validCount` | Integer | Number of valid transactions |
| `invalidCount` | Integer | Number of invalid transactions |

---

## Transaction Object Fields

Each transaction object contains:

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | Long | Unix timestamp in milliseconds |
| `originalAmount` | Double | Original amount before rounding |
| `ceiling` | Double | Rounded up to nearest ₹100 |
| `remanent` | Double | Difference between ceiling and original amount |
| `valid` | Boolean | Whether transaction passed validation |
| `message` | String | Error message if invalid, null if valid |

---

## How to Get Timestamps

### From Date String (using date command on macOS/Linux)

```bash
# Convert "2023-10-12 20:15:30" to milliseconds
date -f "%Y-%m-%d %H:%M:%S" -j "2023-10-12 20:15:30" +%s000
# Output: 1697121930000

# For macOS
date -j -f "%Y-%m-%d %H:%M:%S" "2023-10-12 20:15:30" "+%s" | xargs -I {} echo "{}000"

# Current timestamp in milliseconds
date +%s000
```

### Using Node.js

```bash
node -e "console.log(new Date('2023-10-12 20:15:30').getTime())"
# Output: 1697121930000
```

### Using Python

```bash
python3 -c "from datetime import datetime; print(int(datetime.strptime('2023-10-12 20:15:30', '%Y-%m-%d %H:%M:%S').timestamp()) * 1000)"
# Output: 1697121930000
```

---

## Workflow: Parse → Validate

**Recommended workflow:**

1. First, use `/transactions:parse` to parse and calculate ceiling/remanent from date + amount
2. Then, use `/transactions:validator` to validate the parsed transactions for duplicates

### Example Combined Workflow

```bash
# Step 1: Parse transactions
PARSE_RESPONSE=$(curl -s --location 'http://localhost:5477/blackrock/challenge/v1/transactions:parse' \
  --header 'Content-Type: application/json' \
  --data '[
    {"date": "2023-10-12 20:15:30", "amount": 250},
    {"date": "2023-02-28 15:49:20", "amount": 375}
  ]')

# Step 2: Validate parsed transactions
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
  --header 'Content-Type: application/json' \
  --data "{\"transactions\": $PARSE_RESPONSE}"
```

---

## Common Duplicate Scenarios

### ✅ NOT Duplicates (different amounts)
- Timestamp: 1697121930000, Amount: 250.0
- Timestamp: 1697121930000, Amount: 300.0
→ **Both valid** (different amounts)

### ❌ Duplicates (same timestamp AND amount)
- Timestamp: 1697121930000, Amount: 250.0
- Timestamp: 1697121930000, Amount: 250.0
→ **Second one marked as invalid**

### ✅ NOT Duplicates (different timestamps)
- Timestamp: 1697121930000, Amount: 250.0
- Timestamp: 1697121931000, Amount: 250.0
→ **Both valid** (different timestamps)

---

## Testing with jq (Pretty Print)

```bash
curl -s --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
  --header 'Content-Type: application/json' \
  --data '{
    "transactions": [
      {
        "timestamp": 1697121930000,
        "originalAmount": 250.0,
        "ceiling": 300.0,
        "remanent": 50.0,
        "valid": true,
        "message": null
      }
    ]
  }' | jq
```

---

## Running Tests

```bash
# Run validator tests
mvn test -Dtest=TransactionValidatorServiceTest

# Run integration tests
mvn test -Dtest=FilterControllerIntegrationTest

# Or run all tests
mvn test
```
