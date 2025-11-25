# Data Masking REST API

A Spring Boot REST API for masking sensitive data (like account numbers, IBANs, credit card numbers) in various payload formats including XML, JSON, and fixed-length strings.

## Features

- **Multi-format Support**: Handles XML, JSON, and fixed-length string payloads
- **Auto-detection**: Automatically detects payload type
- **XML Namespace Support**: ISO 20022 message type detection (pain.013, pain.014, camt.035, camt.054) with namespace-aware XPath processing
- **Configurable Masking**: YAML-based configuration for different payload types
- **XPath Support**: Extract and mask XML elements using XPath expressions (with namespace support)
- **JSONPath Support**: Extract and mask JSON fields using JSONPath expressions
- **Index-based Masking**: Mask fixed-length strings using start/end indexes
- **Default Masking**: Automatically masks 10-14 consecutive digits when no rules are configured
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
  "payload_type": "XML|JSON|FIXED",
  "detected_subtype": "XML_PAIN_013|XML_CAMT_054|XML|etc",
  "processing_time_ms": 42
}
```

### GET /api/health

Health check endpoint.

## Configuration

Edit `src/main/resources/masking-config.yaml` to configure masking rules:

### Namespace Mappings (for XML Subtype Detection):
```yaml
masking:
  namespaceMappings:
    - pattern: "pain.013"    # ISO 20022 payment initiation
    - pattern: "pain.014"    # ISO 20022 payment activation
    - pattern: "camt.035"    # ISO 20022 proprietary message
    - pattern: "camt.054"    # ISO 20022 bank to customer debit/credit notification
```

**How it works**: If an XML document's xmlns attribute contains the pattern (e.g., `xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.07"`), the payload type is detected as `XML_PAIN_013`.

### Generic XML Example:
```yaml
masking:
  rules:
    - type: "xml"
      attributes:
        - xpath: "//AccountNumber"
        - xpath: "//BankAccount/Number"
        - xpath: "//CreditCard/CardNumber"
```

### ISO 20022 XML Example (with Namespaces):
```yaml
masking:
  rules:
    - type: "xml_pain_013"
      attributes:
        - xpath: "//ns:CdtrPmtActvtnReq/ns:PmtInf/ns:CdtrAcct/ns:Id/ns:IBAN"
        - xpath: "//ns:DbtrAcct/ns:Id/ns:IBAN"

    - type: "xml_camt_054"
      attributes:
        - xpath: "//ns:Acct/ns:Id/ns:IBAN"
        - xpath: "//ns:RltdAcct/ns:Id/ns:IBAN"
```

**Note**: For namespaced XML, use the `ns:` prefix in XPath expressions. The namespace context is automatically configured based on the detected xmlns URI.

### JSON Example:
```yaml
masking:
  rules:
    - type: "json"
      attributes:
        - jsonpath: "$.account.accountNumber"
        - jsonpath: "$.customer.bankAccount"
```

### Fixed-Length Example:
```yaml
masking:
  rules:
    - type: "fixed"
      attributes:
        - start: 10
          end: 26
        - start: 50
          end: 66
```

### Default Masking:
If no rules match the detected payload type, the default masking processor automatically masks any sequence of 10-14 consecutive digits in the payload.

## Example Usage

### Generic XML Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN001",
    "payload_txt": "<?xml version=\"1.0\"?><Payment><AccountNumber>1234567890123456</AccountNumber><BankAccount><Number>9876543210987654</Number></BankAccount></Payment>"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN001",
  "masked_payload": "<?xml version=\"1.0\"?><Payment><AccountNumber>************3456</AccountNumber><BankAccount><Number>************7654</Number></BankAccount></Payment>",
  "payload_type": "XML",
  "detected_subtype": "XML",
  "processing_time_ms": 4
}
```

### ISO 20022 pain.013 XML Payload (with Namespace)

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN002",
    "payload_txt": "<?xml version=\"1.0\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.013.001.07\"><CdtrPmtActvtnReq><PmtInf><CdtrAcct><Id><IBAN>GB33BUKB20201555555555</IBAN></Id></CdtrAcct></PmtInf></CdtrPmtActvtnReq></Document>"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN002",
  "masked_payload": "<?xml version=\"1.0\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.013.001.07\"><CdtrPmtActvtnReq><PmtInf><CdtrAcct><Id><IBAN>******************5555</IBAN></Id></CdtrAcct></PmtInf></CdtrPmtActvtnReq></Document>",
  "payload_type": "XML",
  "detected_subtype": "XML_PAIN_013",
  "processing_time_ms": 42
}
```

### JSON Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN003",
    "payload_txt": "{\"account\":{\"accountNumber\":\"9876543210987654\"}}"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN003",
  "masked_payload": "{\"account\":{\"accountNumber\":\"************7654\"}}",
  "payload_type": "JSON",
  "detected_subtype": "JSON",
  "processing_time_ms": 3
}
```

### Fixed-Length Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN004",
    "payload_txt": "HEADER0001234567890123456MIDDLE0009876543210987654END"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN004",
  "masked_payload": "HEADER000************3456MIDDLE000************7654END",
  "payload_type": "FIXED",
  "detected_subtype": "FIXED",
  "processing_time_ms": 1
}
```

**Note**: This example uses the default masking processor which automatically detects and masks sequences of 10-14 consecutive digits.

## Project Structure

```
data-masking-api/
├── src/main/java/com/example/masking/
│   ├── controller/
│   │   ├── MaskingController.java          # REST API endpoints
│   │   └── GlobalExceptionHandler.java     # Error handling
│   ├── service/
│   │   ├── DataMaskingService.java         # Main orchestration service
│   │   ├── PayloadTypeDetector.java        # Auto-detect payload type & XML subtypes
│   │   └── processor/
│   │       ├── MaskingProcessor.java       # Interface
│   │       ├── XmlMaskingProcessor.java    # XML/XPath processor (namespace-aware)
│   │       ├── SimpleNamespaceContext.java # XPath namespace context implementation
│   │       ├── JsonMaskingProcessor.java   # JSON/JSONPath processor
│   │       ├── FixedLengthMaskingProcessor.java  # Fixed-length processor
│   │       └── DefaultMaskingProcessor.java      # Default regex-based masking
│   ├── config/
│   │   └── MaskingConfigLoader.java        # YAML config loader
│   ├── model/
│   │   ├── MaskingRequest.java             # API request model
│   │   ├── MaskingResponse.java            # API response model
│   │   ├── MaskingConfig.java              # Config model
│   │   ├── MaskingRule.java                # Rule model
│   │   ├── MaskingAttribute.java           # Attribute model
│   │   ├── NamespaceMapping.java           # Namespace pattern mapping
│   │   └── PayloadType.java                # Enum for payload types (includes XML subtypes)
│   └── DataMaskingApplication.java         # Main application
├── src/main/resources/
│   ├── application.properties              # Spring Boot config
│   └── masking-config.yaml                 # Masking rules config
└── pom.xml                                 # Maven dependencies
```

## Payload Type Detection

The API automatically detects the payload type using the following logic:

1. **XML Detection**: Payloads starting with `<` or `<?xml` are identified as XML
   - **XML Subtype Detection**: If an xmlns attribute is found in the root element, the API checks against configured namespace patterns
   - Pattern matching: `xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.07"` → matches pattern `"pain.013"` → returns `XML_PAIN_013`
   - Falls back to generic `XML` if no pattern matches

2. **JSON Detection**: Payloads starting with `{` or `[` are identified as JSON

3. **Fixed-Length Detection**:
   - Specific formats like MTSFTR (starts with `*FTR`), MTSADM (starts with `*ADM`), MFFIXED (starts with `ACAI`)
   - Falls back to generic `FIXED` for other fixed-length formats

4. **Default Masking**: If no specific rules are configured, the default processor masks 10-14 consecutive digits

## Technologies Used

- Spring Boot 2.7.18
- Java 17
- Jackson (JSON/YAML processing)
- JSONPath (JSON path extraction)
- XPath with Namespace Context (XML path extraction)
- DOM Parser (XML processing)
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
