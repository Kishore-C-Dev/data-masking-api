# Data Masking REST API

A Spring Boot REST API for masking sensitive data (like account numbers) in various payload formats including XML, JSON, and fixed-length strings.

## Features

- **Multi-format Support**: Handles XML, JSON, and fixed-length string payloads
- **Auto-detection**: Automatically detects payload type
- **Configurable Masking**: YAML-based configuration for different services
- **XPath Support**: Extract and mask XML elements using XPath expressions
- **JSONPath Support**: Extract and mask JSON fields using JSONPath expressions
- **Index-based Masking**: Mask fixed-length strings using start/end indexes
- **Multiple Attributes**: Support for masking multiple attributes per service
- **Last 4 Digits Preserved**: Masks all but the last 4 digits (e.g., `1234567890` → `******7890`)

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Application

```bash
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080 by default.

## API Endpoints

### POST /api/mask

Masks sensitive data in the provided payload.

**Request:**
```json
{
  "transaction_id": "TXN123456",
  "payload_txt": "<xml>...</xml> or {json...} or fixed-length-string"
}
```

**Response:**
```json
{
  "transaction_id": "TXN123456",
  "masked_payload": "masked content",
  "payload_type": "XML|JSON|FIXED"
}
```

### GET /api/health

Health check endpoint.

## Configuration

Edit `src/main/resources/masking-config.yaml` to configure masking rules:

### XML Example:
```yaml
masking:
  rules:
    - service: "payment-service"
      type: "xml"
      attributes:
        - xpath: "//AccountNumber"
        - xpath: "//BankAccount/Number"
```

### JSON Example:
```yaml
masking:
  rules:
    - service: "account-service"
      type: "json"
      attributes:
        - jsonpath: "$.account.accountNumber"
        - jsonpath: "$.customer.bankAccount"
```

### Fixed-Length Example:
```yaml
masking:
  rules:
    - service: "legacy-mainframe"
      type: "fixed"
      attributes:
        - start: 10
          end: 26
        - start: 50
          end: 66
```

## Example Usage

### XML Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN001",
    "payload_txt": "<?xml version=\"1.0\"?><Payment><AccountNumber>1234567890123456</AccountNumber></Payment>"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN001",
  "masked_payload": "<?xml version=\"1.0\"?><Payment><AccountNumber>************3456</AccountNumber></Payment>",
  "payload_type": "XML"
}
```

### JSON Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN002",
    "payload_txt": "{\"account\":{\"accountNumber\":\"9876543210987654\"}}"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN002",
  "masked_payload": "{\"account\":{\"accountNumber\":\"************7654\"}}",
  "payload_type": "JSON"
}
```

### Fixed-Length Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN003",
    "payload_txt": "HEADER0001234567890123456MIDDLE0009876543210987654END"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN003",
  "masked_payload": "HEADER000************3456MIDDLE000************7654END",
  "payload_type": "FIXED"
}
```

## Project Structure

```
data-masking-api/
├── src/main/java/com/example/masking/
│   ├── controller/
│   │   ├── MaskingController.java          # REST API endpoints
│   │   └── GlobalExceptionHandler.java     # Error handling
│   ├── service/
│   │   ├── DataMaskingService.java         # Main orchestration service
│   │   ├── PayloadTypeDetector.java        # Auto-detect payload type
│   │   └── processor/
│   │       ├── MaskingProcessor.java       # Interface
│   │       ├── XmlMaskingProcessor.java    # XML/XPath processor
│   │       ├── JsonMaskingProcessor.java   # JSON/JSONPath processor
│   │       └── FixedLengthMaskingProcessor.java  # Fixed-length processor
│   ├── config/
│   │   └── MaskingConfigLoader.java        # YAML config loader
│   ├── model/
│   │   ├── MaskingRequest.java             # API request model
│   │   ├── MaskingResponse.java            # API response model
│   │   ├── MaskingConfig.java              # Config model
│   │   ├── MaskingRule.java                # Rule model
│   │   ├── MaskingAttribute.java           # Attribute model
│   │   └── PayloadType.java                # Enum for payload types
│   └── DataMaskingApplication.java         # Main application
├── src/main/resources/
│   ├── application.properties              # Spring Boot config
│   └── masking-config.yaml                 # Masking rules config
└── pom.xml                                 # Maven dependencies
```

## Technologies Used

- Spring Boot 3.2.0
- Java 17
- Jackson (JSON/XML processing)
- JSONPath (JSON path extraction)
- XPath (XML path extraction)
- Lombok (reduce boilerplate)
- Maven (build tool)

## Error Handling

The API includes comprehensive error handling for:
- Invalid request payloads
- Missing required fields
- Malformed XML/JSON
- Processing errors

All errors return appropriate HTTP status codes and error messages.

## License

This project is open source and available under the MIT License.
