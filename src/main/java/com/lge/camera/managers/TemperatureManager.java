package com.lge.camera.managers;

import android.app.Activity;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;

public class TemperatureManager extends ManagerInterfaceImpl {
    private static final int HEATING_VIDEO_HEIGHT = 720;
    private static final int HEATING_VIDEO_WIDTH = 1280;
    private static final long HEAT_DELAY_10MIN = 600000;
    private static final long HEAT_DELAY_3MIN_AFTER_10MIN = 180000;
    private static final long HEAT_DELAY_ZERO = 0;
    private static final int HEAT_WARNING_1ST = 0;
    private static final int HEAT_WARNING_2ND = 1;
    private static final int MSG_HEATING_SHOW = 0;
    private static final int MSG_HEATING_START = 1;
    private static final int MSG_HEATING_STOP = 2;
    private int mBatteryLevel = 0;
    private boolean mCharging = false;
    private int mHeatWarningCount = 0;
    private TemperatureHandler mTemperatureHandler = new TemperatureHandler();

    private class TemperatureHandler extends Handler {
        private TemperatureHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    TemperatureManager.this.showHeatingWarning();
                    return;
                case 1:
                    TemperatureManager.this.startHeatingWarning();
                    return;
                case 2:
                    TemperatureManager.this.stopHeatingWarning();
                    return;
                default:
                    return;
            }
        }
    }

    public TemperatureManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public int getBatteryLevel() {
        return this.mBatteryLevel;
    }

    public void setBatteryLevel(int level) {
        this.mBatteryLevel = level;
    }

    public void setCharging(boolean charging) {
        this.mCharging = charging;
    }

    public boolean isCharging() {
        return this.mCharging;
    }

    public static boolean IsHeatingVideoSize(String recordingSize) {
        boolean z = true;
        if (recordingSize == null) {
            Log.e(CameraConstants.TAG, "IsHeatingVideoSize : RecordingSize is null");
            return false;
        }
        int[] size = Utils.sizeStringToArray(recordingSize);
        if (size[0] < HEATING_VIDEO_WIDTH || size[1] < 720) {
            z = false;
        }
        return z;
    }

    public static boolean IsHeatingVideoSize(Size recordingSize) {
        if (recordingSize == null) {
            Log.e(CameraConstants.TAG, "IsHeatingVideoSize : RecordingSize is null");
            return false;
        } else if (recordingSize.width < HEATING_VIDEO_WIDTH || recordingSize.height < 720) {
            return false;
        } else {
            return true;
        }
    }

    public void showHeatingWarning() {
        CamLog.m9v(CameraConstants.TAG, "showHeatingWarning");
        if (isCharging()) {
            this.mGet.showToast(String.format(this.mGet.getAppContext().getString(C0088R.string.sp_warning_high_temperature_on_recording_NORMAL), new Object[0]), CameraConstants.TOAST_LENGTH_SHORT);
            Message msg = Message.obtain();
            msg.what = 1;
            if (this.mTemperatureHandler != null) {
                this.mTemperatureHandler.sendMessage(msg);
            }
        }
    }

    public void startHeatingWarning() {
        long delay;
        CamLog.m9v(CameraConstants.TAG, "startHeatingWarning");
        Message msg = Message.obtain();
        switch (this.mHeatWarningCount) {
            case 0:
                msg.what = 0;
                delay = HEAT_DELAY_10MIN;
                break;
            case 1:
                msg.what = 0;
                delay = HEAT_DELAY_3MIN_AFTER_10MIN;
                break;
            default:
                msg.what = 2;
                delay = 0;
                break;
        }
        this.mHeatWarningCount++;
        CamLog.m9v(CameraConstants.TAG, "startHeatingWarning : delay = " + delay);
        this.mTemperatureHandler.sendMessageDelayed(msg, delay);
    }

    public void stopHeatingWarning() {
        CamLog.m9v(CameraConstants.TAG, "stopHeatingWarning");
        if (this.mTemperatureHandler.hasMessages(0)) {
            this.mTemperatureHandler.removeMessages(0);
        }
        if (this.mHeatWarningCount != 0) {
            this.mHeatWarningCount = 0;
        }
    }

    public static void backlightControlByVal(Activity activity, float scale) {
        try {
            int curBrightnessMode = System.getInt(activity.getContentResolver(), "screen_brightness_mode");
            int curBrightnessValue = System.getInt(activity.getContentResolver(), "screen_brightness");
            float curValue = ((float) curBrightnessValue) / 255.0f;
            float ratio = scale;
            LayoutParams params = activity.getWindow().getAttributes();
            if (curBrightnessMode == 0) {
                params.screenBrightness = curValue * ratio;
            } else {
                params.screenBrightness = -1.0f;
            }
            activity.getWindow().setAttributes(params);
            CamLog.m3d(CameraConstants.TAG, "Success to backlight control ByVal:curMode = " + curBrightnessMode + ", curBright (30~255) = " + curBrightnessValue + ", val = " + curValue + ", ratio = " + ratio + ", set = " + (curValue * ratio));
        } catch (Exception e) {
            CamLog.m4d(CameraConstants.TAG, "Fail to backlight control:", e);
        }
    }

    public void setDegree(int degree, boolean animation) {
    }
}
