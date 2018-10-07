package com.arcsoft.stickerlibrary.utils;

public final class ArcBuff {
    public static final int FORMAT_ARGB8888 = 772;
    public static final int FORMAT_GREY = 1793;
    public static final int FORMAT_NV21 = 2050;
    public static final int FORMAT_UNKOWN = 0;
    private static final int PITCH_SIZE = 4;
    public int mFormat;
    public int mHeight;
    public boolean mIsContinues = true;
    public int[] mPitches = new int[4];
    public int mWidth;
    public long nativeBuff;
    public int nativeBuffSize;
    public long vuNativeBuff;
    public long yNativeBuff;
}
