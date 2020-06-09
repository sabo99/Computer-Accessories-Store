package com.alvin.computeraccessoriesstore.EventBus;


public class RefreshViewOrder {
    private boolean isRefresh;

    public RefreshViewOrder(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }
}
