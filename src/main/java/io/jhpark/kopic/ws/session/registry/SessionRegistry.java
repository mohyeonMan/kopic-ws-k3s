package io.jhpark.kopic.ws.session.registry;

import java.time.Instant;
import java.util.Optional;

import io.jhpark.kopic.ws.session.domain.WsSession;

public interface SessionRegistry {

	Optional<WsSession> findBySessionId(String sessionId);

	void save(WsSession session);

	Optional<WsSession> touch(String sessionId, Instant touchedAt);

	void remove(String sessionId);

}
