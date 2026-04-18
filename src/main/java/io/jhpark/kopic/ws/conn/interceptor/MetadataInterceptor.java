package io.jhpark.kopic.ws.conn.interceptor;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    
    public static final String ATTR_ROOM_CODE = "roomId";
    public static final String ATTR_GE_ID = "geId";
	public static final String ATTR_NICKNAME = "nickname";
	public static final String ATTR_ACTION = "action";

    
    @Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes
	) {
		URI uri = request.getURI();
		var queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
		String geId;
		String roomCode;
		String nickname;
		int action;
		try {
			geId = decodeQueryParam(queryParams.getFirst("geId"));
			roomCode = decodeQueryParam(queryParams.getFirst("roomCode"));
			nickname = decodeQueryParam(queryParams.getFirst("nickname"));
			action = parseAction(decodeQueryParam(queryParams.getFirst("action")));
		} catch (IllegalArgumentException e) {
			log.warn("ws handshake rejected invalid query params: {}", e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return false;
		}

		if (geId == null || geId.isBlank() || nickname == null || nickname.isBlank()) {
			log.warn("ws handshake rejected missing query params geId={} nickname={}", geId, nickname);
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return false;
		}

		log.info("ws handshake accepted geId={} roomCode={}", geId, roomCode);
		attributes.put(ATTR_ROOM_CODE, roomCode);
		attributes.put(ATTR_GE_ID, geId);
		attributes.put(ATTR_NICKNAME, nickname);
		attributes.put(ATTR_ACTION, action);
		return true;
	}

	private String decodeQueryParam(String value) {
		if (value == null) {
			return null;
		}
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("invalid url encoding: " + value, e);
		}
	}

	private int parseAction(String value) {
		if (value == null || value.isBlank()) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid action: " + value, e);
		}
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
