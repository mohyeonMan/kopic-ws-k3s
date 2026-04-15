package io.jhpark.kopic.ws.conn.command.handler;

import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;

public interface WsEventHandler {
    
    int supports();

    void handle(String sessionId, String targetGeId, KopicEnvelope envelope);

}
