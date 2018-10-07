package com.lge.camera.util;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;

public class TalkBackUtil {
    private static boolean sIsAnnounced = false;

    public static String getHours(Context context, int hours) {
        String result = "";
        if (hours == 1) {
            return String.valueOf(hours) + context.getString(C0088R.string.timer_hour);
        }
        if (hours > 1) {
            return String.valueOf(hours) + context.getString(C0088R.string.hours);
        }
        return result;
    }

    public static String getMinutes(Context context, int minutes) {
        String result = "";
        if (minutes == 1) {
            return String.valueOf(minutes) + context.getString(C0088R.string.timer_minute);
        }
        if (minutes > 1) {
            return String.valueOf(minutes) + context.getString(C0088R.string.sp_minutes_NORMAL);
        }
        return result;
    }

    public static String getSeconds(Context context, int seconds, boolean needZero) {
        String result = needZero ? 0 + context.getString(C0088R.string.timer_second) : "";
        if (seconds == 1) {
            return String.valueOf(seconds) + context.getString(C0088R.string.timer_second);
        }
        if (seconds > 1) {
            return String.valueOf(seconds) + context.getString(C0088R.string.seconds);
        }
        return result;
    }

    public static void sendAccessibilityEvent(Context context, String className, String description) {
        if (context != null && className != null && description != null && ViewUtil.isAccessibilityServiceEnabled(context)) {
            AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
            AccessibilityEvent event = AccessibilityEvent.obtain(128);
            if (am.isEnabled() && event != null) {
                event.setClassName(className);
                event.setPackageName(context.getPackageName());
                event.getText().add(description);
                event.setAddedCount(description.length());
                am.sendAccessibilityEvent(event);
            }
        }
    }

    public static void setTalkbackDescOnDoubleTap(Context context, String description) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
        if (am.isEnabled() && am.isTouchExplorationEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(16384);
            CamLog.m3d(CameraConstants.TAG, "Talkback desc on double tap: " + description);
            event.getText().add(description);
            am.sendAccessibilityEvent(event);
        }
    }

    public static String makePictureDescription(Context context, String text) {
        if (context == null || text == null) {
            return null;
        }
        return text.replace(".", context.getString(C0088R.string.talkback_dot)).replace("MP", " MP");
    }

    public static void setCameraTypeAnnounced(boolean set) {
        sIsAnnounced = set;
    }

    public static void sendCameraTypeAccessibilityEvent(Context context, String className) {
        if (!sIsAnnounced) {
            setCameraTypeAnnounced(true);
            sendAccessibilityEvent(context, className, context.getString(CameraDeviceUtils.isRearCamera(SharedPreferenceUtil.getCameraId(context)) ? C0088R.string.rear_camera : C0088R.string.front_camera));
        }
    }
}
