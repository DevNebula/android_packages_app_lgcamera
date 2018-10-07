package com.lge.graphy.data;

import android.content.Context;
import android.graphics.Bitmap;

public class GraphyFunctionItem extends GraphyItem {
    private static final String BEST_CATEGORY_NAME = "Best";
    private static final String GRAPHY_NAME = "Graphy";
    private static final String MY_FILTER_CATEGORY_NAME = "My Filter";
    private static final String NONE_NAME = "None";
    private String mCategoryName = "Best";
    private int mCategoryType = 1;

    public GraphyFunctionItem(Context context, int type) {
        super(context);
        this.mType = type;
    }

    public int getIntValue(String key) {
        if (GraphyItem.KEY_CATEGORY_ID_INT.equals(key)) {
            return this.mCategoryType;
        }
        return -1;
    }

    public void setIntValue(String key, int value) {
        this.mCategoryType = value;
        switch (value) {
            case 0:
                this.mCategoryName = "None";
                return;
            case 1:
                this.mCategoryName = "Best";
                return;
            case 2:
                this.mCategoryName = MY_FILTER_CATEGORY_NAME;
                return;
            case 3:
                this.mCategoryName = GRAPHY_NAME;
                return;
            default:
                return;
        }
    }

    public String getStringValue(String key) {
        if (GraphyItem.KEY_CATEGORY_NAME_STR.equals(key)) {
            return this.mCategoryName;
        }
        return null;
    }

    public void setStringValue(String key, String value) {
    }

    public Bitmap getBitmap() {
        return null;
    }

    public double getDoubleValue(String key) {
        return -100.0d;
    }

    public void setDoubleValue(String key, double value) {
    }
}
