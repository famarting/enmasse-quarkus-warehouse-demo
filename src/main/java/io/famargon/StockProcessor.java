package io.famargon;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.vertx.core.json.JsonObject;

/**
 * OrdersProcessor
 */
@Singleton
public class StockProcessor {

    private String processorName = Optional.ofNullable(System.getenv("POD_NAME")).orElseGet(()->UUID.randomUUID().toString());

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Broadcast
    @Incoming("stock-moves-input")
    @Outgoing("stock-changelog-output")
    public JsonObject receive(JsonObject json) {
        logger.info("Stock received... {}", json);
        StockMove move = json.mapTo(StockMove.class);
        JsonObject result = new JsonObject();
        result.put("processedBy", processorName);
        result.put("item", move.getItem());
        result.put("quantity", move.getQuantity());
        result.put("approved", true);
        return result;
    }


}