package io.jhpark.kopic.ws.outbound;

import org.springframework.stereotype.Component;

import io.jhpark.kopic.ws.common.util.CommonMapper;
import io.jhpark.kopic.ws.conn.handler.WsMessageSender;
import io.jhpark.kopic.ws.outbound.dto.GeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsEventSubscriber {
    
    private final WsMessageSender wsMessageSender;
	private final CommonMapper commonMapper;

	public void handle(GeEvent event) {
		log.info("Received event from RabbitMQ: {}", event);
		wsMessageSender.sendMessage(event.targetSessionId(), event.envelope());
	}

    @RabbitListener(
		queues = "#{@rabbitNodeQueue.name}",
		containerFactory = "rabbitListenerContainerFactory"
	)
	public void receive(String payload) {
		log.info("Received message from RabbitMQ: {}", payload);
		GeEvent event = commonMapper.read(payload, GeEvent.class);
		if (event == null) {
			log.warn("Dropping non-JSON or unmappable RabbitMQ payload: {}", payload);
			return;
		}
		handle(event);
	}

}
