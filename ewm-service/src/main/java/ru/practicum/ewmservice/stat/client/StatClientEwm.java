package ru.practicum.ewmservice.stat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.stat.dto.HitDtoRequest;
import ru.practicum.ewmservice.stat.dto.HitDtoStatResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StatClientEwm {

    private final RestTemplate restTemplate;
    private final String statServerUrl;

    public StatClientEwm(RestTemplate restTemplate, @Value("${stat.server.url}") String statServerUrl) {
        this.restTemplate = restTemplate;
        this.statServerUrl = statServerUrl;
    }

    public void saveHit(HitDtoRequest hit) {
        restTemplate.postForEntity(statServerUrl + "/hit", hit, Void.class);
    }

    public long getViews(Long eventId) {
        Map<Long, Long> views = getViews(List.of(new Event() {{ setId(eventId); }}));
        return views.getOrDefault(eventId, 0L);
    }

    public Map<Long, Long> getViews(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        try {
            LocalDateTime startTime = LocalDateTime.now().minusYears(1);
            LocalDateTime endTime = LocalDateTime.now();

            String start = URLEncoder.encode(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);
            String end = URLEncoder.encode(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);

            List<String> uris = events.stream()
                    .map(event -> "/events/" + event.getId())
                    .collect(Collectors.toList());
            String urisParam = URLEncoder.encode(String.join(",", uris), StandardCharsets.UTF_8);

            String url = String.format("%s/stats?start=%s&end=%s&unique=true&uris=%s",
                    statServerUrl, start, end, urisParam);

            ResponseEntity<List<HitDtoStatResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            List<HitDtoStatResponse> stats = response.getBody();
            if (stats == null || stats.isEmpty()) {
                return events.stream().collect(Collectors.toMap(Event::getId, event -> 0L));
            }

            return stats.stream()
                    .filter(s -> s.uri() != null)
                    .collect(Collectors.toMap(
                            s -> extractEventIdFromUri(s.uri()),
                            HitDtoStatResponse::hits,
                            (existing, replacement) -> existing // при дубликатах оставляем существующее
                    ));

        } catch (Exception e) {
            return events.stream().collect(Collectors.toMap(Event::getId, event -> 0L));
        }
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
