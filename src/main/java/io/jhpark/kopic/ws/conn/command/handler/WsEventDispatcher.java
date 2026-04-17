package io.jhpark.kopic.ws.conn.command.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import io.jhpark.kopic.ws.common.util.CommonMapper;
import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WsEventDispatcher {
    
    private final PingHandler pingEventHandler;
    private final DefaultEventHandler defaultEventHandler;
    private final CommonMapper commonMapper;

    public void dispatch(String sessionId, String roomId, String targetGeId, String message) {
        KopicEnvelope envelope = extractEnvelope(message);
        if (envelope == null) {
            log.warn("Failed to parse message: {}", message);
            return;
        }

        log.debug("Dispatching event: {}", envelope);

        if(envelope.e() == 1) { // PING
            pingEventHandler.handle(sessionId, roomId, targetGeId, envelope);
        } else {
            defaultEventHandler.handle(sessionId, roomId, targetGeId, envelope);
        }

    }

    private KopicEnvelope extractEnvelope(String payload) {
        JsonNode root = commonMapper.readTree(payload);
        if (root == null || !root.hasNonNull("e")) {
            return null;
        }

        JsonNode eventNode = root.get("e");
        if (!eventNode.canConvertToInt()) {
            return null;
        }

        int eventCode = eventNode.intValue();
        String payloadJson = root.has("p") && !root.get("p").isNull() ? root.get("p").toString() : null;
        // String requestId = root.hasNonNull("rid") ? root.get("rid").asText() : null;

        return new KopicEnvelope(eventCode, payloadJson/* , requestId */);
    }

}
