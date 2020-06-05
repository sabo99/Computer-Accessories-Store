package com.alvin.computeraccessoriesstore.ui.view_order;

import androidx.lifecycle.MutableLiveData;
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
import android.widget.TextView;
import android.widget.Toast;

import com.alvin.computeraccessoriesstore.Adapter.OrdersAdapter;
import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Common.ILoadOrderCallbackListener;
import com.alvin.computeraccessoriesstore.EventBus.HideFABCart;
import com.alvin.computeraccessoriesstore.EventBus.RefreshViewOrderEvent;
import com.alvin.computeraccessoriesstore.Model.Order;
import com.alvin.computeraccessoriesstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderListFragment extends Fragment implements ILoadOrderCallbackListener {

    private ILoadOrderCallbackListener listener;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private OrderListViewModel orderListViewModel;

    @BindView(R.id.rvDataViewOrder)
    RecyclerView recycler_orders;
    @BindView(R.id.pbOrder)
    ProgressBar progressBar;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;

    OrdersAdapter adapter;

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
            adapter = new OrdersAdapter(getContext(), orderList);
            if (orderList == null || orderList.isEmpty()){
                progressBar.setVisibility(View.GONE);
                recycler_orders.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
            }
            else {
                progressBar.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.GONE);
                recycler_orders.setVisibility(View.VISIBLE);
                recycler_orders.setAdapter(adapter);
            }

        });

        return root;
    }

    private void initViews() {

        listener = this;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);
        txt_empty_cart.setVisibility(View.GONE);
        recycler_orders.setVisibility(View.GONE);

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
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
        orderListViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRefreshViewOrder(RefreshViewOrderEvent event){
        if (event.isRefresh()){
            loadOrderFromFirebase();
        }
    }
}
