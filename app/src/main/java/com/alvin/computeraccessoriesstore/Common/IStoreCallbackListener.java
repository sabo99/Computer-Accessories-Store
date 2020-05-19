package com.alvin.computeraccessoriesstore.Common;

import com.alvin.computeraccessoriesstore.Model.StoreModel;

import java.util.List;

public interface IStoreCallbackListener {
    void onStoreLoadSuccess(List<StoreModel> storeModelList);
    void onStoreLoadFailed(String message);
}
