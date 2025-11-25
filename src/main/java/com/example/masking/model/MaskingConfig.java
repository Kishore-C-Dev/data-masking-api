package com.example.masking.model;

import java.util.List;

public class MaskingConfig {

    private List<MaskingRule> rules;

    public MaskingConfig() {
    }

    public MaskingConfig(List<MaskingRule> rules) {
        this.rules = rules;
    }

    public List<MaskingRule> getRules() {
        return rules;
    }

    public void setRules(List<MaskingRule> rules) {
        this.rules = rules;
    }
}
