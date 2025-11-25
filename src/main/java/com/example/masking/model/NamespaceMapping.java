package com.example.masking.model;

public class NamespaceMapping {
    private String pattern;      // Pattern to search in xmlns (e.g., "pain.013", "camt.054")

    public NamespaceMapping() {
    }

    public NamespaceMapping(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "NamespaceMapping{" +
                "pattern='" + pattern + '\'' +
                '}';
    }
}
