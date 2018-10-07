package com.lge.camera.systeminput;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraHolder;
import com.lge.camera.util.CamLog;

public class ButtonIntentReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "onReceive()");
        if (ModelProperties.isKeyPadSupported(context)) {
            CameraHolder holder = CameraHolder.instance();
            if (holder != null && holder.tryOpen(null, 0, null, context) != null) {
                holder.keep();
                holder.release();
            } else {
                return;
            }
        }
        if (!isRunningCamera(context)) {
            Intent i = new Intent("android.intent.action.MAIN");
            try {
                i.setClass(context, Class.forName("com.lge.camera.app.CameraActivity"));
                i.addCategory("android.intent.category.LAUNCHER");
                i.setFlags(335577088);
                context.startActivity(i);
            } catch (ClassNotFoundException e) {
                CamLog.m11w(CameraConstants.TAG, "not found CameraActivity class");
            }
        }
    }

    private boolean isRunningCamera(Context context) {
        String topActivityPackageName = ((RunningTaskInfo) ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getPackageName();
        CamLog.m3d(CameraConstants.TAG, "topActivityPackageName: " + topActivityPackageName);
        if (topActivityPackageName.contains("com.lge.camera") || topActivityPackageName.contains("HelpActivity")) {
            return true;
        }
        return false;
    }
}
