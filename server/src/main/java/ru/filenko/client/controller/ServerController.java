package ru.filenko.client.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.filenko.client.service.RSocketServerService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ServerController {
    private final RSocketServerService RSocketServerService;

    @MessageMapping("connect")
    public Mono<Void> connect(RSocketRequester requester, @Payload String sessionId) {
        return RSocketServerService.registerClient(requester, sessionId);
    }

    @MessageMapping("broadcast.stream")
    public Flux<String> broadcastStream(String sessionId) {
        return RSocketServerService.getBroadcastFlux(sessionId);
    }
}
