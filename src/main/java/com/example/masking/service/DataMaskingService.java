package com.example.masking.service;

import com.example.masking.model.MaskingAttribute;
import com.example.masking.model.MaskingConfig;
import com.example.masking.model.MaskingRule;
import com.example.masking.model.PayloadType;
import com.example.masking.service.processor.DefaultMaskingProcessor;
import com.example.masking.service.processor.FixedLengthMaskingProcessor;
import com.example.masking.service.processor.JsonMaskingProcessor;
import com.example.masking.service.processor.MaskingProcessor;
import com.example.masking.service.processor.XmlMaskingProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataMaskingService {

    private static final Logger log = LoggerFactory.getLogger(DataMaskingService.class);

    private final PayloadTypeDetector payloadTypeDetector;
    private final XmlMaskingProcessor xmlMaskingProcessor;
    private final JsonMaskingProcessor jsonMaskingProcessor;
    private final FixedLengthMaskingProcessor fixedLengthMaskingProcessor;
    private final DefaultMaskingProcessor defaultMaskingProcessor;
    private final MaskingConfig maskingConfig;

    // Rule index for O(1) lookup (built at startup)
    private final Map<String, List<MaskingAttribute>> ruleIndex;

    // Store last detected subtype for retrieval by controller
    private ThreadLocal<String> lastDetectedSubtype = new ThreadLocal<>();

    public DataMaskingService(PayloadTypeDetector payloadTypeDetector,
                              XmlMaskingProcessor xmlMaskingProcessor,
                              JsonMaskingProcessor jsonMaskingProcessor,
                              FixedLengthMaskingProcessor fixedLengthMaskingProcessor,
                              DefaultMaskingProcessor defaultMaskingProcessor,
                              MaskingConfig maskingConfig) {
        this.payloadTypeDetector = payloadTypeDetector;
        this.xmlMaskingProcessor = xmlMaskingProcessor;
        this.jsonMaskingProcessor = jsonMaskingProcessor;
        this.fixedLengthMaskingProcessor = fixedLengthMaskingProcessor;
        this.defaultMaskingProcessor = defaultMaskingProcessor;
        this.maskingConfig = maskingConfig;

        // Build rule index at startup for fast O(1) lookups
        this.ruleIndex = buildRuleIndex(maskingConfig);
        log.info("Built rule index with {} types", ruleIndex.size());
    }

    public String maskPayload(String payload, PayloadType detectedType) {
        log.info("Masking payload of type: {}", detectedType);

        String detectedNamespace = null;
        String xmlSubtype = null;

        // Detect XML subtype if applicable
        if (detectedType == PayloadType.XML || payload.trim().startsWith("<")) {
            xmlSubtype = payloadTypeDetector.detectXmlSubtype(
                    payload,
                    maskingConfig.getNamespaceMappings()
            );

            if (xmlSubtype != null) {
                // Extract namespace for XPath processing
                detectedNamespace = payloadTypeDetector.extractNamespace(payload);
                log.info("Detected XML subtype: {} with namespace: {}", xmlSubtype, detectedNamespace);
            }
        }

        // Store detected subtype for controller to retrieve
        if (xmlSubtype != null) {
            lastDetectedSubtype.set(xmlSubtype.toUpperCase());
        } else {
            lastDetectedSubtype.set(detectedType.name());
        }

        // Get attributes using subtype if available, otherwise use base type
        List<MaskingAttribute> attributes = xmlSubtype != null ?
                getAttributesForTypeString(xmlSubtype) :
                getAttributesForType(detectedType);

        if (attributes.isEmpty()) {
            log.warn("No masking rules found for payload type: {}. Using default masking (10-14 consecutive digits).",
                    xmlSubtype != null ? xmlSubtype : detectedType);
            return defaultMaskingProcessor.mask(payload, null);
        }

        MaskingProcessor processor = getProcessor(detectedType);

        // Pass namespace to XML processor if detected
        if (processor instanceof XmlMaskingProcessor && detectedNamespace != null) {
            return ((XmlMaskingProcessor) processor).maskWithNamespace(payload, attributes, detectedNamespace);
        }

        return processor.mask(payload, attributes);
    }

    public PayloadType detectPayloadType(String payload) {
        return payloadTypeDetector.detectType(payload);
    }

    public String getLastDetectedSubtype() {
        return lastDetectedSubtype.get();
    }

    /**
     * Clears ThreadLocal state to prevent memory leaks in thread pools.
     * Should be called in finally block after request processing.
     */
    public void clearThreadLocalState() {
        lastDetectedSubtype.remove();
    }

    /**
     * Builds an index of masking rules for O(1) lookup performance.
     * Called once at service initialization.
     */
    private Map<String, List<MaskingAttribute>> buildRuleIndex(MaskingConfig config) {
        Map<String, List<MaskingAttribute>> index = new HashMap<>();

        if (config.getRules() != null) {
            for (MaskingRule rule : config.getRules()) {
                if (rule.getType() != null && rule.getAttributes() != null) {
                    String typeKey = rule.getType().toLowerCase();
                    index.computeIfAbsent(typeKey, k -> new ArrayList<>())
                         .addAll(rule.getAttributes());
                }
            }
        }

        return Collections.unmodifiableMap(index);
    }

    private List<MaskingAttribute> getAttributesForType(PayloadType type) {
        return getAttributesForTypeString(type.name());
    }

    private List<MaskingAttribute> getAttributesForTypeString(String typeString) {
        // Use O(1) index lookup instead of O(N) linear scan
        return ruleIndex.getOrDefault(typeString.toLowerCase(), Collections.emptyList());
    }

    private MaskingProcessor getProcessor(PayloadType type) {
        // Check if it's any XML type (starts with XML)
        if (type.name().startsWith("XML")) {
            return xmlMaskingProcessor;
        } else if (type == PayloadType.JSON) {
            return jsonMaskingProcessor;
        } else {
            // All fixed-length types use the same processor
            return fixedLengthMaskingProcessor;
        }
    }
}
