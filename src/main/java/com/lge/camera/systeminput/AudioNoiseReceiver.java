package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class AudioNoiseReceiver extends CameraBroadCastReceiver {
    public AudioNoiseReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent) && this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "[audio] receive NOISY intent");
            this.mGet.onReceiveAudioNoiseIntent();
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("android.media.AUDIO_BECOMING_NOISY");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
