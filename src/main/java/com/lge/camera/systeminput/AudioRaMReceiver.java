package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class AudioRaMReceiver extends CameraBroadCastReceiver {
    private static final String INTENT_AUDIO_RAM = "com.lge.media.ACTION_RAM_STATUS_CHANGED";
    private static final String INTENT_AUDIO_RAM_STATUS = "status";

    public AudioRaMReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent) && this.mGet != null) {
            this.mGet.onReceiveAudioRaMIntent(intent.getIntExtra("status", 0));
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter(INTENT_AUDIO_RAM);
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
