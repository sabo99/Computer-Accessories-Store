package com.alvin.computeraccessoriesstore.EventBus;

public class CounterViewOrder {
    private boolean success;

    public CounterViewOrder(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
