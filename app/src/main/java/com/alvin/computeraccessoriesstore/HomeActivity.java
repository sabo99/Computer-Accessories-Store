package com.alvin.computeraccessoriesstore;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.CounterCart;
import com.alvin.computeraccessoriesstore.EventBus.CounterViewOrder;
import com.alvin.computeraccessoriesstore.EventBus.HideBadgeCart;
import com.alvin.computeraccessoriesstore.EventBus.HideCivProfile;
import com.alvin.computeraccessoriesstore.EventBus.HideFABCart;
import com.alvin.computeraccessoriesstore.EventBus.ItemsDetailClick;
import com.alvin.computeraccessoriesstore.EventBus.PopularSliderItemClick;
import com.alvin.computeraccessoriesstore.EventBus.StoreItemClick;
import com.alvin.computeraccessoriesstore.EventBus.SweetAlertDialogLogin;
import com.alvin.computeraccessoriesstore.Model.ItemsModel;
import com.alvin.computeraccessoriesstore.Model.Order;
import com.alvin.computeraccessoriesstore.Model.StoreModel;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.alvin.computeraccessoriesstore.RoomDB.CartDataSource;
import com.alvin.computeraccessoriesstore.RoomDB.CartDatabase;
import com.alvin.computeraccessoriesstore.RoomDB.LocalCartDataSource;
import com.andremion.counterfab.CounterFab;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nex3z.notificationbadge.NotificationBadge;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRef;
    StorageReference storageReference;

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    SweetAlertDialog sweetAlertDialog, sweetAlertDialogLogin;
    private CartDataSource cartDataSource;

    CircleImageView imgPhotoHeader;
    TextView tvNameHeader;
    ProgressBar pbHeader;
    Toolbar toolbar;
    NotificationBadge badge;
    RelativeLayout rl_action_cart, rl_action_profile;
    TextView tvCartBadgeDrawer, tvOrderViewBadgeDrawer;
    CircleImageView civProfile;

    UserModel tempModel = new UserModel();

    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        fab.setOnClickListener(v -> {
            navController.navigate(R.id.nav_cart);
        });

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_items, R.id.nav_items_detail,
                R.id.nav_cart, R.id.nav_order)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        initViewUser(navigationView, toolbar);
        initCartBadge(toolbar);
    }

    private void initCartBadge(Toolbar toolbar) {
        rl_action_cart = toolbar.findViewById(R.id.rl_action_cart);
        badge = toolbar.findViewById(R.id.badge);

        rl_action_cart.setVisibility(View.VISIBLE);

        // Cart Badge Notification in Drawer Menu
        tvCartBadgeDrawer = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_cart));
        // View Order Badge Notification in Drawer Menu
        tvOrderViewBadgeDrawer = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_order));
    }

    private void initViewUser(NavigationView navigationView, Toolbar toolbar) {

        View headerView = navigationView.getHeaderView(0);
        tvNameHeader = headerView.findViewById(R.id.tvNameHeader);
        imgPhotoHeader = headerView.findViewById(R.id.imgPhotoHeader);
        pbHeader = headerView.findViewById(R.id.pbHeader);

        rl_action_profile = toolbar.findViewById(R.id.rl_action_profile);
        civProfile = toolbar.findViewById(R.id.civProfile);

        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference(Common.USER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        imgPhotoHeader.setVisibility(View.INVISIBLE);
        civProfile.setVisibility(View.INVISIBLE);

        // Set Image
        StorageReference profileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
        if (profileRef != null) {
            profileRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        imgPhotoHeader.setImageDrawable(null);
                        Picasso.get().load(uri).into(imgPhotoHeader);
                        Picasso.get().load(uri).into(civProfile);
                    })
                    .addOnFailureListener(e -> {
                        imgPhotoHeader.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_48dp));
                        civProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_48dp));
                    });
        } else {
            imgPhotoHeader.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_48dp));
            civProfile.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_white_48dp));
        }


        tvNameHeader.setText(null);

        // Data Profile
        if (firebaseUser != null) {
            userRef.child(firebaseUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);

                            Common.setSpanString("Welcome back, ", userModel.getName(), tvNameHeader);
                            imgPhotoHeader.setVisibility(View.VISIBLE);
                            civProfile.setVisibility(View.VISIBLE);
                            pbHeader.setVisibility(View.GONE);

                            tempModel = userModel;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            sweetAlertDialog.dismissWithAnimation();
                            new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText("Something went wrong!")
                                    .show();
                        }
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void signout(MenuItem item) {
        drawer.closeDrawers();

        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Sign Out!")
                .setContentText("Do you really want to Sign Out?")
                .setCustomImage(R.drawable.ic_exit_to_app_black_24dp)
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        // Clear Model
                        Common.currentUser = null;
                        Common.storeItemsSelected = null;
                        Common.selectedItems = null;
                        // Sign Out
                        firebaseAuth.signOut();
                        Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                })
                .setCancelText("No")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViewUser(navigationView, toolbar);
        countCartItem();
        countViewOrder();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().removeStickyEvent(StoreItemClick.class);
        EventBus.getDefault().removeStickyEvent(ItemsDetailClick.class);
        EventBus.getDefault().removeStickyEvent(SweetAlertDialogLogin.class);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStoreItemSelected(StoreItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_items);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onItemsDetailSelected(ItemsDetailClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_items_detail);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCart event) {
        if (event.isSuccess()) {
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSuccessLogin(SweetAlertDialogLogin event) {
        if (event.isSuccessAutoLogin()) {
            if (firebaseUser != null) {
                sweetAlertDialogLogin = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                sweetAlertDialogLogin.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                sweetAlertDialogLogin.setTitleText("Loading").setCancelable(false);
                sweetAlertDialogLogin.show();
                userRef.child(firebaseUser.getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                sweetAlertDialogLogin.dismissWithAnimation();
                                sweetAlertDialogLogin.dismiss();
                                new SweetAlertDialog(HomeActivity.this)
                                        .setTitleText("Welcome back!")
                                        .setContentText(

                                                String.valueOf(new StringBuilder("Hello, ")
                                                        .append(userModel.getName()))
                                        )
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismissWithAnimation();
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                sweetAlertDialogLogin.dismissWithAnimation();
                                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("Something went wrong!")
                                        .show();
                            }
                        });
            }
        }

        if (event.isSuccessLogin()) {
            if (firebaseUser != null) {
                new SweetAlertDialog(HomeActivity.this)
                        .setTitleText("Success Login")
                        .setContentText("Logged in Successfully!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(firebaseUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                        Log.d("countFAB", integer.toString());

                        if (badge == null) return;
                        if (fab.getCount() == 0) {
                            badge.setVisibility(View.INVISIBLE);
                            badge.setText(null);
                        } else {
                            badge.setVisibility(View.VISIBLE);
                            badge.setText(String.valueOf(integer));
                            tvCartBadgeDrawer.setText(String.valueOf(integer));
                            tvCartBadgeDrawer.setTextColor(getResources().getColor(R.color.colorPrimary));
                            tvCartBadgeDrawer.setGravity(Gravity.CENTER_VERTICAL);
                            tvCartBadgeDrawer.setTypeface(null, Typeface.BOLD);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty")) {
                            //Toast.makeText(HomeActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            fab.setCount(0);
                            badge.setVisibility(View.INVISIBLE);
                            badge.setText(null);
                            tvCartBadgeDrawer.setText(null);
                        }

                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if (event.isHidden())
            fab.hide();
        else
            fab.show();
    }

    public void cartMenu(View view) {
        navController.navigate(R.id.nav_cart);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideBadgeEvent(HideBadgeCart event) {
        if (event.isHidden())
            rl_action_cart.setVisibility(View.GONE);
        else
            rl_action_cart.setVisibility(View.VISIBLE);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCountViewOrder(CounterViewOrder event) {
        if (event.isSuccess())
            countViewOrder();
    }

    private void countViewOrder() {
        List<Order> countList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("email")
                .equalTo(firebaseUser.getEmail())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Order order = ds.getValue(Order.class);
                            countList.add(order);
                        }

                        if (countList.size() == 0 || countList.isEmpty() || countList == null)
                            tvOrderViewBadgeDrawer.setText(null);
                        else {
                            tvOrderViewBadgeDrawer.setText(String.valueOf(countList.size()));
                            tvOrderViewBadgeDrawer.setTextColor(getResources().getColor(R.color.colorPrimary));
                            tvOrderViewBadgeDrawer.setGravity(Gravity.CENTER_VERTICAL);
                            tvOrderViewBadgeDrawer.setTypeface(null, Typeface.BOLD);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("Something went wrong!")
                                .show();
                    }
                });
    }

    public void profileMenu(View view) {
        drawer.closeDrawers();
        startActivity(new Intent(this, ProfileActivity.class));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHiddenCivProfile(HideCivProfile event) {
        if (event.isHidden())
            rl_action_profile.setVisibility(View.GONE);
        else
            rl_action_profile.setVisibility(View.VISIBLE);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularSliderItemClick(PopularSliderItemClick event) {
        if (event.getSliderModel() != null) {

            sweetAlertDialog.show();

            FirebaseDatabase.getInstance().getReference(Common.STORE_REF)
                    .child(event.getSliderModel().getItem_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Common.storeItemsSelected = dataSnapshot.getValue(StoreModel.class);
                                Common.storeItemsSelected.setItem_id(dataSnapshot.getKey());

                                // Load Items
                                FirebaseDatabase.getInstance().getReference(Common.STORE_REF)
                                        .child(event.getSliderModel().getItem_id())
                                        .child(Common.CHILD_ITEMS)
                                        .orderByChild(Common.F_ID)
                                        .equalTo(event.getSliderModel().getId())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot items : dataSnapshot.getChildren()) {
                                                        Common.selectedItems = items.getValue(ItemsModel.class);
                                                        Common.selectedItems.setKey(items.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_items_detail);
                                                } else {
                                                    new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                            .setTitleText("Oops...")
                                                            .setContentText("Something went wrong!")
                                                            .show();
                                                }

                                                sweetAlertDialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Oops...")
                                                        .setContentText("Something went wrong!")
                                                        .show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText("Something went wrong!")
                                    .show();
                        }
                    });
        }
    }
}
