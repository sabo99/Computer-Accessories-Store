package com.alvin.computeraccessoriesstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.CounterCart;
import com.alvin.computeraccessoriesstore.EventBus.UpdateItemInCart;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    List<CartItem> cartItemList;

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Picasso.get().load(cartItemList.get(position).getItemImage()).into(holder.imgCart);
        holder.tvItemName.setText(cartItemList.get(position).getItemName());
        holder.tvItemPrice.setText(new StringBuilder("IDR ")
                .append(Common.formatPrice(cartItemList.get(position).getItemPrice())));
        holder.numberButton.setNumber(String.valueOf(cartItemList.get(position).getItemQuantity()));

        // Event
        holder.numberButton.setOnValueChangeListener((view, oldValue, newValue) -> {
            // When user click this button, will update database
            cartItemList.get(position).setItemQuantity(newValue);
            EventBus.getDefault().postSticky(new UpdateItemInCart(cartItemList.get(position)));
            EventBus.getDefault().postSticky(new CounterCart(true));
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public CartItem getItemAtPosition(int pos){
        return cartItemList.get(pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgCart)
        ImageView imgCart;
        @BindView(R.id.tvItemPrice)
        TextView tvItemPrice;
        @BindView(R.id.tvItemName)
        TextView tvItemName;
        @BindView(R.id.number_button)
        ElegantNumberButton numberButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
