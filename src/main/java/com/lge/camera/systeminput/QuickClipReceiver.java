package com.lge.camera.systeminput;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QuickClipUtil;

public class QuickClipReceiver extends CameraBroadCastReceiver {
    public QuickClipReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context ctx, Intent intent) {
        if (intent == null) {
            CamLog.m3d(CameraConstants.TAG, "Intent is null");
        } else if (intent.getAction().equals(CameraConstants.QUICK_CLIP_FILTER)) {
            ComponentName componentName = (ComponentName) intent.getExtra("android.intent.extra.CHOSEN_COMPONENT");
            String activityName = componentName.getClassName();
            CamLog.m3d(CameraConstants.TAG, "Quickclip selected Activity : " + componentName.getClassName());
            QuickClipUtil.reportActivitySelected(ctx, activityName);
            LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_QUICKCLIP_SHAREBYMORE);
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter(CameraConstants.QUICK_CLIP_FILTER);
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
