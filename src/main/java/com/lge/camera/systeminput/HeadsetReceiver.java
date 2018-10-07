package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.util.AudioUtil;

public class HeadsetReceiver extends CameraBroadCastReceiver {
    public HeadsetReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent) && "android.intent.action.HEADSET_PLUG".equals(intent.getAction())) {
            int state = intent.getIntExtra("state", -1);
            String name = intent.getStringExtra("name");
            int mic = intent.getIntExtra("microphone", 0);
            if (name == null || state != 1) {
                this.mGet.setHeadsetState(0);
            } else if (AudioUtil.isWiredHeadsetHasMicOn() && mic == 1) {
                this.mGet.setHeadsetState(2);
            } else {
                this.mGet.setHeadsetState(1);
            }
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("android.intent.action.HEADSET_PLUG");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
