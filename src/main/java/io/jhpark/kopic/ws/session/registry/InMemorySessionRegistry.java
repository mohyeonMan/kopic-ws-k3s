package io.jhpark.kopic.ws.session.registry;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.jhpark.kopic.ws.session.domain.WsSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InMemorySessionRegistry implements SessionRegistry {

	private final ConcurrentHashMap<String, WsSession> sessionsBySessionId = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, String> sessionIdByWebSocketSessionId = new ConcurrentHashMap<>();

	@Override
	public Optional<WsSession> findBySessionId(String sessionId) {
		return Optional.ofNullable(sessionsBySessionId.get(sessionId));
	}

	@Override
	public void save(WsSession session) {
		sessionsBySessionId.put(session.getSessionId(), session);
		sessionIdByWebSocketSessionId.put(session.getSession().getId(), session.getSessionId());
		log.info("Session saved: {}", session.toString());
	}

	@Override
	public Optional<WsSession> touch(String webSocketSessionId, Instant touchedAt) {
		return findByWebSocketSessionId(webSocketSessionId).map(session -> {
			session.setLastSeenAt(touchedAt);
			log.debug("Session touched: {}", session.getSessionId());
			return session;
		});
	}

	@Override
	public void remove(String sessionId) {
		WsSession removedSession = sessionsBySessionId.remove(sessionId);
		if (removedSession != null) {
			sessionIdByWebSocketSessionId.remove(removedSession.getSession().getId());
		}
	}

	private Optional<WsSession> findByWebSocketSessionId(String webSocketSessionId) {
		String sessionId = sessionIdByWebSocketSessionId.get(webSocketSessionId);
		if (sessionId == null) {
			return Optional.empty();
		}
		return findBySessionId(sessionId);
	}
}
