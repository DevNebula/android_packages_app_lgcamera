package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class LGMdmReceiver extends CameraBroadCastReceiver {
    private static final String MDM_CHANGED = "com.lge.mdm.intent.action.MICROPHONE_POLICY_CHANGED";

    public LGMdmReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            CamLog.m3d(CameraConstants.TAG, "LGMdmReceiver action = " + intent.getAction());
            this.mGet.changeSettingValueInMdm();
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MDM_CHANGED);
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
