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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String mask(String payload, List<MaskingAttribute> attributes) {
        try {
            Configuration conf = Configuration.builder()
                    .options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL)
                    .build();

            DocumentContext document = JsonPath.using(conf).parse(payload);

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
