package io.jhpark.kopic.ws.session.domain;

import java.time.Instant;

import org.springframework.web.socket.WebSocketSession;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WsSession {

	private WebSocketSession session;
	private String sessionId;
	private String nickname;
    private String geId;
	private String roomId;
	private Instant connectedAt;
	private Instant lastSeenAt;

	@Override
	public String toString() {
		return "WsSession{" +
			"sessionId='" + sessionId + '\'' +
			", nickname='" + nickname + '\'' +
			", geId='" + geId + '\'' +
			", roomId='" + roomId + '\'' +
			", connectedAt=" + connectedAt +
			", lastSeenAt=" + lastSeenAt +
			'}';
	}
}