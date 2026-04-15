package io.jhpark.kopic.ws.conn.handler;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import io.jhpark.kopic.ws.common.config.RabbitProperties;
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

    public void publish(String targetGeId, WsEvent event) {
        String exchange = rabbitProperties.outboundExchange();

        log.info(
            "Published ws event senderId={} exchange={} targetGeId={} envelope={}",
            event.senderId(),
            exchange,
            targetGeId,
            event.envelope()
        );
        // rabbitTemplate.convertAndSend(exchange, targetGeId, commonMapper.write(event));
    }

}
