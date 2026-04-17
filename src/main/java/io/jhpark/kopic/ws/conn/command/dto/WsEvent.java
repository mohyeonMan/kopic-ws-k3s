package io.jhpark.kopic.ws.conn.command.dto;


import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;

public record WsEvent(
    String senderSessionId,
    String wsNodeId,
    String roomId,
    KopicEnvelope envelope,
    String sentAt
) {
}
