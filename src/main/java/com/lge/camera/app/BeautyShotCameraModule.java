package com.lge.camera.app;

import android.content.res.Configuration;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.managers.BeautyShotManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;

public class BeautyShotCameraModule extends BeautyShotCameraModuleBase {
    public BeautyShotCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public synchronized void setBeautyEngineOn(boolean bIsOn) {
        CamLog.m7i(CameraConstants.TAG, " setBeautyEngineOn : " + bIsOn + " mBeautyManager.isBeautyOn() = " + this.mBeautyManager.isBeautyOn());
        if (!(isRearCamera() || this.mBeautyManager.isBeautyOn() == bIsOn)) {
            CamLog.m7i(CameraConstants.TAG, " beauty manager Create ");
            this.mBeautyManager.release();
            if (bIsOn && FunctionProperties.isSupportedBeautyShot()) {
                this.mBeautyManager = new BeautyShotManager(this.mGet, bIsOn);
            }
        }
    }

    protected void releaseEngine() {
        CamLog.m3d(CameraConstants.TAG, "Beauty releaseEngine.");
        setPreviewCallbackAll(false);
        this.mBeautyManager.release();
        setBarVisible(false);
    }

    protected void closeCamera() {
        super.closeCamera();
        if (this.mCameraDevice == null) {
            this.mBeautyManager.setCamera(null);
        }
    }

    protected void afterCommonRequester() {
        if (this.mParamUpdater != null) {
            CamLog.m3d(CameraConstants.TAG, "afterCommonRequester");
            if (!isRearCamera()) {
                setBeautyEngineOn(true);
                initBeautyManager();
                this.mParamUpdater = this.mBeautyManager.setBeautyParam(this.mParamUpdater);
                this.mParamUpdater = this.mBeautyManager.setBeautyRelightingParam(this.mParamUpdater);
            }
            super.afterCommonRequester();
        }
    }

    protected synchronized void initBeautyManager() {
        int relightingLevel = 0;
        synchronized (this) {
            if (!(isRearCamera() || this.mBeautyManager.isInit())) {
                int beautyLevel = ModelProperties.getBeautyDefaultLevel();
                String settingValue = this.mGet.getCurSettingValue(Setting.KEY_BEAUTYSHOT);
                if (!(settingValue == null || "".equals(settingValue) || "not found".equals(settingValue))) {
                    beautyLevel = Integer.parseInt(settingValue);
                }
                if (FunctionProperties.isSupportedRelighting()) {
                    relightingLevel = ModelProperties.getRelightingDefaultLevel();
                }
                String relightValue = this.mGet.getCurSettingValue(Setting.KEY_RELIGHTING);
                if (!(relightValue == null || "".equals(relightValue) || "not found".equals(relightValue))) {
                    relightingLevel = Integer.parseInt(relightValue);
                }
                this.mBeautyManager.init(this.mCameraDevice, this.mCameraId, beautyLevel, relightingLevel, 0, 100);
            }
        }
    }

    public void init() {
        super.init();
        this.mIsOneShotPreviewDone = false;
        setBeautyEngineOn(true);
    }

    public void onPauseBefore() {
        releaseEngine();
        super.onPauseBefore();
    }

    public void onDestroy() {
        ViewGroup vg = (ViewGroup) findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mBeautyShotView == null)) {
            vg.removeView(this.mBeautyShotView);
            this.mBeautyShotView = null;
        }
        this.mBeautyManager = BeautyShotManager.NULL;
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        ViewGroup vg = (ViewGroup) findViewById(C0088R.id.camera_controls);
        if (vg != null && this.mBeautyShotView != null) {
            vg.removeView(this.mBeautyShotView);
            this.mBeautyShotView = inflateView(C0088R.layout.beautyshot);
            if (this.mBeautyShotView != null && this.mBeautyManager.isBeautyOn()) {
                vg.addView(this.mBeautyShotView, 0, new LayoutParams(-1, -1));
                initBeautyshotBar();
            }
        }
    }

    protected void onTakePictureBefore() {
        this.mBeautyManager.onTakePictureBefore();
        super.onTakePictureBefore();
    }

    protected void onTakePictureAfter() {
        this.mSnapShotChecker.setPictureCallbackState(3);
        this.mBeautyManager.onTakePictureAfter();
        setBarEnable(true, true);
        CamLog.m3d(CameraConstants.TAG, "onPictureTakenCallback progress = callback after");
        super.onTakePictureAfter();
        this.mSnapShotChecker.setPictureCallbackState(0);
    }

    protected void oneShotPreviewCallbackDone() {
        if (!this.mPostviewManager.isPostviewShowing()) {
            super.oneShotPreviewCallbackDone();
            if (isRearCamera()) {
                CamLog.m3d(CameraConstants.TAG, "oneShotPreviewCallbackDone : not Front Camera !!!!");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "oneShotPreviewCallbackDone");
            this.mIsOneShotPreviewDone = true;
        }
    }

    public boolean doBackKey() {
        if (this.mAdvancedFilmManager == null || !isBarVisible(1)) {
            return super.doBackKey();
        }
        this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
        return true;
    }

    protected void onChangeModuleBefore() {
        super.onChangeModuleBefore();
        releaseEngine();
    }

    protected boolean doSetParamForStartRecording(CameraParameters parameters, boolean recordStart, ListPreference listPref, String videoSize) {
        CamLog.m7i(CameraConstants.TAG, "Video Size : " + videoSize);
        if (!this.mBeautyManager.isSupportRecordingBeauty(videoSize)) {
            releaseEngine();
        }
        this.mParamUpdater = this.mBeautyManager.setBeautyParam(this.mParamUpdater);
        return super.doSetParamForStartRecording(parameters, recordStart, listPref, videoSize);
    }

    protected void setGestureEngineForQuickShot() {
        if (this.mGestureShutterManager != null && "on".equals(getSettingValue(Setting.KEY_MOTION_QUICKVIEWER))) {
            this.mGestureShutterManager.runGestureEngine(false);
            this.mGestureShutterManager.startMotionEngine();
        }
    }
}
