package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.TelephonyUtil;

public class CallPopUpReceiver extends CameraBroadCastReceiver {
    private static final String CALLALERTING_ANSWER = "com.lge.action.CALLALERTING_ANSWER";
    private static final String CALLALERTING_HIDE = "com.lge.action.CALLALERTING_HIDE";
    private static final String CALLALERTING_P2P_ANSWER = "com.lge.p2pclients.call.ANSWER_RINGING_CALL";
    private static final String CALLALERTING_SHOW = "com.lge.action.CALLALERTING_SHOW";

    public CallPopUpReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            CamLog.m3d(CameraConstants.TAG, "CallPopUpReceiver action = " + intent.getAction());
            String action = intent.getAction();
            if (CALLALERTING_SHOW.equals(action)) {
                this.mGet.setBlockTouchByCallPopup(true);
                if (TelephonyUtil.phoneInCall(context)) {
                    this.mGet.disableCheeseShutterByCallPopup();
                }
            } else if (CALLALERTING_HIDE.equals(action)) {
                this.mGet.setBlockTouchByCallPopup(false);
            } else if (CALLALERTING_ANSWER.equals(action) || CALLALERTING_P2P_ANSWER.equals(action)) {
                this.mGet.stopRecordByCallPopup();
            }
        }
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CALLALERTING_SHOW);
        intentFilter.addAction(CALLALERTING_HIDE);
        intentFilter.addAction(CALLALERTING_ANSWER);
        intentFilter.addAction(CALLALERTING_P2P_ANSWER);
        return intentFilter;
    }
}
