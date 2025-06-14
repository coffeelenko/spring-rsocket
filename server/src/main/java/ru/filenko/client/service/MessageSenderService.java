package ru.filenko.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.filenko.client.model.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSenderService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RSocketServerService rsServer;
    private int count = 0;

    @Scheduled(fixedDelay = 10000, initialDelay = 20000)
    public void scheduleFixedDelayTask() {
        String clientId;
        String toClient;
        if(++count % 5 == 0) {
            clientId = null;
            toClient = "all";
        } else {
            clientId = getRandomElementGeneric(rsServer.getClients());
            toClient = clientId;
        }


        String message = LocalDateTime.now().format(FORMATTER);

        rsServer.getSinks().tryEmitNext(new Notification(clientId, message));
        System.out.println("The \"" + message + "\" message was sent to the client: " + toClient);
    }

    public static <T> T getRandomElementGeneric(List<T> list) {
        if (list == null || list.isEmpty())
            return null;

        return list.get(new Random().nextInt(list.size()));
    }
}
