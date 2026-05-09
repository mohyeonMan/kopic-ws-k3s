package io.jhpark.kopic.ws.conn.handler;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import io.jhpark.kopic.ws.common.config.RabbitProperties;
import io.jhpark.kopic.ws.common.metrics.WsMetrics;
import io.jhpark.kopic.ws.common.util.CommonMapper;
import io.jhpark.kopic.ws.conn.command.dto.WsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitProperties rabbitProperties;
    private final CommonMapper commonMapper;
    private final WsMetrics wsMetrics;

    public void publish(String targetGeId, WsEvent event) {
        if (isBlank(targetGeId) || event == null || isBlank(event.senderSessionId()) || event.envelope() == null) {
            log.warn("skip ws event push due to missing target. targetGeId={}, event={}", targetGeId, event);
            wsMetrics.increment(
                "kopic_ws_to_ge_publish_failures_total",
                "reason",
                "invalid_target"
            );
            return;
        }

        String body = commonMapper.write(event);
        if (body == null) {
            log.warn("skip ws event push due to serialization failure. targetGeId={}, event={}", targetGeId, event);
            wsMetrics.increment(
                "kopic_ws_to_ge_publish_failures_total",
                "reason",
                "serialize_failed"
            );
            return;
        }

        String exchange = rabbitProperties.outboundExchange();
        String routingKey = rabbitProperties.outboundRoutingKey(targetGeId);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, body);
            wsMetrics.increment(
                "kopic_ws_to_ge_events_total",
                "event_code",
                String.valueOf(event.envelope().e())
            );
            log.debug(
                "Published ws event senderSessionId={} exchange={} targetGeId={} routingKey={} envelope={}",
                event.senderSessionId(),
                exchange,
                targetGeId,
                routingKey,
                event.envelope()
            );
        } catch (RuntimeException runtimeException) {
            wsMetrics.increment(
                "kopic_ws_to_ge_publish_failures_total",
                "reason",
                "publish_exception"
            );
            log.error(
                "Failed to publish ws event senderSessionId={} exchange={} targetGeId={} routingKey={} envelope={}",
                event.senderSessionId(),
                exchange,
                targetGeId,
                routingKey,
                event.envelope(),
                runtimeException
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
