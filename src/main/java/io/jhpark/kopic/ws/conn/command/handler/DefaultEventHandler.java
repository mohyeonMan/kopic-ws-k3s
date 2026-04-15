package io.jhpark.kopic.ws.conn.command.handler;

import org.springframework.stereotype.Component;

import io.jhpark.kopic.ws.common.util.TimeFormatUtil;
import io.jhpark.kopic.ws.conn.command.dto.WsEvent;
import io.jhpark.kopic.ws.conn.domain.KopicEnvelope;
import io.jhpark.kopic.ws.conn.handler.WsEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultEventHandler implements WsEventHandler {

    private final WsEventPublisher wsEventPublisher;

    @Override
    public int supports() {
        return 0;
    }

    @Override
    public void handle(String sessionId, String targetGeId, KopicEnvelope envelope) {
        log.debug("Handling default event with payload: {}", envelope.p());
        wsEventPublisher.publish(targetGeId, new WsEvent(sessionId, envelope, TimeFormatUtil.now()));
    }

}
