package com.alvin.computeraccessoriesstore.EventBus;

public class RefreshViewOrderEvent {
    private boolean success;

    public RefreshViewOrderEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
