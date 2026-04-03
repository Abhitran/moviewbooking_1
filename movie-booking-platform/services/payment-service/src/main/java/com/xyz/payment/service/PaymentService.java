package com.xyz.payment.service;

import com.xyz.common.events.PaymentFailedEvent;
import com.xyz.common.events.PaymentSuccessEvent;
import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.common.exception.ValidationException;
import com.xyz.payment.dto.PaymentDetailsResponse;
import com.xyz.payment.dto.PaymentInitiateRequest;
import com.xyz.payment.dto.PaymentInitiateResponse;
import com.xyz.payment.dto.RefundRequest;
import com.xyz.payment.dto.RefundResponse;
import com.xyz.payment.dto.WebhookRequest;
import com.xyz.payment.entity.Payment;
import com.xyz.payment.entity.PaymentStatus;
import com.xyz.payment.entity.Refund;
import com.xyz.payment.entity.RefundStatus;
import com.xyz.payment.producer.PaymentEventProducer;
import com.xyz.payment.repository.PaymentRepository;
import com.xyz.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for handling payment operations.
 * Task 6.4: Implements payment initiation logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentEventProducer paymentEventProducer;

    /**
     * Task 6.4: Initiate payment for a booking
     * Creates payment record with PENDING status and generates stubbed gateway URL
     * 
     * @param request Payment initiation request containing bookingId, amount, userId
     * @return PaymentInitiateResponse with paymentId, gatewayUrl, and status
     */
    @Transactional
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment for booking: {}, amount: {}, user: {}", 
                request.getBookingId(), request.getAmount(), request.getUserId());

        // Create payment record with PENDING status
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        // Generate stubbed gateway URL
        String gatewayUrl = generateStubbedGatewayUrl(payment.getPaymentId());

        log.info("Payment initiated successfully: paymentId={}, gatewayUrl={}", 
                payment.getPaymentId(), gatewayUrl);

        // Return response with paymentId, gatewayUrl, and status
        return PaymentInitiateResponse.builder()
                .paymentId(payment.getPaymentId())
                .gatewayUrl(gatewayUrl)
                .status(payment.getStatus().name())
                .build();
    }

    /**
     * Generates a stubbed payment gateway URL
     * In production, this would be a real payment gateway URL
     * 
     * @param paymentId The payment ID
     * @return Stubbed gateway URL
     */
    private String generateStubbedGatewayUrl(UUID paymentId) {
        return String.format("https://stubbed-payment-gateway.example.com/pay?paymentId=%s", paymentId);
    }

    /**
     * Task 6.6: Process payment gateway webhook
     * Updates payment status based on webhook callback
     * Implements idempotency check to prevent duplicate processing
     * 
     * @param request Webhook request containing paymentId, status, and gatewayTransactionId
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional
    public void processWebhook(WebhookRequest request) {
        log.info("Processing webhook for payment: {}, status: {}, gatewayTxnId: {}", 
                request.getPaymentId(), request.getStatus(), request.getGatewayTransactionId());

        // Validate payment exists
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + request.getPaymentId()));

        // Idempotency check: if payment status is not PENDING, return early (already processed)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Webhook already processed for payment: {}, current status: {}", 
                    payment.getPaymentId(), payment.getStatus());
            return;
        }

        // Update payment status based on webhook status
        PaymentStatus newStatus = "SUCCESS".equalsIgnoreCase(request.getStatus()) 
                ? PaymentStatus.SUCCESS 
                : PaymentStatus.FAILED;
        
        payment.setStatus(newStatus);
        payment.setGatewayTransactionId(request.getGatewayTransactionId());
        
        paymentRepository.save(payment);

        log.info("Webhook processed successfully: paymentId={}, newStatus={}, gatewayTxnId={}", 
                payment.getPaymentId(), newStatus, request.getGatewayTransactionId());

        // Task 6.8: Publish payment.success event to Kafka on successful payment
        if (newStatus == PaymentStatus.SUCCESS) {
            PaymentSuccessEvent event = new PaymentSuccessEvent(
                    payment.getPaymentId(),
                    payment.getBookingId(),
                    payment.getAmount(),
                    payment.getGatewayTransactionId()
            );
            paymentEventProducer.publishPaymentSuccess(event);
            log.info("Payment success event published for payment: {}", payment.getPaymentId());
        }
        // Task 6.9: Publish payment.failed event to Kafka on failed payment
        else if (newStatus == PaymentStatus.FAILED) {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    payment.getPaymentId(),
                    payment.getBookingId(),
                    "Payment failed: " + request.getStatus()
            );
            paymentEventProducer.publishPaymentFailed(event);
            log.info("Payment failed event published for payment: {}", payment.getPaymentId());
        }
    }

    /**
     * Task 6.12: Get payment details by payment ID
     * Retrieves payment information including status and timestamps
     * 
     * @param paymentId The payment ID to query
     * @return PaymentDetailsResponse with complete payment information
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional(readOnly = true)
    public PaymentDetailsResponse getPaymentDetails(UUID paymentId) {
        log.info("Retrieving payment details for paymentId: {}", paymentId);

        // Validate payment exists
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + paymentId));

        log.info("Payment details retrieved successfully: paymentId={}, status={}", 
                payment.getPaymentId(), payment.getStatus());

        // Map entity to response DTO
        return PaymentDetailsResponse.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    /**
     * Task 6.11: Initiate refund for a payment
     * Validates payment exists and is successful
     * Creates refund record with PENDING status
     * Stubs refund processing (immediate success)
     * 
     * @param request Refund request containing paymentId and amount
     * @return RefundResponse with refund details
     * @throws ResourceNotFoundException if payment not found
     * @throws ValidationException if payment is not successful
     */
    @Transactional
    public RefundResponse initiateRefund(RefundRequest request) {
        log.info("Initiating refund for payment: {}, amount: {}", 
                request.getPaymentId(), request.getAmount());

        // Validate payment exists
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with ID: " + request.getPaymentId()));

        // Validate payment is successful
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ValidationException(
                    "Cannot refund payment with status: " + payment.getStatus() + 
                    ". Only successful payments can be refunded.");
        }

        // Validate refund amount does not exceed payment amount
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new ValidationException(
                    "Refund amount cannot exceed payment amount. Payment amount: " + 
                    payment.getAmount() + ", Refund amount: " + request.getAmount());
        }

        // Create refund record with PENDING status
        Refund refund = Refund.builder()
                .paymentId(payment.getPaymentId())
                .amount(request.getAmount())
                .status(RefundStatus.PENDING)
                .build();

        refund = refundRepository.save(refund);

        // Stub refund processing - immediately mark as SUCCESS
        // In production, this would integrate with payment gateway
        refund.setStatus(RefundStatus.SUCCESS);
        refund = refundRepository.save(refund);

        log.info("Refund processed successfully: refundId={}, paymentId={}, amount={}, status={}", 
                refund.getRefundId(), refund.getPaymentId(), refund.getAmount(), refund.getStatus());

        // Return response with refund details
        return RefundResponse.builder()
                .refundId(refund.getRefundId())
                .paymentId(refund.getPaymentId())
                .amount(refund.getAmount())
                .status(refund.getStatus().name())
                .build();
    }
}
