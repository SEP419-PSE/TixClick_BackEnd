package com.pse.tixclick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/test")
    public String testRedis() {
        try {
            redisTemplate.opsForValue().set("testKey", "Hello Redis!");
            String value = redisTemplate.opsForValue().get("testKey");
            return "Redis connected! Value: " + value;
        } catch (Exception e) {
            return "Redis connection failed: " + e.getMessage();
        }
    }
}
