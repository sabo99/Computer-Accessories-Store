package com.alvin.computeraccessoriesstore.Common;

import com.alvin.computeraccessoriesstore.Model.SliderModel;

import java.util.List;

public interface ILoadSliderImageCallbackListener {
    void onSliderLoadSuccess(List<SliderModel> sliderModelList);
    void onSliderLoadFailed(String message);
}
