package org.polarmeet.highserver.service;

import org.polarmeet.highserver.model.DemoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DemoService {
    private static final Logger logger = LoggerFactory.getLogger(DemoService.class);
    private final MetricsService metricsService;

    public DemoService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    // Add a counter to track progress
    private final AtomicInteger processedRequests = new AtomicInteger(0);
    private final AtomicLong startTime = new AtomicLong(0);

    // Process a single request with detailed monitoring and error handling
    public Mono<DemoEntity> processRequest(DemoEntity request) {

        // Track progress
        int current = processedRequests.incrementAndGet();
        if (current % 100_000 == 0) {
            long elapsed = System.currentTimeMillis() - startTime.get();
            logger.info("Processed {} requests in {} seconds",
                    current, elapsed/1000.0);
        }

        return Mono.just(request)
                // Validate the request
                .filter(this::validateRequest)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid request")))

                // Process the request
                .map(req -> {
                    // Simulate some processing work
                    try {
                        return new DemoEntity(
                                req.getId(),
                                "Processed: " + req.getContent()
                        );
                    } catch (Exception e) {
                        logger.error("Processing error for request {}: {}", req.getId(), e.getMessage());
                        throw e;
                    }
                })
                .subscribeOn(Schedulers.parallel());
    }

    public Flux<DemoEntity> processMultipleRequests(Flux<DemoEntity> requests) {
        return requests
                // Process in larger batches
                .window(20000)
                // Process each batch with high concurrency
                .flatMap(window -> window
                        .parallel(24)  // Use number of CPU cores
                        .runOn(Schedulers.parallel())
                        .flatMap(this::processRequest)
                        .sequential()
                );
    }

    // Validate incoming requests
    private boolean validateRequest(DemoEntity request) {
        if (request == null) return false;
        if (request.getId() == null || request.getId().trim().isEmpty()) return false;
        if (request.getContent() == null) return false;
        return true;
    }
}