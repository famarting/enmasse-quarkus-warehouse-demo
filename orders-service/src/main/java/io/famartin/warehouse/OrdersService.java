package io.famartin.warehouse;

import java.util.Random;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.Stream;
import io.vertx.core.json.JsonObject;

/**
 * OrdersService
 */
@ApplicationScoped
public class OrdersService {

    private Random random = new Random();

    @Inject
    EventsService events;

    @Inject
    StocksService stocks;

    @Inject
    @Stream("processed-orders")
    Emitter<JsonObject>  processedOrders;

    @Incoming("orders")
    public CompletionStage<?> processOrder(JsonObject order) {
        events.sendEvent("Processing order "+order.getString("order-id"));
        longRunningOperation();
        CompletionStage<JsonObject> response = stocks.requestStock(order.getString("order-id"),
                order.getString("item-id"), order.getInteger("quantity"));
        return response.thenAccept(result -> {

            order.put("processingTimestamp", result.getString("timestamp"));
            order.put("processedBy", EventsService.SERVICE_NAME);

            if (result.containsKey("error")) {
                order.put("error", result.getString("error"));
            } else if (result.getBoolean("approved", false)) {
                order.put("approved", true);
            } else {
                order.put("approved", false);
                order.put("reason", result.getString("message"));
            }
            processedOrders.send(order);
            events.sendEvent("Order " + order.getString("order-id") + " processed");

        });
    }

    private void longRunningOperation() {
        try {
            Thread.sleep(random.nextInt(3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}