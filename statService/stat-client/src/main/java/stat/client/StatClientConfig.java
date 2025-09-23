package stat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatClientConfig {

    @Value("${stat.server.url}")
    private String serverUrl;

    @Bean
    public StatClient statClient() {
        return new StatClient(serverUrl);
    }
}