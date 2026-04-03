package com.xyz.payment.service;

import com.xyz.common.events.PaymentFailedEvent;
import com.xyz.common.events.PaymentSuccessEvent;
import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.payment.dto.PaymentDetailsResponse;
import com.xyz.payment.dto.PaymentInitiateRequest;
import com.xyz.payment.dto.PaymentInitiateResponse;
import com.xyz.payment.dto.WebhookRequest;
import com.xyz.payment.entity.Payment;
import com.xyz.payment.entity.PaymentStatus;
import com.xyz.payment.producer.PaymentEventProducer;
import com.xyz.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 * Task 6.4: Tests payment initiation logic
 * Task 6.9: Tests payment failed event publishing
 * Task 6.12: Tests payment query endpoint
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private UUID bookingId;
    private UUID userId;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        userId = UUID.randomUUID();
        amount = new BigDecimal("500.00");
    }

    @Test
    void initiatePayment_shouldCreatePaymentWithPendingStatus() {
        // Arrange
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .build();

        UUID paymentId = UUID.randomUUID();
        Payment savedPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo(paymentId);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getGatewayUrl()).isNotNull();
        assertThat(response.getGatewayUrl()).contains(paymentId.toString());

        // Verify payment was saved with correct values
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getBookingId()).isEqualTo(bookingId);
        assertThat(capturedPayment.getUserId()).isEqualTo(userId);
        assertThat(capturedPayment.getAmount()).isEqualTo(amount);
        assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void initiatePayment_shouldGenerateStubbedGatewayUrl() {
        // Arrange
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .build();

        UUID paymentId = UUID.randomUUID();
        Payment savedPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        // Assert
        assertThat(response.getGatewayUrl()).startsWith("https://stubbed-payment-gateway.example.com/pay?paymentId=");
        assertThat(response.getGatewayUrl()).contains(paymentId.toString());
    }

    @Test
    void initiatePayment_shouldReturnCorrectResponseStructure() {
        // Arrange
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .build();

        UUID paymentId = UUID.randomUUID();
        Payment savedPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        // Assert - Verify response matches design specification
        assertThat(response.getPaymentId()).isNotNull();
        assertThat(response.getGatewayUrl()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void initiatePayment_shouldHandleDifferentAmounts() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("9999.99");
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(largeAmount)
                .build();

        UUID paymentId = UUID.randomUUID();
        Payment savedPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(largeAmount)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        // Assert
        assertThat(response).isNotNull();
        
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getAmount()).isEqualTo(largeAmount);
    }

    @Test
    void processWebhook_shouldUpdatePaymentStatusToSuccess() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        String gatewayTxnId = "gateway-txn-123";
        
        Payment existingPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        WebhookRequest request = WebhookRequest.builder()
                .paymentId(paymentId)
                .status("SUCCESS")
                .gatewayTransactionId(gatewayTxnId)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        paymentService.processWebhook(request);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment updatedPayment = paymentCaptor.getValue();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(updatedPayment.getGatewayTransactionId()).isEqualTo(gatewayTxnId);

        // Task 6.8: Verify payment.success event is published
        ArgumentCaptor<PaymentSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(paymentEventProducer).publishPaymentSuccess(eventCaptor.capture());
        
        PaymentSuccessEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getPaymentId()).isEqualTo(paymentId);
        assertThat(publishedEvent.getBookingId()).isEqualTo(bookingId);
        assertThat(publishedEvent.getAmount()).isEqualTo(amount.doubleValue());
        assertThat(publishedEvent.getGatewayTransactionId()).isEqualTo(gatewayTxnId);
    }

    @Test
    void processWebhook_shouldUpdatePaymentStatusToFailed() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        String gatewayTxnId = "gateway-txn-456";
        
        Payment existingPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        WebhookRequest request = WebhookRequest.builder()
                .paymentId(paymentId)
                .status("FAILURE")
                .gatewayTransactionId(gatewayTxnId)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        paymentService.processWebhook(request);

        // Assert
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        
        Payment updatedPayment = paymentCaptor.getValue();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(updatedPayment.getGatewayTransactionId()).isEqualTo(gatewayTxnId);

        // Task 6.9: Verify payment.failed event is published
        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(paymentEventProducer).publishPaymentFailed(eventCaptor.capture());
        
        PaymentFailedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getPaymentId()).isEqualTo(paymentId);
        assertThat(publishedEvent.getBookingId()).isEqualTo(bookingId);
        assertThat(publishedEvent.getReason()).contains("Payment failed");
        
        // Verify no success event is published
        verify(paymentEventProducer, never()).publishPaymentSuccess(any(PaymentSuccessEvent.class));
    }

    @Test
    void processWebhook_shouldThrowExceptionWhenPaymentNotFound() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        
        WebhookRequest request = WebhookRequest.builder()
                .paymentId(paymentId)
                .status("SUCCESS")
                .gatewayTransactionId("gateway-txn-789")
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processWebhook(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found with ID: " + paymentId);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processWebhook_shouldBeIdempotent_whenPaymentAlreadyProcessed() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        
        Payment existingPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.SUCCESS) // Already processed
                .gatewayTransactionId("original-txn-id")
                .build();

        WebhookRequest request = WebhookRequest.builder()
                .paymentId(paymentId)
                .status("SUCCESS")
                .gatewayTransactionId("duplicate-txn-id")
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));

        // Act
        paymentService.processWebhook(request);

        // Assert - Should not save again (idempotency check)
        verify(paymentRepository, never()).save(any(Payment.class));
        
        // Task 6.8 & 6.9: Verify no duplicate events are published
        verify(paymentEventProducer, never()).publishPaymentSuccess(any(PaymentSuccessEvent.class));
        verify(paymentEventProducer, never()).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    void processWebhook_shouldNotUpdateWhenStatusIsFailed() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        
        Payment existingPayment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.FAILED) // Already failed
                .gatewayTransactionId("original-txn-id")
                .build();

        WebhookRequest request = WebhookRequest.builder()
                .paymentId(paymentId)
                .status("SUCCESS")
                .gatewayTransactionId("late-success-txn-id")
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));

        // Act
        paymentService.processWebhook(request);

        // Assert - Should not update (idempotency check)
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // Task 6.12: Tests for getPaymentDetails

    @Test
    void getPaymentDetails_shouldReturnPaymentDetailsWhenPaymentExists() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        String gatewayTxnId = "gateway-txn-123";
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .gatewayTransactionId(gatewayTxnId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo(paymentId);
        assertThat(response.getBookingId()).isEqualTo(bookingId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAmount()).isEqualTo(amount);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getGatewayTransactionId()).isEqualTo(gatewayTxnId);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void getPaymentDetails_shouldReturnPendingPaymentDetails() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .gatewayTransactionId(null) // No gateway transaction ID yet
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo(paymentId);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getGatewayTransactionId()).isNull();
    }

    @Test
    void getPaymentDetails_shouldReturnFailedPaymentDetails() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        String gatewayTxnId = "gateway-txn-failed-456";
        
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.FAILED)
                .gatewayTransactionId(gatewayTxnId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getGatewayTransactionId()).isEqualTo(gatewayTxnId);
    }

    @Test
    void getPaymentDetails_shouldThrowExceptionWhenPaymentNotFound() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentDetails(paymentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found with ID: " + paymentId);

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void getPaymentDetails_shouldIncludeAllTimestamps() {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .gatewayTransactionId("txn-123")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        PaymentDetailsResponse response = paymentService.getPaymentDetails(paymentId);

        // Assert - Verify timestamps are included as per FR-PAY-01
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
