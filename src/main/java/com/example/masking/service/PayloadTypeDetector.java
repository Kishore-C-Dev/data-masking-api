package com.example.masking.service;

import com.example.masking.model.PayloadType;
import org.springframework.stereotype.Service;

@Service
public class PayloadTypeDetector {

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

        // Default to fixed-length
        return PayloadType.FIXED;
    }
}
