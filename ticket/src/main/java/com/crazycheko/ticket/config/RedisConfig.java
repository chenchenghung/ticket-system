package com.crazycheko.ticket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


//    @Bean
//    @ConditionalOnMissingBean(name = "redisTemplate")
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // Key 使用 String 序列化
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        // Value 使用 JSON 序列化
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//        template.afterPropertiesSet();
//        return template;
//    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        System.out.println("================= 开始创建 redisTemplate Bean =================");
        System.out.println("connectionFactory: " + connectionFactory);

        try {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.afterPropertiesSet();

            System.out.println("================= redisTemplate Bean 创建成功 =================");
            return template;
        } catch (Exception e) {
            System.err.println("================= redisTemplate Bean 创建失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Bean
    public DefaultRedisScript<Long> buyTicketScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(BUY_TICKET_LUA);
        script.setResultType(Long.class);
        return script;
    }

    private static final String BUY_TICKET_LUA =
            "local ticket_key = KEYS[1]\n" +
                    "local user_key = KEYS[2]\n" +
                    "local quantity = tonumber(ARGV[1])\n" +
                    "local max_per_user = tonumber(ARGV[2])\n" +
                    "\n" +
                    "-- 检查用户限购\n" +
                    "local user_bought = redis.call('GET', user_key)\n" +
                    "if user_bought and tonumber(user_bought) + quantity > max_per_user then\n" +
                    "    return -2\n" +
                    "end\n" +
                    "\n" +
                    "-- 检查剩余票数\n" +
                    "local remaining = redis.call('GET', ticket_key)\n" +
                    "if not remaining then\n" +
                    "    return -1\n" +
                    "end\n" +
                    "\n" +
                    "remaining = tonumber(remaining)\n" +
                    "if remaining < quantity then\n" +
                    "    return -3\n" +
                    "end\n" +
                    "\n" +
                    "-- 扣减票数，记录用户购买\n" +
                    "redis.call('DECRBY', ticket_key, quantity)\n" +
                    "redis.call('INCRBY', user_key, quantity)\n" +
                    "redis.call('EXPIRE', user_key, 3600)\n" +
                    "\n" +
                    "return remaining - quantity\n";
}
