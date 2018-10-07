package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class LGLensSupportChangedReceiver extends CameraBroadCastReceiver {
    private static final String LGLENS_SUPPORT = "lglens_support";
    private static final String LGLENS_SUPPORT_CHANGED = "com.lge.ellievision.action.LGLENS_SUPPORT_CHANGED";

    public LGLensSupportChangedReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            CamLog.m3d(CameraConstants.TAG, "LGLensSupportChangedReceiver action = " + intent.getAction());
            if (LGLENS_SUPPORT_CHANGED.equals(intent.getAction())) {
                String extra = intent.getStringExtra(LGLENS_SUPPORT);
                CamLog.m3d(CameraConstants.TAG, "LGLens support :" + extra);
                if ("support".equals(extra) || "not_support".equals(extra)) {
                    this.mGet.refreshUspZone(false);
                }
            }
        }
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LGLENS_SUPPORT_CHANGED);
        return intentFilter;
    }
}
