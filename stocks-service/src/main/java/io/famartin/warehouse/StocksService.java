package io.famartin.warehouse;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * StocksService
 */
@Singleton
public class StocksService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String STOCKS_ADDRESS = "stocks";

    @Inject
    EventsService events;

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "amqp-host")
    String amqpHost;

    @ConfigProperty(name = "amqp-port")
    int amqpPort;

    @ConfigProperty(name = "amqp-username")
    String amqpUsername;

    @ConfigProperty(name = "amqp-password")
    String amqpPassword;

    public void init(@Observes StartupEvent ev) throws InterruptedException, ExecutionException, TimeoutException {
        AmqpClient client = AmqpClient.create(vertx, new AmqpClientOptions().setSsl(false).setHost(amqpHost)
                .setPort(amqpPort).setUsername(amqpUsername).setPassword(amqpPassword));
        listen(client);
    }

    private void listen(AmqpClient client) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        client.connect(ar -> {
            if(ar.succeeded()) {
                AmqpConnection connection = ar.result();
                connection.createAnonymousSender(responseSender -> {
                    connection.createReceiver(STOCKS_ADDRESS, conn -> {
                        if (conn.succeeded()) {
                            conn.result().handler(msg ->{
                                AmqpMessage response = AmqpMessage.create()
                                .address(msg.replyTo())
                                .correlationId(msg.id()) // send the message id as correlation id
                                .withJsonObjectAsBody(processStockRequests(msg.bodyAsJsonObject()))
                                .build();
                                responseSender.result().sendWithAck(response, ack -> {
                                    if(ack.failed()) {
                                        logger.error("Stock response rejected", ack.cause());
                                    }
                                });
                            });
                            future.complete(null);
                        } else {
                            future.completeExceptionally(conn.cause());
                        }
                    });
                });
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        future.get(15, TimeUnit.SECONDS);
        logger.info("Stocks listener connected");
    }

    private ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    private JsonObject processStockRequests(JsonObject request) {
        if(request.containsKey("action") && request.containsKey("item-id") && request.containsKey("quantity") && request.getInteger("quantity")!=null && request.getInteger("quantity")>0) {
            String action = request.getString("action");
            String itemId = request.getString("item-id");
            int quantity = request.getInteger("quantity");
            try{
                JsonObject response = new JsonObject();
                switch (StockAction.valueOf(action)) {
                    case ADD:
                        Integer newStock = stock.compute(itemId, (id, currentStock) -> {
                            if ( currentStock == null ) {
                                return quantity;
                            } else {
                                return currentStock + quantity;
                            }
                        });
                        events.sendEvent("Stock updated, item: "+itemId+" quantity: "+newStock);
                        break;
                    case SUBSTRACT:
                        Integer result = stock.computeIfPresent(itemId, (id, currentStock) -> {
                            if(currentStock >= quantity) {
                                response.put("approved", true);
                                return currentStock - quantity;
                            } else {
                                response.put("approved", false);
                                response.put("message", "Stock request exceeded current stock").put("original-request", request);
                                return currentStock;
                            }
                        });
                        if (result == null) {
                            response.put("approved", false);
                            response.put("message", "There is not stock for that item").put("original-request", request);
                        } else if (result == 0) {
                            events.sendEvent("Item "+itemId+" ran out of stock");
                        } else {
                            events.sendEvent("Stock updated, item: "+itemId+" quantity: "+result);
                        }
                        break;
                }
                response.put("timestamp", Instant.now().toString());
                return response;
            } catch (IllegalArgumentException e) {
                return new JsonObject().put("error", String.format("Bad request, action %s not exists", action)).put("original-request", request);
            }
        } else {
            return new JsonObject().put("error", "Bad request, bad fields").put("original-request", request);
        }
    }

}