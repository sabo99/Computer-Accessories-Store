package com.alvin.computeraccessoriesstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrdersDetailAdapter extends RecyclerView.Adapter<OrdersDetailAdapter.ViewHolder> {

    Context context;
    List<CartItem> cartItemList;

    public OrdersDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(cartItemList.get(position).getItemImage()).into(holder.img);
        holder.tvName.setText(cartItemList.get(position).getItemName());
        holder.tvQuantity.setText(new StringBuilder("Quantity: ").append(cartItemList.get(position).getItemQuantity()));
        holder.tvPrice.setText(new StringBuilder("IDR ").append(Common.formatPrice(cartItemList.get(position).getItemPrice())));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgOrderDetail)
        ImageView img;
        @BindView(R.id.tvNameOrderDetail)
        TextView tvName;
        @BindView(R.id.tvQuantityOrderDetail)
        TextView tvQuantity;
        @BindView(R.id.tvPriceOrderDetail)
        TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
