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
     *
     * @param payload XML payload string
     * @param mappings Configured namespace mappings
     * @return PayloadType matching the xmlns pattern, or PayloadType.XML if no match
     */
    public PayloadType detectXmlSubtype(String payload, List<NamespaceMapping> mappings) {
        if (payload == null || mappings == null || mappings.isEmpty()) {
            return PayloadType.XML;
        }

        try {
            // 1. Extract root element and attributes using regex (avoid full DOM parsing)
            Matcher matcher = XML_ROOT_PATTERN.matcher(payload.trim());
            if (!matcher.find()) {
                return PayloadType.XML; // Not valid XML or no root element
            }

            String rootAttributes = matcher.group(2);

            // 2. Extract xmlns attributes (xmlns="..." or xmlns:prefix="...")
            Matcher xmlnsMatcher = XMLNS_PATTERN.matcher(rootAttributes);

            // 3. Check each xmlns against namespace mappings
            while (xmlnsMatcher.find()) {
                String namespaceUri = xmlnsMatcher.group(1);

                for (NamespaceMapping mapping : mappings) {
                    if (namespaceUri.contains(mapping.getPattern())) {
                        // Convert pattern to PayloadType enum (e.g., "pain.013" -> XML_PAIN_013)
                        return patternToPayloadType(mapping.getPattern(), namespaceUri);
                    }
                }
            }
        } catch (Exception e) {
            // If regex parsing fails, return generic XML
            return PayloadType.XML;
        }

        return PayloadType.XML; // No matching namespace found
    }

    /**
     * Converts a pattern to its corresponding PayloadType enum.
     *
     * @param pattern The pattern that matched (e.g., "pain.013", "camt.054")
     * @param namespaceUri The full namespace URI (for logging/debugging)
     * @return Corresponding PayloadType enum
     */
    private PayloadType patternToPayloadType(String pattern, String namespaceUri) {
        // Convert pattern like "pain.013" to enum name "XML_PAIN_013"
        String enumName = "XML_" + pattern.replace(".", "_").toUpperCase();

        try {
            return PayloadType.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            // If no matching enum exists, fall back to generic XML
            return PayloadType.XML;
        }
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
