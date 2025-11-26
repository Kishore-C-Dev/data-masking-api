package com.example.masking.service.processor;

import com.example.masking.model.MaskingAttribute;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonMaskingProcessor implements MaskingProcessor {

    // Cache JSONPath configuration (immutable, thread-safe)
    private static final Configuration JSON_PATH_CONFIG = Configuration.builder()
            .options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();

    @Override
    public String mask(String payload, List<MaskingAttribute> attributes) {
        try {
            // Use cached configuration
            DocumentContext document = JsonPath.using(JSON_PATH_CONFIG).parse(payload);

            for (MaskingAttribute attribute : attributes) {
                if (attribute.getJsonpath() != null) {
                    try {
                        Object value = document.read(attribute.getJsonpath());

                        if (value != null) {
                            String maskedValue = maskValue(value.toString());
                            document.set(attribute.getJsonpath(), maskedValue);
                        }
                    } catch (Exception e) {
                        // Path not found or error reading, continue with next attribute
                    }
                }
            }

            return document.jsonString();
        } catch (Exception e) {
            throw new RuntimeException("Error masking JSON payload: " + e.getMessage(), e);
        }
    }
}
