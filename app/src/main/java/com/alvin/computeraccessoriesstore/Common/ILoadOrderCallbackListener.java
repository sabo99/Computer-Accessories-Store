package com.alvin.computeraccessoriesstore.Common;

import com.alvin.computeraccessoriesstore.Model.Order;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<Order> orderList);
    void onLoadOrderFailed(String message);
}
