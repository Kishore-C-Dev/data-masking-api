package com.example.masking.controller;

import com.example.masking.model.MaskingRequest;
import com.example.masking.model.MaskingResponse;
import com.example.masking.model.PayloadType;
import com.example.masking.service.DataMaskingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class MaskingController {

    private static final Logger log = LoggerFactory.getLogger(MaskingController.class);

    private final DataMaskingService dataMaskingService;

    public MaskingController(DataMaskingService dataMaskingService) {
        this.dataMaskingService = dataMaskingService;
    }

    @PostMapping("/mask")
    public ResponseEntity<MaskingResponse> maskPayload(@Valid @RequestBody MaskingRequest request) {
        log.info("Received masking request for transaction_id: {}", request.getTransaction_id());

        long startTime = System.currentTimeMillis();

        try {
            PayloadType detectedType = dataMaskingService.detectPayloadType(request.getPayload_txt());
            log.info("Detected payload type: {}", detectedType);

            String maskedPayload = dataMaskingService.maskPayload(request.getPayload_txt(), detectedType);

            // Get detected subtype if available
            String detectedSubtype = dataMaskingService.getLastDetectedSubtype();

            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;

            log.info("Masking completed for transaction_id: {} in {} ms", request.getTransaction_id(), processingTime);

            MaskingResponse response = new MaskingResponse(
                    request.getTransaction_id(),
                    maskedPayload,
                    detectedType.name(),
                    processingTime
            );
            response.setDetected_subtype(detectedSubtype);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing masking request: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing masking request: " + e.getMessage());
        } finally {
            // Clean up ThreadLocal to prevent memory leaks in thread pools
            dataMaskingService.clearThreadLocalState();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Masking API is running");
    }
}
