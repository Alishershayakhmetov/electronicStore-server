package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class UserCodeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SmsService smsService;

    private static final int EXPIRY_TIME_MINUTES = 5;

    public void sendCode(String phoneNumber) {
        String code = generateRandomCode();
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(phoneNumber, code, Duration.ofMinutes(EXPIRY_TIME_MINUTES));
        smsService.sendSms(phoneNumber, "Your verification code is: " + code);
    }

    public boolean confirmCode(String phoneNumber, String code) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String storedCode = (String) valueOperations.get(phoneNumber);
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(phoneNumber);
            return true;
        }
        return false;
    }

    private String generateRandomCode() {
        Random random = new Random();
        int num = random.nextInt(999999);
        return String.format("%06d", num);
    }
}

