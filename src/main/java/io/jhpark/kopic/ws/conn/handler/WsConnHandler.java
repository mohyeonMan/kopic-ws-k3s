package io.jhpark.kopic.ws.conn.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import io.jhpark.kopic.ws.conn.command.handler.WsEventDispatcher;
import io.jhpark.kopic.ws.conn.config.WsConnProperties;
import io.jhpark.kopic.ws.conn.interceptor.MetadataInterceptor;
import io.jhpark.kopic.ws.session.domain.WsSession;
import io.jhpark.kopic.ws.session.registry.SessionRegistry;

@Component
@Slf4j
@RequiredArgsConstructor
public class WsConnHandler extends TextWebSocketHandler {

    private final WsConnProperties wsConnProperties;
    private final SessionRegistry sessionRegistry;
    private final WsEventDispatcher wsEventDispatcher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(wsConnProperties.maxTextMessageSize());

        sessionRegistry.save(
            new WsSession(
                session,
                session.getId(),
                requireTextAttribute(session, MetadataInterceptor.ATTR_GE_ID),
                requireTextAttribute(session, MetadataInterceptor.ATTR_ROOM_ID),
                Instant.now(),
                Instant.now()
            )
        );

        log.info("connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("message received: {} -> {}", session.getId(), message.getPayload());
        sessionRegistry.touch(session.getId(), Instant.now())
            .ifPresentOrElse(
                touchedSession -> {
                    log.debug("Session touched: {}", touchedSession.getSessionId());
                    wsEventDispatcher.dispatch(
                        touchedSession.getSessionId(),
                        touchedSession.getRoomId(),
                        touchedSession.getGeId(),
                        message.getPayload()
                    );
                },
                () -> log.warn("Received message for unknown session: {}", session.getId())
            );

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("connection closed: {}", session.getId());
        sessionRegistry.remove(session.getId());
    }

    private String requireTextAttribute(WebSocketSession session, String attributeName) {
		Object value = session.getAttributes().get(attributeName);
		if (value instanceof String text && !text.isBlank()) {
			return text;
		}
		throw new IllegalStateException("missing required handshake attribute: " + attributeName);
	}

    
}
