package com.alvin.computeraccessoriesstore.ui.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.computeraccessoriesstore.Adapter.ItemsAdapter;
import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.HideFABCart;
import com.alvin.computeraccessoriesstore.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemsFragment extends Fragment {

    private ItemsViewModel itemsViewModel;

    @BindView(R.id.rvDataItems)
    RecyclerView recycler_items;

    ItemsAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        itemsViewModel =
                ViewModelProviders.of(this).get(ItemsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_items, container, false);

        ButterKnife.bind(this, root);

        initViews();

        itemsViewModel.getListMutableLiveDataItemList().observe(this, itemsModels -> {
            adapter = new ItemsAdapter(getContext(), itemsModels);
            recycler_items.setAdapter(adapter);
        });

        return root;
    }

    private void initViews() {

        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.storeItemsSelected.getName());

        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new HideFABCart(true));

    }
}
