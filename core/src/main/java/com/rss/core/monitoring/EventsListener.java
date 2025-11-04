package com.rss.core.monitoring;

import com.rss.core.trip.domain.event.TripCreatedEvent;
import com.rss.core.trip.domain.event.TripEndedEvent;
import com.rss.core.trip.domain.event.TripMatchedEvent;
import com.rss.core.trip.domain.event.TripStartedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Profile("debug")
@Component
public class EventsListener {

    @EventListener
    public void onTripCreated(TripCreatedEvent event) {
        System.out.println("[Monitoring] CREATED TRIP " + event.id() + " for rider " + event.riderId());
    }
    @EventListener
    public void onTripMatched(TripMatchedEvent event) {
        System.out.println("[Monitoring] MATCHED TRIP " + event.id() + " with driver " + event.driverId());
    }
    @EventListener
    public void onTripStarted(TripStartedEvent event) {
        System.out.println("[Monitoring] STARTED TRIP " + event.id());
    }
    @EventListener
    public void onTripEnded(TripEndedEvent event) {
        System.out.println("[Monitoring] ENDED TRIP " + event.id());
    }
}
