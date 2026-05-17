package com.bank.cardlimitinvoice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LimitService {

    private static final String KEY_PREFIX = "card:limit:";

    private final StringRedisTemplate redisTemplate;

    public void initializeLimit(UUID cardId, BigDecimal limit) {
        redisTemplate.opsForValue().set(key(cardId), limit.toPlainString());
    }

    public BigDecimal getAvailableLimit(UUID cardId) {
        String value = redisTemplate.opsForValue().get(key(cardId));
        return value != null ? new BigDecimal(value) : BigDecimal.ZERO;
    }

    public void decrementLimit(UUID cardId, BigDecimal amount) {
        BigDecimal current = getAvailableLimit(cardId);
        redisTemplate.opsForValue().set(key(cardId), current.subtract(amount).toPlainString());
    }

    public void incrementLimit(UUID cardId, BigDecimal amount) {
        BigDecimal current = getAvailableLimit(cardId);
        redisTemplate.opsForValue().set(key(cardId), current.add(amount).toPlainString());
    }

    public void setLimit(UUID cardId, BigDecimal limit) {
        redisTemplate.opsForValue().set(key(cardId), limit.toPlainString());
    }

    private String key(UUID cardId) {
        return KEY_PREFIX + cardId;
    }
}
