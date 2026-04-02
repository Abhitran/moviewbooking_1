package com.xyz.booking.client;

import com.xyz.booking.dto.PaymentInitiateRequest;
import com.xyz.booking.dto.PaymentInitiateResponse;
import com.xyz.booking.dto.RefundRequest;
import com.xyz.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        String url = paymentServiceUrl + "/api/payments/initiate";
        var response = restTemplate.exchange(
            url, HttpMethod.POST, new HttpEntity<>(request),
            new ParameterizedTypeReference<ApiResponse<PaymentInitiateResponse>>() {});
        if (response.getBody() != null && response.getBody().isSuccess()) {
            return response.getBody().getData();
        }
        throw new RuntimeException("Payment initiation failed");
    }

    public void initiateRefund(RefundRequest request) {
        String url = paymentServiceUrl + "/api/payments/refund";
        try {
            restTemplate.postForObject(url, request, ApiResponse.class);
        } catch (Exception e) {
            log.error("Refund initiation failed for payment {}: {}", request.getPaymentId(), e.getMessage());
        }
    }
}
