package io.jhpark.kopic.ws.outbound.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import io.jhpark.kopic.ws.conn.handler.WsMessageSender;
import io.jhpark.kopic.ws.outbound.dto.GeEvent;
import io.jhpark.kopic.ws.session.registry.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeEventHandler {

	private final SessionRegistry sessionRegistry;
	private final WsMessageSender wsMessageSender;

	public void handle(GeEvent event) {
		if (event.envelope() == null) {
			log.warn("Dropping event with null envelope: {}", event);
			return;
		}

		switch (event.envelope().e()) {
			case 408 -> handleJoinAccepted(event);
			default -> handleDefault(event);
		}
	}

	private void handleJoinAccepted(GeEvent event) {
		JsonNode payload = event.envelope().p();
		String roomId = payload == null || payload.isNull() ? null : payload.path("rid").asText(null);

		if (roomId == null || roomId.isBlank()) {
			log.warn("408 event missing rid for targetSessionId={}", event.targetSessionId());
		} else {
			String resolvedRoomId = roomId;
			sessionRegistry.setRoomId(event.targetSessionId(), resolvedRoomId)
				.ifPresentOrElse(
					session -> log.info("Registered roomId={} for sessionId={}", resolvedRoomId, session.getSessionId()),
					() -> log.warn("Cannot register roomId, unknown targetSessionId={}", event.targetSessionId())
				);
		}
		wsMessageSender.sendMessage(event.targetSessionId(), event.envelope());
	}

	private void handleDefault(GeEvent event) {
		log.debug(
			"Handling default outbound event e={} targetSessionId={}",
			event.envelope().e(),
			event.targetSessionId()
		);
		wsMessageSender.sendMessage(event.targetSessionId(), event.envelope());
	}
}
