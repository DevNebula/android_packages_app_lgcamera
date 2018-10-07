package com.arcsoft.stickerlibrary.codec;

import android.util.Log;

public class CodecLog {
    private static boolean mEnableLog = false;

    public static void enableLog(boolean isEnableLog) {
        mEnableLog = isEnableLog;
    }

    /* renamed from: d */
    public static void m41d(String tag, String msg) {
        if (mEnableLog) {
            Log.d(tag, msg);
        }
    }

    /* renamed from: i */
    public static void m43i(String tag, String msg) {
        if (mEnableLog) {
            Log.i(tag, msg);
        }
    }

    /* renamed from: e */
    public static void m42e(String tag, String msg) {
        if (mEnableLog) {
            Log.e(tag, msg);
        }
    }
}
