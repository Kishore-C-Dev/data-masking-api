package com.example.masking.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskingRequest {

    @NotBlank(message = "transaction_id is required")
    private String transaction_id;

    @NotBlank(message = "payload_txt is required")
    private String payload_txt;
}
