package com.rss.core.trip.application.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rss.core.location.DriverLocation;
import com.rss.core.trip.domain.event.TripEndedEvent;
import com.rss.core.trip.domain.event.TripMatchedEvent;
import com.rss.core.trip.domain.event.TripStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long tripId = getTripId(session);
        if (tripId != null) {
            sessions.put(tripId, session);
            log.info("[TripWS] session connected for trip: {}", tripId);
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Trip ID not found in URI"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long tripId = getTripId(session);
        if (tripId != null) {
            sessions.remove(tripId);
            log.info("[TripWS] session closed for trip: {}", tripId);
        }
    }

    @EventListener
    public void onTripMatched(TripMatchedEvent event) {
        sendTripEvent(event.id(), "trip.matched", Map.of(
                "tripId", event.id(),
                "driverId", event.driverId()
        ));
    }

    @EventListener
    public void onTripStarted(TripStartedEvent event) {
        sendTripEvent(event.id(), "trip.started", Map.of(
                "tripId", event.id(),
                "startTime", event.startTime().toString()
        ));
    }

    @EventListener
    public void onTripEnded(TripEndedEvent event) {
        sendTripEvent(event.id(), "trip.ended", Map.of(
                "tripId", event.id(),
                "endTime", event.endTime() == null ? "0" : event.endTime().toString()
        ));
        closeSession(event.id());
    }

    public void sendDriverLocation(Long tripId, Long driverId, DriverLocation location) {
        if (location == null) return;
        sendTripEvent(tripId, "driver.location", Map.of(
                "tripId", tripId,
                "driverId", driverId,
                "x", location.getX(),
                "y", location.getY(),
                "degree", location.getDegree()
        ));
    }

    private void sendTripEvent(Long tripId, String type, Map<String, Object> payload) {
        WebSocketSession session = sessions.get(tripId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            var msg = Map.of(
                    "type", type,
                    "ts", System.currentTimeMillis(),
                    "payload", payload
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (IOException e) {
            log.warn("[TripWS] failed to send message to trip {}", tripId, e);
        }
    }

    private void closeSession(Long tripId) {
        WebSocketSession session = sessions.get(tripId);
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.warn("[TripWS] failed to close session for trip {}", tripId, e);
            }
        }
    }

    private Long getTripId(WebSocketSession session) {
        if (session.getUri() == null) return null;
        String path = session.getUri().getPath();
        String tripIdStr = path.substring(path.lastIndexOf('/') + 1);
        try {
            return Long.parseLong(tripIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

