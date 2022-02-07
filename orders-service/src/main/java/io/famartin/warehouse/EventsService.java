package io.famartin.warehouse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.vertx.core.json.JsonObject;

/**
 * EventsService
 */
@ApplicationScoped
public class EventsService {

    public static final String SERVICE_NAME = Optional.ofNullable(System.getenv("POD_NAME"))
            .orElseGet(() -> UUID.randomUUID().toString());
    
    @Inject
    @Channel("events")
    Emitter<JsonObject> events;

    public void sendEvent(String event) {
        JsonObject msg = new JsonObject();
        msg.put("timestamp", Instant.now().toString());
        msg.put("from", SERVICE_NAME);
        msg.put("event", event);
        events.send(msg);
    }

}