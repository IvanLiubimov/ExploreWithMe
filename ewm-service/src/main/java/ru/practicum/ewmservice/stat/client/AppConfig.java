package ru.practicum.ewmservice.stat.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    private String url;

    public String getUrl() {
        return url;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
