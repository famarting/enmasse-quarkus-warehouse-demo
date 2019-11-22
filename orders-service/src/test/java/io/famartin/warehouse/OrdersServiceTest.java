package io.famartin.warehouse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpMessage;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * OrdersServiceTest
 */
@QuarkusTest
public class OrdersServiceTest {

    private static EmbeddedActiveMQ broker;

    @BeforeAll
    static void start() throws Exception {
        broker = new EmbeddedActiveMQ();
        broker.start();
    }

    @AfterAll
    static void stop() throws Exception {
        broker.stop();
    }

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

    @Test
    void testProcessOrder() throws InterruptedException, ExecutionException, TimeoutException {
        AmqpClient client = AmqpClient.create(vertx, new AmqpClientOptions()
        .setSsl(false)
        .setHost(amqpHost)
        .setPort(amqpPort)
        .setUsername(amqpUsername)
        .setPassword(amqpPassword)
        .setConnectTimeout(1000));
        CompletableFuture fut = new CompletableFuture<>();
        client.createSender("orders", ar -> {
            if(ar.succeeded()) {
                ar.result().send(AmqpMessage.create()
                .withJsonObjectAsBody(new JsonObject().put("order-id", "asdfafs").put("item-id", "123456").put("quantity", 1))
                .build());
                fut.complete(null);
            } else {
                ar.cause().printStackTrace();
                fut.completeExceptionally(ar.cause());                
            }
        });
        fut.get(1000, TimeUnit.MILLISECONDS);
    }

}