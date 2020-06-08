package com.alvin.computeraccessoriesstore.EventBus;

import com.alvin.computeraccessoriesstore.Model.SliderModel;

public class PopularSliderItemClick {
    private SliderModel sliderModel;

    public PopularSliderItemClick(SliderModel sliderModel) {
        this.sliderModel = sliderModel;
    }

    public SliderModel getSliderModel() {
        return sliderModel;
    }

    public void setSliderModel(SliderModel sliderModel) {
        this.sliderModel = sliderModel;
    }
}
