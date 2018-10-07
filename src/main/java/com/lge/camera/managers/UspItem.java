package com.lge.camera.managers;

public class UspItem {
    private boolean mIsSelected = false;
    private int mLayoutId;
    private int mTitleId;
    private String mValue;

    public UspItem(int titleId, String value, int layoutId) {
        this.mTitleId = titleId;
        this.mValue = value;
        this.mLayoutId = layoutId;
    }

    public UspItem(int titleId, boolean selected, String value, int layoutId) {
        this.mTitleId = titleId;
        this.mIsSelected = selected;
        this.mValue = value;
        this.mLayoutId = layoutId;
    }

    public int getTitleId() {
        return this.mTitleId;
    }

    public void setTitleId(int titleId) {
        this.mTitleId = titleId;
    }

    public boolean isSelected() {
        return this.mIsSelected;
    }

    public void setSelected(boolean selected) {
        this.mIsSelected = selected;
    }

    public String getValue() {
        return this.mValue;
    }

    public void setValue(String value) {
        this.mValue = value;
    }

    public int getLayoutId() {
        return this.mLayoutId;
    }
}
