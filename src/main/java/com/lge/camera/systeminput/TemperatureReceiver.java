package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.SecureImageUtil;

public class TemperatureReceiver extends CameraBroadCastReceiver implements OnRemoveHandler {
    private static final int TOAST_WATING_TIME = 300;

    public TemperatureReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        CamLog.m3d(CameraConstants.TAG, "onReceive(), action :" + intent.getAction());
        if (this.mGet.isRecordingState()) {
            CamLog.m3d(CameraConstants.TAG, "Camera is finishing due to high temperature on recording. It's not an error.");
            if (this.mGet.isScreenPinningState()) {
                Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.high_temp_action_on_recording), 1).show();
                CamLog.m3d(CameraConstants.TAG, "Because CameraApp is pinned, recording is stopped only.");
                this.mGet.stopRecordByCallPopup();
            } else if (SecureImageUtil.isSecureCamera()) {
                this.mGet.showModuleToast(this.mGet.getAppContext().getString(C0088R.string.high_temp_action_on_recording), CameraConstants.TOAST_LENGTH_LONG);
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        TemperatureReceiver.this.mGet.getActivity().finish();
                    }
                }, CameraConstants.TOAST_LENGTH_SHORT);
            } else {
                Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.high_temp_action_on_recording), 1).show();
                this.mGet.getActivity().finish();
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    Toast.makeText(TemperatureReceiver.this.mGet.getAppContext(), TemperatureReceiver.this.mGet.getAppContext().getString(C0088R.string.high_temp_action_on_recording), 0).show();
                }
            }, 300);
        }
    }

    protected IntentFilter getIntentFilter() {
        return new IntentFilter("com.lge.android.intent.action.SHUTDOWN_CAMERA");
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
