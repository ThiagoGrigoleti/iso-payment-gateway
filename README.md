# ISO 8583 Payment Gateway

A high-performance Payment Gateway that bridges modern REST/JSON APIs with legacy banking systems using the ISO 8583 protocol over TCP/Binary.

## Features

- **REST API** - JSON-based payment processing endpoints with versioned API (`/api/v1/`)
- **ISO 8583 Protocol** - Full message construction and parsing (MTI 0200/0210)
- **TCP Communication** - Raw socket communication with banking networks
- **Transaction Persistence** - PostgreSQL storage with full audit trail
- **Retry Logic** - Configurable retry mechanism for bank communication
- **Timeout Handling** - Connection and read timeout configuration
- **Card Masking** - PCI-DSS compliant card number masking
- **Spring Actuator** - Production-ready health checks and metrics
- **Swagger UI** - Interactive API documentation

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| ISO Protocol | j8583 |
| Database | PostgreSQL 15 |
| Monitoring | Spring Boot Actuator |
| Documentation | OpenAPI 3 / Swagger |
| Containerization | Docker |

## Project Structure

```
src/main/java/com/example/isogateway/
├── api/
│   ├── controller/          # REST endpoints
│   └── dto/                 # Request/Response objects
├── config/                  # Configuration classes
├── core/
│   ├── domain/              # JPA entities
│   ├── iso/                 # ISO 8583 configuration
│   └── repository/          # Data access layer
├── exception/               # Exception handling
├── infrastructure/
│   └── tcp/                 # TCP client and mock server
├── service/                 # Business logic
└── util/                    # Utility classes
```

## API Endpoints

### Payment Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process a new payment |
| GET | `/api/v1/payments/{id}` | Get transaction by ID |
| GET | `/api/v1/payments/stan/{stan}` | Get transaction by STAN (System Trace Audit Number) |
| GET | `/api/v1/payments` | List transactions (paginated) |
| GET | `/api/v1/stats` | Transaction statistics (counts, avg processing time) |

### Monitoring (Spring Actuator)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health status (Kubernetes ready) |
| GET | `/actuator/metrics` | JVM and application metrics |
| GET | `/actuator/info` | Application info |

### Documentation
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/swagger-ui.html` | Interactive API Documentation |
| GET | `/api-docs` | OpenAPI JSON specification |

## Quick Start

### Using Docker Compose

```bash
docker-compose up -d
```

### Manual Setup

1. Start PostgreSQL:
```bash
docker run -d --name isobank-db \
  -e POSTGRES_DB=isobank \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 \
  postgres:15-alpine
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `gateway.bank.host` | localhost | Bank server host |
| `gateway.bank.port` | 9999 | Bank server port |
| `gateway.bank.connection-timeout-ms` | 5000 | Connection timeout |
| `gateway.bank.read-timeout-ms` | 30000 | Read timeout |
| `gateway.bank.max-retries` | 3 | Max retry attempts |

## Sample Request

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "amount": 150.00
  }'
```

## Sample Response

```json
{
  "transactionId": 1,
  "stan": "000001",
  "status": "APPROVED",
  "responseCode": "00",
  "responseDescription": "Approved",
  "cardNumberMasked": "411111******1111",
  "amount": 150.00,
  "currency": "BRL",
  "authorizationCode": "123456",
  "processingTimeMs": 245,
  "timestamp": "2026-01-20T10:30:00"
}
```

## Running Tests

```bash
./mvnw test
```

## Future Improvements

The following enhancements are planned to evolve this project into a production-grade financial system:

### Critical (Financial Domain)
| Feature | Description | Why it matters |
|---------|-------------|----------------|
| **Reversal/Void (MTI 0400/0420)** | Automatic reversal when bank doesn't respond | In financial systems, timeout ≠ failure, it means "unknown state". Auto-reversal prevents double charging |
| **Idempotency Keys** | Prevent duplicate transactions via `Idempotency-Key` header | If user clicks "Pay" twice, charge only once. Requires Redis for distributed lock |

### Performance
| Feature | Description | Why it matters |
|---------|-------------|----------------|
| **TCP Connection Pooling** | Reuse socket connections instead of `new Socket()` per request | TCP handshake is slow. Pool of 10 persistent connections = massive throughput gain |
| **Async Processing** | Non-blocking I/O with `CompletableFuture` | Handle thousands of concurrent transactions without thread exhaustion |

### Production Readiness
| Feature | Description | Why it matters |
|---------|-------------|----------------|
| **Database Migrations (Flyway)** | Versioned SQL scripts instead of `hibernate.ddl-auto=update` | `ddl-auto=update` is forbidden in production. Flyway = professional schema management |
| **Distributed Tracing (OpenTelemetry)** | End-to-end request tracing across services | Debug production issues by following a transaction through all systems |
| **Rate Limiting** | Throttle requests per client/IP | Prevent abuse and ensure fair resource allocation |
| **API Authentication** | OAuth2/JWT token validation | Secure the API for production use |

### Testing
| Feature | Description | Why it matters |
|---------|-------------|----------------|
| **Integration Tests (Testcontainers)** | Real PostgreSQL + real TCP server in tests | Unit tests aren't enough for financial systems |
| **Load Testing (Gatling)** | Simulate thousands of concurrent users | Know your system's limits before production |

## License

MIT