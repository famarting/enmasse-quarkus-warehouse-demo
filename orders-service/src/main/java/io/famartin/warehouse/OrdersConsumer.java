package io.famartin.warehouse;

import java.util.Random;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.subscribers.DefaultSubscriber;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.Stream;
import io.vertx.core.json.JsonObject;

@Singleton
public class OrdersConsumer extends DefaultSubscriber<JsonObject>{

    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
    private Random random = new Random();
    private boolean waiting = false;
    
    
    @Inject
    EventsService events;

    @Inject
    StocksService stocks;
    
    @Inject
    @Stream("processed-orders")
    Emitter<JsonObject>  processedOrders;
    
    public void re
    
	@Override
	protected void onStart() {
		request(1);
	}

	@Override
	public void onNext(JsonObject order) {
		JsonObject originalOrder = order.copy();
        events.sendEvent("Processing order "+order.getString("order-id"));
        longRunningOperation();
        CompletionStage<JsonObject> response = stocks.requestStock(order.getString("order-id"),
                order.getString("item-id"), order.getInteger("quantity"));
        response.thenAccept(result -> {

            order.put("processingTimestamp", result.getString("timestamp"));
            order.put("processedBy", EventsService.SERVICE_NAME);

            if (result.containsKey("error")) {
                order.put("error", result.getString("error"));
                order.put("approved", false);
            } else if (result.getBoolean("approved", false)) {
                order.put("approved", true);
            } else {
                order.put("approved", false);
                order.put("reason", result.getString("message"));
            }
            
            //we keep consuming only if there is available stock or if the stock request produced an error
            if (order.getBoolean("approved") || order.containsKey("error")) {

            	processedOrders.send(order);
            	events.sendEvent("Order " + order.getString("order-id") + " processed");

            	request(1);
            } else {
            	//stop processing and ask for more stock
            	waiting = true;
            	events.sendEvent("URGENT more stock needed for product " + order.getString("item-id"));
            	//enqueue order again
            	
            }
        });		
	}

	@Override
	public void onError(Throwable t) {
		logger.error("onError", t);
	}

	@Override
	public void onComplete() {
		logger.info("Orders completed");
	}

    private void longRunningOperation() {
        try {
            Thread.sleep(random.nextInt(3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
	
}
