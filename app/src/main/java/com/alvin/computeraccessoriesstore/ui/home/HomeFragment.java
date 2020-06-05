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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Adapter.SliderAdapter;
import com.alvin.computeraccessoriesstore.Adapter.StoreAdapter;
import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.HideFABCart;
import com.alvin.computeraccessoriesstore.HomeActivity;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.alvin.computeraccessoriesstore.ProfileActivity;
import com.alvin.computeraccessoriesstore.R;
import com.alvin.computeraccessoriesstore.SearchActivity;
import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    private HomeViewModel homeViewModel;

    @BindView(R.id.rvDataStore)
    RecyclerView recycler_store;
    @BindView(R.id.pbStore)
    ProgressBar progressBar1;
    @BindView(R.id.imgSlider)
    SliderView sliderView;
    @BindView(R.id.pbSlider)
    ProgressBar progressBar2;


    @OnClick(R.id.rl_action_search)
    void onSearchItem() {
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
                progressBar1.setVisibility(View.GONE);
            } else {
                recycler_store.setAdapter(adapter);
                progressBar1.setVisibility(View.GONE);
                recycler_store.setVisibility(View.VISIBLE);
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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.account_menu, menu);
        View view = menu.findItem(R.id.action_account_menu).getActionView();
        CircleImageView civProfile = view.findViewById(R.id.civProfile);
        civProfile.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ProfileActivity.class));
        });

        // InitViews
        initViewsImage(civProfile);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initViewsImage(CircleImageView civProfile) {
        StorageReference profileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
        if (profileRef != null) {
            profileRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        civProfile.setVisibility(View.VISIBLE);
                        Picasso.get().load(uri).into(civProfile);
                    });
        } else
            civProfile.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        EventBus.getDefault().postSticky(new HideFABCart(false));
    }
}
