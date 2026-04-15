package io.jhpark.kopic.ws.conn.command.handler;

import org.springframework.stereotype.Component;

import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;
import io.jhpark.kopic.ws.conn.handler.WsMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PingHandler implements WsEventHandler {

    private final WsMessageSender wsMessageSender;    
    
    @Override
    public int supports() {
        return 1; // PING 이벤트 코드
    }

    @Override
    public void handle(String sessionId, String targetGeId, KopicEnvelope envelope) {
        log.debug("Handling PING event for session {}: {}", sessionId, envelope);
        // PING 이벤트에 대한 처리 로직 (예: PONG 응답 전송)
        wsMessageSender.sendMessage(sessionId, new KopicEnvelope(2, null));
    }


}
