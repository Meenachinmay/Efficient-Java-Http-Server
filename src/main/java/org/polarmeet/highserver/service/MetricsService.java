package org.polarmeet.highserver.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final Counter successfulRequests;
    private final Counter failedRequests;
    private final Timer requestLatency;
    private final AtomicInteger concurrentRequests;

    // Constructor with MeterRegistry injection
    public MetricsService(MeterRegistry registry) {
        this.successfulRequests = Counter.builder("http.requests.successful")
                .description("Number of successful HTTP requests")
                .register(registry);

        this.failedRequests = Counter.builder("http.requests.failed")
                .description("Number of failed HTTP requests")
                .register(registry);

        this.requestLatency = Timer.builder("http.request.latency")
                .description("HTTP request latency")
                .register(registry);

        this.concurrentRequests = registry.gauge("http.requests.concurrent",
                new AtomicInteger(0));
    }

    public void recordSuccessfulRequest(long timeInMs) {
        successfulRequests.increment();
        requestLatency.record(java.time.Duration.ofMillis(timeInMs));
        logger.info("Request processed successfully in {}ms", timeInMs);
    }

    public void recordFailedRequest(String reason) {
        failedRequests.increment();
        logger.error("Request failed: {}", reason);
    }

    public void incrementConcurrentRequests() {
        concurrentRequests.incrementAndGet();
    }

    public void decrementConcurrentRequests() {
        concurrentRequests.decrementAndGet();
    }
}
