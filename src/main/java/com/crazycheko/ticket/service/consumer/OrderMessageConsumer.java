package com.crazycheko.ticket.service.consumer;

import com.crazycheko.ticket.config.RabbitMQConfig;
import com.crazycheko.ticket.dto.OrderMessage;
import com.crazycheko.ticket.entity.Order;
import com.crazycheko.ticket.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageConsumer {

    private final OrderRepository orderRepository;

    @Retryable(value = {AmqpException.class}, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_NAME)
    @Transactional
    public void handleOrderMessage(OrderMessage orderMessage){
        log.info("📩 收到订单消息: {}", orderMessage);

        try {
            // 幂等性检查
            if (orderRepository.existsByOrderNo(orderMessage.getOrderNo())) {
                log.warn("订单 {} 已存在，跳过处理", orderMessage.getOrderNo());
                return;
            }

            // 转换为 Order 实体并保存
            Order order = Order.builder()
                    .orderNo(orderMessage.getOrderNo())
                    .eventId(orderMessage.getEventId())
                    .userId(orderMessage.getUserId())
                    .quantity(orderMessage.getQuantity())
                    .status(orderMessage.getStatus())
                    .createdAt(LocalDateTime.now())
                    .build();

            orderRepository.save(order);
            log.info("✅ 订单保存成功: {}", orderMessage.getOrderNo());

        } catch (Exception e) {
            log.error("❌ 订单保存失败: {}", orderMessage.getOrderNo(), e);
            throw new RuntimeException("订单保存失败", e);
        }
    }
}
