package com.example.camundaservice.configuration;

import com.example.camundaservice.configuration.properties.HttpClientProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ReactorNettyHttpClientMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(HttpClientProperties.class)
public class WebClientConfig {

    @Bean
    public ReactorNettyHttpClientMapper customizeReactorNettyHttpClient(final HttpClientProperties httpClientProperties) {
        return httpClient -> httpClient
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) httpClientProperties.getConnectTimeout().toMillis())
            .doOnConnected(connection -> connection
                .addHandlerLast(new ReadTimeoutHandler(httpClientProperties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(httpClientProperties.getWriteTimeout().toMillis(), TimeUnit.MILLISECONDS)));
    }

    @Bean
    public ReactorResourceFactory reactorResourceFactory(final HttpClientProperties httpClientProperties) {
        final HttpClientProperties.Connections connections = httpClientProperties.getConnections();
        final ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
        reactorResourceFactory.setConnectionProviderSupplier(() ->
                                                                 ConnectionProvider.builder("webflux")
                                                                     .maxConnections(connections.getMax())
                                                                     .maxIdleTime(connections.getMaxIdleTime())
                                                                     .maxLifeTime(connections.getMaxLifeTime())
                                                                     .pendingAcquireTimeout(connections.getPendingAcquireTimeout())
                                                                     .build());
        return reactorResourceFactory;
    }

}
