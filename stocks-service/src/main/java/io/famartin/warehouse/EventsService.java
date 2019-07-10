package io.famartin.warehouse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.Stream;
import io.vertx.core.json.JsonObject;

/**
 * EventsService
 */
@ApplicationScoped
public class EventsService {

    private String serviceName = Optional.ofNullable(System.getenv("POD_NAME"))
            .orElseGet(() -> UUID.randomUUID().toString());
    
    @Inject
    @Stream("events")
    Emitter<JsonObject> events;

    public void sendEvent(String event) {
        JsonObject msg = new JsonObject();
        msg.put("timestamp", Instant.now().toString());
        msg.put("from", serviceName);
        msg.put("event", event);
        events.send(msg);
    }

}