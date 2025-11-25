package com.example.masking.model;

public enum PayloadType {
    XML,              // Generic XML (fallback)
    XML_PAIN_013,     // ISO 20022 pain.013
    XML_PAIN_014,     // ISO 20022 pain.014
    XML_CAMT_035,     // ISO 20022 camt.035
    XML_CAMT_054,     // ISO 20022 camt.054
    JSON,
    MTSFTR,           // Fixed payload starting with *FTR
    MTSADM,           // Fixed payload starting with *ADM
    MFFIXED,          // Fixed payload starting with ACAI
    FIXED             // Generic fixed-length (fallback)
}
