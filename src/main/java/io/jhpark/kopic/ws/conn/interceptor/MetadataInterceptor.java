package io.jhpark.kopic.ws.conn.interceptor;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component  
public class MetadataInterceptor implements HandshakeInterceptor {
    
    public static final String ATTR_ROOM_ID = "roomId";
    public static final String ATTR_GE_ID = "geId";
    
    @Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes
	) {
        URI uri = request.getURI();
		var queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
		String geId = queryParams.getFirst("geId");
		String roomId = queryParams.getFirst("roomId");

		if (roomId == null || roomId.isBlank() || geId == null || geId.isBlank()) {
			log.warn("ws handshake rejected missing query params roomId={} geId={}", roomId, geId);
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return false;
		}

		log.info("ws handshake accepted geId={} roomId={}", geId, roomId);
		attributes.put(ATTR_ROOM_ID, roomId);
		attributes.put(ATTR_GE_ID, geId);
		return true;
    }

    @Override
	public void afterHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Exception exception
	) {
	}

}
