package com.rss.core.trip.application.service;

import com.rss.core.trip.application.port.out.NotificationService;
import com.rss.core.trip.domain.entity.Trip;
import com.rss.core.trip.domain.event.TripEndedEvent;
import com.rss.core.trip.domain.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
class TripMatchingScheduler {
    private final TripMatchingTracker tracker;
    private final TripRepository tripRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    // run every 30 seconds by default
    @Scheduled(fixedDelayString = "${trip.matching.sweeper.fixedDelay.ms:30000}")
    public void sweepExpiredMatchingTrips() {
        long now = Instant.now().toEpochMilli();
        // Fetch trips whose expiration time (score) is <= now
        Set<String> expired = tracker.findExpired(now);

        if (expired == null || expired.isEmpty()) return;

        for (String tripIdStr : expired) {
            try {
                Long tripId = Long.valueOf(tripIdStr);
                tripRepository.findById(tripId).ifPresent(trip -> {
                    if (trip.getStatus() == Trip.TripStatus.MATCHING) {
                        trip.setStatus(Trip.TripStatus.NO_DRIVERS_MATCHED);
                        tripRepository.save(trip);

                        eventPublisher.publishEvent(new TripEndedEvent(tripId, null));
                        notificationService.NotifyRiderTripEnded(trip.getRiderId(), trip.getId());
                        log.info("Trip {} set to NO_DRIVERS_MATCHED due to timeout", tripId);
                    }
                });
            } catch (Exception e) {
                log.warn("Failed processing matching timeout for trip {}", tripIdStr, e);
            } finally {
                tracker.removeMatchingTrip(Long.valueOf(tripIdStr));
            }
        }
    }
}
