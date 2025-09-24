package com.tpbs.paymentservice.service;

import com.tpbs.paymentservice.dto.PaymentDto;
import com.tpbs.paymentservice.dto.PaymentStatusDto;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    
    List<PaymentDto> getAllPayments();
    PaymentDto getPaymentById(Long id);
    List<PaymentDto> getPaymentsByUserId(Long userId);
    List<PaymentDto> getPaymentsByBookingId(Long bookingId);
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto updatePayment(Long id, PaymentDto paymentDto);
    PaymentDto updatePaymentStatus(Long id, PaymentStatusDto statusDto);
    void deletePayment(Long id);
    PaymentDto processPayment(PaymentDto paymentDto);
    PaymentDto refundPayment(Long id);
    
    // Enhanced methods with cross-service data
    List<Map<String, Object>> getUserPaymentsWithBookingDetails(Long userId);
    Map<String, Object> getPaymentWithBookingAndUserDetails(Long paymentId);
}