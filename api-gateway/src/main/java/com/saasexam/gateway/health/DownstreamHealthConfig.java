package com.saasexam.gateway.health;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

/**
 * Adds a "services" component to the gateway's /actuator/health that calls each downstream service's own
 * /actuator/health, so one public URL shows whether catalog, question and exam (and their databases) are up.
 * It is not part of the liveness or readiness groups, so a downstream outage never restarts the gateway.
 */
@Configuration
public class DownstreamHealthConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Bean
    CompositeReactiveHealthContributor services(WebClient.Builder builder,
            @Value("${CATALOG_SERVICE_URL:http://localhost:8081}") String catalogUrl,
            @Value("${QUESTION_SERVICE_URL:http://localhost:8082}") String questionUrl,
            @Value("${EXAM_SERVICE_URL:http://localhost:8083}") String examUrl) {
        Map<String, ReactiveHealthIndicator> indicators = new LinkedHashMap<>();
        indicators.put("catalog", downstream(builder, catalogUrl));
        indicators.put("question", downstream(builder, questionUrl));
        indicators.put("exam", downstream(builder, examUrl));
        return CompositeReactiveHealthContributor.fromMap(indicators);
    }

    private static ReactiveHealthIndicator downstream(WebClient.Builder builder, String baseUrl) {
        WebClient client = builder.baseUrl(baseUrl).build();
        return () -> client.get().uri("/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                // A service whose database is down answers 503 with its health body; keep that body.
                .onErrorResume(WebClientResponseException.class, e -> Mono.justOrEmpty(e.getResponseBodyAs(Map.class)))
                .map(body -> Health.status(String.valueOf(body.getOrDefault("status", "UNKNOWN")))
                        .withDetail("url", baseUrl)
                        .withDetails(body.containsKey("components") ? Map.of("components", body.get("components")) : Map.of())
                        .build())
                .timeout(TIMEOUT)
                .onErrorResume(e -> Mono.just(Health.down(e).withDetail("url", baseUrl).build()));
    }
}
