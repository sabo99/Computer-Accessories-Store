package com.alvin.computeraccessoriesstore.ui.cart;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.RoomDB.CartItem;

import java.util.List;


public class CartViewModel extends ViewModel {
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItems;

    public CartViewModel() {
        mutableLiveDataCartItems = new MutableLiveData<>();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems() {
        return mutableLiveDataCartItems;
    }

    public void setMutableLiveDataCartItems(List<CartItem> cartItems) {
     mutableLiveDataCartItems.setValue(cartItems);
    }
}
