package com.rss.core.trip.application.service;

import com.rss.core.location.LocationInternalApi;
import com.rss.core.trip.application.port.in.RequestDriverService;
import com.rss.core.trip.application.port.out.NotificationService;

import com.rss.core.trip.domain.entity.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RequestDriverServiceImpl implements RequestDriverService {
    private final StringRedisTemplate redisTemplate;
    private final LocationInternalApi locationInternalApi;
    private final NotificationService notificationService;

    private final int BATCH_SIZE = 5; // number of drivers to notify at once
    @Value("${driver.request.expiry.seconds:180}")
    private int driverRequestExpirySeconds; // configurable for tests
    private final String DRIVER_REQUEST_KEY = "driver:request:"; // driver_requests:{tripId} -> set of driverIds

    @Override
    public void requestDriver(Trip trip) {
        Set<Long> driverIds = locationInternalApi.findNearbyDrivers(
                trip.getStartPoint().getX(),
                trip.getStartPoint().getY());

        if (driverIds.isEmpty()) {
            System.out.println("[RequestDriverService] No drivers available for trip " + trip.getId());
            return; // No drivers available in range
        }

        // Store driver IDs in Redis set with expiration
        String key = DRIVER_REQUEST_KEY + trip.getId();
        for (Long driverId : driverIds) {
            redisTemplate.opsForSet().add(key, driverId.toString());
        }
        redisTemplate.expire(key, driverRequestExpirySeconds, TimeUnit.SECONDS);

        // TODO: handle no drivers available case (e.g., notify user, retry later, etc.)
        // TODO: handle case where no drivers respond in time (e.g., cancel trip or retry)

        // Notify drivers in batches
        notifyDriversInBatches(driverIds, trip.getId());
    }

    /**
     * Notify drivers in batches to avoid overwhelming the notification service
     */
    private void notifyDriversInBatches(Set<Long> driverIds, Long tripId) {
        List<Long> driverIdList = new ArrayList<>(driverIds);

        for (int i = 0; i < driverIdList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, driverIdList.size());
            List<Long> batch = driverIdList.subList(i, end);

            notificationService.NotifyDriverRequest(batch, tripId);
        }
    }

    @Override
    public boolean isDriverRequestedForTrip(Long driverId, Long tripId) {
        String key = DRIVER_REQUEST_KEY + tripId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, driverId.toString());
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * Remove a driver from the requested list for a trip
     */
    public void removeDriverRequest(Long driverId, Long tripId) {
        String key = DRIVER_REQUEST_KEY + tripId;
        redisTemplate.opsForSet().remove(key, driverId.toString());
    }
}
