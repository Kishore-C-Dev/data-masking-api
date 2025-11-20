package com.example.masking.controller;

import com.example.masking.model.MaskingRequest;
import com.example.masking.model.MaskingResponse;
import com.example.masking.model.PayloadType;
import com.example.masking.service.DataMaskingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class MaskingController {

    private final DataMaskingService dataMaskingService;

    public MaskingController(DataMaskingService dataMaskingService) {
        this.dataMaskingService = dataMaskingService;
    }

    @PostMapping("/mask")
    public ResponseEntity<MaskingResponse> maskPayload(@Valid @RequestBody MaskingRequest request) {
        log.info("Received masking request for transaction_id: {}", request.getTransaction_id());

        try {
            PayloadType detectedType = dataMaskingService.detectPayloadType(request.getPayload_txt());
            log.info("Detected payload type: {}", detectedType);

            String maskedPayload = dataMaskingService.maskPayload(request.getPayload_txt(), detectedType);

            MaskingResponse response = new MaskingResponse(
                    request.getTransaction_id(),
                    maskedPayload,
                    detectedType.name()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing masking request: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing masking request: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Masking API is running");
    }
}
