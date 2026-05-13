package com.crazycheko.ticket.service;


import com.crazycheko.ticket.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private DefaultRedisScript<Long> butTicketScript;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TicketService ticketService;


    void setUp(){
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        System.out.println("goodJob");
    }
    @Test
    void buyTicket_Success_ShouldeReturnSuccess(){
        lenient().when(redisTemplate.execute(eq(butTicketScript), anyList(), eq(2), eq(5))).thenReturn(98L);

        // 执行
        TicketService.BuyResult result = ticketService.buyTicket(
                "concert_2026", "user_001", 2, 5
        );

        // 验证
        assertTrue(result.success());
        assertEquals(98, result.remainingTickets());
        assertNotNull(result.orderNo());

        // 验证数据库保存被调用了一次
        verify(orderRepository, times(1)).save(any());
    }

}
