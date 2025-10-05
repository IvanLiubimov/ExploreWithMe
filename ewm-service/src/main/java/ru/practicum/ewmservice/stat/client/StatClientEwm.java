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
import java.util.List;

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
        String start = URLEncoder.encode("2000-01-01 00:00:00", StandardCharsets.UTF_8);
        String end = URLEncoder.encode(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), StandardCharsets.UTF_8);
        String uri = "/events/" + eventId;

        String url = String.format("%s/stats?start=%s&end=%s&unique=true&uris=%s",
                statServerUrl, start, end, uri);

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
        return 0L;
    }
}