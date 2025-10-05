package ru.practicum.ewmservice.stat.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewmservice.stat.dto.HitDtoRequest;
import ru.practicum.ewmservice.stat.dto.HitDtoStatResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class StatClientEwm {

    private final RestTemplate restTemplate;
    private final String statServerUrl;

    public StatClientEwm(RestTemplate restTemplate, @Value("${stat.server.url}") String statServerUrl) {
        this.restTemplate = restTemplate;
        this.statServerUrl = statServerUrl;
    }

    public long hitAndGetViews(Long eventId, String ip) {
        HitDtoRequest hit = HitDtoRequest.builder()
                .app("ewm-service")
                .uri("/events/" + eventId)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        saveHit(hit);
        return getViews(eventId);
    }

    public void saveHit(HitDtoRequest hit) {
        restTemplate.postForEntity(statServerUrl + "/hit", hit, Void.class);
    }

    public long getViews(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.now();

            String startParam = URLEncoder.encode(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);
            String endParam = URLEncoder.encode(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);
            String uriParam = URLEncoder.encode("/events/" + eventId, StandardCharsets.UTF_8);

            String url = String.format("%s/stats?start=%s&end=%s&unique=true&uris=%s",
                    statServerUrl, startParam, endParam, uriParam);

            ResponseEntity<List<HitDtoStatResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            List<HitDtoStatResponse> stats = response.getBody();
            if (stats != null && !stats.isEmpty()) {
                return stats.get(0).hits();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }
}
