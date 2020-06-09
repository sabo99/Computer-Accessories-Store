package com.alvin.computeraccessoriesstore.ui.items_detail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.ItemsModel;

public class ItemsDetailViewModel extends ViewModel {

    private MutableLiveData<ItemsModel> mutableLiveDataItems;

    public ItemsDetailViewModel() {

    }

    public MutableLiveData<ItemsModel> getMutableLiveDataItems() {
        if (mutableLiveDataItems == null)
            mutableLiveDataItems = new MutableLiveData<>();
        mutableLiveDataItems.setValue(Common.selectedItems);
        return mutableLiveDataItems;
    }
}