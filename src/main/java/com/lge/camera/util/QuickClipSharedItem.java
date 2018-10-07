package com.lge.camera.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class QuickClipSharedItem {
    public String mActivityName;
    public Drawable mAppIcon;
    public int mAppIconResourceId;
    public String mLabel;
    public String mPackageName;
    public Drawable mResizeAppIcon;

    public QuickClipSharedItem(Drawable appIcon, String label, String packageName, String name) {
        this.mAppIcon = appIcon;
        this.mResizeAppIcon = appIcon;
        this.mLabel = label;
        this.mPackageName = packageName;
        this.mActivityName = name;
    }

    public QuickClipSharedItem(Drawable appIcon, String label, String packageName, String name, int iconResource) {
        this.mAppIcon = appIcon;
        this.mResizeAppIcon = appIcon;
        this.mLabel = label;
        this.mPackageName = packageName;
        this.mActivityName = name;
        this.mAppIconResourceId = iconResource;
    }

    public Drawable getAppIcon() {
        return this.mAppIcon;
    }

    public int getAppIconResource() {
        return this.mAppIconResourceId;
    }

    public Drawable getAppIcon(Resources res, int width, int height) {
        if (this.mResizeAppIcon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) this.mResizeAppIcon).getBitmap();
            if (!(bitmap.isRecycled() || (bitmap.getWidth() == width && bitmap.getHeight() == height))) {
                this.mResizeAppIcon = new BitmapDrawable(res, Bitmap.createScaledBitmap(bitmap, width, height, true));
            }
        }
        return this.mResizeAppIcon;
    }

    public void setAppIcon(Drawable mAppIcon) {
        this.mAppIcon = mAppIcon;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public String getActivityName() {
        return this.mActivityName;
    }

    public void setActivityName(String mActivityName) {
        this.mActivityName = mActivityName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }
}
