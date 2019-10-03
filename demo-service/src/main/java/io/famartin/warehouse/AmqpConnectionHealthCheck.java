package io.famartin.warehouse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.amqp.AmqpClientOptions;
import io.vertx.axle.amqp.AmqpConnection;

/**
 * OrdersServiceHealthCheck
 */
@Liveness
@Readiness
public class AmqpConnectionHealthCheck implements HealthCheck {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    io.vertx.axle.core.Vertx vertx;

    @ConfigProperty(name = "amqp-host")
    String amqpHost;

    @ConfigProperty(name = "amqp-port")
    int amqpPort;

    @ConfigProperty(name = "amqp-username")
    String amqpUsername;

    @ConfigProperty(name = "amqp-password")
    String amqpPassword;

    private io.vertx.axle.amqp.AmqpClient axleClient;

    @PostConstruct
    public void init() {
        axleClient = io.vertx.axle.amqp.AmqpClient.create(vertx, new AmqpClientOptions().setSsl(false).setHost(amqpHost)
                .setPort(amqpPort).setUsername(amqpUsername).setPassword(amqpPassword));
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("amqp-connection");
        try {
            AmqpConnection conn = axleClient.connect().toCompletableFuture().get(5, TimeUnit.SECONDS);
            conn.close();
            responseBuilder.up();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error in health check", e);
            responseBuilder.down();
        }
        return responseBuilder.build();
    }

    
}