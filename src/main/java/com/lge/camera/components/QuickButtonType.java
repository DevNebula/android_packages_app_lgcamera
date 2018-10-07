package com.lge.camera.components;

import android.graphics.drawable.Drawable;

public class QuickButtonType {
    public static final int FIRST_INDEX = 0;
    public static final int NONE = 0;
    public int[] mAnimationDrawableIds;
    public Drawable mBackground;
    public int[] mClickEventMessages;
    public boolean mClickable;
    public int[] mDescription;
    public int[] mDrawableIds;
    public boolean mEnable;
    public boolean mFocusable;
    public int mHeight;
    public int mId;
    public int mInitDrawableIndex;
    public String mKey;
    public boolean mNeedDefault;
    public int mSelectedDrawableId;
    public boolean mSetDisableColorFilter;
    public int[] mStringIds;
    public int mVisibility;
    public int mWidth;

    public QuickButtonType(int id, String key, int width, int height, boolean clickable, boolean focusable, int[] description, boolean enable, int[] drawableIds, int[] messages, int initDrawableIndex, Drawable background, int visibility) {
        this.mKey = "";
        this.mEnable = true;
        this.mNeedDefault = false;
        this.mSetDisableColorFilter = true;
        this.mInitDrawableIndex = 0;
        this.mVisibility = 0;
        this.mSelectedDrawableId = 0;
        this.mId = id;
        this.mKey = key;
        this.mWidth = width;
        this.mHeight = height;
        this.mClickable = clickable;
        this.mFocusable = focusable;
        this.mDescription = description;
        this.mEnable = enable;
        this.mDrawableIds = drawableIds;
        this.mClickEventMessages = messages;
        this.mInitDrawableIndex = initDrawableIndex;
        this.mBackground = background;
        this.mVisibility = visibility;
    }

    public QuickButtonType(int id, String key, int width, int height, boolean clickable, boolean focusable, int[] description, boolean enable, int[] drawableIds, int[] messages, int initDrawableIndex, Drawable background, int visibility, int selectedDrawableId) {
        this(id, key, width, height, clickable, focusable, description, enable, drawableIds, messages, initDrawableIndex, background, visibility);
        this.mSelectedDrawableId = selectedDrawableId;
    }

    public QuickButtonType(int id, String key, int width, int height, boolean clickable, boolean focusable, int[] description, boolean enable, int[] drawableIds, int[] messages, int initDrawableIndex, Drawable background, int visibility, int selectedDrawableId, boolean setDisableColorFilter) {
        this(id, key, width, height, clickable, focusable, description, enable, drawableIds, messages, initDrawableIndex, background, visibility);
        this.mSelectedDrawableId = selectedDrawableId;
        this.mSetDisableColorFilter = setDisableColorFilter;
    }

    public QuickButtonType(int id, String key, int width, int height, boolean clickable, boolean focusable, int[] description, boolean enable, int[] drawableIds, int[] messages, int initDrawableIndex, Drawable background, int visibility, int selectedDrawableId, boolean setDisableColorFilter, int[] stringId, int[] animationDrawableIds) {
        this(id, key, width, height, clickable, focusable, description, enable, drawableIds, messages, initDrawableIndex, background, visibility);
        this.mSelectedDrawableId = selectedDrawableId;
        this.mSetDisableColorFilter = setDisableColorFilter;
        this.mStringIds = stringId;
        this.mAnimationDrawableIds = animationDrawableIds;
    }

    public void setDrawableId(int[] values) {
        this.mDrawableIds = values;
    }

    public void setStringId(int[] stringIds) {
        this.mStringIds = stringIds;
    }

    public void setAnimationDrwableIds(int[] ids) {
        this.mAnimationDrawableIds = ids;
    }
}
