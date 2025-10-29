package com.rss.core.trip.application.ws;

import com.rss.core.common.component.AuthenticationFacade;
import com.rss.core.trip.domain.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TripHandshakeInterceptor implements HandshakeInterceptor {

    private final TripRepository tripRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extract trip id from path: /ws/trip/{tripId}
        String path = request.getURI().getPath();
        String tripIdStr = path.substring(path.lastIndexOf('/') + 1);
        if (tripIdStr.isEmpty()) {
            setStatus(response, HttpStatus.BAD_REQUEST);
            return false;
        }

        Long tripId;
        try {
            tripId = Long.parseLong(tripIdStr);
        } catch (NumberFormatException e) {
            setStatus(response, HttpStatus.BAD_REQUEST);
            return false;
        }

        // Extract token from query (?token=...) sent by the dashboard client, or Authorization header as fallback
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            setStatus(response, HttpStatus.UNAUTHORIZED);
            return false;
        }

        // Resolve rider id from token and ensure this rider owns the trip
        Long riderId;
        try {
            riderId = authenticationFacade.getAuthenticatedRiderId(token);
        } catch (RuntimeException ex) {
            setStatus(response, HttpStatus.UNAUTHORIZED);
            return false;
        }

        var tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty()) {
            setStatus(response, HttpStatus.NOT_FOUND);
            return false;
        }

        boolean allowed = Objects.equals(tripOpt.get().getRiderId(), riderId);
        if (!allowed) {
            setStatus(response, HttpStatus.FORBIDDEN);
            return false;
        }

        // Store attributes for downstream handlers if needed
        attributes.put("tripId", tripId);
        attributes.put("riderId", riderId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }

    private void setStatus(ServerHttpResponse response, HttpStatus status) {
        try {
            response.setStatusCode(status);
        } catch (Exception ignored) {
        }
    }

    private String extractToken(ServerHttpRequest request) {
        // Prefer query parameter used by browser client
        String query = request.getURI().getQuery();
        if (query != null && !query.isBlank()) {
            String[] parts = query.split("&");
            for (String part : parts) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    if ("token".equalsIgnoreCase(key) || "access_token".equalsIgnoreCase(key)) {
                        return value;
                    }
                }
            }
        }

        // Fallback to Authorization header if present (useful for non-browser clients)
        List<String> authHeaders = request.getHeaders().getOrDefault("Authorization", Collections.emptyList());
        if (!authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header != null) {
                String lower = header.toLowerCase(Locale.ROOT);
                if (lower.startsWith("bearer ")) {
                    return header.substring(7).trim();
                }
                return header.trim();
            }
        }

        return null;
    }
}
