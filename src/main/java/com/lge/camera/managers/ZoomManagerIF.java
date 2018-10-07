package com.lge.camera.managers;

import android.view.View;
import com.lge.camera.components.BarAction;
import com.lge.camera.components.BarView.BarManagerListener;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.util.Utils;

public class ZoomManagerIF extends ZoomManagerBase implements BarAction, BarManagerListener {
    public ZoomManagerIF(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initInAndOutZoomBarValue() {
    }

    public void onBarSwitchedDuringRecording() {
    }

    public void notifySwitchingFinished() {
    }

    public boolean isInAndOutSwithing() {
        return false;
    }

    public void setMaxZoomForWideCamera(int secondZoomDefaultzoom) {
    }

    public int getMaxZoomForWideCamera() {
        return 0;
    }

    public void runOnUiThread(Object action) {
        this.mGet.runOnUiThread(action);
    }

    public void postOnUiThread(Object action, long delay) {
        this.mGet.postOnUiThread(action, delay);
    }

    public void removePostRunnable(Object object) {
        this.mGet.removePostRunnable(object);
    }

    public View findViewById(int id) {
        return this.mGet.findViewById(id);
    }

    public void rotateSettingBar(int mBarType, int value, boolean useAnim) {
    }

    public void updateAllBars(int mBarType, int value) {
    }

    public boolean isPaused() {
        return false;
    }

    public String getSettingValue(String key) {
        return this.mGet.getSettingValue(key);
    }

    public String getBarSettingValue(String key) {
        return null;
    }

    public boolean setBarSetting(String key, String value, boolean save) {
        return false;
    }

    public void resetBarDisappearTimer(int barType, int duration) {
        this.mGet.resetBarDisappearTimer(barType, duration);
    }

    public void switchCamera() {
        if (this.mGet != null && this.mGet.checkModuleValidate(192)) {
            if (FunctionProperties.isSupportedSwitchingAnimation()) {
                this.mGet.setGestureType(3);
                this.mGet.getCurPreviewBlurredBitmap(136, 240, 25, false);
                this.mGet.startCameraSwitchingAnimation(1);
            }
            this.mGet.handleSwitchCamera();
        }
    }

    public void resumeShutterless() {
    }

    public void pauseShutterless() {
    }

    public boolean checkZoomBarVisibilityCondition() {
        return true;
    }

    public void setZoomButtonVisibility(int visibility) {
    }

    public boolean isPreview4by3() {
        if (this.mGet == null) {
            return false;
        }
        String previewSize;
        if (this.mGet.getCameraState() == 6 || this.mGet.getCameraState() == 7) {
            previewSize = this.mGet.getCurrentSelectedVideoSize();
        } else {
            previewSize = this.mGet.getCurrentSelectedPreviewSize();
        }
        return Utils.calculate4by3Preview(previewSize);
    }

    public void stopDrawingExceedsLevel() {
    }

    public boolean isRearCamera() {
        if (this.mGet == null) {
            return false;
        }
        return this.mGet.isRearCamera();
    }

    public void initZoomValues(CameraParameters param) {
    }

    public String getShotMode() {
        if (this.mGet == null) {
            return "mode_normal";
        }
        return this.mGet.getShotMode();
    }
}
