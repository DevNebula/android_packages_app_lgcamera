package com.lge.camera.components;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.drawable.Animatable2.AnimationCallback;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class QuickButton extends RotateImageButton {
    protected int[] mAnimationDrawableIds;
    private AnimatedVectorDrawable mAvd;
    protected int[] mClickEventMessages;
    protected int[] mContentDescriptionId;
    protected int[] mDrawableIds;
    protected int mIndex;
    protected String mKey;
    protected int mSelectedDrawableId;
    protected boolean mSetDisableColorFilter;
    protected int[] mStringIds;

    public QuickButton(Context context) {
        this(context, null);
    }

    public QuickButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIndex = 0;
        this.mKey = "";
        this.mSetDisableColorFilter = true;
        this.mStringIds = null;
    }

    public void init(Context context, QuickButtonType type) {
        this.mDrawableIds = type.mDrawableIds;
        this.mClickEventMessages = type.mClickEventMessages;
        this.mIndex = type.mInitDrawableIndex;
        this.mKey = type.mKey;
        this.mContentDescriptionId = type.mDescription;
        this.mSelectedDrawableId = type.mSelectedDrawableId;
        this.mSetDisableColorFilter = type.mSetDisableColorFilter;
        this.mStringIds = type.mStringIds;
        setId(type.mId);
        setImageResource(this.mDrawableIds[this.mIndex]);
        if (this.mStringIds == null || this.mStringIds[this.mIndex] == -1) {
            setText(null);
        } else {
            setText(getContext().getString(this.mStringIds[this.mIndex]));
        }
        setLayoutParams(new LayoutParams(-2, -2));
        setClickable(type.mClickable);
        setFocusable(type.mFocusable);
        setNextFocusUpId(type.mId);
        setContentDescription(getContext().getString(this.mContentDescriptionId[this.mIndex]));
        this.mContentDescriptionId = type.mDescription;
        setEnabled(type.mEnable);
        if (type.mBackground != null) {
            setBackground(type.mBackground);
        } else {
            setBackgroundResource(C0088R.drawable.cam_icon_empty);
        }
        setVisibility(type.mVisibility);
    }

    public int getIndex() {
        return this.mIndex;
    }

    public String getKey() {
        return this.mKey;
    }

    public int[] getStringIds() {
        return this.mStringIds;
    }

    public void setIndex(int index) {
        if (index >= this.mDrawableIds.length) {
            index = 0;
        }
        this.mIndex = index;
        setImageResource(this.mDrawableIds[this.mIndex]);
        if (this.mStringIds == null || this.mStringIds[this.mIndex] == -1) {
            setText(null);
        } else {
            setText(getContext().getString(this.mStringIds[this.mIndex]));
        }
        setContentDescription(getContext().getString(this.mContentDescriptionId[this.mIndex]));
    }

    public int[] getContentDescriptionStringId() {
        return this.mContentDescriptionId;
    }

    public void changeToNextIndex() {
        this.mIndex = (this.mIndex + 1) % this.mDrawableIds.length;
        CamLog.m3d(CameraConstants.TAG, "changeToNextIndex mDrawableIds " + this.mDrawableIds.length + ", index : " + this.mIndex);
        setImageResource(this.mDrawableIds[this.mIndex]);
        if (this.mStringIds == null || this.mStringIds[this.mIndex] == -1) {
            setText(null);
        } else {
            setText(getContext().getString(this.mStringIds[this.mIndex]));
        }
        setContentDescription(getContext().getString(this.mContentDescriptionId[this.mIndex]));
    }

    public int[] getDrawableIds() {
        return this.mDrawableIds;
    }

    public void setDrawableId(int[] values) {
        this.mDrawableIds = values;
    }

    public int[] getClickEventMessages() {
        return this.mClickEventMessages;
    }

    public int getSelectedDrawableId() {
        return this.mSelectedDrawableId;
    }

    public void setSelectedDrawableId(int selectedDrawableId) {
        this.mSelectedDrawableId = selectedDrawableId;
    }

    public boolean getSetDisableColorFilter() {
        return this.mSetDisableColorFilter;
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (!selected) {
            setImageResource(this.mDrawableIds[this.mIndex]);
        } else if (this.mSelectedDrawableId != 0) {
            setImageResource(this.mSelectedDrawableId);
        }
    }

    public void setColorFilter(ColorFilter cf) {
        if (this.mSetDisableColorFilter) {
            super.setColorFilter(cf);
        }
    }

    public int[] getAnimationDrawableIds() {
        return this.mAnimationDrawableIds;
    }

    public void setQuickButtonAnimationDrawableIds(int[] ids) {
        this.mAnimationDrawableIds = ids;
    }

    public boolean readyQuickButtonAnimation() {
        if (this.mAnimationDrawableIds == null || this.mAnimationDrawableIds[this.mIndex] == -1) {
            return false;
        }
        setImageResource(this.mAnimationDrawableIds[this.mIndex]);
        this.mAvd = (AnimatedVectorDrawable) getDrawable();
        return true;
    }

    public void startQuickButtonAnimation(AnimationCallback listener) {
        if (this.mAvd != null) {
            this.mAvd.registerAnimationCallback(listener);
            this.mAvd.start();
        }
    }

    public void restoreQuickButtonAnimationDrwable() {
        setImageResource(this.mDrawableIds[this.mIndex]);
    }
}
