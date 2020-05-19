package com.alvin.computeraccessoriesstore.ui.view_order;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alvin.computeraccessoriesstore.Adapter.OrdersAdapter;
import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Common.ILoadOrderCallbackListener;
import com.alvin.computeraccessoriesstore.Model.Order;
import com.alvin.computeraccessoriesstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderListFragment extends Fragment implements ILoadOrderCallbackListener {

    private OrderListViewModel orderListViewModel;

    @BindView(R.id.rvDataViewOrder)
    RecyclerView recycler_orders;
    @BindView(R.id.pbOrder)
    ProgressBar progressBar;

    ILoadOrderCallbackListener listener;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        orderListViewModel =
                ViewModelProviders.of(this).get(OrderListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order_list, container, false);

        ButterKnife.bind(this, root);

        initViews();
        loadOrderFromFirebase();

        orderListViewModel.getMutableLiveDataOrderList().observe(this, orderList -> {
            if (orderList != null){
                progressBar.setVisibility(View.GONE);
                recycler_orders.setVisibility(View.VISIBLE);
                OrdersAdapter adapter = new OrdersAdapter(getContext(), orderList);
                recycler_orders.setAdapter(adapter);
            }
            else {
                progressBar.setVisibility(View.GONE);
            }

        });

        return root;
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

    private void initViews() {

        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        listener = this;
        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
    }


    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        orderListViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
