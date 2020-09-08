package io.famartin.warehouse;

import java.time.Duration;

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

/**
 * OrdersServiceHealthCheck
 */
@Liveness
@Readiness
public class AmqpConnectionHealthCheck implements HealthCheck {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    io.vertx.mutiny.core.Vertx vertx;

    @ConfigProperty(name = "amqp-host")
    String amqpHost;

    @ConfigProperty(name = "amqp-port")
    int amqpPort;

    @ConfigProperty(name = "amqp-username")
    String amqpUsername;

    @ConfigProperty(name = "amqp-password")
    String amqpPassword;

    private io.vertx.mutiny.amqp.AmqpClient amqpClient;

    @PostConstruct
    public void init() {
        logger.info("Connecting to "+amqpHost+":"+amqpPort+ " usr "+amqpUsername+" "+amqpPassword);
        amqpClient = io.vertx.mutiny.amqp.AmqpClient.create(vertx, 
            new AmqpClientOptions()
                .setSsl(false)
                .setHost(amqpHost)
                .setPort(amqpPort)
                .setUsername(amqpUsername)
                .setPassword(amqpPassword)
                .setConnectTimeout(5 * 1000)
        );
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("amqp-connection");
        try {
            amqpClient.connect().await().atMost(Duration.ofSeconds(10)).close();
            responseBuilder.up();
        } catch (Exception e) {
            logger.error("Error in health check", e);
            responseBuilder.down();
        }
        return responseBuilder.build();
    }

    
}