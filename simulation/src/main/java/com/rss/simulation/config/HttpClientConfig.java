// simulation/src/main/java/com/rss/simulation/config/HttpClientConfig.java
package com.rss.simulation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class HttpClientConfig {
  @Bean
  public WebClient coreWebClient(CoreApiProperties props) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build())
        .build();
  }
}