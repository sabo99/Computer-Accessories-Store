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
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItems;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();
    }

    public void initCartDataSource(Context context) {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItems() {
        if (mutableLiveDataCartItems == null)
            mutableLiveDataCartItems = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartItems;
    }

    private void getAllCartItems() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        compositeDisposable.add(cartDataSource.getAllCart(user.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    mutableLiveDataCartItems.setValue(cartItems);
                }, throwable -> {
                    mutableLiveDataCartItems.setValue(null);
                }));
    }
}
