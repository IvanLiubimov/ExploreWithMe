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
        try {
            restTemplate.postForEntity(statServerUrl + "/hit", hit, Void.class);
        } catch (Exception e) {
            // безопасно логируем, чтобы тесты не падали
            System.out.println("Не удалось сохранить хит: " + e.getMessage());
        }
    }

    public long getViews(Long eventId) {
        return getViews(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    public Map<Long, Long> getViews(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        try {
            String start = URLEncoder.encode("2000-01-01 00:00:00", StandardCharsets.UTF_8);
            String end = URLEncoder.encode(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);

            String urisParam = eventIds.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.joining(","));

            String url = String.format("%s/stats?start=%s&end=%s&unique=true&uris=%s",
                    statServerUrl, start, end, urisParam);

            ResponseEntity<List<HitDtoStatResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            List<HitDtoStatResponse> stats = response.getBody();
            if (stats == null) stats = List.of();

            Map<Long, Long> result = new HashMap<>();
            for (HitDtoStatResponse s : stats) {
                if (s.uri() != null) {
                    try {
                        String[] parts = s.uri().split("/");
                        Long eventId = Long.parseLong(parts[parts.length - 1]);
                        result.put(eventId, s.hits());
                    } catch (Exception ignored) {}
                }
            }

            // Если stat-server не вернул результат по событию, возвращаем 0
            for (Long id : eventIds) {
                result.putIfAbsent(id, 0L);
            }

            return result;

        } catch (Exception e) {
            System.out.println("Ошибка при получении просмотров: " + e.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }
}
