package com.alvin.computeraccessoriesstore;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.CounterCartEvent;
import com.alvin.computeraccessoriesstore.EventBus.HideFABCart;
import com.alvin.computeraccessoriesstore.EventBus.ItemsDetailClick;
import com.alvin.computeraccessoriesstore.EventBus.RefreshViewOrderEvent;
import com.alvin.computeraccessoriesstore.EventBus.StoreItemClick;
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
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private CartDataSource cartDataSource;

    CircleImageView imgPhotoHeader;
    TextView tvNameHeader, tvEmailHeader;
    ProgressBar pbHeader;

    @BindView(R.id.fab)
    CounterFab fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        initViewUser(navigationView);

        //countCartItem();
    }

    private void initViewUser(NavigationView navigationView) {

        View headerView = navigationView.getHeaderView(0);
        tvNameHeader = headerView.findViewById(R.id.tvNameHeader);
        tvEmailHeader = headerView.findViewById(R.id.tvEmailHeader);
        imgPhotoHeader = headerView.findViewById(R.id.imgPhotoHeader);
        pbHeader = headerView.findViewById(R.id.pbHeader);

        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference(Common.USER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        imgPhotoHeader.setVisibility(View.INVISIBLE);

        // Set Image
        StorageReference profileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
        if (profileRef != null){
            profileRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(imgPhotoHeader);
                    });
        }

        tvNameHeader.setText(null);
        tvEmailHeader.setText(null);

        // Data Profile
        userRef.child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);

                        tvNameHeader.setText(userModel.getName());
                        tvEmailHeader.setText(userModel.getEmail());
                        imgPhotoHeader.setVisibility(View.VISIBLE);
                        pbHeader.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(HomeActivity.this, "Some error with database", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);

//        getMenuInflater().inflate(R.menu.account_menu, menu);
//        View view = menu.findItem(R.id.account_menu).getActionView();
//        CircleImageView civProfile = view.findViewById(R.id.civProfile);
//
//        StorageReference profileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
//        if (profileRef != null){
//            profileRef.getDownloadUrl()
//                    .addOnSuccessListener(uri -> {
//                        Picasso.get().load(uri).into(civProfile);
//                    });
//        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViewUser(navigationView);
        countCartItem();
    }

//    public void account(MenuItem item) {
//        drawer.closeDrawers();
//        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
//    }

    public void signout(MenuItem item) {
        drawer.closeDrawers();

        builder = new AlertDialog.Builder(this).setCancelable(false);
        builder.setTitle("Sign Out")
                .setMessage("Do you really want to Sign Out?")
                .setNegativeButton("NO", (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .setPositiveButton("YES", (dialog1, which) -> {
                    dialog1.dismiss();
                    // Clear Model
                    Common.currentUser = null;
                    Common.storeItemsSelected = null;
                    Common.selectedItems = null;
                    // Sign Out
                    firebaseAuth.signOut();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                });
        dialog = builder.create();
        dialog.show();

        Button btnPos;
        btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPos.setTextColor(Color.RED);
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
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStoreItemSelected(StoreItemClick event){
        if (event.isSuccess()){
            navController.navigate(R.id.nav_items);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onItemsDetailSelected(ItemsDetailClick event){
        if (event.isSuccess()){
            navController.navigate(R.id.nav_items_detail);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event){
        if (event.isSuccess()){
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRefreshRecyclerViewOrder(RefreshViewOrderEvent event){
        if (event.isSuccess())
            navController.navigate(R.id.nav_order);
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
                        Log.d("countFAB",integer.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(HomeActivity.this, "[COUNT CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        else
                            fab.setCount(0);
                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event)
    {
        if (event.isHidden())
        {
            fab.hide();
        }
        else
            fab.show();
    }
}
