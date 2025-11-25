package com.example.masking.model;

import javax.validation.constraints.NotBlank;

public class MaskingRequest {

    @NotBlank(message = "transaction_id is required")
    private String transaction_id;

    @NotBlank(message = "payload_txt is required")
    private String payload_txt;

    public MaskingRequest() {
    }

    public MaskingRequest(String transaction_id, String payload_txt) {
        this.transaction_id = transaction_id;
        this.payload_txt = payload_txt;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getPayload_txt() {
        return payload_txt;
    }

    public void setPayload_txt(String payload_txt) {
        this.payload_txt = payload_txt;
    }
}
