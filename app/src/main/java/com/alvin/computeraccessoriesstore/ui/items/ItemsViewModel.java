package com.alvin.computeraccessoriesstore.ui.items;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.ItemsModel;

import java.util.List;

public class ItemsViewModel extends ViewModel {

    private MutableLiveData<List<ItemsModel>> listMutableLiveDataItemList;

    public ItemsViewModel() {

    }

    public MutableLiveData<List<ItemsModel>> getListMutableLiveDataItemList() {
        if (listMutableLiveDataItemList == null)
            listMutableLiveDataItemList = new MutableLiveData<>();

        listMutableLiveDataItemList.setValue(Common.storeItemsSelected.getItems());
        return listMutableLiveDataItemList;
    }
}