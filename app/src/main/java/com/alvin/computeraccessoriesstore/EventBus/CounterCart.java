package com.alvin.computeraccessoriesstore.EventBus;

public class CounterCart {
    private boolean success;

    public CounterCart(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
