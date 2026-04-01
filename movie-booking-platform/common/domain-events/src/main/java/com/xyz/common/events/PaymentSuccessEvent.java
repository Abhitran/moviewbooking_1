package com.xyz.common.events;

import java.util.UUID;

public class PaymentSuccessEvent extends DomainEvent {
    private UUID paymentId;
    private UUID bookingId;
    private double amount;
    private String gatewayTransactionId;

    public PaymentSuccessEvent() {
        super("PAYMENT_SUCCESS");
    }

    public PaymentSuccessEvent(UUID paymentId, UUID bookingId, double amount, String gatewayTransactionId) {
        super("PAYMENT_SUCCESS");
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
}
