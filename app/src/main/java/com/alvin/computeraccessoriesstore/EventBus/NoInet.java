package com.alvin.computeraccessoriesstore.EventBus;

public class NoInet {
    private boolean connected;

    public NoInet(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
