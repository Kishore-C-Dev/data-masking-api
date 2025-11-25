package com.example.masking.service;

import com.example.masking.model.NamespaceMapping;
import com.example.masking.model.PayloadType;
import com.example.masking.model.XmlSubtypeInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PayloadTypeDetector {

    // Regex to extract root element and its attributes
    private static final Pattern XML_ROOT_PATTERN =
            Pattern.compile("<\\?xml[^>]*>\\s*<([^\\s>]+)([^>]*)>", Pattern.DOTALL);

    // Regex to extract xmlns attributes (xmlns="..." or xmlns:prefix="...")
    private static final Pattern XMLNS_PATTERN = Pattern.compile("xmlns(?::[^=]+)?=\"([^\"]+)\"");

    public PayloadType detectType(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be null or empty");
        }

        String trimmed = payload.trim();

        // Check for XML
        if (trimmed.startsWith("<") || trimmed.startsWith("<?xml")) {
            return PayloadType.XML;
        }

        // Check for JSON
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return PayloadType.JSON;
        }

        // Check for specific fixed-length formats by starting characters
        if (trimmed.startsWith("*FTR")) {
            return PayloadType.MTSFTR;
        }

        if (trimmed.startsWith("*ADM")) {
            return PayloadType.MTSADM;
        }

        if (trimmed.startsWith("ACAI")) {
            return PayloadType.MFFIXED;
        }

        // Default to generic fixed-length
        return PayloadType.FIXED;
    }

    /**
     * Detects XML subtype by examining xmlns attributes in the root element.
     * Uses lightweight regex parsing to avoid full DOM overhead during detection phase.
     * Returns a string subtype identifier instead of requiring PayloadType enum updates.
     *
     * @param payload XML payload string
     * @param mappings Configured namespace mappings
     * @return String subtype identifier (e.g., "xml_pain_013") or null if no match
     */
    public String detectXmlSubtype(String payload, List<NamespaceMapping> mappings) {
        if (payload == null || mappings == null || mappings.isEmpty()) {
            return null;
        }

        try {
            // 1. Extract root element and attributes using regex (avoid full DOM parsing)
            Matcher matcher = XML_ROOT_PATTERN.matcher(payload.trim());
            if (!matcher.find()) {
                return null; // Not valid XML or no root element
            }

            String rootAttributes = matcher.group(2);

            // 2. Extract xmlns attributes (xmlns="..." or xmlns:prefix="...")
            Matcher xmlnsMatcher = XMLNS_PATTERN.matcher(rootAttributes);

            // 3. Check each xmlns against namespace mappings
            while (xmlnsMatcher.find()) {
                String namespaceUri = xmlnsMatcher.group(1);

                for (NamespaceMapping mapping : mappings) {
                    if (namespaceUri.contains(mapping.getPattern())) {
                        // Convert pattern to string type identifier (e.g., "pain.013" -> "xml_pain_013")
                        return patternToTypeIdentifier(mapping.getPattern());
                    }
                }
            }
        } catch (Exception e) {
            // If regex parsing fails, return null
            return null;
        }

        return null; // No matching namespace found
    }

    /**
     * Converts a pattern to its corresponding type identifier string.
     *
     * @param pattern The pattern that matched (e.g., "pain.013", "camt.054", "payment_request")
     * @return Type identifier string (e.g., "xml_pain_013", "xml_camt_054", "xml_payment_request")
     */
    private String patternToTypeIdentifier(String pattern) {
        // Convert pattern like "pain.013" to type identifier "xml_pain_013"
        // Replace dots with underscores and convert to lowercase
        return "xml_" + pattern.replace(".", "_").toLowerCase();
    }

    /**
     * Extracts the xmlns namespace URI from the XML root element.
     *
     * @param payload XML payload string
     * @return The namespace URI, or null if not found
     */
    public String extractNamespace(String payload) {
        if (payload == null) {
            return null;
        }

        try {
            Matcher matcher = XML_ROOT_PATTERN.matcher(payload.trim());
            if (!matcher.find()) {
                return null;
            }

            String rootAttributes = matcher.group(2);
            Matcher xmlnsMatcher = XMLNS_PATTERN.matcher(rootAttributes);

            // Return the first xmlns found
            if (xmlnsMatcher.find()) {
                return xmlnsMatcher.group(1);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}
