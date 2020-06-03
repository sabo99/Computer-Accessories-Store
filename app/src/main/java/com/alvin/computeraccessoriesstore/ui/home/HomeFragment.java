package com.alvin.computeraccessoriesstore.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Adapter.SliderAdapter;
import com.alvin.computeraccessoriesstore.Adapter.StoreAdapter;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.SearchActivity;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    @BindView(R.id.rvDataStore)
    RecyclerView recycler_store;
    @BindView(R.id.pbStore)
    ProgressBar progressBar;
    @BindView(R.id.imgSlider)
    SliderView sliderView;
    @BindView(R.id.pbSlider)
    ProgressBar progressBar2;
    @BindView(R.id.pbTitleHome)
    ProgressBar pbTitleHome;
    @BindView(R.id.rl_title_home)
    RelativeLayout rl_title_home;


    @OnClick(R.id.rl_action_search)
    void onSearchItem(){
        startActivity(new Intent(getContext(), SearchActivity.class));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(this, root);

        init();

        homeViewModel.getStoreList().observe(this, storeModelList -> {
            StoreAdapter adapter = new StoreAdapter(getContext(), storeModelList);

            if (storeModelList == null || storeModelList.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                pbTitleHome.setVisibility(View.GONE);
            } else {
                recycler_store.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                recycler_store.setVisibility(View.VISIBLE);

                pbTitleHome.setVisibility(View.GONE);
                rl_title_home.setVisibility(View.VISIBLE);
            }
        });

        homeViewModel.getSliderList().observe(this, sliderModels -> {
            SliderAdapter adapter = new SliderAdapter(getContext(), sliderModels);

            if (sliderModels == null || sliderModels.isEmpty()) {
                progressBar2.setVisibility(View.GONE);
            } else {
                progressBar2.setVisibility(View.GONE);
                sliderView.setVisibility(View.VISIBLE);
                sliderView.setSliderAdapter(adapter);
                sliderView.startAutoCycle();
            }
        });

        return root;
    }

    private void init() {

        setHasOptionsMenu(true);

        recycler_store.setVisibility(View.GONE);
        recycler_store.setHasFixedSize(true);
        recycler_store.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        sliderView.setIndicatorAnimation(IndicatorAnimations.THIN_WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);

        sliderView.setOnIndicatorClickListener(position -> {
            sliderView.setCurrentPagePosition(position);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
