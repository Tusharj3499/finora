package com.finora.payment.controller;

import com.finora.payment.domain.enums.PaymentStatus;
import com.finora.payment.dto.request.CreatePaymentRequest;
import com.finora.payment.dto.request.UpdatePaymentStatusRequest;
import com.finora.payment.dto.response.PaymentResponse;
import com.finora.payment.service.PaymentService;
import constants.AppConstants;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId){
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        if (page < 0 || size < 1 || size > AppConstants.MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        return ResponseEntity.ok(
                paymentService.getAllPayments(page, size)
        );
    }

    @GetMapping("/status/{paymentStatus}")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        if (page < 0 || size < 1 || size > AppConstants.MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(paymentStatus,page, size)
        );
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        paymentId,
                        request.getPaymentStatus()));
    }

}
