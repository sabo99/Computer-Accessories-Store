package com.alvin.computeraccessoriesstore.ui.view_order;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.Model.Order;

import java.util.List;

public class OrderListViewModel extends ViewModel {

    private MutableLiveData<List<Order>> mutableLiveDataOrderList;

    public OrderListViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<Order> orderList) {
        mutableLiveDataOrderList.setValue(orderList);
    }

}
