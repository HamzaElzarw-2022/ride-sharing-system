package com.rss.simulation.client;

import com.rss.simulation.client.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CoreApiClient {
  private final WebClient client;
  public CoreApiClient(WebClient coreWebClient) { this.client = coreWebClient; }

  public Mono<AuthResponse> registerDriver(RegisterDriverRequest req) {
    return client.post()
        .uri("/api/auth/register/driver")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(AuthResponse.class);
  }

  public Mono<AuthResponse> authenticate(AuthRequest req) {
    return client.post()
        .uri("/api/auth/authenticate")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(AuthResponse.class);
  }

  public Mono<RouteResponse> getRoute(Point start, Point end, String jwt) {
    return post("/api/map/route", new RouteRequest(start, end), jwt, RouteResponse.class);
  }

  public Mono<RouteResponse> getSimRoute(SimRouteRequest req, String jwt) {
    return post("/api/map/simRoute", req, jwt, RouteResponse.class);
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

    public void updateLocation(Point location, double degree, String jwt) {

    }
}