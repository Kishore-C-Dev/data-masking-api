package com.example.masking.service.processor;

import com.example.masking.model.MaskingAttribute;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FixedLengthMaskingProcessor implements MaskingProcessor {

    @Override
    public String mask(String payload, List<MaskingAttribute> attributes) {
        StringBuilder result = new StringBuilder(payload);

        for (MaskingAttribute attribute : attributes) {
            if (attribute.getStart() != null && attribute.getEnd() != null) {
                int start = attribute.getStart();
                int end = attribute.getEnd();

                if (start >= 0 && end <= payload.length() && start < end) {
                    String value = payload.substring(start, end);
                    String maskedValue = maskValue(value);

                    result.replace(start, end, maskedValue);
                }
            }
        }

        return result.toString();
    }
}
