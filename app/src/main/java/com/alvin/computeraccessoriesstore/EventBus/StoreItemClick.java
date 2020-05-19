package com.alvin.computeraccessoriesstore.EventBus;

import com.alvin.computeraccessoriesstore.Model.StoreModel;

public class StoreItemClick {
    private boolean success;
    private StoreModel storeModel;

    public StoreItemClick(boolean success, StoreModel storeModel) {
        this.success = success;
        this.storeModel = storeModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public StoreModel getStoreModel() {
        return storeModel;
    }

    public void setStoreModel(StoreModel storeModel) {
        this.storeModel = storeModel;
    }
}
