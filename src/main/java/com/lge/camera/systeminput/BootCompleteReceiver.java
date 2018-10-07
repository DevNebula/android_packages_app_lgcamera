package com.lge.camera.systeminput;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.lge.app.permission.RequestPermissionsHelper;
import com.lge.camera.C0088R;
import com.lge.camera.app.CreatePopoutShortcutActivity;
import com.lge.camera.app.CreateSquareCameraShortcutActivity;
import com.lge.camera.app.SquareCameraActivity;
import com.lge.camera.app.ext.PopoutActivity;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler;
import com.lge.camera.managers.ext.sticker.utils.StickerPreloadPackageUtil;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.CamLog;

public class BootCompleteReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "[boot] Start time check");
        ConfigurationUtil.setConfiguration(context);
        boolean rawsupport = FunctionProperties.isSupportedRAWPictureSaving();
        CamLog.m3d(CameraConstants.TAG, "[boot] end time check raw support = " + rawsupport);
        rawSupportSetting(context, rawsupport);
        boolean isOwner = isOwnerUser();
        boolean hasDO = AppControlUtil.hasDeviceOwner(context);
        CamLog.m3d(CameraConstants.TAG, "isOwner = " + isOwner + " hasDO = " + hasDO);
        if (!isOwner || hasDO) {
            CamLog.m3d(CameraConstants.TAG, "do not startService");
        } else if (RequestPermissionsHelper.hasPermissions(context, CameraConstants.UI_REQUIRED_PERMISSIONS)) {
            Intent startService = new Intent();
            startService.setClassName("com.lge.camera", "com.lge.camera.app.BoostService");
            context.startService(startService);
            CamLog.m3d(CameraConstants.TAG, "startService");
        } else {
            CamLog.m5e(CameraConstants.TAG, "Can't start BootService as not have dangerous permissions");
            return;
        }
        if (context.getResources().getBoolean(C0088R.bool.popout_shortcut_available)) {
            enableSpecificActivity(context, CreatePopoutShortcutActivity.class, true);
        } else {
            enableSpecificActivity(context, PopoutActivity.class, false);
        }
        if (context.getResources().getBoolean(C0088R.bool.square_shortcut_available)) {
            enableSpecificActivity(context, CreateSquareCameraShortcutActivity.class, true);
        } else {
            enableSpecificActivity(context, SquareCameraActivity.class, false);
        }
        if (FunctionProperties.isSupportedSticker() && DecompressScheduler.needDecompressPreloadedContents(context)) {
            DecompressScheduler ds = DecompressScheduler.getInstance(context);
            if (ds != null) {
                ds.preloadedExcuteJob(context.getFilesDir().getAbsolutePath(), C0088R.raw.sticker);
            }
            StickerPreloadPackageUtil.saveCurrentPreloadPackageVersion(context);
        }
    }

    private boolean isOwnerUser() {
        try {
            UserHandle myHandle = Process.myUserHandle();
            CamLog.m5e(CameraConstants.TAG, " " + myHandle + " **  " + UserHandle.OWNER + "  ** ");
            if (UserHandle.OWNER.equals(myHandle)) {
                return true;
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, " Exception " + e);
        }
        return false;
    }

    public void rawSupportSetting(Context context, boolean set) {
        CamLog.m3d(CameraConstants.TAG, "[boot] set = " + set);
        if (set) {
            try {
                System.putInt(context.getContentResolver(), "camera_raw_support", 1);
            } catch (Exception e) {
                CamLog.m11w(CameraConstants.TAG, "fail to write CAMERA_RAW_SUPPORT: ");
                e.printStackTrace();
            }
        } else {
            System.putInt(context.getContentResolver(), "camera_raw_support", 0);
        }
        int raw_support = 0;
        try {
            raw_support = System.getInt(context.getContentResolver(), "camera_raw_support");
        } catch (SettingNotFoundException e2) {
            CamLog.m3d(CameraConstants.TAG, "[boot] CAMERA_RAW_SUPPORT not found");
            e2.printStackTrace();
        }
        CamLog.m3d(CameraConstants.TAG, "[boot] RAW support = " + raw_support);
    }

    public void enableSpecificActivity(Context context, Class<?> className, boolean enable) {
        if (context == null || className == null) {
            CamLog.m3d(CameraConstants.TAG, "context or className is null");
            return;
        }
        int enableFlag = enable ? 1 : 2;
        PackageManager pm = context.getPackageManager();
        ComponentName componentName = new ComponentName(context.getApplicationContext(), className);
        CamLog.m3d(CameraConstants.TAG, "[boot]componentName = " + componentName + ", enable : " + enable);
        pm.setComponentEnabledSetting(componentName, enableFlag, 1);
    }
}
