package com.example.masking.model;

import java.util.List;

public class MaskingRule {

    private String type;
    private List<MaskingAttribute> attributes;

    public MaskingRule() {
    }

    public MaskingRule(String type, List<MaskingAttribute> attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MaskingAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<MaskingAttribute> attributes) {
        this.attributes = attributes;
    }
}
