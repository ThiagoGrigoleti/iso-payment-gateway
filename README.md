# ISO 8583 Payment Gateway

A high-performance Payment Gateway that bridges modern REST/JSON APIs with legacy banking systems using the ISO 8583 protocol over TCP/Binary.

## Features

- **REST API** - JSON-based payment processing endpoints
- **ISO 8583 Protocol** - Full message construction and parsing (MTI 0200/0210)
- **TCP Communication** - Raw socket communication with banking networks
- **Transaction Persistence** - PostgreSQL storage with full audit trail
- **Retry Logic** - Configurable retry mechanism for bank communication
- **Timeout Handling** - Connection and read timeout configuration
- **Card Masking** - PCI-DSS compliant card number masking
- **Metrics** - Transaction statistics and monitoring endpoints
- **Swagger UI** - Interactive API documentation

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| ISO Protocol | j8583 |
| Database | PostgreSQL 15 |
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

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process a new payment |
| GET | `/api/v1/payments/{id}` | Get transaction by ID |
| GET | `/api/v1/payments/stan/{stan}` | Get transaction by STAN |
| GET | `/api/v1/payments` | List transactions (paginated) |
| GET | `/api/v1/health` | Health check |
| GET | `/api/v1/metrics` | Transaction metrics |
| GET | `/swagger-ui.html` | API Documentation |

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

## License

MIT

Nota: A aplicação iniciará simultaneamente um Mock Server TCP na porta 9999 para simular a instituição financeira.

3. Documentação da API
Após a inicialização, a documentação Swagger estará disponível em:
http://localhost:8080/swagger-ui/index.html

Exemplo de Uso

Para realizar uma transação de teste, utilize o seguinte comando cURL:

curl -X POST http://localhost:8080/api/payments -H "Content-Type: application/json" -d "{ "cardNumber": "4758123456789010", "amount": 150.00 }"

Resposta Esperada:
Aprovado: 00