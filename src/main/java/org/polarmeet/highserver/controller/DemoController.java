package org.polarmeet.highserver.controller;

import org.polarmeet.highserver.model.DemoEntity;
import org.polarmeet.highserver.service.DemoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final DemoService demoService;

    // Constructor injection is preferred over @Autowired
    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    // Handles single POST request and returns response status
    @PostMapping("/process")
    public Mono<HttpStatus> processSingleRequest(@RequestBody DemoEntity request) {
        return demoService.processRequest(request)
                .map(result -> HttpStatus.OK)
                .onErrorReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handles multiple requests in a batch
    @PostMapping("/process-batch")
    public Flux<HttpStatus> processMultipleRequests(@RequestBody Flux<DemoEntity> requests) {
        return demoService.processMultipleRequests(requests)
                .map(result -> HttpStatus.OK)
                .onErrorReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}