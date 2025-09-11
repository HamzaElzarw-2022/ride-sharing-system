package com.rss.simulation.trip;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/** Listens to trip request events from Core and stores them for drivers. */
@Component
public class IncomingTripEventListener {

    public static final String QUEUE_NAME = "driver.request.queue";

    private final TripRequestInbox inbox;

    public IncomingTripEventListener(TripRequestInbox inbox) {
        this.inbox = inbox;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = "trip.events.exchange"),
            key = "driver.request"
    ))
    public void onTripRequest(@Payload Map<String, Object> event) {
        try {
            Object type = event.get("eventType");
            if (type == null || !"DriverRequested".equals(type.toString())) {
                return;
            }
            Object driverIdObj = event.get("driverId");
            Object tripIdObj = event.get("tripId");
            if (driverIdObj == null || tripIdObj == null) return;

            long driverId = toLong(driverIdObj);
            long tripId = toLong(tripIdObj);

            Instant ts = null;
            Object tsObj = event.get("timestamp");
            if (tsObj != null) {
                try { ts = Instant.parse(tsObj.toString()); } catch (Exception ignored) {}
            }

            inbox.add(driverId, tripId, ts);
            System.out.println("[IncomingTripEventListener] queued tripId=" + tripId + " for driverId=" + driverId);
        } catch (Exception e) {
            System.out.println("[IncomingTripEventListener] failed to process event: " + e.getMessage());
        }
    }

    private long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
