package com.example.masking.model;

public enum PayloadType {
    XML,      // Generic XML (subtypes detected dynamically via xmlns patterns)
    JSON,
    MTSFTR,   // Fixed payload starting with *FTR
    MTSADM,   // Fixed payload starting with *ADM
    MFFIXED,  // Fixed payload starting with ACAI
    FIXED     // Generic fixed-length (fallback)
}
