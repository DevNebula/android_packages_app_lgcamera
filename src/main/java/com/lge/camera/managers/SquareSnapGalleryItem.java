package com.lge.camera.managers;

import android.graphics.Bitmap;
import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class SquareSnapGalleryItem {
    public static final int BITMAP_STATE_IDLE = 0;
    public static final int BITMAP_STATE_LCD_FIT = 1;
    public static final int BITMAP_STATE_ORIGINAL = 2;
    public int mBitmapState = 0;
    public Bitmap mPauseScreenBitmap;
    public Bitmap mThumbBitmap;
    public int mType;
    public Uri mUri;

    public SquareSnapGalleryItem(Uri uri, Bitmap bmp, int type) {
        this.mUri = uri;
        this.mThumbBitmap = bmp;
        this.mType = type;
        this.mPauseScreenBitmap = null;
        this.mBitmapState = 0;
    }

    public SquareSnapGalleryItem(SquareSnapGalleryItem item) {
        this.mUri = item.mUri;
        this.mThumbBitmap = item.mThumbBitmap;
        this.mType = item.mType;
        this.mPauseScreenBitmap = null;
        this.mBitmapState = 0;
    }

    public void unbind() {
        this.mUri = null;
        if (!(this.mThumbBitmap == null || this.mThumbBitmap.isRecycled())) {
            this.mThumbBitmap.recycle();
        }
        this.mThumbBitmap = null;
        if (!(this.mPauseScreenBitmap == null || this.mPauseScreenBitmap.isRecycled())) {
            this.mPauseScreenBitmap.recycle();
        }
        this.mPauseScreenBitmap = null;
        this.mBitmapState = 0;
    }

    public void setPauseBitmap(Bitmap bmp) {
        this.mPauseScreenBitmap = bmp;
    }

    public void unsetPauseBitmap() {
        if (!(this.mPauseScreenBitmap == null || this.mPauseScreenBitmap.isRecycled())) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] unsetPauseBitmap - recycle");
            this.mPauseScreenBitmap.recycle();
        }
        this.mPauseScreenBitmap = null;
    }
}
