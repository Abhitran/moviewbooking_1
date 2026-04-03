package com.xyz.payment.controller;

import com.xyz.common.dto.ApiResponse;
import com.xyz.payment.dto.PaymentDetailsResponse;
import com.xyz.payment.dto.PaymentInitiateRequest;
import com.xyz.payment.dto.PaymentInitiateResponse;
import com.xyz.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for payment operations.
 * Task 6.4: Implements payment initiation endpoint
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Task 6.4: POST /api/payments/initiate
     * Initiates payment for a confirmed booking
     * 
     * Request: { bookingId, amount, userId }
     * Response: { paymentId, gatewayUrl, status: "PENDING" }
     * 
     * @param request Payment initiation request
     * @return PaymentInitiateResponse with payment details
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request) {
        
        log.info("Received payment initiation request for booking: {}", request.getBookingId());
        
        PaymentInitiateResponse response = paymentService.initiatePayment(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Payment initiated successfully"));
    }

    /**
     * Task 6.6: POST /api/payments/webhook
     * Handles payment gateway webhook callbacks
     * 
     * Request: { paymentId, status: "SUCCESS|FAILURE", gatewayTransactionId }
     * Response: { message: "Webhook processed" }
     * 
     * @param request Webhook request from payment gateway
     * @return Success response
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @Valid @RequestBody com.xyz.payment.dto.WebhookRequest request) {
        
        log.info("Received webhook for payment: {}, status: {}", 
                request.getPaymentId(), request.getStatus());
        
        paymentService.processWebhook(request);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed successfully"));
    }

    /**
     * Task 6.12: GET /api/payments/{paymentId}
     * Retrieves payment details by payment ID
     * 
     * Response: { paymentId, bookingId, userId, amount, status, gatewayTransactionId, createdAt, updatedAt }
     * 
     * @param paymentId The payment ID to query
     * @return PaymentDetailsResponse with complete payment information
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailsResponse>> getPaymentDetails(
            @PathVariable UUID paymentId) {
        
        log.info("Received request to get payment details for paymentId: {}", paymentId);
        
        PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Payment details retrieved successfully"));
    }

    /**
     * Task 6.11: POST /api/payments/refund
     * Initiates refund for a successful payment
     * 
     * Request: { paymentId, amount }
     * Response: { refundId, paymentId, amount, status }
     * 
     * @param request Refund request
     * @return RefundResponse with refund details
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<com.xyz.payment.dto.RefundResponse>> initiateRefund(
            @Valid @RequestBody com.xyz.payment.dto.RefundRequest request) {
        
        log.info("Received refund request for payment: {}, amount: {}", 
                request.getPaymentId(), request.getAmount());
        
        com.xyz.payment.dto.RefundResponse response = paymentService.initiateRefund(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Refund initiated successfully"));
    }
}
