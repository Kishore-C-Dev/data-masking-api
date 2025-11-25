package com.example.masking.model;

import java.util.List;

public class MaskingRule {

    private String service;
    private String type;
    private List<MaskingAttribute> attributes;

    public MaskingRule() {
    }

    public MaskingRule(String service, String type, List<MaskingAttribute> attributes) {
        this.service = service;
        this.type = type;
        this.attributes = attributes;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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
