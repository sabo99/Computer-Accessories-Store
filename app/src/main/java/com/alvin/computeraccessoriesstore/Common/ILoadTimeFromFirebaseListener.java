package com.alvin.computeraccessoriesstore.Common;

import com.alvin.computeraccessoriesstore.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
