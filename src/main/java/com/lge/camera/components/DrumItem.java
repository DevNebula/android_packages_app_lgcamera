package com.lge.camera.components;

import com.lge.camera.constants.LdbConstants;

public class DrumItem {
    public static final int RECOMMEND_TYPE_END = 3;
    public static final int RECOMMEND_TYPE_INBOUND = 2;
    public static final int RECOMMEND_TYPE_NONE = 0;
    public static final int RECOMMEND_TYPE_START = 1;
    public int mEndBarColor = 0;
    public boolean mIsShowTitle = true;
    public String mKey = "none";
    public int mPosition = 0;
    public boolean mRecommend = false;
    public int mRecommendType = 0;
    public int mResId = 0;
    public boolean mSelected = false;
    public int mStartBarColor = 0;
    public String mTitle = LdbConstants.LDB_LOOP_RECORDING_NONE;
    public String mValue = "none";

    public int getIconResId() {
        return this.mResId;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getKey() {
        return this.mKey;
    }

    public String getValue() {
        return this.mValue;
    }

    public boolean getCorrection() {
        return this.mRecommend;
    }

    public boolean getSelected() {
        return this.mSelected;
    }
}
