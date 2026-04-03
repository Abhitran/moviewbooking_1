package com.xyz.payment.producer;

import com.xyz.common.events.PaymentFailedEvent;
import com.xyz.common.events.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for publishing payment-related events to Kafka.
 * Task 6.8: Publishes payment.success events
 * Task 6.9: Publishes payment.failed events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String PAYMENT_SUCCESS_TOPIC = "payment.success";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes payment success event to Kafka
     * 
     * @param event PaymentSuccessEvent containing paymentId, bookingId, amount, gatewayTransactionId
     */
    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Publishing payment.success event: paymentId={}, bookingId={}, amount={}, gatewayTxnId={}", 
                event.getPaymentId(), event.getBookingId(), event.getAmount(), event.getGatewayTransactionId());
        
        kafkaTemplate.send(PAYMENT_SUCCESS_TOPIC, event.getPaymentId().toString(), event);
        
        log.debug("Payment success event published successfully to topic: {}", PAYMENT_SUCCESS_TOPIC);
    }

    /**
     * Publishes payment failed event to Kafka
     * Task 6.9: Triggers seat lock release in Booking Service
     * 
     * @param event PaymentFailedEvent containing paymentId, bookingId, reason
     */
    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing payment.failed event: paymentId={}, bookingId={}, reason={}", 
                event.getPaymentId(), event.getBookingId(), event.getReason());
        
        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event.getPaymentId().toString(), event);
        
        log.debug("Payment failed event published successfully to topic: {}", PAYMENT_FAILED_TOPIC);
    }
}
