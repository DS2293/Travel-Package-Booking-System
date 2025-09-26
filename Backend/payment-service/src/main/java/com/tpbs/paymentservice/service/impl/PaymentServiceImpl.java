package com.tpbs.paymentservice.service.impl;

import com.tpbs.paymentservice.client.BookingServiceClient;
import com.tpbs.paymentservice.client.UserServiceClient;
import com.tpbs.paymentservice.dto.PaymentDto;
import com.tpbs.paymentservice.dto.PaymentStatusDto;
import com.tpbs.paymentservice.model.Payment;
import com.tpbs.paymentservice.repository.PaymentRepository;
import com.tpbs.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;
    private final UserServiceClient userServiceClient;
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return toDto(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Payment payment = toEntity(paymentDto);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PENDING");
        Payment savedPayment = paymentRepository.save(payment);
        return toDto(savedPayment);
    }
      @Override
    public PaymentDto updatePayment(Long id, PaymentDto paymentDto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setStatus(paymentDto.getStatus());
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setCardLastFour(paymentDto.getCardLastFour());
        payment.setDescription(paymentDto.getDescription());
        
        Payment updatedPayment = paymentRepository.save(payment);
        return toDto(updatedPayment);
    }
    
    @Override
    public PaymentDto updatePaymentStatus(Long id, PaymentStatusDto statusDto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        
        payment.setStatus(statusDto.getStatus());
        
        if (statusDto.getTransactionId() != null && !statusDto.getTransactionId().trim().isEmpty()) {
            payment.setTransactionId(statusDto.getTransactionId());
        }
        
        if (statusDto.getDescription() != null && !statusDto.getDescription().trim().isEmpty()) {
            payment.setDescription(statusDto.getDescription());
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment status updated to {} for payment ID: {}", statusDto.getStatus(), id);
        return toDto(updatedPayment);
    }
    
    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
    
    @Override
    public PaymentDto processPayment(PaymentDto paymentDto) {
        Payment payment = toEntity(paymentDto);
        payment.setPaymentDate(LocalDateTime.now());
        
        // Simulate payment processing
        try {
            // In real implementation, integrate with payment gateway
            String transactionId = "TXN-" + System.currentTimeMillis();
            payment.setTransactionId(transactionId);
            payment.setStatus("COMPLETED");
            
            Payment processedPayment = paymentRepository.save(payment);
            log.info("Payment processed successfully with transaction ID: {}", transactionId);
            return toDto(processedPayment);
            
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage());
            payment.setStatus("FAILED");
            Payment failedPayment = paymentRepository.save(payment);
            return toDto(failedPayment);
        }
    }
    
    @Override
    public PaymentDto refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        
        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Can only refund completed payments");
        }
        
        payment.setStatus("REFUNDED");
        Payment refundedPayment = paymentRepository.save(payment);
        return toDto(refundedPayment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserPaymentsWithBookingDetails(Long userId) {
        List<PaymentDto> userPayments = getPaymentsByUserId(userId);
        List<Map<String, Object>> enrichedPayments = new ArrayList<>();
        
        for (PaymentDto payment : userPayments) {
            Map<String, Object> enrichedPayment = new HashMap<>();
            enrichedPayment.put("payment", payment);
            
            try {
                // Fetch booking details
                ResponseEntity<Map<String, Object>> bookingResponse = 
                    bookingServiceClient.getBookingById(payment.getBookingId());
                if (bookingResponse.getStatusCode().is2xxSuccessful() && bookingResponse.getBody() != null) {
                    enrichedPayment.put("booking", bookingResponse.getBody().get("data"));
                }
            } catch (Exception e) {
                log.error("Failed to fetch booking details for payment {}: {}", payment.getPaymentId(), e.getMessage());
                enrichedPayment.put("booking", null);
            }
            
            enrichedPayments.add(enrichedPayment);
        }
        
        return enrichedPayments;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentWithBookingAndUserDetails(Long paymentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            PaymentDto payment = getPaymentById(paymentId);
            result.put("payment", payment);
            
            // Fetch booking details with fallback
            try {
                ResponseEntity<Map<String, Object>> bookingResponse = 
                    bookingServiceClient.getBookingById(payment.getBookingId());
                if (bookingResponse.getStatusCode().is2xxSuccessful() && bookingResponse.getBody() != null) {
                    result.put("booking", bookingResponse.getBody().get("data"));
                } else {
                    result.put("booking", getBookingFallback(payment.getBookingId()));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch booking details for bookingId: {}, using fallback", payment.getBookingId());
                result.put("booking", getBookingFallback(payment.getBookingId()));
            }
            
            // Fetch user details with fallback
            try {
                ResponseEntity<Map<String, Object>> userResponse = 
                    userServiceClient.getUserById(payment.getUserId());
                if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                    result.put("user", userResponse.getBody().get("data"));
                } else {
                    result.put("user", getUserFallback(payment.getUserId()));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user details for userId: {}, using fallback", payment.getUserId());
                result.put("user", getUserFallback(payment.getUserId()));
            }
            
        } catch (Exception e) {
            log.error("Failed to fetch payment details with related data for payment {}: {}", paymentId, e.getMessage());
            result.put("error", "Failed to fetch payment details");
        }
        
        return result;
    }
    
    // Fallback methods for cross-service communication failures
    private Map<String, Object> getBookingFallback(Long bookingId) {
        return Map.of(
            "bookingId", bookingId,
            "status", "Unknown",
            "packageTitle", "Booking information unavailable"
        );
    }
    
    private Map<String, Object> getUserFallback(Long userId) {
        return Map.of(
            "userId", userId,
            "name", "Unknown User",
            "email", "user@unknown.com"
        );
    }
    
    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getPaymentId(),
                payment.getUserId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getTransactionId(),
                payment.getCardLastFour(),
                payment.getDescription()
        );
    }
    
    private Payment toEntity(PaymentDto dto) {
        return new Payment(
                dto.getPaymentId(),
                dto.getUserId(),
                dto.getBookingId(),
                dto.getAmount(),
                dto.getPaymentMethod(),
                dto.getStatus(),
                dto.getPaymentDate(),
                dto.getTransactionId(),
                dto.getCardLastFour(),
                dto.getDescription()
        );
    }
}