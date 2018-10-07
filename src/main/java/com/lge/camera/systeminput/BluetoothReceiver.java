package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class BluetoothReceiver extends CameraBroadCastReceiver {
    public static final String ACTION_DUAL_NOTI_REFRESH = "com.lge.bluetoothsetting.dualnoti_update";

    public BluetoothReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            checkBTState(intent);
            return;
        }
        int connectState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
        CamLog.m3d(CameraConstants.TAG, "onReceive(), action:" + action + ", state : " + connectState);
        if ("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED".equals(action)) {
            if (connectState == 2) {
                this.mGet.onBTConnectionStateChanged(true);
            } else if (connectState == 0) {
                this.mGet.onBTConnectionStateChanged(false);
            }
        }
        if ("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED".equals(action)) {
            if (connectState == 12) {
                this.mGet.onBTAudioConnectionStateChanged(true);
            } else if (connectState == 10) {
                this.mGet.onBTAudioConnectionStateChanged(false);
            }
        }
        if (ACTION_DUAL_NOTI_REFRESH.equals(action)) {
            this.mGet.onDualConnectionDeviceTypeChanged(intent.getIntExtra("extra_selected_button", -1));
        }
    }

    private void checkBTState(Intent intent) {
        int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
        int prevState = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
        CamLog.m3d(CameraConstants.TAG, "onReceive(), action:android.bluetooth.adapter.action.STATE_CHANGED, state : " + state + " , prevState " + prevState);
        if (state != prevState) {
            if (state == 10) {
                this.mGet.onBTStateChanged(false);
            } else if (state == 12) {
                this.mGet.onBTStateChanged(true);
            }
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        intentFilter.addAction(ACTION_DUAL_NOTI_REFRESH);
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
