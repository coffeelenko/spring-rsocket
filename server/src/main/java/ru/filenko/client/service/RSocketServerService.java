package ru.filenko.client.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import ru.filenko.client.model.Notification;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class RSocketServerService {
    private static final String WELCOME_TEMPLATE = "Welcome to RSocket: %s";
    private final Queue<String> newClientsQueue = new ConcurrentLinkedQueue<>();
    @Getter
    private final Sinks.Many<Notification> sinks = Sinks.many().multicast().onBackpressureBuffer();
    @Getter
    private final List<String> clients = new CopyOnWriteArrayList<>();

    public Mono<Void> registerClient(RSocketRequester requester, String clientId) {
        newClientsQueue.add(clientId);
        clients.add(clientId);

        return requester.rsocket()
                .onClose()
                .doFirst(() -> log.info("Client connected: {}", clientId))
                .doOnError(error -> log.error("Connection error for client {}: {}", clientId, error.getMessage()))
                .doFinally(termination -> {
                    log.info("Client {} disconnected", clientId);
                    clients.remove(clientId);
                })
                .then();
    }

    public Flux<String> getBroadcastFlux(String clientId) {
        Flux<String> welcomeFlux = Flux.defer(() -> {
            if (newClientsQueue.remove(clientId)) {
                return Flux.just(String.format(WELCOME_TEMPLATE, clientId));
            }
            return Flux.empty();
        });

        Flux<String> mainFlux = sinks.asFlux()
                .filter(e -> isNull(e.getSessionId()) || e.getSessionId().equals(clientId))
                .map(Notification::getMessage);

        return welcomeFlux
                .concatWith(mainFlux);
    }
}
