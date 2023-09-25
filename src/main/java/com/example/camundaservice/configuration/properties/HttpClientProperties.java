package com.example.camundaservice.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "http.client")
@Data
@Validated
public class HttpClientProperties {

    @NotNull
    private Duration readTimeout;
    @NotNull
    private Duration connectTimeout;
    @NotNull
    private Duration writeTimeout;
    @NotNull
    private Integer retryAttempt;
    @NotNull
    private Duration retryDelay;
    private Connections connections = new Connections();

    @Data
    public static class Connections {
        private Integer max = 500;
        private Duration maxIdleTime = Duration.ofMillis(-1);
        private Duration maxLifeTime = Duration.ofMillis(-1);
        private Duration pendingAcquireTimeout = Duration.ofMillis(45000);
    }

}
