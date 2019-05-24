package io.famargon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * OrdersProcessor
 */
@Singleton
@Path("/stocks")
public class StockProcessor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    Map<String, Integer> stocks = new ConcurrentHashMap<>();

    @Incoming("stock-moves-input")
    public void receive(JsonObject json) {
        StockMove move = json.mapTo(StockMove.class);
        stocks.compute(move.getItem(), (key, value) -> value == null ? move.quantity : (value + move.quantity) );
        logger.info("Stocks status {}", stocks);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Integer> stocksStatus() {
        return stocks;
    }
}