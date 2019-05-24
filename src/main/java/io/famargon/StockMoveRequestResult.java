package io.famargon;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * ApiResult
 */
@RegisterForReflection
public class StockMoveRequestResult {

    private String orderId;
    private String error;

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public static StockMoveRequestResult success(String orderId) {
        StockMoveRequestResult ar = new StockMoveRequestResult();
        ar.setOrderId(orderId);
        return ar;
    }


}