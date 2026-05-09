package io.jhpark.kopic.ws.conn.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import io.jhpark.kopic.ws.common.metrics.WsMetrics;
import io.jhpark.kopic.ws.common.util.CommonMapper;
import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;
import io.jhpark.kopic.ws.session.registry.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WsMessageSender {
    
    private final CommonMapper commonMapper;
    private final SessionRegistry sessionRegistry;
    private final WsMetrics wsMetrics;

    public void sendMessage(String sessionId, KopicEnvelope envelope) {
        if (isBlank(sessionId) || envelope == null) {
            wsMetrics.increment(
                "kopic_ws_to_client_send_failures_total",
                "reason",
                "invalid_target"
            );
            return;
        }

        String message = commonMapper.write(envelope);
        if (message == null) {
            wsMetrics.increment(
                "kopic_ws_to_client_send_failures_total",
                "reason",
                "serialize_failed"
            );
            return;
        }

        sessionRegistry.findBySessionId(sessionId)
            .ifPresentOrElse(
                wsSession -> {
                    if (wsSession.getSession() == null || !wsSession.getSession().isOpen()) {
                        wsMetrics.increment(
                            "kopic_ws_to_client_send_failures_total",
                            "reason",
                            "session_closed"
                        );
                        log.warn("Attempted to send message to closed session: {}", sessionId);
                        return;
                    }
                    try {
                        wsSession.getSession().sendMessage(new TextMessage(message));
                        wsMetrics.increment(
                            "kopic_ws_to_client_events_total",
                            "event_code",
                            String.valueOf(envelope.e())
                        );
                        log.debug("Sent message to session {}: {}", sessionId, message);
                    } catch (Exception e) {
                        wsMetrics.increment(
                            "kopic_ws_to_client_send_failures_total",
                            "reason",
                            "send_exception"
                        );
                        log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
                    }
                },
                () -> {
                    wsMetrics.increment(
                        "kopic_ws_to_client_send_failures_total",
                        "reason",
                        "session_not_found"
                    );
                    log.warn("Attempted to send message to unknown session: {}, {}", sessionId, message);
                }
            );
        
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
