package io.famargon;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.Stream;
import io.vertx.core.json.JsonObject;

@Path("/stockmoves")
public class StockMovesResource {

    @Inject
    @Stream("stock-moves-output")
    Emitter<JsonObject> stream;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public StockMoveRequestResult request(StockMove move) {
        move.id = UUID.randomUUID().toString();
        stream.send(JsonObject.mapFrom(move));
        return StockMoveRequestResult.success(move.id);
    }
}