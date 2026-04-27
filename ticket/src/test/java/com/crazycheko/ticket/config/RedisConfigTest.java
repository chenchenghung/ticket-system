package com.crazycheko.ticket.config;

import com.crazycheko.ticket.bean.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
public class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @Test
    public void testRedisTemplateConfiguration(){
        assertNotNull(redisTemplate);

        // 验证序列化器已正确配置
        assertNotNull(redisTemplate.getKeySerializer());
        assertNotNull(redisTemplate.getValueSerializer());

        System.out.println("✅ RedisTemplate 配置成功！");
        System.out.println("Key Serializer: " + redisTemplate.getKeySerializer().getClass().getSimpleName());
        System.out.println("Value Serializer: " + redisTemplate.getValueSerializer().getClass().getSimpleName());
    }

//    @Test
    public void testSerialization() {
        User user = new User("张三", 25);

        // 存储
        redisTemplate.opsForValue().set("user:1", user);

        // 读取
        User result = (User) redisTemplate.opsForValue().get("user:1");

        // ❌ 这里会失败！
        // 因为默认 JDK 序列化存储后，读取时可能：
        // 1. 类型转换异常
        // 2. 或者读出来是乱码
        assertEquals("张三", result.getName());
    }

    @Test
    public void testBeanExists() {
        if (redisTemplate == null) {
            System.err.println("❌ redisTemplate Bean 不存在！检查 Redis 配置");
        } else {
            System.out.println("✅ redisTemplate Bean 存在");
            try {
                String pong = redisTemplate.getConnectionFactory().getConnection().ping();
                System.out.println("✅ Redis 连接成功: " + pong);
            } catch (Exception e) {
                System.err.println("❌ Redis 连接失败: " + e.getMessage());
            }
        }
    }


}
