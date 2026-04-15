package io.jhpark.kopic.ws.conn.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import io.jhpark.kopic.ws.conn.handler.WsConnHandler;
import io.jhpark.kopic.ws.conn.interceptor.MetadataInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@EnableConfigurationProperties(WsConnProperties.class)
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketConfigurer {

    private final WsConnProperties wsConnProperties;
    private final WsConnHandler wsConnHandler;
	private final MetadataInterceptor metadataInterceptor;
    
    @Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(wsConnHandler, wsConnProperties.endpoint())
			.addInterceptors(metadataInterceptor)
			.setAllowedOriginPatterns("*");
	}

}
