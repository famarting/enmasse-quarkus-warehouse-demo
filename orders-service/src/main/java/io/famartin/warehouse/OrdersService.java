package io.famartin.warehouse;

import java.util.Random;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

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
    @Channel("processed-orders")
    Emitter<JsonObject>  processedOrders;

    @Incoming("orders")
    public CompletionStage<Void> processOrder(JsonObject order) {
        events.sendEvent("Processing order "+order.getString("order-id"));
        return stocks.requestStock(order.getString("order-id"),
                order.getString("item-id"), order.getInteger("quantity"))
            .thenApplyAsync(result -> {
                longRunningOperation();
                return result;
            })
            .thenAccept(result -> {
                order.put("processingTimestamp", result.getString("timestamp"));
                order.put("processedBy", EventsService.SERVICE_NAME);
                if (result.containsKey("error")) {
                    order.put("error", result.getString("error"));
                    order.put("approved", false);
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