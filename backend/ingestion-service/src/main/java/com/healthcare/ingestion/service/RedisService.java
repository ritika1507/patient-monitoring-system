package com.healthcare.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.ingestion.model.VitalsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Service for caching vitals in Redis
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.redis.vitals-key-prefix}")
    private String keyPrefix;

    @Value("${app.redis.ttl-seconds}")
    private long ttlSeconds;

    /**
     * Cache latest vitals for a patient
     */
    public void cacheLatestVitals(VitalsData vitals) {
        String key = keyPrefix + vitals.getPatientId() + ":latest";
        try {
            String json = objectMapper.writeValueAsString(vitals);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
            log.debug("Cached vitals for patient {} in Redis", vitals.getPatientId());
        } catch (JsonProcessingException e) {
            log.error("Error caching vitals to Redis: {}", e.getMessage());
        }
    }

    /**
     * Get latest cached vitals for a patient
     */
    public Optional<VitalsData> getLatestVitals(String patientId) {
        String key = keyPrefix + patientId + ":latest";
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                VitalsData vitals = objectMapper.readValue(json, VitalsData.class);
                return Optional.of(vitals);
            }
        } catch (Exception e) {
            log.error("Error retrieving vitals from Redis: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Publish vitals update event to Redis Pub/Sub
     * (For real-time dashboard updates)
     */
    public void publishVitalsUpdate(VitalsData vitals) {
        try {
            String channel = "vitals:updates";
            String json = objectMapper.writeValueAsString(vitals);
            redisTemplate.convertAndSend(channel, json);
            log.debug("Published vitals update to Redis channel for patient {}", vitals.getPatientId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing to Redis: {}", e.getMessage());
        }
    }
}