package com.example.masking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskingResponse {

    private String transaction_id;
    private String masked_payload;
    private String payload_type;
}
