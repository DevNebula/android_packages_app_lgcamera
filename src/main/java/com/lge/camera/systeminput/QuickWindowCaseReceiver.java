package com.lge.camera.systeminput;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.QuickWindowUtils;
import com.lge.camera.util.SecureImageUtil;

public class QuickWindowCaseReceiver extends CameraBroadCastReceiver {
    public QuickWindowCaseReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(CameraConstants.ACTION_ACCESSORY_EVENT);
        intentFilter.addAction(CameraConstants.INTENT_ACTION_CAMERA_FINISH);
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] onReceive()");
        if (!checkOnReceive(intent)) {
            return;
        }
        if (QuickWindowUtils.getSmartCoverManager(this.mGet.getActivity()).getCoverSetting()) {
            String action = intent.getAction();
            if (action.equals(CameraConstants.INTENT_ACTION_CAMERA_FINISH)) {
                if (!this.mGet.getActivity().isFinishing() && CheckStatusManager.getCheckEnterOutSecure() == 0) {
                    this.mGet.getActivity().finish();
                    return;
                }
                return;
            } else if (action.equals(CameraConstants.ACTION_ACCESSORY_EVENT)) {
                int coverState = intent.getIntExtra(CameraConstants.EXTRA_ACCESSORY_STATE, 0);
                CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] state:" + coverState);
                if (coverState == 0) {
                    QuickWindowUtils.setQuickWindowCaseClosed(false);
                    this.mGet.changeCoverState(false);
                    return;
                } else if (coverState == 1) {
                    QuickWindowUtils.setQuickWindowCaseClosed(true);
                    this.mGet.changeCoverState(true);
                    checkSecureCamera();
                    return;
                } else if (coverState == 2) {
                    QuickWindowUtils.setQuickWindowCaseClosed(false);
                    this.mGet.changeCoverState(false);
                    return;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        CamLog.m3d(CameraConstants.TAG, "[QUICK COVER] setting disable.");
    }

    private void checkSecureCamera() {
        if (SecureImageUtil.isSecureCamera()) {
            Activity activity = this.mGet.getActivity();
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
