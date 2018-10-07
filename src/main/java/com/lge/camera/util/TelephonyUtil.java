package com.lge.camera.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.lge.camera.constants.CameraConstants;

public class TelephonyUtil {
    public static int getPhoneCallState(Context context) {
        int callState = ((TelephonyManager) context.getSystemService("phone")).getCallState();
        CamLog.m3d(CameraConstants.TAG, "TelephonyUtil : Phone call state = " + callState);
        return callState;
    }

    public static boolean phoneIsIdle(int callState) {
        return callState == 0;
    }

    public static boolean phoneIsOffhook(int callState) {
        return callState == 2;
    }

    public static boolean phoneInCall(Context context) {
        return phoneInCall(getPhoneCallState(context));
    }

    public static boolean phoneInCall(int callState) {
        return !phoneIsIdle(callState) || phoneIsOffhook(callState);
    }

    public static boolean phoneInVTCall(Context context) {
        return phoneInVTCall(getPhoneCallState(context));
    }

    public static boolean phoneInVTCall(int callState) {
        CamLog.m3d(CameraConstants.TAG, "phoneInVTCall : vtCallState = " + callState);
        return callState >= 100;
    }

    public static boolean isRinging(Context context) {
        return getPhoneCallState(context) == 1;
    }
}
