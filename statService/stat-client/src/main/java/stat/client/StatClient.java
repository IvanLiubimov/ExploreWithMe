package stat.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import stat.dto.HitDtoRequest;
import stat.dto.HitDtoStatResponse;

import java.util.List;

public class  StatClient {
    private final String serverUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public StatClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void saveHit(HitDtoRequest hit) {
        restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
    }

    public List<HitDtoStatResponse> getStats(String start, String end, List<String> uris, boolean unique) {
        String uri = serverUrl + "/stats?start=" + start + "&end=" + end + "&unique=" + unique;
        for (String u : uris) {
            uri += "&uris=" + u;
        }
        ResponseEntity<List<HitDtoStatResponse>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<HitDtoStatResponse>>() {}
        );
        return response.getBody();
    }

}