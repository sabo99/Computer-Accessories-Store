package com.alvin.computeraccessoriesstore.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alvin.computeraccessoriesstore.Adapter.CartAdapter;
import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Common.ILoadTimeFromFirebaseListener;
import com.alvin.computeraccessoriesstore.Common.SwipeHelper;
import com.alvin.computeraccessoriesstore.EventBus.CounterCartEvent;
import com.alvin.computeraccessoriesstore.EventBus.HideFABCart;
import com.alvin.computeraccessoriesstore.EventBus.UpdateItemInCart;
import com.alvin.computeraccessoriesstore.Model.Order;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.RoomDB.CartDataSource;
import com.alvin.computeraccessoriesstore.RoomDB.CartDatabase;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.alvin.computeraccessoriesstore.RoomDB.LocalCartDataSource;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private CartViewModel cartViewModel;
    private Parcelable recyclerViewState;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;
    private CartAdapter adapter;
    private ILoadTimeFromFirebaseListener listener;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @BindView(R.id.txt_text)
    CardView txt_text;
    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    TextView tvName, tvEmail;
    TextInputLayout tilAddress;
    TextInputEditText etAddress;
    Button btnCancel, btnSubmit;

    String address;

    LinearLayout.LayoutParams paramsAddress =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT);   // Height

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ButterKnife.bind(this, root);

        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(this, cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                recycler_cart.setVisibility(View.GONE);
                txt_text.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setText(View.VISIBLE);
            } else {
                recycler_cart.setVisibility(View.VISIBLE);
                txt_text.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                adapter = new CartAdapter(getContext(), cartItems);
                recycler_cart.setAdapter(adapter);
            }
        });

        initViews();

        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        txt_empty_cart.setVisibility(View.VISIBLE);

        listener = this;

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        SwipeHelper swipeHelper = new SwipeHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateButtonSwipe(RecyclerView.ViewHolder viewHolder, List<ButtonSwipe> buf) {
                buf.add(new ButtonSwipe(getContext(), "Remove", 30, 0, Color.RED,
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            adapter.notifyItemRemoved(pos);
                                            // Update Total Price
                                            sumAllItemCart();
                                            // Update Counter FAB (COUNT)
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            Toast.makeText(getContext(), "Remove item successful!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));
            }
        };

        // Sum Total Price
        sumAllItemCart();
    }

    private void sumAllItemCart() {
        cartDataSource.sumPriceInCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total : IDR ").append(Common.formatPrice(aDouble)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
//        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.clear_cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            showDialogClearCart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogClearCart() {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setCancelable(false);
        builder.setTitle("Clear All Cart")
                .setMessage("Are you sure clear all cart?")
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("YES", (dialog, which) -> {
                    dialog.dismiss();
                    clearCart();
                });
        alertDialog = builder.create();
        alertDialog.show();
        Button positif = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positif.setTextColor(Color.RED);
    }

    private void clearCart() {
        cartDataSource.cleanCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Toast.makeText(getContext(), "Clear Cart Success", Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        txt_empty_cart.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {

        // compositeDisposable clear
        cartViewModel.onStop();
        compositeDisposable.clear();

        EventBus.getDefault().postSticky(new HideFABCart(false));

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("Total : IDR ").append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Toast.makeText(getContext(), "[SUM CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.btn_place_holder)
    void placeHolderClick() {


        View view = LayoutInflater.from(getContext()).inflate(R.layout.place_holder, null);

        tilAddress = view.findViewById(R.id.tilAddress);
        etAddress = view.findViewById(R.id.etAddress);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvName = view.findViewById(R.id.tvName);


        DatabaseReference name = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());
        name.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                tvName.setText(userModel.getName());
                tvEmail.setText(userModel.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setCancelable(false).setView(view);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });

            btnSubmit.setOnClickListener(v -> {

                // Get Text
                address = etAddress.getText().toString();

                if (checkAddress(true, address)){

                    compositeDisposable.add(cartDataSource.getAllCart(firebaseUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItems -> {

                        // getTotal Price
                        cartDataSource.sumPriceInCart(firebaseUser.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Double>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Double totalPrice) {
                                        double finalPrice = totalPrice;
                                        Order order = new Order();
                                        order.setName(tvName.getText().toString());
                                        order.setEmail(tvEmail.getText().toString());
                                        order.setAddress(address);
                                        order.setCartItems(cartItems);
                                        order.setTotalPayment(finalPrice);
                                        order.setOrderStatus(1); // Status : Ordered

                                        createDate(order);
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }
                                });
                    }));
                }
            });
        });
        dialog.show();
    }

    private boolean checkAddress(boolean check, String address) {

        if (TextUtils.isEmpty(address) || address.equals("")){
            check = false;
            tilAddress.setHelperTextEnabled(true);
            tilAddress.setHelperText("Please Enter your address!");
            paramsAddress.setMargins(0, 0, 0, 50);
            tilAddress.setLayoutParams(paramsAddress);

        }
        if (!TextUtils.isEmpty(address) || !address.equals("")){
            check = true;
            tilAddress.setHelperTextEnabled(false);
            paramsAddress.setMargins(0,0,0,0);
            tilAddress.setLayoutParams(paramsAddress);
        }

        return check;
    }

    private void createDate(Order order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long offset = dataSnapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date resultDate = new Date(estimatedServerTimeMs);

                Log.d("OrderDate", ""+sdf.format(resultDate));

                // Date to Long to Firebase
                listener.onLoadTimeSuccess(order, estimatedServerTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onLoadTimeFailed(databaseError.getMessage());
            }
        });
    }

    @Override
    public void onLoadTimeSuccess(Order order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderNumber(Common.createOrderNumber());
        order.setOrderDate(String.valueOf(estimateTimeInMs));

        // Send Firebase
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(order.getOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    cartDataSource.cleanCart(firebaseUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    Toast.makeText(getContext(), "Order placed Successfully", Toast.LENGTH_SHORT).show();
                                    txt_empty_cart.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });
                });
    }

}
