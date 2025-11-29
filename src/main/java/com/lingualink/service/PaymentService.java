package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.PaymentRequest;
import com.lingualink.dto.response.PaymentResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Payment;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.PaymentMapper;
import com.lingualink.repository.BookingRepository;
import com.lingualink.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper,
                         BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.bookingRepository = bookingRepository;
    }

    // Create
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        Payment payment = paymentMapper.toEntity(request, booking);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Payment> getAllPayments(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Payment> getAllPaymentsWithFilters(PageParams pageParams, Long bookingId, String status, 
                                                             String currency, String method,
                                                             BigDecimal minAmount, BigDecimal maxAmount,
                                                             LocalDateTime paidFrom, LocalDateTime paidTo) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findByFilters(bookingId, status, currency, method, 
                                                               minAmount, maxAmount, paidFrom, paidTo, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getAllPaymentsWithFiltersAsResponse(PageParams pageParams, Long bookingId, String status, 
                                                                             String currency, String method,
                                                                             BigDecimal minAmount, BigDecimal maxAmount,
                                                                             LocalDateTime paidFrom, LocalDateTime paidTo) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findByFilters(bookingId, status, currency, method, 
                                                               minAmount, maxAmount, paidFrom, paidTo, pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPaymentsAsResponse() {
        List<Payment> payments = getAllPayments();
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByIdAsResponse(Long id) {
        Payment payment = getPaymentById(id);
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBooking_Id(bookingId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Payment> getPaymentsByBookingId(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findByBooking_Id(bookingId, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getPaymentsByBookingIdAsResponse(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findByBooking_Id(bookingId, pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBookingIdAsResponse(Long bookingId) {
        List<Payment> payments = getPaymentsByBookingId(bookingId);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Payment> getPaymentsByStatus(String status, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findByStatus(status, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getPaymentsByStatusAsResponse(String status, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("paidAt");
        Page<Payment> page = paymentRepository.findByStatus(status, pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatusAsResponse(String status) {
        List<Payment> payments = getPaymentsByStatus(status);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
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

    public PaymentResponse updatePayment(Long id, PaymentRequest request) {
        Payment payment = getPaymentById(id);
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : payment.getBooking();
        paymentMapper.updateEntityFromRequest(request, payment, booking);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
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

    public PaymentResponse patchPayment(Long id, PaymentRequest request) {
        Payment payment = getPaymentById(id);
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        paymentMapper.updateEntityFromRequest(request, payment, booking);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    // Delete
    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }
}


