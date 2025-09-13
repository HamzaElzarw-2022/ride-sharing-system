package com.rss.simulation.client;

import com.rss.simulation.client.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CoreApiClient {
    private final WebClient client;
    public CoreApiClient(WebClient coreWebClient) {
        this.client = coreWebClient;
    }

    public Mono<AuthResponse> registerDriver(RegisterDriverRequest req) {
        return post("/api/auth/register/driver", req, null, AuthResponse.class);
    }

    public Mono<AuthResponse> registerRider(RegisterRiderRequest req) {
        return post("/api/auth/register/rider", req, null, AuthResponse.class);
    }

    public Mono<AuthResponse> authenticate(AuthRequest req) {
        return post("/api/auth/authenticate", req, null, AuthResponse.class);
    }

    public Mono<RouteResponse> getRoute(Point start, Point end, String jwt) {
        return post("/api/map/route", new RouteRequest(start, end), jwt, RouteResponse.class);
    }

    public Mono<RouteResponse> getSimRoute(SimRouteRequest req, String jwt) {
        return post("/api/map/simRoute", req, jwt, RouteResponse.class);
    }

    public void updateLocation(Point location, double degree, String jwt) {
        post("/api/drivers/location/update?x=" + location.x()+ "&y=" + location.y() + "&degree=" + degree,
                jwt, Void.class).subscribe();
    }

    public Mono<TripDto> acceptTrip(Long tripId, String jwt) {
        return post("/api/trips/" + tripId + "/accept", jwt, TripDto.class);
    }

    public Mono<TripDto> startTrip(Long tripId, String jwt) {
        return post("/api/trips/" + tripId + "/start", jwt, TripDto.class);
    }

    public Mono<TripDto> endTrip(Long tripId, String jwt) {
        return post("/api/trips/" + tripId + "/end", jwt, TripDto.class);
    }

    public Mono<TripDto> getTrip(Long tripId, String jwt) {
        return post("/api/trips/" + tripId, jwt, TripDto.class);
    }

    private <T> Mono<T> post(String uri, Object reqBody, String jwt, Class<T> clazz) {
        var spec = client.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON);

        if(jwt != null && !jwt.isBlank()) {
            spec.header("Authorization", "Bearer " + jwt);
        }
        return spec
                .bodyValue(reqBody)
                .retrieve()
                .bodyToMono(clazz);
    }

    private <T> Mono<T> post(String uri, String jwt, Class<T> clazz) {
        var spec = client.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON);

        if(jwt != null && !jwt.isBlank()) {
            spec.header("Authorization", "Bearer " + jwt);
        }
        return spec.retrieve().bodyToMono(clazz);
    }
}