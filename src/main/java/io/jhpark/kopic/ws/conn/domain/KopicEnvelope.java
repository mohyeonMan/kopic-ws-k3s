package io.jhpark.kopic.ws.conn.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record KopicEnvelope(
    int e,
    JsonNode p
    // String rid
) {
}
