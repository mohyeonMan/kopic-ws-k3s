package io.jhpark.kopic.ws.conn.command.dto;


import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;

public record WsEvent(
    String senderId,
    KopicEnvelope envelope,
    String sentAt
) {
}
