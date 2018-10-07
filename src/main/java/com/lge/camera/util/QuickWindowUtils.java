package com.lge.camera.util;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.SmartCoverManager;

public class QuickWindowUtils {
    public static final int DISPLAY_DURATION = 300;
    public static final float NORMAL_RESOLUTION_RATIO = 1.3333334f;
    public static final float WIDE_RESOLUTION_RATIO = 1.7777778f;
    private static boolean sIsQuickWindowCameraMode = false;
    private static boolean sIsQuickWindowCaseClosed = false;
    private static int sQuickCoverState = 0;
    public static QuickWindowUtils sQuickWindowUtils = null;
    private static SmartCoverManager sSmartCoverManager = null;

    public static QuickWindowUtils getInstance() {
        if (sQuickWindowUtils == null) {
            sQuickWindowUtils = new QuickWindowUtils();
        }
        return sQuickWindowUtils;
    }

    public static SmartCoverManager getSmartCoverManager(Activity activity) {
        if (sSmartCoverManager == null) {
            sSmartCoverManager = (SmartCoverManager) new LGContext(activity.getApplicationContext()).getLGSystemService("smartcover");
        }
        return sSmartCoverManager;
    }

    public static void setQuickWindowCameraFromIntent(Activity activity, Intent intent, boolean isSecure) {
        checkCurrentCoverStatus(getCurrentCoverStatus(activity));
        boolean isFromQuickWindowCase = false;
        getSmartCoverManager(activity);
        int cover_type = sSmartCoverManager.getCoverType();
        CamLog.m3d(CameraConstants.TAG, "cover type:" + cover_type);
        if (cover_type == 1 || cover_type == 3 || cover_type == 4 || cover_type == 6) {
            if (!(intent == null || intent.getAction() == null)) {
                isFromQuickWindowCase = CameraConstants.LAUNCH_FROM_SMARTCOVER.equals(intent.getAction());
            }
            if (isSupportQuickWindowCase(activity, intent) && isQuickWindowCaseClosed() && (isFromQuickWindowCase || isSecure)) {
                CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] set mode : quick window camera");
                setQuickWindowCameraMode(true);
                return;
            }
            setQuickWindowCameraMode(false);
            return;
        }
        setQuickWindowCameraMode(false);
    }

    private static void checkCurrentCoverStatus(int coverState) {
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] quick window case state:" + coverState);
        if (coverState == 0 || coverState == 2) {
            setQuickWindowCaseClosed(false);
        } else if (coverState == 1 || coverState == 4) {
            setQuickWindowCaseClosed(true);
        }
    }

    public static int getCurrentCoverStatus(Activity activity) {
        IntentFilter intentFilter = new IntentFilter(CameraConstants.ACTION_ACCESSORY_EVENT);
        intentFilter.addAction(CameraConstants.INTENT_ACTION_CAMERA_FINISH);
        Intent intent = activity.registerReceiver(null, intentFilter);
        if (intent == null || intent == null || !intent.getAction().equals(CameraConstants.ACTION_ACCESSORY_EVENT)) {
            return 0;
        }
        int coverState = intent.getIntExtra(CameraConstants.EXTRA_ACCESSORY_STATE, 0);
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] current cover state:" + coverState);
        return coverState;
    }

    private static boolean isSupportQuickWindowCase(Activity activity, Intent intent) {
        if (sSmartCoverManager == null ? false : sSmartCoverManager.getCoverSetting()) {
            CamLog.m3d(CameraConstants.TAG, "QuickWindowCase setting enable.");
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "QuickWindowCase setting disable.");
        return false;
    }

    public static void setQuickWindowCameraMode(boolean isQuickWindow) {
        sIsQuickWindowCameraMode = isQuickWindow;
    }

    public static boolean isQuickWindowCameraMode() {
        return sIsQuickWindowCameraMode;
    }

    public static boolean isQuickWindowCaseClosed() {
        return sIsQuickWindowCaseClosed;
    }

    public static void setQuickWindowCaseClosed(boolean set) {
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] set cover close : " + set);
        sIsQuickWindowCaseClosed = set;
    }

    public static void setQuickCoverState(int state) {
        sQuickCoverState = state;
    }

    public static int getQuickCoverState() {
        return sQuickCoverState;
    }
}
