package io.jhpark.kopic.ws.conn.command.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import io.jhpark.kopic.ws.common.metrics.WsMetrics;
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
    private final WsMetrics wsMetrics;

    public void dispatch(String sessionId, String roomId, String targetGeId, String message) {
        KopicEnvelope envelope = extractEnvelope(message);
        if (envelope == null) {
            log.warn("Failed to parse message: {}", message);
            wsMetrics.increment(
                "kopic_ws_client_inbound_rejected_total",
                "reason",
                "invalid_envelope"
            );
            return;
        }

        wsMetrics.increment(
            "kopic_ws_client_inbound_events_total",
            "event_code",
            String.valueOf(envelope.e())
        );
        log.debug("Dispatching event: {}", envelope);
        String handlerLabel = envelope.e() == 1 ? "ping" : "default";
        long startedAtNanos = System.nanoTime();
        try {
            if(envelope.e() == 1) { // PING
                pingEventHandler.handle(sessionId, roomId, targetGeId, envelope);
            } else {
                defaultEventHandler.handle(sessionId, roomId, targetGeId, envelope);
            }
        } finally {
            wsMetrics.recordDuration(
                "kopic_ws_dispatch_duration_seconds",
                System.nanoTime() - startedAtNanos,
                "handler",
                handlerLabel
            );
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
        JsonNode payloadNode = root.has("p") && !root.get("p").isNull() ? root.get("p") : null;

        return new KopicEnvelope(eventCode, payloadNode/* , requestId */);
    }

}
