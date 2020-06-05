package com.alvin.computeraccessoriesstore.EventBus;

public class RefreshCartEvent {
    private boolean success;

    public RefreshCartEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
