package ru.filenko.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notification {
    private String sessionId;
    private String message;
}
