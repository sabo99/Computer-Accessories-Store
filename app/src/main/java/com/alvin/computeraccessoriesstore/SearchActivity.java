package com.alvin.computeraccessoriesstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import com.alvin.computeraccessoriesstore.Adapter.ItemsAdapter;
import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.ItemsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    LinearLayoutManager layoutManager;
    ItemsAdapter adapter, searchAdapter;
    List<ItemsModel> tempList = null;
    List<ItemsModel> result = null;
    List<ItemsModel> localDataSource = new ArrayList<>();

    @BindView(R.id.rvDataItemsSearch)
    RecyclerView rvDataItemsSearch;
    @BindView(R.id.searchBar)
    MaterialSearchBar searchBar;
    @BindView(R.id.pbSearch)
    ProgressBar pbSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
    }

    private void initViews() {
        ButterKnife.bind(this);

        rvDataItemsSearch.setVisibility(View.INVISIBLE);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvDataItemsSearch.setLayoutManager(layoutManager);

        retrieveData();

        searchBar.setHint("Search your gear");
        searchBar.setCardViewElevation(10);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                startSearchItems(text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled)
                    rvDataItemsSearch.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearchItems(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearchItems(CharSequence text) {
        result = new ArrayList<>();
        for (ItemsModel itemsModel : localDataSource) {
            if (itemsModel.getName().toLowerCase().contains(text) || itemsModel.getName().contains(text))
                result.add(itemsModel);
        }

        searchAdapter = new ItemsAdapter(SearchActivity.this, result);
        rvDataItemsSearch.setAdapter(searchAdapter);
    }

    private void retrieveData() {
        pbSearch.setVisibility(View.VISIBLE);
        DatabaseReference searchRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);
        searchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tempList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child(Common.CHILD_ITEMS).getValue() != null) {
                        for (DataSnapshot items : ds.child(Common.CHILD_ITEMS).getChildren()) {
                            ItemsModel itemsModel = items.getValue(ItemsModel.class);
                            tempList.add(itemsModel);
                        }
                    }
                }
                showData(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showData(List<ItemsModel> tempList) {
        localDataSource = tempList;
        adapter = new ItemsAdapter(SearchActivity.this, tempList);
        rvDataItemsSearch.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        pbSearch.setVisibility(View.INVISIBLE);
        rvDataItemsSearch.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchBar.enableSearch();
    }

    @Override
    protected void onStop() {
        searchBar.setText(null);
        searchBar.disableSearch();
        super.onStop();
    }
}
