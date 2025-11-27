package com.lingualink.service;

import com.lingualink.entity.Payment;
import com.lingualink.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBooking_Id(bookingId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    // Update
    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        payment.setBooking(paymentDetails.getBooking());
        payment.setAmount(paymentDetails.getAmount());
        payment.setCurrency(paymentDetails.getCurrency());
        payment.setMethod(paymentDetails.getMethod());
        payment.setStatus(paymentDetails.getStatus());
        payment.setPaidAt(paymentDetails.getPaidAt());
        return paymentRepository.save(payment);
    }

    // Delete
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}

