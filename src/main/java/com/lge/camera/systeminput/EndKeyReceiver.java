package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class EndKeyReceiver extends CameraBroadCastReceiver {
    public EndKeyReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK")) {
            boolean isEndKey = intent.getBooleanExtra("com.lge.intent.extra.isEndKey", false);
            String topPackage = intent.getStringExtra("com.lge.intent.extra.topPkgName");
            CamLog.m3d(CameraConstants.TAG, "is END key pressed? " + isEndKey);
            CamLog.m3d(CameraConstants.TAG, "is top package camera? " + topPackage.equals(context.getPackageName()));
            if (topPackage.equals(context.getPackageName()) && this.mGet.getActivity() != null && this.mGet.isRecordingState()) {
                setResultCode(1);
                setResultData(context.getPackageName());
                abortBroadcast();
            }
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
