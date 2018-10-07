package com.lge.camera.util;

import android.os.SystemProperties;
import android.os.Trace;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;

public class CamLog {
    private static final int REAL_METHOD_POS = 2;
    private static boolean sLogOn = true;
    private static boolean sTagExceptionLogOn = false;
    private static int sUiHashCode = 0;

    public enum TraceTag {
        MANDATORY,
        OPTIONAL
    }

    private static String prefix() {
        StackTraceElement realMethod = new Throwable().getStackTrace()[2];
        return "[" + realMethod.getFileName() + ":" + realMethod.getLineNumber() + ":" + realMethod.getMethodName() + "()-[Thread:" + (sUiHashCode == Thread.currentThread().hashCode() ? "UI" : "Other") + "] ";
    }

    public static void setUiThreadHashCode(int uiHashCode) {
        sUiHashCode = uiHashCode;
    }

    public static void setLogOn() {
        int logServiceEnable = SystemProperties.getInt("persist.service.main.enable", 0);
        if (logServiceEnable > 0) {
            Log.d(CameraConstants.TAG, "####### logServiceEnable = " + logServiceEnable + " : Log service is enable. You can debug log messages. ");
            sLogOn = true;
            return;
        }
        Log.d(CameraConstants.TAG, "####### logServiceEnable = " + logServiceEnable + " : Log service is disable. Please set log service to enable for debug. ");
        sLogOn = false;
    }

    public static String getCurThread() {
        return sUiHashCode == Thread.currentThread().hashCode() ? "UI" : "Other";
    }

    public static boolean getLogOn() {
        return sLogOn;
    }

    public static boolean isTagExceptionLogOn() {
        return sTagExceptionLogOn;
    }

    /* renamed from: d */
    public static void m3d(String tag, String msg) {
        if (sLogOn) {
            Log.d(tag, prefix() + msg);
        }
    }

    /* renamed from: i */
    public static void m7i(String tag, String msg) {
        if (sLogOn) {
            Log.i(tag, prefix() + msg);
        }
    }

    /* renamed from: e */
    public static void m5e(String tag, String msg) {
        Log.e(tag, prefix() + msg);
    }

    /* renamed from: v */
    public static void m9v(String tag, String msg) {
        if (sLogOn) {
            Log.v(tag, prefix() + msg);
        }
    }

    /* renamed from: w */
    public static void m11w(String tag, String msg) {
        if (sLogOn) {
            Log.w(tag, prefix() + msg);
        }
    }

    /* renamed from: d */
    public static void m4d(String tag, String msg, Throwable tr) {
        if (sLogOn) {
            Log.d(tag, prefix() + msg, tr);
        }
    }

    /* renamed from: i */
    public static void m8i(String tag, String msg, Throwable tr) {
        if (sLogOn) {
            Log.i(tag, prefix() + msg, tr);
        }
    }

    /* renamed from: e */
    public static void m6e(String tag, String msg, Throwable tr) {
        if (sLogOn) {
            Log.e(tag, prefix() + msg, tr);
        }
    }

    /* renamed from: v */
    public static void m10v(String tag, String msg, Throwable tr) {
        if (sLogOn) {
            Log.v(tag, prefix() + msg, tr);
        }
    }

    /* renamed from: w */
    public static void m12w(String tag, String msg, Throwable tr) {
        if (sLogOn) {
            Log.w(tag, prefix() + msg, tr);
        }
    }

    public static void traceBegin(TraceTag tag, String msg, int cookie) {
        if (sLogOn || tag == TraceTag.MANDATORY) {
            Trace.asyncTraceBegin(PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID, "##" + msg, cookie);
        }
    }

    public static void traceEnd(TraceTag tag, String msg, int cookie) {
        if (sLogOn || tag == TraceTag.MANDATORY) {
            Trace.asyncTraceEnd(PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID, "##" + msg, cookie);
        }
    }
}
