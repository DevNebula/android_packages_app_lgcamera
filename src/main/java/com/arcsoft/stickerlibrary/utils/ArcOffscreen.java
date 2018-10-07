package com.arcsoft.stickerlibrary.utils;

import android.support.annotation.NonNull;

public class ArcOffscreen {
    public static final int ASVL_PAF_I420 = 1537;
    public static final int ASVL_PAF_NV12 = 2049;
    public static final int ASVL_PAF_NV21 = 2050;
    public static final int ASVL_PAF_RGB32_A8R8G8B8 = 772;
    public static final int ASVL_PAF_RGB32_R8G8B8A8 = 773;
    public static final int ASVL_PAF_YUYV = 1281;
    private int mFormat;
    private byte[] mFrameData;
    private int mHeight;
    private int mWidth;

    public ArcOffscreen(int width, int height, int format) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFormat = format;
        this.mFrameData = null;
    }

    public ArcOffscreen(int width, int height, int format, @NonNull byte[] frameData) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFormat = format;
        this.mFrameData = frameData;
    }

    public int getFrameFormat() {
        return this.mFormat;
    }

    public void setFormat(int format) {
        this.mFormat = format;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public void setFrameData(@NonNull byte[] frameData) {
        this.mFrameData = frameData;
    }

    public byte[] getFrameData() {
        return this.mFrameData;
    }
}
