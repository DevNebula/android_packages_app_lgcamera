package com.lge.camera.systeminput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class CameraBroadCastReceiver extends BroadcastReceiver {
    protected boolean mFinished = false;
    protected ReceiverInterface mGet = null;

    protected abstract IntentFilter getIntentFilter();

    protected abstract CameraBroadCastReceiver getReceiver();

    public abstract void onReceive(Context context, Intent intent);

    public CameraBroadCastReceiver(ReceiverInterface receiverInterface) {
        this.mGet = receiverInterface;
    }

    protected boolean checkOnReceive(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        return true;
    }
}
