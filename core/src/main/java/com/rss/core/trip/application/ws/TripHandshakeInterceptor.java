package com.rss.core.trip.application.ws;

import com.rss.core.common.component.AuthenticationFacade;
import com.rss.core.trip.domain.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TripHandshakeInterceptor implements HandshakeInterceptor {

    private final TripRepository tripRepository;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String path = request.getURI().getPath();
        String tripIdStr = path.substring(path.lastIndexOf('/') + 1);
        if (tripIdStr.isEmpty()) {
            return false;
        }
        try {
            Long tripId = Long.parseLong(tripIdStr);
            Long riderId = authenticationFacade.getAuthenticatedRiderId();
            return tripRepository.findById(tripId)
                    .map(trip -> trip.getRiderId().equals(riderId))
                    .orElse(false);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}

