package com.alvin.computeraccessoriesstore.ui.view_order;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Common.ILoadOrderCallbackListener;
import com.alvin.computeraccessoriesstore.Model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderListViewModel extends ViewModel implements ILoadOrderCallbackListener {

    private MutableLiveData<List<Order>> mutableLiveDataOrderList;
    private MutableLiveData<String> messageError;
    private ILoadOrderCallbackListener listener;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    public OrderListViewModel() {
        listener = this;
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList() {
        if (mutableLiveDataOrderList == null){
            mutableLiveDataOrderList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadOrderFromFirebase();
        }
        return mutableLiveDataOrderList;
    }

    private void loadOrderFromFirebase() {
        List<Order> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("email")
                .equalTo(user.getEmail())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            Order order = ds.getValue(Order.class);
                            order.setOrderNumber(ds.getKey()); // Remember set it
                            orderList.add(order);
                        }

                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onLoadOrderFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        mutableLiveDataOrderList.setValue(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        messageError.setValue(message);
    }
}
