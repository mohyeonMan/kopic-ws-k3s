package io.jhpark.kopic.ws.conn.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jhpark.kopic.ws.common.util.CommonMapper;
import io.jhpark.kopic.ws.conn.command.handler.WsEventDispatcher;
import io.jhpark.kopic.ws.conn.config.WsConnProperties;
import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;
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
    private final CommonMapper commonMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(wsConnProperties.maxTextMessageSize());

        WsSession wsSession = new WsSession(
            session,
            "sid_"+session.getId().substring(0, 8),
            requireTextAttribute(session, MetadataInterceptor.ATTR_NICKNAME),
            requireTextAttribute(session, MetadataInterceptor.ATTR_GE_ID),
            null, // join_accepted가 오기전엔 없어야함.
            Instant.now(),
            Instant.now()
        );

        sessionRegistry.save(wsSession);

        
        
        ObjectNode payload = commonMapper.rawMapper().createObjectNode()
            .put("nickname", wsSession.getNickname());
        String roomCode = getTextAttribute(session, MetadataInterceptor.ATTR_ROOM_CODE);
        int action = getIntAttribute(session, MetadataInterceptor.ATTR_ACTION, 0);

        if(roomCode != null) {
            payload.put("roomCode", roomCode);
        }

        int e = action == 0 ? 101 : 103;

        wsEventDispatcher.dispatch(
            wsSession.getSessionId(),
            null,
            wsSession.getGeId(),
            commonMapper.write(
                new KopicEnvelope(e, 
                payload
            ))
        );
        
        // log.info("connection established: {}", session.getId());
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
        sessionRegistry.touch(session.getId(), Instant.now())
            .ifPresentOrElse(
                wsSession -> {
                    if(wsSession.getRoomId() != null) {
                        wsEventDispatcher.dispatch(
                            wsSession.getSessionId(),
                            wsSession.getRoomId(),
                            wsSession.getGeId(),
                            commonMapper.write(
                                new KopicEnvelope(102, 
                                null
                            ))
                        );
                    }
                    sessionRegistry.remove(wsSession.getSessionId());
                },
                () -> log.warn("Connection closed for unknown session: {}", session.getId())
            );
        // log.info("connection closed: {}", session.getId());
    }

    private String requireTextAttribute(WebSocketSession session, String attributeName) {
		String value = getTextAttribute(session, attributeName);
		if (value == null) {
			throw new IllegalStateException("missing required handshake attribute: " + attributeName);
		}
		return value;
	}

    private String getTextAttribute(WebSocketSession session, String attributeName) {
        Object value = session.getAttributes().get(attributeName);
        if (value instanceof String text) {
            return text;
        }
        return null;
    }

    private int getIntAttribute(WebSocketSession session, String attributeName, int defaultValue) {
        Object value = session.getAttributes().get(attributeName);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                log.warn("Invalid int attribute {}='{}', defaulting to {}", attributeName, text, defaultValue);
                return defaultValue;
            }
        }
        log.warn("Invalid attribute type {}={} defaulting to {}", attributeName, value.getClass().getSimpleName(), defaultValue);
        return defaultValue;
    }

    
}
