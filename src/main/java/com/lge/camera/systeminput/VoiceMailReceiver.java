package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class VoiceMailReceiver extends CameraBroadCastReceiver {
    public VoiceMailReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("com.lge.vvm.NEW_VVM_NOTIFICATION_RECEIVED")) {
            return;
        }
        if (intent.getIntExtra("vvm_unreadcount", 0) == 0) {
            try {
                CamLog.m3d(CameraConstants.TAG, "voice mail is zero");
                this.mGet.setVoiceMailIndicatorReceived(false);
                return;
            } catch (NumberFormatException e) {
                CamLog.m3d(CameraConstants.TAG, "NumberFormatException e = " + e.getMessage());
                return;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "voice mail is not zero");
        this.mGet.setVoiceMailIndicatorReceived(true);
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("com.lge.vvm.NEW_VVM_NOTIFICATION_RECEIVED");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
