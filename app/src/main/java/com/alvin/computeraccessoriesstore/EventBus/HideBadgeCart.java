package com.alvin.computeraccessoriesstore.EventBus;

public class HideBadgeCart {
    private boolean hidden;

    public HideBadgeCart(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
