package ru.practicum.ewmservice.stat.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewmservice.stat.dto.HitDtoRequest;
import ru.practicum.ewmservice.stat.dto.HitDtoStatResponse;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class StatClientEwm {

    private final RestTemplate restTemplate;
    private final String statServerUrl;

    public StatClientEwm(RestTemplate restTemplate, @Value("${stat.server.url}") String statServerUrl) {
        this.restTemplate = restTemplate;
        this.statServerUrl = statServerUrl;
    }

    public void saveHit(HitDtoRequest hit) {
        log.info("Сохраняем HIT = {}, {}, {}, {}", hit.app(), hit.ip(), hit.uri(), hit.timestamp());
        restTemplate.postForEntity(statServerUrl + "/hit", hit, Void.class);
    }

    public List<HitDtoStatResponse> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder url = new StringBuilder(statServerUrl + "/stats?start={start}&end={end}");

        Map<String, Object> params = new HashMap<>();
        params.put("start", start.format(formatter));
        params.put("end", end.format(formatter));

        if (uris != null && !uris.isEmpty()) {
            url.append("&uris={uris}");
            params.put("uris", String.join(",", uris));
        }
        if (unique != null) {
            url.append("&unique={unique}");
            params.put("unique", unique.toString());
        }

        ResponseEntity<HitDtoStatResponse[]> response = restTemplate.getForEntity(
                url.toString(),
                HitDtoStatResponse[].class,
                params
        );

        HitDtoStatResponse[] body = response.getBody();
        return body != null ? List.of(body) : List.of();
    }

    public long getViews(Long eventId, Boolean unique) {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();

        List<HitDtoStatResponse> stats = getStats(start, end, List.of("/events/" + eventId), unique);

        return stats.stream()
                .mapToLong(HitDtoStatResponse::hits)
                .sum();
    }
}

