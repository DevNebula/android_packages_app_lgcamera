package com.lge.camera.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.lge.camera.constants.CameraConstants;

public class IntentBroadcastUtil {
    public static boolean sIS_CHANGE_MODE_STATUS = false;

    public static void setFmRadioOff(Context context) {
        Intent i = new Intent("com.lge.fmradio.command.fmradioservice");
        i.putExtra("request", "power_off");
        i.addFlags(16777216);
        context.sendBroadcast(i);
    }

    public static void blockAlarmInRecording(Activity activity, boolean block) {
        if (activity != null && block) {
            CamLog.m3d(CameraConstants.TAG, "BlockAlarmInRecording");
            Intent recording_start = new Intent();
            recording_start.putExtra("packageName", "com.lge.camera");
            recording_start.setAction("voice_video_record_playing");
            recording_start.addFlags(16777216);
            activity.sendBroadcast(recording_start);
        }
    }

    public static void unblockAlarmInRecording(Activity activity) {
        if (activity != null) {
            CamLog.m3d(CameraConstants.TAG, "UnblockAlarmInRecording");
            Intent recording_finish = new Intent();
            recording_finish.putExtra("packageName", "com.lge.camera");
            recording_finish.setAction("voice_video_record_finish");
            recording_finish.addFlags(16777216);
            activity.sendBroadcast(recording_finish);
        }
    }

    public static void stopVoiceRec(Activity activity, boolean stop) {
        if (activity != null && stop) {
            CamLog.m3d(CameraConstants.TAG, "StopVoiceRec");
            Intent stopVoiceRec = new Intent();
            stopVoiceRec.setAction("Stop_Voice_Rec");
            stopVoiceRec.addFlags(16777216);
            activity.sendBroadcast(stopVoiceRec);
        }
    }

    public static void sendBroadcastIntentCameraStarted(Activity activity) {
        if (!sIS_CHANGE_MODE_STATUS) {
            CamLog.m7i(CameraConstants.TAG, "Send broadcast: com.lge.intent.action.FLOATING_WINDOW_ENTER_LOWPROFILE, Extra value : hide is true");
            Intent intent = new Intent("com.lge.intent.action.FLOATING_WINDOW_ENTER_LOWPROFILE");
            intent.putExtra("hide", true);
            intent.putExtra("package", activity.getPackageName());
            intent.addFlags(16777216);
            activity.sendBroadcast(intent);
            Intent camIntent = new Intent("com.lge.camera.action.START_CAMERA_APP");
            camIntent.addFlags(16777216);
            activity.sendBroadcast(camIntent);
        }
        sIS_CHANGE_MODE_STATUS = false;
    }

    public static void sendBroadcastIntentCameraEnded(Activity activity) {
        CamLog.m7i(CameraConstants.TAG, "Send broadcast : com.lge.intent.action.FLOATING_WINDOW_EXIT_LOWPROFILE");
        Intent intent = new Intent("com.lge.intent.action.FLOATING_WINDOW_EXIT_LOWPROFILE");
        intent.putExtra("package", activity.getPackageName());
        intent.addFlags(16777216);
        activity.sendBroadcast(intent);
        Intent camIntent = new Intent("com.lge.camera.action.STOP_CAMERA_APP");
        camIntent.addFlags(16777216);
        activity.sendBroadcast(camIntent);
        sIS_CHANGE_MODE_STATUS = false;
    }

    public static boolean isIntentAvailable(PackageManager pm, Intent intent) {
        return pm.queryIntentActivities(intent, 65536).size() > 0;
    }
}
