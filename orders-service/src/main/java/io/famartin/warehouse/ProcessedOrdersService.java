package io.famartin.warehouse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.amqp.AmqpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.amqp.AmqpClient;
import io.vertx.mutiny.amqp.AmqpMessage;
import io.vertx.mutiny.amqp.AmqpSender;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class ProcessedOrdersService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String PROCESSED_ORDERS_ADDRESS = "processed-orders";

    private AmqpClient client;
    private AmqpSender sender;

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
        sender = client.createSenderAndAwait(PROCESSED_ORDERS_ADDRESS);
    }

    public void orderPrcessed(JsonObject order) {
        sender.send(AmqpMessage.create()
            .withJsonObjectAsBody(order)
            .build());
    }

}