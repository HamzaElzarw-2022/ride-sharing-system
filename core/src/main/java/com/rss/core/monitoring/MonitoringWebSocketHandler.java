package com.rss.core.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rss.core.location.DriverLocation;
import com.rss.core.location.LocationInternalApi;
import com.rss.core.trip.domain.event.TripCreatedEvent;
import com.rss.core.trip.domain.event.TripEndedEvent;
import com.rss.core.trip.domain.event.TripMatchedEvent;
import com.rss.core.trip.domain.event.TripStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringWebSocketHandler extends TextWebSocketHandler {

    private final LocationInternalApi locationInternalApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("[MonitoringWS] session connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("[MonitoringWS] session closed: {}", session.getId());
    }

    // Broadcast driver locations every 1 second
    @Scheduled(fixedRate = 1000)
    public void tickDriverLocations() {
        if (sessions.isEmpty()) return;
        try {
            Map<Long, DriverLocation> all = locationInternalApi.getAllDriverLocations();
            var payload = Map.of(
                    "type", "driver.locations",
                    "ts", System.currentTimeMillis(),
                    "drivers", all
            );
            String json = objectMapper.writeValueAsString(payload);
            broadcast(json);
        } catch (Exception e) {
            log.warn("[MonitoringWS] failed to publish driver locations", e);
        }
    }

    // Forward trip events to WebSocket clients as they occur
    @EventListener
    public void onTripCreated(TripCreatedEvent e) { sendTripEvent("trip.created", Map.of(
            "tripId", e.id(),
            "riderId", e.riderId()
    )); }

    @EventListener
    public void onTripMatched(TripMatchedEvent e) { sendTripEvent("trip.matched", Map.of(
            "tripId", e.id(),
            "driverId", e.driverId()
    )); }

    @EventListener
    public void onTripStarted(TripStartedEvent e) { sendTripEvent("trip.started", Map.of(
            "tripId", e.id()
    )); }

    @EventListener
    public void onTripEnded(TripEndedEvent e) { sendTripEvent("trip.ended", Map.of(
            "tripId", e.id(),
            "endTime", e.endTime() == null ? "0" : e.endTime().toString()
    )); }

    private void sendTripEvent(String type, Map<String, Object> payload) {
        if (sessions.isEmpty()) return;
        try {
            var msg = Map.of(
                    "type", type,
                    "ts", System.currentTimeMillis(),
                    "payload", payload
            );
            String json = objectMapper.writeValueAsString(msg);
            broadcast(json);
        } catch (Exception ex) {
            log.warn("[MonitoringWS] failed to send trip event {}", type, ex);
        }
    }

    private void broadcast(String json) {
        for (WebSocketSession s : sessions) {
            if (!s.isOpen()) continue;
            try {
                s.sendMessage(new TextMessage(json));
            } catch (IOException ignored) {
            }
        }
    }
}
