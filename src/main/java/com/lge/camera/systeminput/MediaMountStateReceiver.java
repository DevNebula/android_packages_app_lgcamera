package com.lge.camera.systeminput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.StorageProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class MediaMountStateReceiver extends BroadcastReceiver {
    static boolean sIsAfterShutdown = false;

    public void onReceive(Context context, Intent intent) {
        updateMediaStatus(context, intent);
    }

    public static void updateMediaStatus(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
            CamLog.m3d(CameraConstants.TAG, "receive Intent.ACTION_SHUTDOWN !!!!");
            sIsAfterShutdown = true;
        } else if (!sIsAfterShutdown) {
            CamLog.m3d(CameraConstants.TAG, "MediaMountStateReceiver intent path" + intent.getData().getEncodedPath());
            CamLog.m3d(CameraConstants.TAG, "MediaMountStateReceiver sd path" + StorageProperties.getSd(context).path);
            int showValue = SharedPreferenceUtil.getNeedShowStorageInitDialog(context);
            if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                if (intent.getData().getEncodedPath().equals(StorageProperties.getSd(context).path)) {
                    if (SharedPreferenceUtil.getPastSDInsertionStatus(context) == 0) {
                        showValue = 0;
                    }
                    SharedPreferenceUtilBase.savePastSDInsertionStatus(context, 1);
                }
            } else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action) && !intent.getData().getEncodedPath().equals(StorageProperties.getInternal(context).path)) {
                SharedPreferenceUtilBase.savePastSDInsertionStatus(context, 0);
                showValue = 1;
            }
            SharedPreferenceUtilBase.saveNeedShowStorageInitDialog(context, showValue);
        }
    }
}
