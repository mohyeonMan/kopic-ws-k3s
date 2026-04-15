package io.jhpark.kopic.ws.conn.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

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

    public void sendMessage(String sessionId, KopicEnvelope envelope) {
        String message = commonMapper.write(envelope);
        sessionRegistry.findBySessionId(sessionId)
            .ifPresentOrElse(
                wsSession -> {
                    try {
                        wsSession.getSession().sendMessage(new TextMessage(message));
                        log.debug("Sent message to session {}: {}", sessionId, message);
                    } catch (Exception e) {
                        log.error("Failed to send message to session {}: {}", sessionId, e.getMessage());
                    }
                },
                () -> log.warn("Attempted to send message to unknown session: {}", sessionId)
            );
        
    }

}
