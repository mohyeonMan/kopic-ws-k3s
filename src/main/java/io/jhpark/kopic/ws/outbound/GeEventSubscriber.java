package io.jhpark.kopic.ws.outbound;

import org.springframework.stereotype.Component;

import io.jhpark.kopic.ws.common.metrics.WsMetrics;
import io.jhpark.kopic.ws.common.util.CommonMapper;
import io.jhpark.kopic.ws.outbound.dto.GeEvent;
import io.jhpark.kopic.ws.outbound.handler.GeEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeEventSubscriber {
    
    private final GeEventHandler geEventHandler;
	private final CommonMapper commonMapper;
	private final WsMetrics wsMetrics;

	public void handle(GeEvent event) {
		if (event == null) {
			wsMetrics.increment(
				"kopic_ws_ge_inbound_rejected_total",
				"reason",
				"missing_event"
			);
			return;
		}
		if (event.envelope() == null) {
			wsMetrics.increment(
				"kopic_ws_ge_inbound_rejected_total",
				"reason",
				"missing_envelope"
			);
			return;
		}
		wsMetrics.increment(
			"kopic_ws_ge_inbound_events_total",
			"event_code",
			String.valueOf(event.envelope().e())
		);
		geEventHandler.handle(event);
	}

    @RabbitListener(
		queues = "#{@rabbitNodeQueue.name}",
		containerFactory = "rabbitListenerContainerFactory"
	)
	public void receive(String payload) {
		log.debug("Received message from RabbitMQ: {}", payload);
		GeEvent event = commonMapper.read(payload, GeEvent.class);
		if (event == null) {
			log.warn("Dropping non-JSON or unmappable RabbitMQ payload: {}", payload);
			wsMetrics.increment(
				"kopic_ws_ge_inbound_rejected_total",
				"reason",
				"deserialize_failed"
			);
			return;
		}
		handle(event);
	}

}
