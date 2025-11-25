package com.example.masking.service.processor;

import com.example.masking.model.MaskingAttribute;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DefaultMaskingProcessor implements MaskingProcessor {

    // Regex to match consecutive 10-14 digit numbers (with or without word boundaries)
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{10,14}");

    @Override
    public String mask(String payload, List<MaskingAttribute> attributes) {
        // For default processing, we ignore attributes and auto-detect account numbers
        return maskConsecutiveDigits(payload);
    }

    /**
     * Automatically detects and masks any consecutive 10-14 digit numbers in the payload.
     * This is a fallback when no specific rules match the payload type.
     */
    private String maskConsecutiveDigits(String payload) {
        if (payload == null || payload.isEmpty()) {
            return payload;
        }

        StringBuffer maskedPayload = new StringBuffer();
        Matcher matcher = ACCOUNT_NUMBER_PATTERN.matcher(payload);

        while (matcher.find()) {
            String accountNumber = matcher.group();
            String masked = maskValue(accountNumber);
            matcher.appendReplacement(maskedPayload, masked);
        }

        matcher.appendTail(maskedPayload);
        return maskedPayload.toString();
    }
}
