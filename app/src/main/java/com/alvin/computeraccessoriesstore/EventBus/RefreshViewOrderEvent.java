package com.alvin.computeraccessoriesstore.EventBus;

import com.alvin.computeraccessoriesstore.Model.Order;

import java.util.List;

public class RefreshViewOrderEvent {
    private boolean isRefresh;

    public RefreshViewOrderEvent(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }
}
