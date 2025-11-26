# Data Masking REST API

A Spring Boot REST API for masking sensitive data (like account numbers, IBANs, credit card numbers) in various payload formats including XML, JSON, and fixed-length strings.

## Features

- **Multi-format Support**: Handles XML, JSON, and fixed-length string payloads
- **Auto-detection**: Automatically detects payload type
- **Dynamic XML Namespace Support**: Configuration-driven XML subtype detection - add new patterns without code changes
- **ISO 20022 Support**: Built-in support for pain.013, pain.014, camt.035, camt.054 message types
- **Extensible Architecture**: Add custom XML namespaces by editing YAML config only
- **Namespace-aware XPath**: Full support for namespaced XML elements with automatic namespace context setup
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
    - pattern: "pain.013"        # ISO 20022 payment initiation
    - pattern: "pain.014"        # ISO 20022 payment activation
    - pattern: "camt.035"        # ISO 20022 proprietary message
    - pattern: "camt.054"        # ISO 20022 bank to customer debit/credit notification
    - pattern: "payment_request" # Custom payment request namespace
    - pattern: "invoice"         # Custom invoice namespace
```

**How it works**:
- If an XML document's xmlns attribute contains the pattern (e.g., `xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.07"`), the subtype is detected as `XML_PAIN_013`
- Pattern names are automatically converted to type identifiers: `"pain.013"` → `"xml_pain_013"`, `"invoice"` → `"xml_invoice"`
- **No Java code changes required** - just add the pattern to the config and restart the application

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

### Custom XML Namespace Example:
```yaml
masking:
  rules:
    # Add your custom namespace rule (must match the pattern name)
    - type: "xml_invoice"
      attributes:
        - xpath: "//ns:Invoice/ns:BillingAccount"
        - xpath: "//ns:PaymentDetails/ns:AccountNumber"
        - xpath: "//ns:Customer/ns:TaxId"

    - type: "xml_payment_request"
      attributes:
        - xpath: "//ns:PaymentRequest/ns:Account/ns:AccountNumber"
        - xpath: "//ns:SourceAccount/ns:Number"
```

**Important**: The rule type must match the pattern from namespaceMappings (e.g., pattern `"invoice"` requires rule type `"xml_invoice"`)

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

## Adding New XML Namespace Patterns

To add support for a new XML namespace pattern **without modifying Java code**:

1. **Add the pattern to namespaceMappings** in `masking-config.yaml`:
   ```yaml
   namespaceMappings:
     - pattern: "my_custom_schema"
   ```

2. **Add masking rules for the pattern**:
   ```yaml
   rules:
     - type: "xml_my_custom_schema"
       attributes:
         - xpath: "//ns:YourElement/ns:SensitiveField"
   ```

3. **Restart the application** - that's it!

**Example**: Adding a "purchase_order" namespace:

```yaml
masking:
  namespaceMappings:
    - pattern: "purchase_order"

  rules:
    - type: "xml_purchase_order"
      attributes:
        - xpath: "//ns:PurchaseOrder/ns:PaymentInfo/ns:CreditCard"
        - xpath: "//ns:Vendor/ns:BankAccount"
```

The system will automatically:
- Detect XML documents with `xmlns` containing "purchase_order"
- Report subtype as `XML_PURCHASE_ORDER`
- Apply the configured XPath masking rules with namespace awareness

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

### Custom Invoice XML Payload (Dynamic Pattern)

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN003",
    "payload_txt": "<?xml version=\"1.0\"?><Invoice xmlns=\"http://example.com/schemas/invoice/v2\"><BillingAccount>1234567890123456</BillingAccount><PaymentDetails><AccountNumber>9876543210987654</AccountNumber></PaymentDetails><Customer><TaxId>5555666677778888</TaxId></Customer></Invoice>"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN003",
  "masked_payload": "<?xml version=\"1.0\"?><Invoice xmlns=\"http://example.com/schemas/invoice/v2\"><BillingAccount>************3456</BillingAccount><PaymentDetails><AccountNumber>************7654</AccountNumber></PaymentDetails><Customer><TaxId>************8888</TaxId></Customer></Invoice>",
  "payload_type": "XML",
  "detected_subtype": "XML_INVOICE",
  "processing_time_ms": 30
}
```

**Note**: The `invoice` pattern was added via configuration only - no Java code changes required!

### JSON Payload

**Request:**
```bash
curl -X POST http://localhost:8080/api/mask \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN004",
    "payload_txt": "{\"account\":{\"accountNumber\":\"9876543210987654\"}}"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN004",
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
    "transaction_id": "TXN005",
    "payload_txt": "HEADER0001234567890123456MIDDLE0009876543210987654END"
  }'
```

**Response:**
```json
{
  "transaction_id": "TXN005",
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
│   │   └── PayloadType.java                # Enum for base payload types (XML, JSON, FIXED)
│   └── DataMaskingApplication.java         # Main application
├── src/main/resources/
│   ├── application.properties              # Spring Boot config
│   └── masking-config.yaml                 # Masking rules config
└── pom.xml                                 # Maven dependencies
```

## Payload Type Detection

The API automatically detects the payload type using the following logic:

1. **XML Detection**: Payloads starting with `<` or `<?xml` are identified as XML
   - **Dynamic XML Subtype Detection**: Uses regex-based xmlns pattern matching
   - Pattern matching: `xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.07"` → matches pattern `"pain.013"` → returns subtype `XML_PAIN_013`
   - Pattern to type conversion: `"pain.013"` → `"xml_pain_013"`, `"invoice"` → `"xml_invoice"`
   - Falls back to generic `XML` if no pattern matches
   - **Fully extensible**: Add new patterns in YAML without code changes

2. **JSON Detection**: Payloads starting with `{` or `[` are identified as JSON

3. **Fixed-Length Detection**:
   - Specific formats like MTSFTR (starts with `*FTR`), MTSADM (starts with `*ADM`), MFFIXED (starts with `ACAI`)
   - Falls back to generic `FIXED` for other fixed-length formats

4. **Default Masking**: If no specific rules are configured, the default processor masks 10-14 consecutive digits

## Architecture Highlights

### Dynamic XML Subtype System

Unlike traditional enum-based approaches that require code changes for every new XML type, this API uses a **string-based dynamic detection system**:

- **PayloadType enum** only contains base types: `XML`, `JSON`, `FIXED`, etc.
- **XML subtypes are detected dynamically** based on YAML configuration
- **No enum updates needed** - the system converts patterns to type identifiers at runtime
- **Type identifier generation**: Patterns are normalized to lowercase with underscores (e.g., `"pain.013"` becomes `"xml_pain_013"`)
- **Rule matching**: Case-insensitive string matching against configured rule types

**Benefits**:
- Add new ISO 20022 message types without touching Java code
- Support custom organizational XML schemas through configuration
- Faster development and deployment cycles
- Reduced coupling between configuration and code

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
