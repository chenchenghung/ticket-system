package com.crazycheko.ticket.repository;

import com.crazycheko.ticket.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(String userId);

    List<Order> findByEventId(String eventId);

    Page<Order> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    boolean existsByOrderNo(String orderNo);
}