package com.lge.camera.settings;

import android.graphics.drawable.LevelListDrawable;

public class ModeItem {
    private String mDesc = null;
    private LevelListDrawable mDrawable = null;
    private int mImgResId = 0;
    private boolean mIsDeletable = false;
    private boolean mIsSelected = false;
    private String mKey = null;
    private String mTitle = null;
    private String mValue = null;

    public ModeItem(String key, String value, String title, String desc, int imgResId) {
        this.mKey = key;
        this.mValue = value;
        this.mTitle = title;
        this.mDesc = desc;
        this.mImgResId = imgResId;
    }

    public ModeItem(String key, String value, String title, String desc, int imgResId, boolean seleted) {
        this.mKey = key;
        this.mValue = value;
        this.mTitle = title;
        this.mDesc = desc;
        this.mImgResId = imgResId;
        this.mIsSelected = seleted;
    }

    public ModeItem(String key, String value, String title, String desc, int imgResId, boolean seleted, boolean deletable) {
        this.mKey = key;
        this.mValue = value;
        this.mTitle = title;
        this.mDesc = desc;
        this.mImgResId = imgResId;
        this.mIsSelected = seleted;
        this.mIsDeletable = deletable;
    }

    public String getKey() {
        return this.mKey;
    }

    public String getValue() {
        return this.mValue;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getDescription() {
        return this.mDesc;
    }

    public int getImageResourceId() {
        return this.mImgResId;
    }

    public boolean isSelected() {
        return this.mIsSelected;
    }

    public boolean isDeletable() {
        return this.mIsDeletable;
    }

    public void setImageDrawable(LevelListDrawable drawable) {
        this.mDrawable = drawable;
    }

    public LevelListDrawable getImageDrawable() {
        return this.mDrawable;
    }

    public void setSelected(boolean selected) {
        this.mIsSelected = selected;
    }
}
