package com.lingualink.service;

import com.lingualink.entity.Payment;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Create
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBooking_Id(bookingId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    // Update - Full update
    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = getPaymentById(id);
        payment.setBooking(paymentDetails.getBooking());
        payment.setAmount(paymentDetails.getAmount());
        payment.setCurrency(paymentDetails.getCurrency());
        payment.setMethod(paymentDetails.getMethod());
        payment.setStatus(paymentDetails.getStatus());
        payment.setPaidAt(paymentDetails.getPaidAt());
        return paymentRepository.save(payment);
    }

    // Update - Partial update
    public Payment patchPayment(Long id, Payment paymentDetails) {
        Payment payment = getPaymentById(id);
        
        if (paymentDetails.getBooking() != null) {
            payment.setBooking(paymentDetails.getBooking());
        }
        if (paymentDetails.getAmount() != null) {
            payment.setAmount(paymentDetails.getAmount());
        }
        if (paymentDetails.getCurrency() != null) {
            payment.setCurrency(paymentDetails.getCurrency());
        }
        if (paymentDetails.getMethod() != null) {
            payment.setMethod(paymentDetails.getMethod());
        }
        if (paymentDetails.getStatus() != null) {
            payment.setStatus(paymentDetails.getStatus());
        }
        if (paymentDetails.getPaidAt() != null) {
            payment.setPaidAt(paymentDetails.getPaidAt());
        }
        return paymentRepository.save(payment);
    }

    // Delete
    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }
}


