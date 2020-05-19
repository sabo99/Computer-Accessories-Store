package com.alvin.computeraccessoriesstore.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Common.ILoadSliderImageCallbackListener;
import com.alvin.computeraccessoriesstore.Common.IStoreCallbackListener;
import com.alvin.computeraccessoriesstore.Model.SliderModel;
import com.alvin.computeraccessoriesstore.Model.StoreModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IStoreCallbackListener, ILoadSliderImageCallbackListener {

    private MutableLiveData<List<SliderModel>> sliderList;
    private MutableLiveData<List<StoreModel>> storeList;
    private MutableLiveData<String> messageError;
    private IStoreCallbackListener listenerStore;
    private ILoadSliderImageCallbackListener listenerSlider;

    public HomeViewModel() {
        listenerStore = this;
        listenerSlider = this;
    }

    public MutableLiveData<List<SliderModel>> getSliderList() {
        if (sliderList == null){
            sliderList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadSliderImage();
        }
        return sliderList;
    }

    private void loadSliderImage() {
        List<SliderModel> tempList = new ArrayList<>();
        DatabaseReference sliderRef = FirebaseDatabase.getInstance().getReference(Common.SLIDER_REF);
        sliderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    SliderModel sliderModel = ds.getValue(SliderModel.class);
                    tempList.add(sliderModel);
                }
                listenerSlider.onSliderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listenerSlider.onSliderLoadFailed(databaseError.getMessage());
            }
        });
    }

    public MutableLiveData<List<StoreModel>> getStoreList() {
        if (storeList == null){
            storeList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadItemStore();
        }
        return storeList;
    }

    private void loadItemStore() {
        List<StoreModel> tempList = new ArrayList<>();
        DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference(Common.STORE_REF);
        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    StoreModel storeModel = ds.getValue(StoreModel.class);
                    storeModel.setItem_id(ds.getKey());
                    tempList.add(storeModel);
                }
                listenerStore.onStoreLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listenerStore.onStoreLoadFailed(databaseError.getMessage());
            }
        });

    }

    @Override
    public void onStoreLoadSuccess(List<StoreModel> storeModelList) {
        storeList.setValue(storeModelList);
    }

    @Override
    public void onStoreLoadFailed(String message) {
        messageError.setValue(message);
    }


    @Override
    public void onSliderLoadSuccess(List<SliderModel> sliderModelModelList) {
        sliderList.setValue(sliderModelModelList);
    }

    @Override
    public void onSliderLoadFailed(String message) {
        messageError.setValue(message);
    }
}