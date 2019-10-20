package io.famartin.warehouse;

import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.json.JsonObject;

@Path("/orders")
public class OrdersResource {

    @Inject
    @Channel("orders")
    Emitter<JsonObject> orders;

    @Inject
    EventsService events;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response request(@NotNull JsonObject order) {
        order.put("order-id", UUID.randomUUID().toString());
        if(isValid(order)) {
            orders.send(order);
            events.sendEvent(String.format("Order %s enqueued", order.getString("subject", order.getString("order-id"))));
            return Response.ok(order).build();
        } else {
            events.sendEvent(String.format("Invalid order %s", order.encodePrettily()));
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    private boolean isValid(JsonObject order) {
        try {
            return order != null && order.containsKey("order-id")
            && order.containsKey("item-id") && order.containsKey("quantity") && order.getInteger("quantity")!=null && order.getInteger("quantity")>0;
        } catch (RuntimeException e) {
            return false;
        }
    }

}