package com.arcsoft.stickerlibrary.utils;

import android.util.Log;

public class LogUtil {
    public static final String LOG_TAG = "LGSticker";
    private static boolean mInitOrhanLogger = false;
    private static boolean mbEnableOrhanLogger = false;
    private static boolean mbEnabled = false;

    public static void setEnabled(boolean enabled) {
        mbEnabled = enabled;
    }

    public static boolean isEnabled() {
        return mbEnabled;
    }

    private static void createLogger() {
    }

    private static void initOrhanLogger() {
        if (!mInitOrhanLogger) {
            createLogger();
            mInitOrhanLogger = true;
        }
    }

    public static void LogV(String tag, String msg) {
        if (!mbEnabled) {
            return;
        }
        if (mbEnableOrhanLogger) {
            initOrhanLogger();
        } else {
            Log.v(LOG_TAG, getLogMessage(tag, msg));
        }
    }

    public static void LogD(String tag, String msg) {
        if (!mbEnabled) {
            return;
        }
        if (mbEnableOrhanLogger) {
            initOrhanLogger();
        } else {
            Log.d(LOG_TAG, getLogMessage(tag, msg));
        }
    }

    public static void LogI(String tag, String msg) {
        if (!mbEnabled) {
            return;
        }
        if (mbEnableOrhanLogger) {
            initOrhanLogger();
        } else {
            Log.i(LOG_TAG, getLogMessage(tag, msg));
        }
    }

    public static void LogW(String tag, String msg) {
        if (!mbEnabled) {
            return;
        }
        if (mbEnableOrhanLogger) {
            initOrhanLogger();
        } else {
            Log.w(LOG_TAG, getLogMessage(tag, msg));
        }
    }

    public static void LogE(String tag, String msg) {
        if (!mbEnabled) {
            return;
        }
        if (mbEnableOrhanLogger) {
            initOrhanLogger();
        } else {
            Log.e(LOG_TAG, getLogMessage(tag, msg));
        }
    }

    private static String getLogMessage(String tag, String msg) {
        return tag + " " + msg;
    }
}
