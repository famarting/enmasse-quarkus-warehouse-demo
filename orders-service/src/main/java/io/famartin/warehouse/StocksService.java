package io.famartin.warehouse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * StocksService
 */
@ApplicationScoped
public class StocksService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String STOCKS_ADDRESS = "stocks";

    private AmqpClient client;

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

    @PostConstruct
    public void init() {
        client = AmqpClient.create(vertx, new AmqpClientOptions()
            .setSsl(false)
            .setHost(amqpHost)
            .setPort(amqpPort)
            .setUsername(amqpUsername)
            .setPassword(amqpPassword));
    }

    public CompletionStage<JsonObject> requestStock(String requestId, String itemId, int quantity) {
        return send(requestId, itemId, quantity, "SUBSTRACT");
    }
 
    private CompletionStage<JsonObject> send(String requestId, String itemId, int quantity, String action) {
        JsonObject body = new JsonObject();
        body.put("item-id", itemId);
        body.put("quantity", quantity);
        body.put("action", action);
        CompletableFuture<JsonObject> stage = new CompletableFuture<>();
        client.connect(ar -> {
            if(ar.succeeded()) {
                AmqpConnection connection = ar.result();
                connection.createDynamicReceiver(replyReceiver -> {
                    if(replyReceiver.succeeded()){
                        String replyToAddress = replyReceiver.result().address();
                        replyReceiver.result().handler(msg -> {
                            stage.complete(msg.bodyAsJsonObject());
                            connection.close(Future.succeededFuture());
                        });
                        replyReceiver.result().exceptionHandler(ex -> {
                            logger.error("Error in stocks service ", ex);
                            stage.completeExceptionally(ex);
                            connection.close(Future.succeededFuture());
                        });
                        connection.createSender(STOCKS_ADDRESS, sender -> {
                            if(sender.succeeded()) {
                                AmqpMessage requestMessage = AmqpMessage.create()
                                        .replyTo(replyToAddress)
                                        .id(requestId)
                                        .withJsonObjectAsBody(body)
                                        .build();
                                sender.result().send(requestMessage);
                            } else {
                                logger.error("Request to stocks-service returned error", sender.cause());
                                stage.completeExceptionally(sender.cause());
                                connection.close(Future.succeededFuture());
                            }
                        });
                    } else {
                        logger.error("Request to stocks-service returned error", replyReceiver.cause());
                        stage.completeExceptionally(replyReceiver.cause());
                        connection.close(Future.succeededFuture());
                    }
                });
            } else {
                logger.error("Request to stocks-service returned error", ar.cause());
                stage.completeExceptionally(ar.cause());
            }
        });
        return stage;
    }

}