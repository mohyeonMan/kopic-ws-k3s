package io.jhpark.kopic.ws.conn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kopic.ws")
public record WsConnProperties(
    String endpoint,
	int maxTextMessageSize
) {

    public WsConnProperties {
		endpoint = endpoint == null || endpoint.isBlank() ? "/ws" : endpoint;
		maxTextMessageSize = maxTextMessageSize <= 0 ? 8192 : maxTextMessageSize;
	}
    
}
