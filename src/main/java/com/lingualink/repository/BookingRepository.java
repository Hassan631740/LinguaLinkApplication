package com.lingualink.repository;

import com.lingualink.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEvent_Id(Long eventId);
    List<Booking> findByInterpreter_Id(Long interpreterId);
    List<Booking> findByStatus(String status);
}

