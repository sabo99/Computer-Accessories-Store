package com.alvin.computeraccessoriesstore.ui.items_detail;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.CounterCartEvent;
import com.alvin.computeraccessoriesstore.Model.ItemsModel;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.RoomDB.CartDataSource;
import com.alvin.computeraccessoriesstore.RoomDB.CartDatabase;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.alvin.computeraccessoriesstore.RoomDB.LocalCartDataSource;
import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ItemsDetailFragment extends Fragment {

    private ItemsDetailViewModel itemsDetailViewModel;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.rlTop)
    RelativeLayout rlTop;
    @BindView(R.id.llTop)
    LinearLayout llTop;
    @BindView(R.id.imgItemsDetail)
    ImageView imgItemsDetail;
    @BindView(R.id.imgItemsDetail2)
    ImageView imgItemsDetail2;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.itemsName)
    TextView itemsName;
    @BindView(R.id.itemsName2)
    TextView itemsName2;
    @BindView(R.id.itemsPrice)
    TextView itemsPrice;
    @BindView(R.id.itemsPrice2)
    TextView itemsPrice2;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @BindView(R.id.itemsDescription)
    ExpandableTextView itemsDescription;
    @BindView(R.id.itemsSpecification)
    ExpandableTextView itemsSpecification;

    @OnClick(R.id.btnCart)
    void cart() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        CartItem cartItem = new CartItem();
        cartItem.setUid(firebaseUser.getUid());
        cartItem.setUserEmail(firebaseUser.getEmail());

        cartItem.setItemId(Common.selectedItems.getId());
        cartItem.setItemName(Common.selectedItems.getName());
        cartItem.setItemImage(Common.selectedItems.getImage());
        cartItem.setItemPrice(Double.valueOf(String.valueOf(Common.selectedItems.getPrice())));
        cartItem.setItemQuantity(Integer.valueOf(numberButton.getNumber()));
        cartItem.setItemExtraPrice(0.0); // Default


        cartDataSource.getItemWithAllOptionsInCart(firebaseUser.getUid(),
                cartItem.getItemId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {
                        if (cartItemFromDB.equals(cartItem)) {
                            // Already in database, just update
                            cartItemFromDB.setItemExtraPrice(cartItemFromDB.getItemExtraPrice());
                            cartItemFromDB.setItemQuantity(cartItemFromDB.getItemQuantity() + cartItem.getItemQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Log.d("s", integer.toString());
                                            Toast.makeText(getContext(), "Update Cart success", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Item not available in cart before, insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")) {
                            // Default, if Cart is empty, this code will be fired
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "Add to Cart success", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        } else
                            Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        itemsDetailViewModel =
                ViewModelProviders.of(this).get(ItemsDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_items_detail, container, false);

        ButterKnife.bind(this, root);

        cartDataSource = new
                LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        itemsDetailViewModel.getMutableLiveDataItems().observe(this, itemsModel -> {
            displayInfo(itemsModel);
        });

        return root;
    }

    private void displayInfo(ItemsModel itemsModel) {
        Picasso.get().load(itemsModel.getImage()).into(imgItemsDetail);
        itemsName.setText(itemsModel.getName());
        itemsPrice.setText(new StringBuilder("IDR ").append(Common.formatPrice(Double.valueOf(itemsModel.getPrice()))));
        itemsName2.setText(itemsModel.getName());
        itemsPrice2.setText(new StringBuilder("IDR ").append(Common.formatPrice(Double.valueOf(itemsModel.getPrice()))));
        itemsDescription.setText(itemsModel.getDescription());

        String string = itemsModel.getSpecification();
        String str = string.replace("/n", "\n");

        itemsSpecification.setText(str);


        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedItems.getName());

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;
            Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_imgdetail);
            Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_imgdetail);

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    //Toast.makeText(getContext(), String.valueOf(verticalOffset), Toast.LENGTH_SHORT).show();
                }
                if (scrollRange + verticalOffset == 0) {
                    rlTop.setVisibility(View.VISIBLE);
                    Picasso.get().load(itemsModel.getImage()).into(imgItemsDetail2);
                    rlTop.startAnimation(fadeIn);
                    llTop.startAnimation(fadeOut);
                    llTop.setVisibility(View.GONE);
                    llTop.clearAnimation();
                    isShow = true;
                } else if (isShow) {

                    rlTop.startAnimation(fadeOut);
                    llTop.startAnimation(fadeIn);
                    rlTop.setVisibility(View.GONE);
                    llTop.setVisibility(View.VISIBLE);

                    isShow = false;
                }
            }
        });
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
