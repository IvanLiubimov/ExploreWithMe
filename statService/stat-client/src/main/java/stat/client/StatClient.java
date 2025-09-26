package stat.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import stat.dto.HitDtoRequest;
import stat.dto.HitDtoStatResponse;

import java.util.List;

@Component
public class StatClient {

    private final String serverUrl;
    private final RestTemplate restTemplate;

    public StatClient(StatClientConfig config, RestTemplate restTemplate) {
        this.serverUrl = config.getUrl();
        this.restTemplate = restTemplate;
    }

    public void saveHit(HitDtoRequest hit) {
        restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
    }

    public List<HitDtoStatResponse> getStats(String start, String end, List<String> uris, boolean unique) {
        StringBuilder uri = new StringBuilder(serverUrl + "/stats?start=" + start + "&end=" + end + "&unique=" + unique);
        for (String u : uris) {
            uri.append("&uris=").append(u);
        }

        ResponseEntity<List<HitDtoStatResponse>> response = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }
}