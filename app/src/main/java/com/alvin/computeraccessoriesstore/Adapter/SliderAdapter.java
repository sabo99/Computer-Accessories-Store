package com.alvin.computeraccessoriesstore.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alvin.computeraccessoriesstore.EventBus.PopularSliderItemClick;
import com.alvin.computeraccessoriesstore.Model.SliderModel;
import com.alvin.computeraccessoriesstore.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.ViewHolder> {

    Context context;
    List<SliderModel> sliderModelList;

    public SliderAdapter(Context context, List<SliderModel> sliderModelList) {
        this.context = context;
        this.sliderModelList = sliderModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_slide, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        SliderModel sliderModel = sliderModelList.get(position);

        Picasso.get().load(sliderModel.getImage()).into(holder.imgSlider);
        holder.tvDescription.setText(sliderModel.getDescription());

        holder.itemView.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new PopularSliderItemClick(sliderModelList.get(position)));
        });
    }

    @Override
    public int getCount() {
        return sliderModelList.size();
    }

    public class ViewHolder extends SliderViewAdapter.ViewHolder{

        @BindView(R.id.imgSlider)
        ImageView imgSlider;
        @BindView(R.id.tvDescription)
        TextView tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
