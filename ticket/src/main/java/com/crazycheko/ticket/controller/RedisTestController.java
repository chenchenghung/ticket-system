package com.crazycheko.ticket.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/redis")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> setValue(@RequestParam String key, @RequestParam String value){
        Map<String, Object> result = new HashMap<>();

        try{
            redisTemplate.opsForValue().set(key, value);
            result.put("success", true);
            result.put("message", "Key '" + key + "' set sucessfulely");
            result.put("value", value);

        }catch (Exception e){
            result.put("success", false);
            result.put("error", e.getMessage());
        }


        return result;
    }
}
