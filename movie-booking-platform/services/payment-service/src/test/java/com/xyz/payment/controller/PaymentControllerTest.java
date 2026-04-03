package com.xyz.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.payment.dto.PaymentDetailsResponse;
import com.xyz.payment.dto.PaymentInitiateRequest;
import com.xyz.payment.dto.PaymentInitiateResponse;
import com.xyz.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController
 * Task 6.4: Tests payment initiation endpoint
 * Task 6.12: Tests payment query endpoint
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void initiatePayment_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(amount)
                .build();

        PaymentInitiateResponse response = PaymentInitiateResponse.builder()
                .paymentId(paymentId)
                .gatewayUrl("https://stubbed-payment-gateway.example.com/pay?paymentId=" + paymentId)
                .status("PENDING")
                .build();

        when(paymentService.initiatePayment(any(PaymentInitiateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment initiated successfully"))
                .andExpect(jsonPath("$.data.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.data.gatewayUrl").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void initiatePayment_shouldValidateRequiredFields() throws Exception {
        // Arrange - Request with missing bookingId
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("500.00"))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiatePayment_shouldValidateAmountIsPositive() throws Exception {
        // Arrange - Request with zero amount
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(BigDecimal.ZERO)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiatePayment_shouldAcceptValidRequest() throws Exception {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("1500.50"))
                .build();

        PaymentInitiateResponse response = PaymentInitiateResponse.builder()
                .paymentId(paymentId)
                .gatewayUrl("https://stubbed-payment-gateway.example.com/pay?paymentId=" + paymentId)
                .status("PENDING")
                .build();

        when(paymentService.initiatePayment(any(PaymentInitiateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").exists())
                .andExpect(jsonPath("$.data.gatewayUrl").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void handleWebhook_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        com.xyz.payment.dto.WebhookRequest request = com.xyz.payment.dto.WebhookRequest.builder()
                .paymentId(paymentId)
                .status("SUCCESS")
                .gatewayTransactionId("gateway-txn-123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));

        verify(paymentService).processWebhook(any(com.xyz.payment.dto.WebhookRequest.class));
    }

    @Test
    void handleWebhook_shouldValidateRequiredFields() throws Exception {
        // Arrange - Request with missing paymentId
        com.xyz.payment.dto.WebhookRequest request = com.xyz.payment.dto.WebhookRequest.builder()
                .status("SUCCESS")
                .gatewayTransactionId("gateway-txn-123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleWebhook_shouldAcceptFailureStatus() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        com.xyz.payment.dto.WebhookRequest request = com.xyz.payment.dto.WebhookRequest.builder()
                .paymentId(paymentId)
                .status("FAILURE")
                .gatewayTransactionId("gateway-txn-456")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService).processWebhook(any(com.xyz.payment.dto.WebhookRequest.class));
    }

    // Task 6.12: Tests for GET /api/payments/{paymentId}

    @Test
    void getPaymentDetails_shouldReturnPaymentDetailsWhenPaymentExists() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        PaymentDetailsResponse response = PaymentDetailsResponse.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("500.00"))
                .status("SUCCESS")
                .gatewayTransactionId("gateway-txn-123")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(paymentService.getPaymentDetails(paymentId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment details retrieved successfully"))
                .andExpect(jsonPath("$.data.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.data.bookingId").value(bookingId.toString()))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.amount").value(500.00))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.gatewayTransactionId").value("gateway-txn-123"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());

        verify(paymentService).getPaymentDetails(paymentId);
    }

    @Test
    void getPaymentDetails_shouldReturnPendingPaymentDetails() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentDetailsResponse response = PaymentDetailsResponse.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("750.00"))
                .status("PENDING")
                .gatewayTransactionId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentDetails(paymentId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.gatewayTransactionId").isEmpty());

        verify(paymentService).getPaymentDetails(paymentId);
    }

    @Test
    void getPaymentDetails_shouldReturnFailedPaymentDetails() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentDetailsResponse response = PaymentDetailsResponse.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("300.00"))
                .status("FAILED")
                .gatewayTransactionId("gateway-txn-failed-456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentDetails(paymentId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.gatewayTransactionId").value("gateway-txn-failed-456"));

        verify(paymentService).getPaymentDetails(paymentId);
    }

    @Test
    void getPaymentDetails_shouldReturn404WhenPaymentNotFound() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();

        when(paymentService.getPaymentDetails(paymentId))
                .thenThrow(new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        // Act & Assert
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(paymentService).getPaymentDetails(paymentId);
    }

    @Test
    void getPaymentDetails_shouldIncludeAllRequiredFields() throws Exception {
        // Arrange
        UUID paymentId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        PaymentDetailsResponse response = PaymentDetailsResponse.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .amount(new BigDecimal("1200.50"))
                .status("SUCCESS")
                .gatewayTransactionId("gateway-txn-789")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(paymentService.getPaymentDetails(paymentId)).thenReturn(response);

        // Act & Assert - Verify all fields as per FR-PAY-01
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").exists())
                .andExpect(jsonPath("$.data.bookingId").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.amount").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());

        verify(paymentService).getPaymentDetails(paymentId);
    }
}
