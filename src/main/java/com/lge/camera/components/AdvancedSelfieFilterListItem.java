package com.lge.camera.components;

import android.content.Context;

public class AdvancedSelfieFilterListItem {
    private int mFilmIndex;
    private String mFilterName;
    private String mFilterValue;
    protected int mViewDegree = 0;

    public void initFilterItem(Context c, String filterName, int filterIndex, String entryValue, int degree) {
        this.mFilterName = filterName;
        this.mFilmIndex = filterIndex;
        this.mFilterValue = entryValue;
        this.mViewDegree = degree;
    }

    public String getFilterValue() {
        return this.mFilterValue;
    }

    public int getFilterIndex() {
        return this.mFilmIndex;
    }

    public String getFilterName() {
        return this.mFilterName;
    }
}
