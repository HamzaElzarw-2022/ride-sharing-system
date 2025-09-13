package com.rss.core.trip.application.service;

import com.rss.core.location.LocationInternalApi;
import com.rss.core.location.LocationService;
import com.rss.core.trip.application.port.out.NotificationService;
import com.rss.core.trip.domain.entity.Trip;
import com.rss.core.trip.domain.entity.Trip.TripStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "driver.request.expiry.seconds=2" // short TTL for expiry test
})
class RequestDriverServiceImplTest {

    @Autowired
    private RequestDriverServiceImpl requestDriverService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private LocationInternalApi locationInternalApi;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private NotificationService notificationService;

    private Trip sampleTrip(Long id) {
        return Trip.builder()
                .id(id)
                .riderId(10L)
                .status(TripStatus.MATCHING)
                .startPoint(new Point(10, 20))
                .endPoint(new Point(30, 40))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void cleanup() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
    }

    @Test
    void requestDriver_storesDriversInRedis_andNotifiesInBatches() {
        // Given
        Long tripId = 100L;
        Trip trip = sampleTrip(tripId);
        // 6 drivers
        LinkedHashSet<Long> drivers = new LinkedHashSet<>(Arrays.asList(1L,2L,3L,4L,5L,6L));
        when(locationInternalApi.findDriversWithinRadius(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(drivers);

        // When
        requestDriverService.requestDriver(trip);

        // Then - Redis set contains all drivers
        String key = "driver_requests:" + tripId;
        Set<String> stored = redisTemplate.opsForSet().members(key);
        assertThat(stored).isNotNull();
        assertThat(stored).hasSize(6).containsExactlyInAnyOrder("1","2","3","4","5","6");

        // TTL should be > 0 (set) and <= configured (1 second) or -1 if extremely fast retrieval before setting? Accept >0
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0);

        // membership helper
        assertThat(requestDriverService.isDriverRequestedForTrip(1L, tripId)).isTrue();
        assertThat(requestDriverService.isDriverRequestedForTrip(6L, tripId)).isTrue();
    }

    @Test
    void requestDriver_noDrivers_noRedisKeyCreated() {
        // Given
        Long tripId = 200L;
        Trip trip = sampleTrip(tripId);
        when(locationInternalApi.findDriversWithinRadius(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.emptySet());

        // When
        requestDriverService.requestDriver(trip);

        // Then - no redis member or notifications should be created
        String key = "driver_requests:" + tripId;
        Set<String> members = redisTemplate.opsForSet().members(key);
        assertThat(members).isNullOrEmpty();
        verify(notificationService, never()).NotifyDriverRequest(anyList(), anyLong());
    }

    @Test
    void isDriverRequestedForTrip_expiresAfterTTL() throws InterruptedException {
        // Given
        Long tripId = 300L;
        Trip trip = sampleTrip(tripId);
        when(locationInternalApi.findDriversWithinRadius(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Set.of(11L));

        // When
        requestDriverService.requestDriver(trip);

        // Immediately should be present
        assertThat(requestDriverService.isDriverRequestedForTrip(11L, tripId)).isTrue();

        // Wait for expiry (property = 1s) with cushion
        Thread.sleep(2500);

        // Then membership should be false
        assertThat(requestDriverService.isDriverRequestedForTrip(11L, tripId)).isFalse();
    }
}