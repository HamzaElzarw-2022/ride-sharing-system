package com.rss.core.trip.infrastructure;

import com.rss.core.trip.application.port.out.NotificationService;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitProducer implements NotificationService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void NotifyDriverRequest(Long driverId, Long tripId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "DriverRequested");
        event.put("tripId", tripId);
        event.put("driverId", driverId);
        event.put("timestamp", Instant.now().toString());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.DRIVER_REQUEST_ROUTING_KEY,
                event
        );
    }

    @Override
    public void NotifyDriverRequest(List<Long> driverIds, Long tripId) {
        if (driverIds == null || driverIds.isEmpty()) return;

        for (Long driverId : driverIds) {
            System.out.println("[RabbitProducer] Notifying driver " + driverId + " for trip " + tripId);
            NotifyDriverRequest(driverId, tripId);
        }
    }

    @Override
    public void NotifyRiderTripEnded(Long riderId, Long tripId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "TripEnded");
        event.put("tripId", tripId);
        event.put("riderId", riderId);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.TRIP_ENDED_ROUTING_KEY,
                event
        );
    }
}
