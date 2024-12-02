package org.polarmeet.highserver.config;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    @Bean
    public ReactiveWebServerFactory reactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();

        // Create event loops optimized for your M3 Max
        LoopResources loops = LoopResources.create(
                "high-perf",
                32,    // Event loop threads matching CPU cores
                64,    // Worker threads (2x cores)
                true
        );

        // Configure Netty for maximum throughput
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(32);
        ((NioEventLoopGroup) eventLoopGroup).setIoRatio(99); // Prioritize I/O operations

        factory.addServerCustomizers(httpServer ->
                httpServer
                        .runOn(loops)
                        .option(ChannelOption.SO_BACKLOG, 100000)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        // Disable all logging for maximum performance
                        .accessLog(false)
                        .wiretap(false)
        );

        return factory;
    }

    @Bean
    public ReactorResourceFactory reactorResourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);

        // Optimize connection pool for M3 Max's memory capacity
        ConnectionProvider provider = ConnectionProvider.builder("m3-connection-pool")
                .maxConnections(200000)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofMillis(50))
                .evictInBackground(Duration.ofSeconds(30))
                .build();

        factory.setConnectionProvider(provider);
        return factory;
    }
}