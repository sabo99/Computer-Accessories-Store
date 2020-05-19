package com.alvin.computeraccessoriesstore.EventBus;

import com.alvin.computeraccessoriesstore.Model.ItemsModel;

public class ItemsDetailClick {
    private boolean success;
    private ItemsModel itemsModel;

    public ItemsDetailClick(boolean success, ItemsModel itemsModel) {
        this.success = success;
        this.itemsModel = itemsModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ItemsModel getItemsModel() {
        return itemsModel;
    }

    public void setItemsModel(ItemsModel itemsModel) {
        this.itemsModel = itemsModel;
    }
}
