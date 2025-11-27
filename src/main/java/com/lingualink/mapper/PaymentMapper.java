package com.lingualink.mapper;

import com.lingualink.dto.request.PaymentRequest;
import com.lingualink.dto.response.PaymentResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequest request, Booking booking) {
        if (request == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setMethod(request.getMethod());
        payment.setStatus(request.getStatus());
        return payment;
    }

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBooking() != null ? payment.getBooking().getId() : null);
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setPaidAt(payment.getPaidAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(PaymentRequest request, Payment payment, Booking booking) {
        if (request == null || payment == null) {
            return;
        }
        if (booking != null) {
            payment.setBooking(booking);
        }
        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            payment.setCurrency(request.getCurrency());
        }
        if (request.getMethod() != null) {
            payment.setMethod(request.getMethod());
        }
        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
        }
    }
}

