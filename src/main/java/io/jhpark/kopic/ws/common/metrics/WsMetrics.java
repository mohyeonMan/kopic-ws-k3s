package io.jhpark.kopic.ws.common.metrics;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.jhpark.kopic.ws.session.registry.SessionRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class WsMetrics {

	private static final String UNKNOWN = "unknown";

	private final MeterRegistry meterRegistry;

	public WsMetrics(MeterRegistry meterRegistry, SessionRegistry sessionRegistry) {
		this.meterRegistry = meterRegistry;
		registerSessionGauges(sessionRegistry);
	}

	public void increment(String metricName, String... rawLabelPairs) {
		increment(metricName, 1.0, rawLabelPairs);
	}

	public void increment(String metricName, double amount, String... rawLabelPairs) {
		if (metricName == null || metricName.isBlank() || amount <= 0) {
			return;
		}
		String[] tags = normalizeRawTags(rawLabelPairs);
		meterRegistry.counter(metricName, tags).increment(amount);
	}

	public void recordDuration(String metricName, long durationNanos, String... rawLabelPairs) {
		if (metricName == null || metricName.isBlank() || durationNanos < 0) {
			return;
		}
		String[] tags = normalizeRawTags(rawLabelPairs);
		meterRegistry.timer(metricName, tags).record(durationNanos, TimeUnit.NANOSECONDS);
	}

	private void registerSessionGauges(SessionRegistry sessionRegistry) {
		Gauge.builder("kopic_ws_sessions_active", sessionRegistry::countActiveSessions)
			.description("number of active websocket sessions")
			.register(meterRegistry);
		Gauge.builder("kopic_ws_sessions_in_room", sessionRegistry::countSessionsInRoom)
			.description("number of websocket sessions that joined a room")
			.register(meterRegistry);
	}

	private String[] normalizeRawTags(String... rawLabelPairs) {
		if (rawLabelPairs == null || rawLabelPairs.length == 0) {
			return new String[0];
		}
		ArrayList<String> tags = new ArrayList<>(rawLabelPairs.length);
		for (int index = 0; index + 1 < rawLabelPairs.length; index += 2) {
			String key = rawLabelPairs[index];
			String value = rawLabelPairs[index + 1];
			if (key == null || key.isBlank()) {
				continue;
			}
			tags.add(key.trim());
			tags.add(normalizeRawLabelValue(value));
		}
		return tags.toArray(new String[0]);
	}

	private String normalizeRawLabelValue(String value) {
		if (value == null || value.isBlank()) {
			return UNKNOWN;
		}
		return value.trim();
	}
}
