package com.example.masking.model;

public class MaskingResponse {

    private String transaction_id;
    private String masked_payload;
    private String payload_type;
    private Long processing_time_ms;

    public MaskingResponse() {
    }

    public MaskingResponse(String transaction_id, String masked_payload, String payload_type) {
        this.transaction_id = transaction_id;
        this.masked_payload = masked_payload;
        this.payload_type = payload_type;
    }

    public MaskingResponse(String transaction_id, String masked_payload, String payload_type, Long processing_time_ms) {
        this.transaction_id = transaction_id;
        this.masked_payload = masked_payload;
        this.payload_type = payload_type;
        this.processing_time_ms = processing_time_ms;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getMasked_payload() {
        return masked_payload;
    }

    public void setMasked_payload(String masked_payload) {
        this.masked_payload = masked_payload;
    }

    public String getPayload_type() {
        return payload_type;
    }

    public void setPayload_type(String payload_type) {
        this.payload_type = payload_type;
    }

    public Long getProcessing_time_ms() {
        return processing_time_ms;
    }

    public void setProcessing_time_ms(Long processing_time_ms) {
        this.processing_time_ms = processing_time_ms;
    }
}
