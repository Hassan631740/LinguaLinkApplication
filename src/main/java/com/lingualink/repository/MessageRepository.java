package com.lingualink.repository;

import com.lingualink.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByBooking_Id(Long bookingId);
    List<Message> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId);
}

