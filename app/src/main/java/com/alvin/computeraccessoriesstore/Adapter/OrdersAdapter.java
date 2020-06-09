package com.alvin.computeraccessoriesstore.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.CounterViewOrder;
import com.alvin.computeraccessoriesstore.EventBus.RefreshViewOrder;
import com.alvin.computeraccessoriesstore.Model.Order;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.RoomDB.CartItem;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    Context context;
    List<Order> orderList;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    SweetAlertDialog sweetAlertDialog;

    public OrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        initSweetAlert();
    }


    private void initSweetAlert() {
        sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.colorPrimary));
        sweetAlertDialog.setTitleText("Loading...");
        sweetAlertDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(orderList.get(position).getCartItems().get(0).getItemImage()).into(holder.imgOrder);

        calendar.setTimeInMillis(orderList.get(position).getCreateDate());
        Date date = new Date(orderList.get(position).getCreateDate());
        holder.tvOrderDate.setText(new StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
        .append(" ")
        .append(simpleDateFormat.format(date)));

        holder.tvOrderNumber.setText(new StringBuilder("Order Number: ").append(orderList.get(position).getOrderNumber()));
        holder.tvOrderStatus.setText(new StringBuilder("Status: ").append(Common.convertStatusToText(orderList.get(position).getOrderStatus())));

        holder.btnDetailOrder.setOnClickListener(v -> {
            showDialog(orderList.get(position).getCartItems(), position);
        });

        holder.btnCancelOrder.setOnClickListener(v -> {
            sweetAlertDialog.show();
            showDialogCancelOrder(orderList.get(position).getOrderNumber(), position);
        });
    }

    private void showDialogCancelOrder(String orderNumber, int position) {

        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Cancel Order")
                .setContentText("Are you sure you canceled this order?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog1) {
                        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF);
                        orderRef.child(orderNumber)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        EventBus.getDefault().postSticky(new RefreshViewOrder(true));
                                        EventBus.getDefault().postSticky(new CounterViewOrder(true));
                                        sweetAlertDialog.dismiss();
                                        sweetAlertDialog1.dismissWithAnimation();
                                        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Success")
                                                .setContentText("Order Successfully Canceled!")
                                                .show();
                                        //Toast.makeText(context, "Order Canceled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setCancelText("No")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog1) {
                        sweetAlertDialog.dismiss();
                        sweetAlertDialog1.dismissWithAnimation();
                    }
                })
                .show();
    }

    private void showDialog(List<CartItem> cartItemList, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_order_detail, null);
        builder = new AlertDialog.Builder(context).setView(view).setCancelable(false);

        Button btnOK = view.findViewById(R.id.btnOK);
        RecyclerView recycler_order_detail = view.findViewById(R.id.rvDataOrderDetail);
        TextView tvTotalPayment = view.findViewById(R.id.tvTotalOrder);
        recycler_order_detail.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_order_detail.setLayoutManager(layoutManager);

        OrdersDetailAdapter adapter = new OrdersDetailAdapter(context, cartItemList);
        recycler_order_detail.setAdapter(adapter);

        dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {

            btnOK.setOnClickListener(v -> {
                dialog.dismiss();
            });

            tvTotalPayment.setText(new StringBuilder("Total Payment :\n IDR ")
                    .append(Common.formatPrice(orderList.get(position).getTotalPayment())));
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgOrder)
        ImageView imgOrder;
        @BindView(R.id.tvOrderNumber)
        TextView tvOrderNumber;
        @BindView(R.id.tvOrderDate)
        TextView tvOrderDate;
        @BindView(R.id.tvOrderStatus)
        TextView tvOrderStatus;
        @BindView(R.id.btnCancelOrder)
        Button btnCancelOrder;
        @BindView(R.id.btnDetailOrder)
        Button btnDetailOrder;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

        }
    }
}
