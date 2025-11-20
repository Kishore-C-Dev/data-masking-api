package com.example.masking.service.processor;

import com.example.masking.model.MaskingAttribute;

import java.util.List;

public interface MaskingProcessor {

    String mask(String payload, List<MaskingAttribute> attributes);

    default String maskValue(String value) {
        if (value == null || value.length() <= 4) {
            return value;
        }

        int length = value.length();
        int maskLength = length - 4;
        String masked = "*".repeat(maskLength);
        String lastFour = value.substring(length - 4);

        return masked + lastFour;
    }
}
