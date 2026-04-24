package com.crazycheko.ticket.service;

import com.crazycheko.ticket.bean.Order;
import com.crazycheko.ticket.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> buyTicketScript;
    private final OrderRepository orderRepository;


    /**
     * 初始化活动票数
     */
    public void initEvent(String eventId, int totalTickets) {
        String key = getTicketKey(eventId);
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, totalTickets);
        if (Boolean.TRUE.equals(set)) {
            log.info("初始化活动 {}，票数 {}", eventId, totalTickets);
        } else {
            log.info("活动 {} 已存在，当前票数 {}", eventId, redisTemplate.opsForValue().get(key));
        }
    }

    /**
     * 获取剩余票数
     */
    public int getRemainingTickets(String eventId) {
        Object value = redisTemplate.opsForValue().get(getTicketKey(eventId));
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    /**
     * 购票（同步扣票 + 异步写库）
     */
    public BuyResult buyTicket(String eventId, String userId, int quantity, int maxPerUser) {
        // 1. Redis 原子扣票（核心，同步）
        Long result = redisTemplate.execute(
                buyTicketScript,
                List.of(getTicketKey(eventId), getUserKey(eventId, userId)),
                quantity, maxPerUser
        );

        String orderNo = generateOrderNo();

        if (result >= 0) {
            // 2. 购票成功，异步写入数据库（不阻塞响应）
            saveOrderAsync(orderNo, eventId, userId, quantity, Order.OrderStatus.SUCCESS);
//            log.info("✅ 用户 {} 购票成功，订单号: {}", userId, orderNo);
            return BuyResult.success(result.intValue(), orderNo);
        }

        // 3. 购票失败，异步记录失败日志
        saveOrderAsync(orderNo, eventId, userId, quantity, Order.OrderStatus.FAILED);

        switch (result.intValue()) {
            case -1:
                return BuyResult.fail("活动不存在");
            case -2:
                return BuyResult.fail("超过限购数量");
            case -3:
                return BuyResult.fail("票数不足");
            default:
                return BuyResult.fail("系统错误");
        }
    }

    /**
     * 异步写入数据库
     */
    @Async
    @Transactional
    public void saveOrderAsync(String orderNo, String eventId, String userId, int quantity, Order.OrderStatus status) {
        try {
            Order order = Order.builder()
                    .orderNo(orderNo)
                    .eventId(eventId)
                    .userId(userId)
                    .quantity(quantity)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
//            log.debug("订单已保存: {}", orderNo);
        } catch (Exception e) {
//            log.error("保存订单失败: {}", orderNo, e);
            // 可以写入死信队列或告警
        }
    }

    /**
     * 查询用户订单（从 DB 读）
     */
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * 查询活动订单统计（从 DB 读）
     */
    public long getEventOrderCount(String eventId) {
        return orderRepository.findByEventId(eventId).size();
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    private String getTicketKey(String eventId) {
        return "ticket:event:" + eventId;
    }

    private String getUserKey(String eventId, String userId) {
        return "ticket:user:" + eventId + ":" + userId;
    }

    public record BuyResult(boolean success, String message, int remainingTickets, String orderNo) {
        public static BuyResult success(int remainingTickets, String orderNo) {
            return new BuyResult(true, "购票成功", remainingTickets, orderNo);
        }
        public static BuyResult fail(String message) {
            return new BuyResult(false, message, 0, null);
        }
    }
}
