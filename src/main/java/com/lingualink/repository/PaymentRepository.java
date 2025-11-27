package com.lingualink.repository;

import com.lingualink.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBooking_Id(Long bookingId);
    List<Payment> findByStatus(String status);
}

