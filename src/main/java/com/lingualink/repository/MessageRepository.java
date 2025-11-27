package com.lingualink.repository;

import com.lingualink.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByBooking_Id(Long bookingId);
    List<Message> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId);
    
    // Pagination support
    Page<Message> findAll(Pageable pageable);
    Page<Message> findByBooking_Id(Long bookingId, Pageable pageable);
    Page<Message> findBySender_IdAndReceiver_Id(Long senderId, Long receiverId, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT m FROM Message m WHERE " +
           "(:bookingId IS NULL OR m.booking.id = :bookingId) AND " +
           "(:senderId IS NULL OR m.sender.id = :senderId) AND " +
           "(:receiverId IS NULL OR m.receiver.id = :receiverId) AND " +
           "(:content IS NULL OR LOWER(m.content) LIKE LOWER(CONCAT('%', :content, '%'))) AND " +
           "(:sentFrom IS NULL OR m.sentAt >= :sentFrom) AND " +
           "(:sentTo IS NULL OR m.sentAt <= :sentTo)")
    Page<Message> findByFilters(@Param("bookingId") Long bookingId,
                                @Param("senderId") Long senderId,
                                @Param("receiverId") Long receiverId,
                                @Param("content") String content,
                                @Param("sentFrom") LocalDateTime sentFrom,
                                @Param("sentTo") LocalDateTime sentTo,
                                Pageable pageable);
}

