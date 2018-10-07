package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;

public class HdmiReceiver extends CameraBroadCastReceiver {
    private static final String DUAL_DISPLAY_INTENT = "android.intent.action.DUALDISPLAY";
    private static final String HDMI_CABLE_AUDIO_PLUG_NVIDIA = "android.intent.action.HDMI_AUDIO_PLUG";
    private static final String HDMI_CABLE_CONNECTED = "HDMI_CABLE_CONNECTED";
    private static final String HDMI_CABLE_DISCONNECTED = "HDMI_CABLE_DISCONNECTED";

    public HdmiReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        CamLog.m9v(CameraConstants.TAG, "mHdmiReciever RECEIVER IN");
        if (checkOnReceive(intent)) {
            String action = intent.getAction();
            if (HDMI_CABLE_CONNECTED.equals(action)) {
                CamLog.m9v(CameraConstants.TAG, "HDMICableConnectedEvent IN");
                hdmiConnectedAction();
            } else if (HDMI_CABLE_DISCONNECTED.equals(action)) {
                CamLog.m9v(CameraConstants.TAG, "HDMICable DisconnectedEvent IN");
                hdmiDisconnectedAction();
            } else if (HDMI_CABLE_DISCONNECTED.equals(action)) {
                CamLog.m9v(CameraConstants.TAG, "HDMICableConnectedEventFornVidia IN");
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    hdmiConnectedAction();
                } else if (state == 0) {
                    hdmiDisconnectedAction();
                }
            } else if (DUAL_DISPLAY_INTENT.equals(action)) {
                boolean state2 = intent.getBooleanExtra("state", false);
                CamLog.m9v(CameraConstants.TAG, "Dual Display Intent received, state: " + state2);
                if (state2) {
                    DualDisplayConnectedAction();
                } else {
                    DualDisplayDisconnectedAction();
                }
            } else {
                CamLog.m9v(CameraConstants.TAG, "other HDMI RCVR IN");
            }
        }
    }

    private void hdmiConnectedAction() {
        if (ModelProperties.isSupportHDMI_MHL()) {
            CamLog.m3d(CameraConstants.TAG, "It can support HDMI/MHL!!");
        } else if (CheckStatusManager.getCheckEnterOutSecure() == 0) {
            Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.error_cannot_use_hdmi), 0).show();
            this.mGet.getActivity().finish();
        }
    }

    private void hdmiDisconnectedAction() {
        if (ModelProperties.isSupportHDMI_MHL()) {
            CamLog.m3d(CameraConstants.TAG, "It can support HDMI/MHL!!");
        }
    }

    private void DualDisplayConnectedAction() {
        CamLog.m3d(CameraConstants.TAG, "DualDisplayConnectedAction");
        if (!ModelProperties.isSupportHDMI_MHL()) {
            CamLog.m3d(CameraConstants.TAG, "It can support HDMI/MHL!!");
        } else if (CheckStatusManager.getCheckEnterOutSecure() == 0) {
            Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.sp_dual_display_status_NORMAL), 0).show();
            this.mGet.getActivity().finish();
        }
    }

    private void DualDisplayDisconnectedAction() {
        CamLog.m3d(CameraConstants.TAG, "DualDisplayDisconnectedAction");
        if (!ModelProperties.isSupportHDMI_MHL()) {
        }
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory("android.intent.category.DEFAULT");
        intentFilter.addAction(HDMI_CABLE_CONNECTED);
        intentFilter.addAction(HDMI_CABLE_DISCONNECTED);
        intentFilter.addAction(HDMI_CABLE_AUDIO_PLUG_NVIDIA);
        intentFilter.addAction(DUAL_DISPLAY_INTENT);
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
