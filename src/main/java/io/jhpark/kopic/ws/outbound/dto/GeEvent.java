package io.jhpark.kopic.ws.outbound.dto;

import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;

public record GeEvent(
    String targetSessionId,
    KopicEnvelope envelope,
    String sentAt
) {
}