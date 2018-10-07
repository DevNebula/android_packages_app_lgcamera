package com.lge.camera.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManagerEx;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.p000v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import java.net.URISyntaxException;
import java.util.List;

public class AppControlUtil extends AppControlUtilBase {
    private static float sBackupTransitionScale;
    private static float sBackupWidowScale;
    private static int sCurMultiViewFrameType = 0;
    private static int sCurrentBatteryLevel = 99;
    private static float sCurrentVoltageLevel = 0.0f;
    public static boolean sIsChangeAnimationScale = false;
    private static boolean sIsGalleryLaunched = false;
    private static boolean sIsNeedQuickShotTaking = false;
    private static boolean sIsPopoutFirstTakePictureBefore = false;
    private static boolean sIsQuickShotMode = false;
    private static boolean sIsStartFromOnCreate = false;
    private static boolean sIsTouchShotOnMultiView = false;
    public static boolean sIsTurnOffWindowAnimation = true;
    private static boolean sIsViewModeChangedForConeUIByPhoneState = false;
    public static int sSETTING_EXPAND_MAX_COLUMN = 3;

    public static boolean isQuickShotMode() {
        return sIsQuickShotMode;
    }

    public static void setQuickShotMode(boolean lock) {
        sIsQuickShotMode = lock;
    }

    public static boolean isNeedQuickShotTaking() {
        return sIsNeedQuickShotTaking;
    }

    public static void setNeedQuickShotTaking(boolean lock) {
        sIsNeedQuickShotTaking = lock;
    }

    public static void setQuickShotCondition(Activity activity) {
        setQuickShotMode(false);
        setNeedQuickShotTaking(false);
        if ((1048576 & activity.getIntent().getFlags()) == 0) {
            boolean isHighTier;
            boolean isEnabled;
            if (ConfigurationUtil.sAPP_TIER >= 4) {
                isHighTier = true;
            } else {
                isHighTier = false;
            }
            boolean isVolumeKey = checkFromVolumeKey(activity.getIntent());
            if (Global.getInt(activity.getContentResolver(), "shortcutkey_quickshot_enabled", 1) == 1) {
                isEnabled = true;
            } else {
                isEnabled = false;
            }
            CamLog.m3d(CameraConstants.TAG, "Quickshot condition :" + isEnabled + "," + isHighTier + "," + isVolumeKey);
            if (isEnabled && isHighTier && isVolumeKey) {
                setQuickShotMode(true);
                setNeedQuickShotTaking(true);
            }
        }
    }

    public static boolean isChangedViewModeForConeUIByPhoneState(Context context) {
        if (FunctionProperties.isSupportedConeUI()) {
            return sIsViewModeChangedForConeUIByPhoneState;
        }
        return false;
    }

    public static void setViewModeChangedForConeUIByPhoneState(boolean changed, Context context) {
        if (FunctionProperties.isSupportedConeUI()) {
            sIsViewModeChangedForConeUIByPhoneState = changed;
        } else {
            sIsViewModeChangedForConeUIByPhoneState = false;
        }
    }

    public static boolean getEnableSafetyCare(Context context) {
        boolean remoteCareEnabled = Boolean.parseBoolean(System.getString(context.getContentResolver(), CameraConstants.SETTINGS_VALUE_REMOVE_CARE_ENABLED));
        CamLog.m3d(CameraConstants.TAG, "getEnableSafetyCare=" + remoteCareEnabled);
        return remoteCareEnabled;
    }

    public static void configureWindowFlag(Window window, boolean fullScreen, boolean isSecureCamera, boolean isQuickWindowCamera, boolean isQuickCamera) {
        if (window != null) {
            if (fullScreen) {
                window.addFlags(1024);
            }
            CamLog.m3d(CameraConstants.TAG, "set flag for SecureCamera : " + isSecureCamera + " & QuickWindowCamera  : " + isQuickWindowCamera + " & QuickCamera : " + isQuickCamera);
            if (isSecureCamera || isQuickWindowCamera) {
                window.addFlags(6815744);
            } else if (isQuickCamera) {
                window.addFlags(6291456);
            }
        }
    }

    public static void wakedUpScreen(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        if (!powerManager.isInteractive()) {
            WakeLock wakeLock = powerManager.newWakeLock(805306394, context.getClass().getName());
            wakeLock.setReferenceCounted(false);
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            }
        }
    }

    public static boolean isCameraIntentFromCase(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        return CameraConstants.INTENT_ACTION_CAMERA_START_FROM_COVER.equals(intent.getAction());
    }

    public static boolean isGuestMode() {
        if ("kids".equals(SystemProperties.get("service.plushome.currenthome"))) {
            return true;
        }
        return false;
    }

    public static boolean checkGalleryEnabledOnGuestMode(ContentResolver contentResolver) {
        if (!isGuestMode()) {
            return false;
        }
        String gallery = CameraConstants.PACKAGE_GALLERY;
        String authority = "com.lge.launcher2.settings";
        String table_favorite = "favorites";
        String parameter_notify = "notify";
        String selection = "container=-103";
        String intentStr = "intent";
        Cursor c = contentResolver.query(Uri.parse("content://com.lge.launcher2.settings/favorites?notify=true"), null, "container=-103", null, null);
        if (c == null) {
            return false;
        }
        try {
            int intentIndex = c.getColumnIndexOrThrow("intent");
            Intent intent = null;
            while (c.moveToNext()) {
                try {
                    intent = Intent.parseUri(c.getString(intentIndex), 0);
                    if (CameraConstants.PACKAGE_GALLERY.equals(intent.getComponent().getPackageName())) {
                        c.close();
                        return true;
                    }
                    CamLog.m3d(CameraConstants.TAG, "intent " + intent);
                } catch (URISyntaxException e) {
                }
            }
            c.close();
            return false;
        } catch (Exception e2) {
            CamLog.m12w(CameraConstants.TAG, "Desktop items loading interrupted:", e2);
        } catch (Throwable th) {
            c.close();
        }
    }

    public static void turnOffAnimation() {
        if (sIsTurnOffWindowAnimation && !sIsChangeAnimationScale) {
            CamLog.m3d(CameraConstants.TAG, "turnOffAnimation");
            sIsChangeAnimationScale = true;
            IWindowManager windowManager = Stub.asInterface(ServiceManager.getService("window"));
            try {
                sBackupWidowScale = windowManager.getAnimationScale(0);
                sBackupTransitionScale = windowManager.getAnimationScale(1);
                windowManager.setAnimationScale(0, 0.0f);
                windowManager.setAnimationScale(1, 0.0f);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (NoSuchMethodError e2) {
                e2.printStackTrace();
            }
        }
    }

    public static void turnOnAnimation() {
        if (sIsTurnOffWindowAnimation && sIsChangeAnimationScale) {
            sIsChangeAnimationScale = false;
            CamLog.m3d(CameraConstants.TAG, "turnOnAnimation");
            IWindowManager windowManager = Stub.asInterface(ServiceManager.getService("window"));
            try {
                windowManager.setAnimationScale(0, sBackupWidowScale);
                windowManager.setAnimationScale(1, sBackupTransitionScale);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (NoSuchMethodError e2) {
                e2.printStackTrace();
            }
        }
    }

    public static boolean checkFromBleKey(Intent intent) {
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra(CameraConstants.LAUNCH_FROM_BLE_ONEKEY, false);
    }

    public static boolean checkFromVolumeKey(Intent intent) {
        if (intent != null && intent.getIntExtra(CameraConstants.EXTRA_CAMERA_LAUNCH_PATH, 0) == 4) {
            return true;
        }
        return false;
    }

    public static void setSystemSettingUseSDcard(ContentResolver cr, String storage) {
        if (cr != null) {
            int type = 0;
            if (CameraConstants.STORAGE_NAME_EXTERNAL.equals(storage)) {
                type = 1;
            } else if (CameraConstants.STORAGE_NAME_NAS.equals(storage)) {
                type = 2;
            }
            CamLog.m3d(CameraConstants.TAG, "set use SD card =" + storage + " setValue " + type);
            try {
                System.putInt(cr, CameraConstants.KEY_SAVE_TO_SD, type);
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, "fail to write KEY_SAVE_TO_SD: " + e);
                e.printStackTrace();
            }
        }
    }

    public static void setBypassAttribute(Window window, int bypassKeyCode, boolean bypass) {
        if (window != null) {
            try {
                CamLog.m3d(CameraConstants.TAG, "bypass key, bypassKeyCode = " + bypassKeyCode);
                LayoutParams attrs = window.getAttributes();
                if (bypass) {
                    attrs.bypassKeyFlags |= bypassKeyCode;
                } else {
                    attrs.bypassKeyFlags &= bypassKeyCode ^ -1;
                }
                window.setAttributes(attrs);
            } catch (Exception e) {
                e.printStackTrace();
                CamLog.m5e(CameraConstants.TAG, "Cannot bypass key, message = " + e.getMessage());
            }
        }
    }

    public static int getSettingExpandMaxColumn() {
        return sSETTING_EXPAND_MAX_COLUMN;
    }

    public static void setSettingExpandMaxColumn(int column) {
        sSETTING_EXPAND_MAX_COLUMN = column;
    }

    public static int getBatteryLevel() {
        return sCurrentBatteryLevel;
    }

    public static void setBatteryLevel(int batteryLevel) {
        sCurrentBatteryLevel = batteryLevel;
    }

    public static void blurRecentThumbnail(Activity activity, boolean blur) {
        if (activity != null) {
            TaskDescription td = new TaskDescription(activity.getApplicationContext().getString(C0088R.string.app_name), BitmapManagingUtil.getBitmap(activity.getApplicationContext(), C0088R.mipmap.lg_iconframe_camera));
            if (blur) {
                td.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            }
            activity.setTaskDescription(td);
            activity.setDisablePreviewScreenshots(blur);
        }
    }

    public static void blurRecentThumbnail(int flag, Activity activity) {
        if (activity != null) {
            try {
                ActivityManagerEx am = (ActivityManagerEx) activity.getSystemService("activity");
                if (am != null) {
                    am.getClass().getMethod("updateFlag", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(am, new Object[]{Integer.valueOf(flag), Integer.valueOf(activity.getTaskId())});
                }
            } catch (Exception e) {
                CamLog.m4d(CameraConstants.TAG, "fail to call updateFlag = ", e);
            }
        }
    }

    public static void setVoltageLevel(float voltage) {
        sCurrentVoltageLevel = voltage;
    }

    public static float getVoltageLevel() {
        return sCurrentVoltageLevel;
    }

    public static boolean isGalleryLaunched() {
        return sIsGalleryLaunched;
    }

    public static void setLaunchingGallery(boolean launch) {
        sIsGalleryLaunched = launch;
    }

    public static boolean isQuickTools(Intent intent) {
        if (intent == null) {
            return false;
        }
        boolean ret = intent.getBooleanExtra("quicktray", false);
        CamLog.m3d(CameraConstants.TAG, "is QuickTools ? " + ret);
        return ret;
    }

    public static boolean isTouchShotOnMultiView() {
        return sIsTouchShotOnMultiView;
    }

    public static void setTouchShotOnMultiView(boolean isTouchShot) {
        sIsTouchShotOnMultiView = isTouchShot;
    }

    public static int getSnapMovieMVFrameType() {
        return sCurMultiViewFrameType;
    }

    public static void setSnapMovieMVFrameType(int set) {
        sCurMultiViewFrameType = set;
        CamLog.m3d(CameraConstants.TAG, "setSnapMovieMVFrameType sCurMultiViewFrameType = " + sCurMultiViewFrameType);
    }

    public static void setStartFromOnCreate(boolean set) {
        sIsStartFromOnCreate = set;
    }

    public static boolean isStartFromOnCreate() {
        return sIsStartFromOnCreate;
    }

    public static String getTopActivity(Context context) {
        String topActivity = "";
        try {
            List<RunningTaskInfo> list = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
            if (list == null) {
                Log.i(CameraConstants.TAG, "Running Task == null");
                return null;
            }
            RunningTaskInfo info = (RunningTaskInfo) list.get(0);
            if (info == null || info.topActivity == null) {
                Log.i(CameraConstants.TAG, "info == null");
                return null;
            }
            topActivity = info.topActivity.getClassName();
            Log.i(CameraConstants.TAG, "getClassName() ==>> " + topActivity);
            return topActivity;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setPopoutFirstTakePicture(boolean set) {
        sIsPopoutFirstTakePictureBefore = set;
    }

    public static boolean isPopoutFirstTakePicture() {
        return sIsPopoutFirstTakePictureBefore;
    }

    public static boolean hasDeviceOwner(Context context) {
        return !TextUtils.isEmpty(((DevicePolicyManager) context.getSystemService("device_policy")).getDeviceOwner());
    }

    public static boolean isPowerSaveMaximumModeOn(Context context) {
        if (Global.getInt(context.getContentResolver(), "low_power", 0) == 1 && Global.getInt(context.getContentResolver(), "battery_saver_mode_ex", 1) == 2) {
            return true;
        }
        return false;
    }
}
