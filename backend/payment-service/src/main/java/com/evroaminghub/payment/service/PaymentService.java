package com.evroaminghub.payment.service;

import com.evroaminghub.payment.dto.*;
import com.evroaminghub.payment.entity.*;
import com.evroaminghub.payment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTxRepository;
    private final RazorpayGatewayService razorpayService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public InitiatePaymentResponse initiatePayment(UUID userId, InitiatePaymentRequest request) {
        // Check if user wants to pay from wallet
        if ("WALLET".equals(request.getPaymentMethod())) {
            return payFromWallet(userId, request);
        }

        // Create Razorpay order (mock in dev)
        RazorpayOrderResponse razorpayOrder = razorpayService.createOrder(
                request.getAmount(), "INR", request.getInvoiceId().toString());

        Payment payment = Payment.builder()
                .invoiceId(request.getInvoiceId())
                .userId(userId)
                .gateway(PaymentGateway.RAZORPAY)
                .gatewayOrderId(razorpayOrder.getOrderId())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .upiVpa(request.getUpiVpa())
                .amount(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.INITIATED)
                .initiatedAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment initiated: {} for invoice {} amount ₹{}",
                payment.getId(), request.getInvoiceId(), request.getAmount());

        return InitiatePaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(razorpayOrder.getOrderId())
                .razorpayKeyId(razorpayService.getKeyId())
                .amount(request.getAmount())
                .currency("INR")
                .status("INITIATED")
                .build();
    }

    @Transactional
    public PaymentResponse confirmPayment(UUID paymentId, ConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        // Verify signature (Razorpay signature validation)
        boolean signatureValid = razorpayService.verifySignature(
                payment.getGatewayOrderId(),
                request.getPaymentId(),
                request.getSignature());

        if (!signatureValid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);
            throw new RuntimeException("Payment signature verification failed");
        }

        payment.setGatewayPaymentId(request.getPaymentId());
        payment.setGatewaySignature(request.getSignature());
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setAmountPaid(payment.getAmount());
        payment.setCompletedAt(Instant.now());
        payment = paymentRepository.save(payment);

        // Publish payment completed event
        kafkaTemplate.send("payment.completed", paymentId.toString(),
                Map.of("paymentId", paymentId.toString(),
                        "invoiceId", payment.getInvoiceId().toString(),
                        "userId", payment.getUserId().toString(),
                        "amount", payment.getAmount().toString()));

        log.info("Payment confirmed: {} for invoice {}", paymentId, payment.getInvoiceId());
        return toResponse(payment);
    }

    private InitiatePaymentResponse payFromWallet(UUID userId, InitiatePaymentRequest request) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient wallet balance. Available: ₹" + wallet.getBalance());
        }

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        // Record transaction
        walletTxRepository.save(WalletTransaction.builder()
                .wallet(wallet)
                .userId(userId)
                .transactionType("DEBIT")
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .referenceId(request.getInvoiceId())
                .description("Payment for invoice " + request.getInvoiceId())
                .build());

        Payment payment = Payment.builder()
                .invoiceId(request.getInvoiceId())
                .userId(userId)
                .gateway(PaymentGateway.WALLET)
                .paymentMethod(PaymentMethod.WALLET)
                .amount(request.getAmount())
                .amountPaid(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.CAPTURED)
                .initiatedAt(Instant.now())
                .completedAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);

        kafkaTemplate.send("payment.completed", payment.getId().toString(),
                Map.of("paymentId", payment.getId().toString(),
                        "invoiceId", request.getInvoiceId().toString(),
                        "userId", userId.toString(),
                        "amount", request.getAmount().toString(),
                        "method", "WALLET"));

        return InitiatePaymentResponse.builder()
                .paymentId(payment.getId())
                .status("CAPTURED")
                .amount(request.getAmount())
                .currency("INR")
                .build();
    }

    @Transactional
    public WalletResponse topUpWallet(UUID userId, WalletTopUpRequest request) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().userId(userId).balance(BigDecimal.ZERO).currency("INR").build()));

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        wallet = walletRepository.save(wallet);

        walletTxRepository.save(WalletTransaction.builder()
                .wallet(wallet)
                .userId(userId)
                .transactionType("TOPUP")
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .description("Wallet top-up via " + request.getPaymentMethod())
                .build());

        log.info("Wallet topped up: user={} amount=₹{} new balance=₹{}", userId, request.getAmount(), wallet.getBalance());

        return WalletResponse.builder()
                .userId(userId)
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(w -> WalletResponse.builder()
                        .userId(userId)
                        .balance(w.getBalance())
                        .currency(w.getCurrency())
                        .build())
                .orElse(WalletResponse.builder()
                        .userId(userId)
                        .balance(BigDecimal.ZERO)
                        .currency("INR")
                        .build());
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .invoiceId(p.getInvoiceId())
                .gateway(p.getGateway().name())
                .paymentMethod(p.getPaymentMethod().name())
                .amount(p.getAmount())
                .amountPaid(p.getAmountPaid())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .initiatedAt(p.getInitiatedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }
}
