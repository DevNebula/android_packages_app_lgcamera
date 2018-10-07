package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class UplusNotificationReceiver extends CameraBroadCastReceiver {
    public UplusNotificationReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "showModuleToast");
        this.mGet.showModuleToast(this.mGet.getAppContext().getString(C0088R.string.message_received), CameraConstants.TOAST_LENGTH_SHORT);
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("com.lguplus.umcgp5.im.action.msg.notification");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
