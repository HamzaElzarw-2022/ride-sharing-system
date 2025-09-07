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
}