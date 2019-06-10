package io.famargon;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * StockChangelog
 */
@Path("/stocks")
@Singleton
public class StockChangelog {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<JsonObject> stocks = new CopyOnWriteArrayList<>();

    @Incoming("stock-changelog-input")
    public void changelog(JsonObject json) {
        logger.info("Changelog: item {} processed by {}",json.getString("item"), json.getString("processedBy"));
        stocks.add(json);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonObject> stocksStatus() {
        return stocks;
    }
    
}