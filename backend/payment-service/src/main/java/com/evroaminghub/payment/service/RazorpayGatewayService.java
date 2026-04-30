package com.evroaminghub.payment.service;

import com.evroaminghub.payment.dto.RazorpayOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Razorpay UPI Payment Gateway integration.
 * Uses mock responses when API keys are not configured.
 * Wire up real credentials in application.yml for live/sandbox mode.
 */
@Slf4j
@Service
public class RazorpayGatewayService {

    @Value("${razorpay.key-id:rzp_test_MOCK_KEY_ID}")
    private String keyId;

    @Value("${razorpay.key-secret:mock_secret_key_evroaming}")
    private String keySecret;

    @Value("${razorpay.mock-mode:true}")
    private boolean mockMode;

    public String getKeyId() {
        return keyId;
    }

    /**
     * Create a Razorpay order for UPI payment.
     * Amount is in INR (converted to paise for API).
     */
    public RazorpayOrderResponse createOrder(BigDecimal amount, String currency, String receipt) {
        if (mockMode) {
            String mockOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            log.info("[MOCK] Created Razorpay order: {} for ₹{}", mockOrderId, amount);
            return RazorpayOrderResponse.builder()
                    .orderId(mockOrderId)
                    .amount(amount)
                    .currency(currency)
                    .receipt(receipt)
                    .status("created")
                    .build();
        }

        // Production: Call Razorpay API
        // RazorpayClient client = new RazorpayClient(keyId, keySecret);
        // JSONObject orderRequest = new JSONObject();
        // orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        // orderRequest.put("currency", currency);
        // orderRequest.put("receipt", receipt);
        // Order order = client.orders.create(orderRequest);
        throw new UnsupportedOperationException("Set razorpay.mock-mode=false and configure real API keys");
    }

    /**
     * Verify Razorpay payment signature.
     * HMAC-SHA256(orderId + "|" + paymentId, keySecret) should match signature.
     */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (mockMode) {
            log.info("[MOCK] Signature verification passed for order={} payment={}", orderId, paymentId);
            return true;
        }

        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    public void initiateRefund(String gatewayPaymentId, BigDecimal amount) {
        if (mockMode) {
            log.info("[MOCK] Refund initiated for payment={} amount=₹{}", gatewayPaymentId, amount);
            return;
        }
        // Production: client.payments.refund(gatewayPaymentId, ...)
    }
}
