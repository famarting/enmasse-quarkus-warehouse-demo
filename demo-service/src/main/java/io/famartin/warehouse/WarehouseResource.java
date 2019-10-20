package io.famartin.warehouse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.reactivestreams.Publisher;

import io.quarkus.runtime.StartupEvent;
import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@Path("/warehouse")
public class WarehouseResource {

    @Inject
    @Channel("processed-orders")
    Publisher<JsonObject> orders;

    @Inject
    StocksService stocks;

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

    private OutboundSseEvent.Builder eventBuilder;
    private SseEventSink sseEventSink = null;

    @Context
    public void setSse(Sse sse) {
        this.eventBuilder = sse.newEventBuilder();
    }

    // @GET
    // @Path("/events")
    // @Produces(MediaType.SERVER_SENT_EVENTS)
    // public Publisher<String> events() {
    // return Flowable.fromPublisher(events)
    // .map(JsonObject::encode);
    // }

    public void init(@Observes StartupEvent ev) throws InterruptedException, ExecutionException, TimeoutException {
        AmqpClient client = AmqpClient.create(vertx, new AmqpClientOptions()
                    .setSsl(false)
                    .setHost(amqpHost)
                    .setPort(amqpPort)
                    .setUsername(amqpUsername)
                    .setPassword(amqpPassword));
        listen(client);
    }

    @GET
    @Path("/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void consume(@Context SseEventSink sseEventSink) {
        this.sseEventSink = sseEventSink;
    }

    @GET
    @Path("/orders")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> orders() {
        return Flowable.fromPublisher(orders).map(JsonObject::encode);
    }

    @POST
    @Path("/stocks")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletionStage<JsonObject> addStock(JsonObject request) {
        return stocks.addStock(UUID.randomUUID().toString(), request.getString("item-id"),
                request.getInteger("quantity"));
    }

    private void listen(AmqpClient client) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> future = new CompletableFuture<>();
        client.connect(ar -> {
            if (ar.succeeded()) {
                AmqpConnection connection = ar.result();
                connection.createReceiver("events", conn -> {
                    if (conn.succeeded()) {
                        conn.result().handler( message -> {
                            if (sseEventSink != null) {
                                OutboundSseEvent sseEvent = this.eventBuilder
                                        .id(message.id())
                                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                                        .data(message.bodyAsJsonObject().encode())
                                        .build();
                                sseEventSink.send(sseEvent);
                            }
                        });
                        future.complete(null);
                    }else {
                        future.completeExceptionally(conn.cause());
                    }
                });
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        future.get(15, TimeUnit.SECONDS);
    }

}
