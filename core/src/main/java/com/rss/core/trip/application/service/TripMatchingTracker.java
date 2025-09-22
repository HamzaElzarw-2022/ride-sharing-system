package com.rss.core.trip.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Tracks trips in MATCHING state in Redis so that a scheduler can mark them
 * as NO_DRIVERS_MATCHED after a timeout.
 */
@Component
@RequiredArgsConstructor
class TripMatchingTracker {
    private final StringRedisTemplate redis;

    // Sorted set: member = tripId, score = epoch millis when it should expire
    private static final String MATCHING_ZSET = "trip:matching";

    @Value("${trip.matching.timeout.seconds:80}")
    private long matchingTimeoutSeconds;

    void addMatchingTrip(Long tripId, long createdAtEpochMillis) {
        long expireAt = createdAtEpochMillis + Duration.ofSeconds(matchingTimeoutSeconds).toMillis();
        redis.opsForZSet().add(MATCHING_ZSET, tripId.toString(), expireAt);
    }

    void removeMatchingTrip(Long tripId) {
        redis.opsForZSet().remove(MATCHING_ZSET, tripId.toString());
    }

    String zsetKey() { return MATCHING_ZSET; }

    java.util.Set<String> findExpired(long nowEpochMillis) {
        return redis.opsForZSet().rangeByScore(MATCHING_ZSET, 0, nowEpochMillis);
    }
}
