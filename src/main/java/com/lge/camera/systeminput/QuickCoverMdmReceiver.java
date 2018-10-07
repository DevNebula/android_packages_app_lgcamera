package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.QuickWindowUtils;

public class QuickCoverMdmReceiver extends CameraBroadCastReceiver {
    private static final String MDM_QUICKCOVER_CHANGED = "com.lge.mdm.intent.action.ACTION_QUICKCIRCLE_POLICY_CHANGED";

    public QuickCoverMdmReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            CamLog.m3d(CameraConstants.TAG, "QuickCoverMDMReceiver action = " + intent.getAction());
            if (QuickWindowUtils.getCurrentCoverStatus(this.mGet.getActivity()) != 0) {
                QuickWindowUtils.setQuickWindowCaseClosed(false);
                this.mGet.changeCoverState(false);
            }
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MDM_QUICKCOVER_CHANGED);
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
