package com.alvin.computeraccessoriesstore.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.RoomDB.CartDataSource;
import com.alvin.computeraccessoriesstore.RoomDB.CartDatabase;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.alvin.computeraccessoriesstore.RoomDB.LocalCartDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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
