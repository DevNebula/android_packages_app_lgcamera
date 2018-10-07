package com.lge.camera.managers;

import android.content.Context;
import com.lge.camera.C0088R;

public class ColorItem {
    private int mDrawableId;
    private boolean mIsSelected = false;
    private String mTitle = null;
    private String mValue = null;

    public ColorItem(Context context, String value) {
        this.mValue = value;
        this.mIsSelected = false;
        setResource(context);
    }

    public ColorItem(Context context, String value, boolean selected) {
        this.mValue = value;
        this.mIsSelected = selected;
        setResource(context);
    }

    private void setResource(Context context) {
        int titldId = C0088R.string.film_emulator_film_none;
        this.mDrawableId = C0088R.drawable.levellist_color_effect_none;
        if ("negative".equals(this.mValue)) {
            titldId = C0088R.string.color_effect_negative;
            this.mDrawableId = C0088R.drawable.levellist_color_effect_negative;
        } else if ("posterize".equals(this.mValue)) {
            titldId = C0088R.string.color_effect_posterize;
            this.mDrawableId = C0088R.drawable.levellist_color_effect_posterize;
        } else if ("aqua".equals(this.mValue)) {
            titldId = C0088R.string.color_effect_aqua;
            this.mDrawableId = C0088R.drawable.levellist_color_effect_aqua;
        } else if ("mono".equals(this.mValue)) {
            titldId = C0088R.string.color_effect_mono;
            this.mDrawableId = C0088R.drawable.levellist_color_effect_mono;
        } else if ("sepia".equals(this.mValue)) {
            titldId = C0088R.string.color_effect_sepia;
            this.mDrawableId = C0088R.drawable.levellist_color_effect_sepia;
        }
        this.mTitle = context.getString(titldId);
    }

    public ColorItem(String value, String title, int drawableId) {
        this.mValue = value;
        this.mTitle = title;
        this.mDrawableId = drawableId;
        this.mIsSelected = false;
    }

    public ColorItem(String value, String title, int drawableId, boolean selected) {
        this.mValue = value;
        this.mTitle = title;
        this.mDrawableId = drawableId;
        this.mIsSelected = selected;
    }

    public String getValue() {
        return this.mValue;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public boolean isSelected() {
        return this.mIsSelected;
    }

    public int getDrawableId() {
        return this.mDrawableId;
    }

    public void setValue(String mValue) {
        this.mValue = mValue;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }

    public void setDrawableId(int drawableId) {
        this.mDrawableId = drawableId;
    }
}
