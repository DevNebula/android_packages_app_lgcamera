package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class CameraPolicyReceiver extends CameraBroadCastReceiver {
    private static final String MDM_CAMERA_CHANGED = "com.lge.mdm.intent.action.CAMERA_POLICY_CHANGED";

    public CameraPolicyReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            CamLog.m3d(CameraConstants.TAG, "CameraPolicyReceiver action = " + intent.getAction());
            this.mGet.cameraPolicyChanged();
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MDM_CAMERA_CHANGED);
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
