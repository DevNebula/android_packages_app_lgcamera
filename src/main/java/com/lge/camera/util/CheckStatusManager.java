package com.lge.camera.util;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.MmsProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.StorageProperties;
import com.lge.camera.managers.ToastManager;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGPowerManagerHelper;
import com.lge.systemservice.core.LGThermalManager;

public class CheckStatusManager {
    private static final int[] CAMERA_OUT_STR_ID = new int[]{-1, C0088R.string.sp_error_call_camera_NORMAL, C0088R.string.error_video_recording_during_call, C0088R.string.error_camera_during_video_call, C0088R.string.sp_lowbattery, C0088R.string.sp_media_scanning_NORMAL, C0088R.string.sp_media_scanning_NORMAL, C0088R.string.sp_block_camera_NORMAL, C0088R.string.error_cannot_use_hdmi, C0088R.string.sp_block_camera_NORMAL, C0088R.string.sp_message_recording_limit_NORMAL, C0088R.string.sp_high_temp_action_on_enter_NORMAL, C0088R.string.error_internal_storage_full, C0088R.string.not_available_during_remote_care, C0088R.string.block_recording_and_cheeseshutter_mdm, C0088R.string.block_recording_mdm, C0088R.string.camera_unavilable_as_not_granted_permissions, C0088R.string.camera_unavilable_due_to_no_storage, C0088R.string.recentapps_incompatible_app_message};
    public static final String[] CAMERA_PROJECTION = new String[]{"enabled", "user_enabled"};
    public static final int CHECK_ENTER_BATTERY = 4;
    public static final int CHECK_ENTER_CALL = 1;
    public static final int CHECK_ENTER_CALL_CAMCORDER = 2;
    public static final int CHECK_ENTER_DATA_STORAGE = 12;
    public static final int CHECK_ENTER_DEV_POLOCY = 7;
    public static final int CHECK_ENTER_EXT_MEDIA_SCANNING = 5;
    public static final int CHECK_ENTER_HDMI = 8;
    public static final int CHECK_ENTER_INT_MEDIA_SCANNING = 6;
    public static final int CHECK_ENTER_MDM_POLOCY = 14;
    public static final int CHECK_ENTER_MDM_POLOCY_NOT_SUPPORTED_VOICESHUTTER = 15;
    public static final int CHECK_ENTER_MMS_REC_SIZE = 10;
    public static final int CHECK_ENTER_MULTI_WINDOW = 18;
    public static final int CHECK_ENTER_NO_STORAGE = 17;
    public static final int CHECK_ENTER_OK = 0;
    public static final int CHECK_ENTER_PERMISSIONS = 16;
    public static final int CHECK_ENTER_SAFETY_CARE = 13;
    public static final int CHECK_ENTER_SDM = 9;
    public static final int CHECK_ENTER_TEMPERATURE = 11;
    public static final int CHECK_ENTER_VT_CALL = 3;
    public static final int CHECK_OUT_FINISH = 2;
    public static final int CHECK_OUT_IDLE = 0;
    public static final int CHECK_OUT_SECURE = 1;
    private static final String HDMI_STATE_PATH = "/sys/class/switch/hdmi/state";
    public static final String PROPERTY_TEMPERATURE = "/sys/devices/platform/msm_ssbi.0/pm8921-core/pm8xxx-adc/xo_therm";
    public static final Uri SDM_CONTENT_URI = Uri.parse("content://com.innopath.activecare.dev.oem/camerainfo");
    public static final float TEMPERATURE_LCD_CONTROL_RATIO = setRatioForBacklightInRecording();
    public static final long TEMPERATURE_LCD_CONTROL_SECOND = setSecondForBacklightInRecording();
    private static final String TEMPERATURE_PROP_LCD_CONTROL_PERCENT = "ro.lge.heat_lcd_percent";
    private static final String TEMPERATURE_PROP_LCD_CONTROL_SECOND = "ro.lge.heat_lcd_second";
    private static int sCheckEnterKind = 0;
    private static int sCheckEnterOutSecure = 0;
    private static boolean sEnterCheckComplete = false;
    private static boolean sIsEnterDuringCall = false;
    private static boolean sIsTelephonyStateCheckSkip = false;

    public static boolean isEnterCheckComplete() {
        return sEnterCheckComplete;
    }

    public static void setEnterCheckComplete(boolean complete) {
        sEnterCheckComplete = complete;
    }

    public static int getCheckEnterKind() {
        return sCheckEnterKind;
    }

    public static int getCheckEnterOutSecure() {
        return sCheckEnterOutSecure;
    }

    public static void setCheckEnterOutSecure(int status) {
        sCheckEnterOutSecure = status;
    }

    public static boolean isEnterDuringCall() {
        return sIsEnterDuringCall;
    }

    private static void setIsEnterDuringCall(boolean duringCall) {
        CamLog.m3d(CameraConstants.TAG, "Enter camera during call : " + duringCall);
        sIsEnterDuringCall = duringCall;
    }

    public static boolean checkCurrentBatteryStatus(Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "check enter by battery status");
        IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.BATTERY_LOW");
        Intent intent = activity.registerReceiver(null, intentFilter);
        if (intent != null && intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
            int level = intent.getIntExtra("level", 0);
            if (intent.getIntExtra("level", 0) <= -1) {
                CamLog.m11w(CameraConstants.TAG, "Battery level is low : " + level);
                sCheckEnterKind = 4;
                return false;
            }
            CamLog.m7i(CameraConstants.TAG, "Current battery level is " + level);
            AppControlUtil.setBatteryLevel(level);
        }
        return true;
    }

    public static boolean checkCurrentTemperature(Activity activity) {
        Log.d(CameraConstants.TAG, "check enter by Temperature");
        LGPowerManagerHelper pmh = (LGPowerManagerHelper) new LGContext(activity).getLGSystemService("lgpowermanagerhelper");
        String triggerName = "com.lge.camera.dont_start";
        if (pmh != null) {
            try {
                if (((Boolean) pmh.getClass().getMethod("isThermalActionTriggered", new Class[]{String.class}).invoke(pmh, new Object[]{triggerName})).booleanValue()) {
                    CamLog.m3d(CameraConstants.TAG, "Cannot exeute camera because it's too hot.");
                    sCheckEnterKind = 11;
                    return false;
                }
            } catch (NoSuchMethodException e) {
                CamLog.m3d(CameraConstants.TAG, "Cannot found isThermalActionTriggered, in LGPowerManagerHelper, so find in LGThermalService");
                if (!((LGThermalManager) new LGContext(activity).getLGSystemService("lgthermal")).isThermalActionTriggered(triggerName)) {
                    return true;
                }
                CamLog.m3d(CameraConstants.TAG, "Cannot exeute camera because it's too hot.");
                sCheckEnterKind = 11;
                return false;
            } catch (Exception e2) {
                CamLog.m12w(CameraConstants.TAG, "fail to check temperature = ", e2);
                return true;
            }
        }
        return true;
    }

    public static boolean checkDataStorageEnough() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long availBlocks = statFs.getAvailableBlocksLong();
        long blockSize = statFs.getBlockSizeLong();
        CamLog.m3d(CameraConstants.TAG, "DATA STORAGE = " + (availBlocks * blockSize));
        if (availBlocks * blockSize > 10) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "Data storage(/data/) is full!!!!");
        sCheckEnterKind = 12;
        return false;
    }

    public static boolean checkDataStorageEnough(long minSize) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long availBlocks = statFs.getAvailableBlocksLong();
        long blockSize = statFs.getBlockSizeLong();
        if (minSize < 10) {
            minSize = 10;
        }
        CamLog.m3d(CameraConstants.TAG, "DATA STORAGE = " + (availBlocks * blockSize));
        if (availBlocks * blockSize > minSize) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "Data storage(/data/) is not enough : " + minSize);
        return false;
    }

    public static void setTelephonyStateCheckSkip(boolean callCheck) {
        sIsTelephonyStateCheckSkip = callCheck;
        CamLog.m3d(CameraConstants.TAG, "TelephonyStateCheck = " + sIsTelephonyStateCheckSkip);
    }

    private static boolean checkAllCallStatus(Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "check enter by call status");
        if (sIsTelephonyStateCheckSkip) {
            return true;
        }
        int callState = TelephonyUtil.getPhoneCallState(activity.getApplicationContext());
        if (TelephonyUtil.phoneInVTCall(callState)) {
            sCheckEnterKind = 3;
            return false;
        } else if (!TelephonyUtil.phoneInCall(callState)) {
            setIsEnterDuringCall(false);
            return true;
        } else if (!"android.media.action.VIDEO_CAPTURE".equals(activity.getIntent().getAction())) {
            return true;
        } else {
            sCheckEnterKind = 2;
            return false;
        }
    }

    public static boolean checkVTCallStatus(Activity activity) {
        if (!TelephonyUtil.phoneInVTCall(activity.getApplicationContext())) {
            return true;
        }
        sCheckEnterKind = 3;
        return false;
    }

    public static boolean checkDevicePolicy(Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "check enter by Device Policy status");
        boolean allowCamera = false;
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getApplicationContext().getSystemService("device_policy");
        if (dpm != null) {
            allowCamera = !dpm.getCameraDisabled(null);
            CamLog.m3d(CameraConstants.TAG, "allowCamera = " + allowCamera);
            if (!allowCamera) {
                sCheckEnterKind = 7;
            }
        }
        return allowCamera;
    }

    public static boolean checkAllowCameraInSDM(Activity activity) {
        if (ModelProperties.getCarrierCode() != 6) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "check enter In SDM");
        int cameraEnableStatus = 1;
        Cursor cursor = null;
        try {
            cursor = activity.getContentResolver().query(SDM_CONTENT_URI, CAMERA_PROJECTION, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                CamLog.m11w(CameraConstants.TAG, "*** cannot access to SDM server DB, cursor = " + cursor);
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
            cameraEnableStatus = cursor.getInt(0);
            CamLog.m11w(CameraConstants.TAG, "*** cameraEnableStatus = " + cameraEnableStatus);
            if (cursor != null) {
                cursor.close();
            }
            if (cameraEnableStatus == 1) {
                return true;
            }
            sCheckEnterKind = 9;
            return false;
        } catch (SQLiteException e) {
            CamLog.m6e(CameraConstants.TAG, "Could not load photo from database", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0088 A:{SYNTHETIC, Splitter: B:30:0x0088} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0098 A:{SYNTHETIC, Splitter: B:36:0x0098} */
    private static boolean checkHdmiStatus(android.app.Activity r10) {
        /*
        r9 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r7 = 1;
        r8 = com.lge.camera.constants.ModelProperties.isSupportHDMI_MHL();
        if (r8 == 0) goto L_0x0012;
    L_0x0009:
        r8 = "CameraApp";
        r9 = "It can support HDMI/MHL!!";
        com.lge.camera.util.CamLog.m3d(r8, r9);
        r2 = r7;
    L_0x0011:
        return r2;
    L_0x0012:
        r2 = 1;
        r6 = 0;
        r0 = new char[r9];
        r3 = 0;
        r4 = new java.io.FileReader;	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x007e }
        r8 = "/sys/class/switch/hdmi/state";
        r4.<init>(r8);	 Catch:{ FileNotFoundException -> 0x0067, Exception -> 0x007e }
        r8 = 0;
        r9 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r5 = r4.read(r0, r8, r9);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x00a8, all -> 0x00a5 }
        r8 = new java.lang.String;	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x00a8, all -> 0x00a5 }
        r9 = 0;
        r8.<init>(r0, r9, r5);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x00a8, all -> 0x00a5 }
        r8 = r8.trim();	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x00a8, all -> 0x00a5 }
        r8 = java.lang.Integer.valueOf(r8);	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x00a8, all -> 0x00a5 }
        r6 = r8.intValue();	 Catch:{ FileNotFoundException -> 0x00ab, Exception -> 0x00a8, all -> 0x00a5 }
        if (r4 == 0) goto L_0x00ae;
    L_0x0039:
        r4.close();	 Catch:{ IOException -> 0x005d }
        r3 = r4;
    L_0x003d:
        if (r6 != r7) goto L_0x0044;
    L_0x003f:
        r2 = 0;
        r7 = 8;
        sCheckEnterKind = r7;
    L_0x0044:
        r7 = "CameraApp";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "checkHdmiStatus: is disconnected?";
        r8 = r8.append(r9);
        r8 = r8.append(r2);
        r8 = r8.toString();
        com.lge.camera.util.CamLog.m7i(r7, r8);
        goto L_0x0011;
    L_0x005d:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        r3 = r4;
        goto L_0x003d;
    L_0x0067:
        r1 = move-exception;
    L_0x0068:
        r8 = "CameraApp";
        r9 = "This kernel does not have dock station support";
        com.lge.camera.util.CamLog.m12w(r8, r9, r1);	 Catch:{ all -> 0x0095 }
        if (r3 == 0) goto L_0x003d;
    L_0x0071:
        r3.close();	 Catch:{ IOException -> 0x0075 }
        goto L_0x003d;
    L_0x0075:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        goto L_0x003d;
    L_0x007e:
        r1 = move-exception;
    L_0x007f:
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);	 Catch:{ all -> 0x0095 }
        if (r3 == 0) goto L_0x003d;
    L_0x0088:
        r3.close();	 Catch:{ IOException -> 0x008c }
        goto L_0x003d;
    L_0x008c:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        goto L_0x003d;
    L_0x0095:
        r7 = move-exception;
    L_0x0096:
        if (r3 == 0) goto L_0x009b;
    L_0x0098:
        r3.close();	 Catch:{ IOException -> 0x009c }
    L_0x009b:
        throw r7;
    L_0x009c:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        goto L_0x009b;
    L_0x00a5:
        r7 = move-exception;
        r3 = r4;
        goto L_0x0096;
    L_0x00a8:
        r1 = move-exception;
        r3 = r4;
        goto L_0x007f;
    L_0x00ab:
        r1 = move-exception;
        r3 = r4;
        goto L_0x0068;
    L_0x00ae:
        r3 = r4;
        goto L_0x003d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.CheckStatusManager.checkHdmiStatus(android.app.Activity):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0074 A:{SYNTHETIC, Splitter: B:26:0x0074} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0084 A:{SYNTHETIC, Splitter: B:32:0x0084} */
    public static boolean isHDMIConnected() {
        /*
        r8 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r6 = 1;
        r7 = 0;
        r5 = 0;
        r0 = new char[r8];
        r2 = 0;
        r3 = new java.io.FileReader;	 Catch:{ FileNotFoundException -> 0x0053, Exception -> 0x006a }
        r8 = "/sys/class/switch/hdmi/state";
        r3.<init>(r8);	 Catch:{ FileNotFoundException -> 0x0053, Exception -> 0x006a }
        r8 = 0;
        r9 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r4 = r3.read(r0, r8, r9);	 Catch:{ FileNotFoundException -> 0x0099, Exception -> 0x0096, all -> 0x0093 }
        r8 = new java.lang.String;	 Catch:{ FileNotFoundException -> 0x0099, Exception -> 0x0096, all -> 0x0093 }
        r9 = 0;
        r8.<init>(r0, r9, r4);	 Catch:{ FileNotFoundException -> 0x0099, Exception -> 0x0096, all -> 0x0093 }
        r8 = r8.trim();	 Catch:{ FileNotFoundException -> 0x0099, Exception -> 0x0096, all -> 0x0093 }
        r8 = java.lang.Integer.valueOf(r8);	 Catch:{ FileNotFoundException -> 0x0099, Exception -> 0x0096, all -> 0x0093 }
        r5 = r8.intValue();	 Catch:{ FileNotFoundException -> 0x0099, Exception -> 0x0096, all -> 0x0093 }
        if (r3 == 0) goto L_0x009c;
    L_0x002a:
        r3.close();	 Catch:{ IOException -> 0x0049 }
        r2 = r3;
    L_0x002e:
        r8 = "CameraApp";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "mHDMIState = ";
        r9 = r9.append(r10);
        r9 = r9.append(r5);
        r9 = r9.toString();
        com.lge.camera.util.CamLog.m3d(r8, r9);
        if (r5 != r6) goto L_0x0091;
    L_0x0048:
        return r6;
    L_0x0049:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        r2 = r3;
        goto L_0x002e;
    L_0x0053:
        r1 = move-exception;
    L_0x0054:
        r8 = "CameraApp";
        r9 = "This kernel does not have dock station support";
        com.lge.camera.util.CamLog.m12w(r8, r9, r1);	 Catch:{ all -> 0x0081 }
        if (r2 == 0) goto L_0x002e;
    L_0x005d:
        r2.close();	 Catch:{ IOException -> 0x0061 }
        goto L_0x002e;
    L_0x0061:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        goto L_0x002e;
    L_0x006a:
        r1 = move-exception;
    L_0x006b:
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);	 Catch:{ all -> 0x0081 }
        if (r2 == 0) goto L_0x002e;
    L_0x0074:
        r2.close();	 Catch:{ IOException -> 0x0078 }
        goto L_0x002e;
    L_0x0078:
        r1 = move-exception;
        r8 = "CameraApp";
        r9 = "";
        com.lge.camera.util.CamLog.m6e(r8, r9, r1);
        goto L_0x002e;
    L_0x0081:
        r6 = move-exception;
    L_0x0082:
        if (r2 == 0) goto L_0x0087;
    L_0x0084:
        r2.close();	 Catch:{ IOException -> 0x0088 }
    L_0x0087:
        throw r6;
    L_0x0088:
        r1 = move-exception;
        r7 = "CameraApp";
        r8 = "";
        com.lge.camera.util.CamLog.m6e(r7, r8, r1);
        goto L_0x0087;
    L_0x0091:
        r6 = r7;
        goto L_0x0048;
    L_0x0093:
        r6 = move-exception;
        r2 = r3;
        goto L_0x0082;
    L_0x0096:
        r1 = move-exception;
        r2 = r3;
        goto L_0x006b;
    L_0x0099:
        r1 = move-exception;
        r2 = r3;
        goto L_0x0054;
    L_0x009c:
        r2 = r3;
        goto L_0x002e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.CheckStatusManager.isHDMIConnected():boolean");
    }

    private static boolean checkMinimumMMSRecordingSize(Activity activity) {
        if ("android.media.action.VIDEO_CAPTURE".equals(activity.getIntent().getAction())) {
            CamLog.m3d(CameraConstants.TAG, "check enter by minimum mms recording size");
            Bundle getExBundle = activity.getIntent().getExtras();
            if (getExBundle != null) {
                long mRequestedSizeLimit = getExBundle.getLong("android.intent.extra.sizeLimit", 0);
                CamLog.m3d(CameraConstants.TAG, "requested size :" + mRequestedSizeLimit);
                if (mRequestedSizeLimit != 0 && mRequestedSizeLimit < MmsProperties.getMmsVideoMinimumSize(activity.getContentResolver())) {
                    sCheckEnterKind = 10;
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkSafetyCareStatus(Activity activity) {
        boolean enterCamera = !AppControlUtil.getEnableSafetyCare(activity.getApplicationContext());
        if (!enterCamera) {
            sCheckEnterKind = 13;
        }
        return enterCamera;
    }

    public static synchronized boolean checkEnterApplication(Activity activity, boolean bResume) {
        boolean z = true;
        synchronized (CheckStatusManager.class) {
            CamLog.m3d(CameraConstants.TAG, "checkEnterApplication : mEnterCheckComplete = " + sEnterCheckComplete);
            sCheckEnterKind = 0;
            if (!sEnterCheckComplete) {
                if (bResume) {
                    if (SecureImageUtil.getScreenLock()) {
                        setTelephonyStateCheckSkip(true);
                    }
                }
                if (checkAllCallStatus(activity) && checkCurrentBatteryStatus(activity) && checkDevicePolicy(activity) && checkHdmiStatus(activity) && checkAllowCameraInSDM(activity) && checkMinimumMMSRecordingSize(activity) && checkCurrentTemperature(activity) && checkDataStorageEnough() && checkSafetyCareStatus(activity) && checkMdmMicStatus(activity) && checkMdmCameraStatus(activity) && checkNoStorage(activity) && checkMultiWindowMode(activity)) {
                    setTelephonyStateCheckSkip(false);
                    sEnterCheckComplete = true;
                } else {
                    setTelephonyStateCheckSkip(false);
                    z = false;
                }
            }
        }
        return z;
    }

    public static void checkCameraOutByPermissionCheck(final Activity activity, Handler handler, final boolean needScreenOff) {
        if (activity != null) {
            String sMsg = null;
            if (sCheckEnterKind >= 0 && sCheckEnterKind < CAMERA_OUT_STR_ID.length && CAMERA_OUT_STR_ID[sCheckEnterKind] >= 0) {
                sMsg = activity.getString(CAMERA_OUT_STR_ID[sCheckEnterKind]);
            }
            if (sMsg != null) {
                ToastManager.showToastForSecure(activity, handler, sMsg, 500);
                if (handler != null) {
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if (needScreenOff) {
                                activity.getWindow().clearFlags(6815744);
                            }
                            activity.finish();
                        }
                    }, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                } else {
                    activity.finish();
                }
            }
        }
    }

    public static void checkCameraOut(final Activity activity, final Handler handler, boolean showToast) {
        if (activity != null) {
            String sMsg = null;
            if (sCheckEnterKind >= 0 && sCheckEnterKind < CAMERA_OUT_STR_ID.length && CAMERA_OUT_STR_ID[sCheckEnterKind] >= 0) {
                sMsg = activity.getString(CAMERA_OUT_STR_ID[sCheckEnterKind]);
            }
            if (sMsg != null) {
                if (showToast && handler != null) {
                    Runnable doDisplayError;
                    final String msg = sMsg;
                    if (SecureImageUtil.isSecureCameraIntent(activity.getIntent())) {
                        doDisplayError = new Runnable() {
                            public void run() {
                                ToastManager.showToastForSecure(activity, handler, msg, 0);
                            }
                        };
                    } else {
                        doDisplayError = new Runnable() {
                            public void run() {
                                Toast.makeText(activity.getApplicationContext(), msg, 1).show();
                            }
                        };
                    }
                    handler.post(doDisplayError);
                } else if (showToast) {
                    Toast.makeText(activity.getApplicationContext(), sMsg, 1).show();
                }
                if (handler != null) {
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            activity.finish();
                        }
                    }, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                } else {
                    activity.finish();
                }
            }
        }
    }

    public static final long setTemperatureCondition(String prop, long defaultValue) {
        long result = defaultValue;
        String value = SystemProperties.get(prop);
        if (value == null) {
            CamLog.m3d(CameraConstants.TAG, "Temperature condition is null (" + prop + "). So use default value =" + result);
        } else if ("".equals(value)) {
            CamLog.m3d(CameraConstants.TAG, "Temperature condition is empty (" + prop + "). So use default value =" + result);
        } else {
            result = Long.parseLong(value);
        }
        CamLog.m3d(CameraConstants.TAG, "value =" + result);
        return result;
    }

    public static final long setSecondForBacklightInRecording() {
        CamLog.m3d(CameraConstants.TAG, "back setSecondForBacklight");
        long result = setTemperatureCondition(TEMPERATURE_PROP_LCD_CONTROL_SECOND, -1);
        if (result < 0) {
            result = -1;
        }
        CamLog.m3d(CameraConstants.TAG, "back setSecondForBacklight end : " + result);
        return result;
    }

    public static final float setRatioForBacklightInRecording() {
        CamLog.m3d(CameraConstants.TAG, "back setRatioForBacklightInRecording");
        long result = setTemperatureCondition(TEMPERATURE_PROP_LCD_CONTROL_PERCENT, -1);
        if (result > 100 || result < 50) {
            return 1.0f;
        }
        CamLog.m3d(CameraConstants.TAG, "back setRatioForBacklightInRecording end : " + result);
        return ((float) result) * 0.01f;
    }

    public static final boolean useBackLightControlInRecording() {
        CamLog.m3d(CameraConstants.TAG, "useBackLightControlInRecording");
        return TEMPERATURE_LCD_CONTROL_SECOND >= 0;
    }

    public static boolean checkMdmMicStatus(Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "[MDMDPM] check enter by mdm mic status");
        if (!"android.media.action.VIDEO_CAPTURE".equals(activity.getIntent().getAction()) || MDMUtil.allowMicrophone()) {
            return true;
        }
        sCheckEnterKind = FunctionProperties.isSupportedVoiceShutter() ? 14 : 15;
        return false;
    }

    public static boolean checkMdmCameraStatus(Activity activity) {
        CamLog.m3d(CameraConstants.TAG, "[MDMDPM] check enter by mdm policy status");
        if (MDMUtil.allowCamera(activity)) {
            return true;
        }
        sCheckEnterKind = 7;
        CamLog.m3d(CameraConstants.TAG, "[MDMDPM] CHECK_ENTER_MDM_POLOCY : " + sCheckEnterKind);
        return false;
    }

    public static boolean checkCameraPolicy() {
        return Integer.parseInt(SystemProperties.get("sys.secpolicy.camera.disabled")) == 0;
    }

    public static void setCheckCameraPermissions() {
        CamLog.m3d(CameraConstants.TAG, "setCheckCameraPermissions");
        sCheckEnterKind = 16;
    }

    public static boolean checkNoStorage(Activity activity) {
        if (StorageProperties.getNoOfStorageVolumes(activity.getApplicationContext()) != 0) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "checkNoStorage");
        sCheckEnterKind = 17;
        return false;
    }

    public static void setSystemSettingUseLocation(ContentResolver cr, boolean isUse) {
        CamLog.m3d(CameraConstants.TAG, "set use location =" + isUse);
        try {
            Secure.putInt(cr, "location_mode", isUse ? 3 : 0);
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "set use location fail" + e);
            e.printStackTrace();
        }
    }

    public static boolean isSystemSettingUseLocation(ContentResolver cr) {
        boolean isUseLocation = false;
        if (Secure.getInt(cr, "location_mode", 0) != 0) {
            isUseLocation = true;
        }
        CamLog.m3d(CameraConstants.TAG, "get use location =" + isUseLocation);
        return isUseLocation;
    }

    public static void setSystemSettingLensType(ContentResolver cr, String lensType) {
        CamLog.m3d(CameraConstants.TAG, "set lens type =" + lensType);
        try {
            Secure.putString(cr, CameraConstants.CAMERA_GLOBAL_SETTING_LENS_TYPE, lensType);
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "set lens type fail" + e);
            e.printStackTrace();
        }
    }

    public static String getSystemSettingLensType(ContentResolver cr) {
        String lensType = Secure.getString(cr, CameraConstants.CAMERA_GLOBAL_SETTING_LENS_TYPE);
        CamLog.m3d(CameraConstants.TAG, "get lens type =" + lensType);
        return lensType;
    }

    public static boolean checkMultiWindowMode(Activity activity) {
        try {
            if (activity.isInMultiWindowMode()) {
                CamLog.m3d(CameraConstants.TAG, "checkMultiWindowMode");
                sCheckEnterKind = 18;
                return false;
            }
        } catch (NoSuchMethodError e) {
            CamLog.m6e(CameraConstants.TAG, "NoSuchMethodError : ", e);
        }
        return true;
    }
}
