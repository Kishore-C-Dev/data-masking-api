package com.example.masking.service;

import com.example.masking.model.MaskingAttribute;
import com.example.masking.model.MaskingConfig;
import com.example.masking.model.MaskingRule;
import com.example.masking.model.PayloadType;
import com.example.masking.service.processor.FixedLengthMaskingProcessor;
import com.example.masking.service.processor.JsonMaskingProcessor;
import com.example.masking.service.processor.MaskingProcessor;
import com.example.masking.service.processor.XmlMaskingProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DataMaskingService {

    private final PayloadTypeDetector payloadTypeDetector;
    private final XmlMaskingProcessor xmlMaskingProcessor;
    private final JsonMaskingProcessor jsonMaskingProcessor;
    private final FixedLengthMaskingProcessor fixedLengthMaskingProcessor;
    private final MaskingConfig maskingConfig;

    public DataMaskingService(PayloadTypeDetector payloadTypeDetector,
                              XmlMaskingProcessor xmlMaskingProcessor,
                              JsonMaskingProcessor jsonMaskingProcessor,
                              FixedLengthMaskingProcessor fixedLengthMaskingProcessor,
                              MaskingConfig maskingConfig) {
        this.payloadTypeDetector = payloadTypeDetector;
        this.xmlMaskingProcessor = xmlMaskingProcessor;
        this.jsonMaskingProcessor = jsonMaskingProcessor;
        this.fixedLengthMaskingProcessor = fixedLengthMaskingProcessor;
        this.maskingConfig = maskingConfig;
    }

    public String maskPayload(String payload, PayloadType detectedType) {
        log.info("Masking payload of type: {}", detectedType);

        List<MaskingAttribute> attributes = getAttributesForType(detectedType);

        if (attributes.isEmpty()) {
            log.warn("No masking rules found for payload type: {}. Returning original payload.", detectedType);
            return payload;
        }

        MaskingProcessor processor = getProcessor(detectedType);
        return processor.mask(payload, attributes);
    }

    public PayloadType detectPayloadType(String payload) {
        return payloadTypeDetector.detectType(payload);
    }

    private List<MaskingAttribute> getAttributesForType(PayloadType type) {
        List<MaskingAttribute> allAttributes = new ArrayList<>();

        if (maskingConfig.getRules() != null) {
            for (MaskingRule rule : maskingConfig.getRules()) {
                if (rule.getType() != null && rule.getType().equalsIgnoreCase(type.name())) {
                    if (rule.getAttributes() != null) {
                        allAttributes.addAll(rule.getAttributes());
                    }
                }
            }
        }

        return allAttributes;
    }

    private MaskingProcessor getProcessor(PayloadType type) {
        return switch (type) {
            case XML -> xmlMaskingProcessor;
            case JSON -> jsonMaskingProcessor;
            case FIXED -> fixedLengthMaskingProcessor;
        };
    }
}
