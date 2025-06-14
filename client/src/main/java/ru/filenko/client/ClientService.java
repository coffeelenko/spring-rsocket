package ru.filenko.client;

import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
public class ClientService {

    private final RSocketRequester requester;

    private final UUID SESSION_ID = UUID.randomUUID();

    public ClientService(RSocketRequester.Builder builder) {
        this.requester = builder
                .setupData(DefaultPayload.create(SESSION_ID.toString()))
                .websocket(URI.create("ws://localhost:7000/rsocket"));

        connect();
    }

    public void connect() {
        log.info("Client connected : {}", SESSION_ID);

        // Sending a connection request
        requester.route("connect")
                .data(SESSION_ID.toString())
                .send()
                .subscribe();

        // Subscribe to broadcast.stream with our clientId
        requester.route("broadcast.stream")
                .data(SESSION_ID.toString())
                .retrieveFlux(String.class)
                .subscribe(message -> log.info("Received message: {}", message));
    }
}
