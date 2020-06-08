package com.alvin.computeraccessoriesstore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Common.IRecyclerClickListener;
import com.alvin.computeraccessoriesstore.EventBus.CounterCart;
import com.alvin.computeraccessoriesstore.EventBus.ItemsDetailClick;
import com.alvin.computeraccessoriesstore.HomeActivity;
import com.alvin.computeraccessoriesstore.Model.ItemsModel;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.RoomDB.CartDataSource;
import com.alvin.computeraccessoriesstore.RoomDB.CartDatabase;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.alvin.computeraccessoriesstore.RoomDB.LocalCartDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    Context context;
    List<ItemsModel> itemsModelList;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;

    public ItemsAdapter(Context context, List<ItemsModel> itemsModelList) {
        this.context = context;
        this.itemsModelList = itemsModelList;
        this.compositeDisposable = new CompositeDisposable();
        this.cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_store_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ItemsModel list = itemsModelList.get(position);

        Picasso.get().load(list.getImage()).into(holder.imgItems);
        holder.nameItems.setText(list.getName());
        holder.descriptionItems.setText(list.getDescription());
        holder.priceItems.setText(new StringBuilder("IDR ").append(Common.formatPrice(Double.valueOf(list.getPrice()))));
        // Event
        holder.setListener((view, pos) -> {
            context.startActivity(new Intent(context, HomeActivity.class));
            Common.selectedItems = itemsModelList.get(pos);
            Common.selectedItems.setKey(String.valueOf(pos));
            EventBus.getDefault().postSticky(new ItemsDetailClick(true, itemsModelList.get(pos)));
        });

        holder.imgQuickCart.setOnClickListener(v -> {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            CartItem cartItem = new CartItem();
            cartItem.setUid(firebaseUser.getUid());
            cartItem.setUserEmail(firebaseUser.getEmail());

            cartItem.setItemId(list.getId());
            cartItem.setItemName(list.getName());
            cartItem.setItemImage(list.getImage());
            cartItem.setItemPrice(Double.valueOf(String.valueOf(list.getPrice())));
            cartItem.setItemQuantity(1);
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
                            if (cartItemFromDB.equals(cartItem)){
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
                                                Toast.makeText(context, "Update Cart", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCart(true));
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Toast.makeText(context, "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {
                                // Item not available in cart before, insert new
                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() ->{
                                            Toast.makeText(context, "Add to Cart", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCart(true));
                                        }, throwable -> {
                                            Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e.getMessage().contains("empty"))
                            {
                                // Default, if Cart is empty, this code will be fired
                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() ->{
                                            Toast.makeText(context, "Add to Cart", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCart(true));
                                        }, throwable -> {
                                            Toast.makeText(context, "[CART ERROR]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }));
                            }
                            else
                                Toast.makeText(context, "[GET CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return itemsModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.imgItems)
        ImageView imgItems;
        @BindView(R.id.tvNameItems)
        TextView nameItems;
        @BindView(R.id.tvDescriptionItems)
        TextView descriptionItems;
        @BindView(R.id.tvPriceItems)
        TextView priceItems;
        @BindView(R.id.imgQuickCart)
        ImageView imgQuickCart;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
