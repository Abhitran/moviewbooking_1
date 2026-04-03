package com.xyz.payment.producer;

import com.xyz.common.events.PaymentFailedEvent;
import com.xyz.common.events.PaymentSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PaymentEventProducer
 * Task 6.8: Tests payment.success event publishing
 * Task 6.9: Tests payment.failed event publishing
 */
@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    private UUID paymentId;
    private UUID bookingId;
    private double amount;
    private String gatewayTransactionId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        amount = 500.00;
        gatewayTransactionId = "gateway-txn-123";
    }

    @Test
    void publishPaymentSuccess_shouldSendEventToKafka() {
        // Arrange
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                paymentId,
                bookingId,
                amount,
                gatewayTransactionId
        );

        // Act
        paymentEventProducer.publishPaymentSuccess(event);

        // Assert - Verify event was sent to correct topic with correct key
        ArgumentCaptor<PaymentSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(kafkaTemplate).send(
                eq("payment.success"),
                eq(paymentId.toString()),
                eventCaptor.capture()
        );

        PaymentSuccessEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isEqualTo(paymentId);
        assertThat(capturedEvent.getBookingId()).isEqualTo(bookingId);
        assertThat(capturedEvent.getAmount()).isEqualTo(amount);
        assertThat(capturedEvent.getGatewayTransactionId()).isEqualTo(gatewayTransactionId);
    }

    @Test
    void publishPaymentSuccess_shouldUsePaymentIdAsKey() {
        // Arrange
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                paymentId,
                bookingId,
                amount,
                gatewayTransactionId
        );

        // Act
        paymentEventProducer.publishPaymentSuccess(event);

        // Assert - Verify paymentId is used as Kafka message key
        verify(kafkaTemplate).send(
                eq("payment.success"),
                eq(paymentId.toString()),
                eq(event)
        );
    }

    @Test
    void publishPaymentSuccess_shouldIncludeAllRequiredFields() {
        // Arrange
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                paymentId,
                bookingId,
                amount,
                gatewayTransactionId
        );

        // Act
        paymentEventProducer.publishPaymentSuccess(event);

        // Assert - Verify all required fields are present in the event
        ArgumentCaptor<PaymentSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(kafkaTemplate).send(
                eq("payment.success"),
                eq(paymentId.toString()),
                eventCaptor.capture()
        );

        PaymentSuccessEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isNotNull();
        assertThat(capturedEvent.getBookingId()).isNotNull();
        assertThat(capturedEvent.getAmount()).isPositive();
        assertThat(capturedEvent.getGatewayTransactionId()).isNotNull();
    }

    @Test
    void publishPaymentFailed_shouldSendEventToKafka() {
        // Arrange
        String reason = "Payment declined by gateway";
        PaymentFailedEvent event = new PaymentFailedEvent(
                paymentId,
                bookingId,
                reason
        );

        // Act
        paymentEventProducer.publishPaymentFailed(event);

        // Assert - Verify event was sent to correct topic with correct key
        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(kafkaTemplate).send(
                eq("payment.failed"),
                eq(paymentId.toString()),
                eventCaptor.capture()
        );

        PaymentFailedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isEqualTo(paymentId);
        assertThat(capturedEvent.getBookingId()).isEqualTo(bookingId);
        assertThat(capturedEvent.getReason()).isEqualTo(reason);
    }

    @Test
    void publishPaymentFailed_shouldUsePaymentIdAsKey() {
        // Arrange
        PaymentFailedEvent event = new PaymentFailedEvent(
                paymentId,
                bookingId,
                "Payment failed"
        );

        // Act
        paymentEventProducer.publishPaymentFailed(event);

        // Assert - Verify paymentId is used as Kafka message key
        verify(kafkaTemplate).send(
                eq("payment.failed"),
                eq(paymentId.toString()),
                eq(event)
        );
    }

    @Test
    void publishPaymentFailed_shouldIncludeAllRequiredFields() {
        // Arrange
        String reason = "Insufficient funds";
        PaymentFailedEvent event = new PaymentFailedEvent(
                paymentId,
                bookingId,
                reason
        );

        // Act
        paymentEventProducer.publishPaymentFailed(event);

        // Assert - Verify all required fields are present in the event
        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(kafkaTemplate).send(
                eq("payment.failed"),
                eq(paymentId.toString()),
                eventCaptor.capture()
        );

        PaymentFailedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isNotNull();
        assertThat(capturedEvent.getBookingId()).isNotNull();
        assertThat(capturedEvent.getReason()).isNotNull();
    }
}
