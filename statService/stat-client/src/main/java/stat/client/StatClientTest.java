package stat.client;

import stat.dto.HitDtoRequest;
import stat.dto.HitDtoStatResponse;

import java.util.List;

public class StatClientTest {
    public static void main(String[] args) {
        StatClient client = new StatClient("http://localhost:8080");

        HitDtoRequest hit = new HitDtoRequest(1L, "explore-app", "/events/1", "192.168.1.1");
        client.saveHit(hit);
        System.out.println("Hit отправлен");

        List<HitDtoStatResponse> stats = client.getStats(
                "2025-09-23 00:00:00",
                "2025-09-24 00:00:00",
                List.of("/events/1"),
                false
        );
        stats.forEach(System.out::println);
    }
}
