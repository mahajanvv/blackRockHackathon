# transactions:validator API - Updated CURL Examples

## Refactored Endpoint
**POST** `/blackrock/challenge/v1/transactions:validator`

## New Request Format
```json
{
    "wage": 50000,
    "transactions": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 300.0,
            "remanent": 50.0
        },
        ...
    ]
}
```

## New Response Format
```json
{
    "valid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 300.0,
            "remanent": 50.0
        }
    ],
    "invalid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 300.0,
            "remanent": 50.0,
            "message": "Duplicate transaction: same date and amount"
        }
    ]
}
```

---

## CURL Example 1: Valid Transactions

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
    "wage": 50000,
    "transactions": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-02-28 15:49:20",
            "amount": 3750,
            "ceiling": 3800.0,
            "remanent": 50.0
        }
    ]
}'
```

**Expected Response:**
```json
{
    "valid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-02-28 15:49:20",
            "amount": 3750.0,
            "ceiling": 3800.0,
            "remanent": 50.0
        }
    ],
    "invalid": []
}
```

---

## CURL Example 2: Detect Duplicates

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
    "wage": 50000,
    "transactions": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 2600.0,
            "remanent": 88.0
        }
    ]
}'
```

**Expected Response:**
```json
{
    "valid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 2600.0,
            "remanent": 88.0
        }
    ],
    "invalid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 2600.0,
            "remanent": 88.0,
            "message": "Duplicate transaction: same date and amount"
        },
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 2600.0,
            "remanent": 88.0,
            "message": "Duplicate transaction: same date and amount"
        }
    ]
}
```

---

## CURL Example 3: Mixed Valid, Invalid, and Duplicates

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
    "wage": 50000,
    "transactions": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-11-15 10:30:45",
            "amount": -500,
            "ceiling": 0.0,
            "remanent": 0.0
        },
        {
            "date": "2023-12-20 14:22:10",
            "amount": 1500,
            "ceiling": 1600.0,
            "remanent": 100.0
        }
    ]
}'
```

**Expected Response:**
```json
{
    "valid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 2600.0,
            "remanent": 88.0
        },
        {
            "date": "2023-12-20 14:22:10",
            "amount": 1500.0,
            "ceiling": 1600.0,
            "remanent": 100.0
        }
    ],
    "invalid": [
        {
            "date": "2023-10-12 20:15:30",
            "amount": 2512.0,
            "ceiling": 2600.0,
            "remanent": 88.0,
            "message": "Duplicate transaction: same date and amount"
        },
        {
            "date": "2023-11-15 10:30:45",
            "amount": -500.0,
            "ceiling": 0.0,
            "remanent": 0.0,
            "message": "Negative amounts are not allowed"
        }
    ]
}
```

---

## CURL Example 4: Empty Transaction List

```bash
curl --location 'http://localhost:5477/blackrock/challenge/v1/transactions:validator' \
--header 'Content-Type: application/json' \
--data '{
    "wage": 50000,
    "transactions": []
}'
```

**Expected Response:**
```json
{
    "valid": [],
    "invalid": []
}
```

---

## Key Changes

### Request Changes
- **Before**: Flat list of ParsedTransaction objects
- **Now**: Wrapper object with `wage` and `transactions` array
- **Transaction format**: Now includes `date` (string), `amount`, `ceiling`, `remanent`

### Response Changes
- **Before**: `{"validTransactions": [...], "invalidTransactions": [...], "totalCount": ..., "validCount": ..., "invalidCount": ...}`
- **Now**: `{"valid": [...], "invalid": [...]}`
- **Invalid items**: Now include a `message` field explaining why they're invalid
- **Duplicate detection**: Uses `date` + `amount` combination

### Validation Logic
1. ✅ **Valid transactions**: First occurrence of date+amount combination
2. ❌ **Duplicates**: Same date AND amount appears multiple times
3. ❌ **Negative amounts**: Amount < 0

---

## Testing

```bash
# Compile
mvn clean compile -DskipTests

# Test
mvn test -Dtest=FilterControllerIntegrationTest

# Or run all tests
mvn test
```

---

## Response DTOs Created

1. **ValidatorTransactionResponse**: For valid transactions (date, amount, ceiling, remanent)
2. **InvalidTransactionResponse**: For invalid transactions (extends with message field)
3. **ValidatorResponse**: Top-level response (valid array, invalid array)
