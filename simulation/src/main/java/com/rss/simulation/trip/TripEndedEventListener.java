package com.rss.simulation.trip;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Listens to TripEnded events from Core and marks riders available. */
@Component
public class TripEndedEventListener {
    public static final String QUEUE_NAME = "trip.ended.queue";

    private final RiderAvailabilityInbox availabilityInbox;

    public TripEndedEventListener(RiderAvailabilityInbox availabilityInbox) {
        this.availabilityInbox = availabilityInbox;
    }

    @RabbitListener(queues = {QUEUE_NAME})
    public void onTripEnded(@Payload Map<String, Object> event) {
        try {
            if (event == null) return;
            Object type = event.get("eventType");
            if (type == null || !"TripEnded".equals(type.toString())) {
                return;
            }
            Object riderIdObj = event.get("riderId");
            if (riderIdObj == null) return;

            long riderId = toLong(riderIdObj);
            availabilityInbox.markAvailable(riderId);
            System.out.println("[TripEndedEventListener] rider=" + riderId + " marked available");
        } catch (Exception e) {
            System.out.println("[TripEndedEventListener] failed to process event: " + e.getMessage());
        }
    }

    private long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
