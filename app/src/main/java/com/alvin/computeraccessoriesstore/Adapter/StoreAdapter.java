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
import com.alvin.computeraccessoriesstore.Common.IRecyclerClickListener;
import com.alvin.computeraccessoriesstore.EventBus.StoreItemClick;
import com.alvin.computeraccessoriesstore.Model.StoreModel;
import com.alvin.computeraccessoriesstore.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    Context context;
    List<StoreModel> storeModelList;

    public StoreAdapter(Context context, List<StoreModel> storeModelList) {
        this.context = context;
        this.storeModelList = storeModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_store, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreModel list = storeModelList.get(position);

        holder.name.setText(list.getName());
        Picasso.get().load(list.getImage()).into(holder.image);

        // Event
        holder.setListener((view, pos) -> {
            Common.storeItemsSelected = storeModelList.get(pos);
            EventBus.getDefault().postSticky(new StoreItemClick(true,storeModelList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return storeModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.imageItem)
        ImageView image;

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

    @Override
    public int getItemViewType(int position) {
        if (storeModelList.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;
        else
        {
            if (storeModelList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position > 1 && position == storeModelList.size() -1)? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
        }
    }
}
