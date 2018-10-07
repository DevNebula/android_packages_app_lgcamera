package com.lge.camera.managers;

import android.provider.Settings.System;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.systemservice.core.LEDManager;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.LGLedRecord;

public class CamLEDManager extends ManagerInterfaceImpl {
    private final int LED_ENABLE = 1;
    private final String NAME_FACE_DETECTION_SETTING = "emotional_led_back_camera_face_detecting_noti";
    private final String NAME_TIMER_SETTING = "emotional_led_back_camera_timer_noti";
    private int mFaceLedEnabled = 1;
    private LEDManager mLEDManager = null;
    private LGLedRecord mRecord = null;
    private int mTimerLedEnabled = 1;

    public CamLEDManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        initEmotionalLED();
    }

    private boolean isFaceLEDSettingEnabled() {
        this.mFaceLedEnabled = System.getInt(this.mGet.getAppContext().getContentResolver(), "emotional_led_back_camera_face_detecting_noti", 1);
        if (this.mFaceLedEnabled == 1) {
            return true;
        }
        return false;
    }

    private boolean isTimerLEDSettingEnabled() {
        this.mTimerLedEnabled = System.getInt(this.mGet.getAppContext().getContentResolver(), "emotional_led_back_camera_timer_noti", 1);
        if (this.mTimerLedEnabled == 1) {
            return true;
        }
        return false;
    }

    private void initEmotionalLED() {
        CamLog.m3d(CameraConstants.TAG, "Initialize Emotional LED");
        this.mLEDManager = (LEDManager) new LGContext(this.mGet.getAppContext()).getLGSystemService("emotionled");
        this.mRecord = new LGLedRecord();
        this.mRecord.priority = 0;
        this.mRecord.flags = 1;
        this.mRecord.whichLedPlay = 2;
    }

    public void startLED(int emotionalLEDId) {
        if (emotionalLEDId == 0 && !isTimerLEDSettingEnabled()) {
            CamLog.m3d(CameraConstants.TAG, "Because Timer LED setting is disabled, Can't start LED");
        } else if (emotionalLEDId != 1 || isFaceLEDSettingEnabled()) {
            CamLog.m3d(CameraConstants.TAG, "LED is started, ID : " + emotionalLEDId);
            if (this.mLEDManager != null) {
                this.mLEDManager.startPattern(this.mGet.getActivity().getPackageName(), emotionalLEDId, this.mRecord);
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "Because Face LED setting is disabled, Can't start LED");
        }
    }

    public void stopLED(int emotionalLEDId) {
        if (emotionalLEDId == 0 && !isTimerLEDSettingEnabled()) {
            CamLog.m3d(CameraConstants.TAG, "Because Timer LED setting is disabled, Can't stop LED");
        } else if (emotionalLEDId != 1 || isFaceLEDSettingEnabled()) {
            CamLog.m3d(CameraConstants.TAG, "LED is stopped, ID : " + emotionalLEDId);
            if (this.mLEDManager != null) {
                this.mLEDManager.stopPattern(this.mGet.getActivity().getPackageName(), emotionalLEDId);
            }
        } else {
            CamLog.m3d(CameraConstants.TAG, "Because Face LED setting is disabled, Can't stop LED");
        }
    }

    public void setRecordPatternId(String filaPath) {
        CamLog.m3d(CameraConstants.TAG, "LED pattern file : " + filaPath);
        if (this.mRecord != null) {
            this.mRecord.patternFilePath = filaPath;
        }
    }

    public String getTimerPatternId(String timer) {
        if ("3".equals(timer)) {
            return CameraConstants.ID_CAMERA_TIMER_EFFECT_3SEC;
        }
        return CameraConstants.ID_CAMERA_TIMER_EFFECT_10SEC;
    }

    public void setDegree(int degree, boolean animation) {
    }

    public void onDestroy() {
        this.mLEDManager = null;
        this.mRecord = null;
        super.onDestroy();
    }
}
