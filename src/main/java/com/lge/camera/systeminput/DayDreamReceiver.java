package com.lge.camera.systeminput;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.SecureImageUtil;

public class DayDreamReceiver extends CameraBroadCastReceiver implements OnRemoveHandler {
    public DayDreamReceiver(ReceiverInterface bridge) {
        super(bridge);
    }

    public void onReceive(Context context, Intent intent) {
        boolean screenLock = SecureImageUtil.getScreenLock();
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        String className = null;
        if (am.getRunningTasks(1) != null) {
            className = ((RunningTaskInfo) am.getRunningTasks(1).get(0)).topActivity.getClassName();
        }
        CamLog.m3d(CameraConstants.TAG, "CameraDayDreamReceiver : screenLock = " + screenLock + " getClassname : " + className);
        if (screenLock) {
            if ("com.lge.camera.app.SecureCameraActivity".equals(className)) {
                CamLog.m3d(CameraConstants.TAG, "CameraDayDreamReceiver : getClassName = " + className + " finish!");
                finishActivity();
            }
        } else if ("com.lge.camera.CameraAppLauncher".equals(className) || "com.lge.camera.app.CameraActivity".equals(className) || "com.lge.camera.VideoCamera".equals(className)) {
            CamLog.m3d(CameraConstants.TAG, "CameraDayDreamReceiver : getClassName = " + className + " finish!");
            finishActivity();
        }
    }

    private void finishActivity() {
        if (this.mGet != null && this.mGet.getActivity() != null && !this.mGet.getActivity().isFinishing() && CheckStatusManager.getCheckEnterOutSecure() == 0) {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (DayDreamReceiver.this.mGet != null && DayDreamReceiver.this.mGet.getActivity() != null) {
                        DayDreamReceiver.this.mGet.getActivity().finish();
                    }
                }
            }, 1000);
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("android.intent.action.DREAMING_STARTED");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
