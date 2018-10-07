package com.lge.camera.app.ext;

import android.content.res.Configuration;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CineZoomCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.hardware.LGCamera.CineZoomData;
import com.lge.hardware.LGCamera.CineZoomStateListener;

public class CinemaModule extends CinemaModuleBase {
    private CineZoomCallback mCineZoomCallback = new C03551();
    protected CineZoomStateListener mCineZoomListener = new C03562();
    private boolean mIsRestartInCine = false;

    /* renamed from: com.lge.camera.app.ext.CinemaModule$1 */
    class C03551 implements CineZoomCallback {
        C03551() {
        }

        public void onCineZoom(int status) {
            CinemaModule.this.onCineZoomHAL3(status);
        }
    }

    /* renamed from: com.lge.camera.app.ext.CinemaModule$2 */
    class C03562 implements CineZoomStateListener {
        C03562() {
        }

        public void onCineZoomListen(CineZoomData cineZoomData) {
            CamLog.m3d(CameraConstants.TAG, "[CineZoom] onCineZoomListen");
            CinemaModule.this.onCineZoom(cineZoomData);
        }
    }

    /* renamed from: com.lge.camera.app.ext.CinemaModule$3 */
    class C03573 implements onQuickClipListListener {
        C03573() {
        }

        public void onListOpend() {
            CinemaModule.this.showCommandArearUI(false);
            CinemaModule.this.access$000(false);
            if (CinemaModule.this.mCineZoomManager != null) {
                CinemaModule.this.mCineZoomManager.setCineZoomLayoutVisibility(false);
            }
        }

        public void onListClosed() {
            CinemaModule.this.showCommandArearUI(true);
            CinemaModule.this.access$100();
            if (CinemaModule.this.mCinemaFilterManager != null && !CinemaModule.this.mCinemaFilterManager.isCinemaLUTVisible() && CinemaModule.this.mCineZoomManager != null) {
                CinemaModule.this.mCineZoomManager.setCineZoomLayoutVisibility(true);
            }
        }
    }

    public CinemaModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mCinemaFilterManager);
        this.mManagerList.add(this.mCineZoomManager);
        setCineZoomListener(true);
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        super.doRunnableStartRecorder(info);
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.doRunnableStartRecorder();
        }
    }

    protected void onCineZoom(CineZoomData cineZoomData) {
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.onCineZoom(cineZoomData);
        }
    }

    protected void onCineZoomHAL3(int status) {
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.onCineZoomHAL3(status);
        }
    }

    public void prepareRecordingVideo() {
        if (this.mCineZoomManager == null || !this.mCineZoomManager.isCineZoomJogBarTouching()) {
            super.prepareRecordingVideo();
        }
    }

    public void onVideoShutterClicked() {
        super.onVideoShutterClicked();
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.onVideoShutterClicked();
        }
    }

    protected void restoreRecorderToIdle() {
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.onStopCineZoom();
        }
        super.restoreRecorderToIdle();
    }

    public void onVideoStopClicked(boolean useThread, boolean stopByButton) {
        super.onVideoStopClicked(useThread, stopByButton);
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.onStopCineZoom();
        }
    }

    protected void setCineZoomListener(boolean enable) {
        CineZoomStateListener cineZoomStateListener = null;
        if (this.mCameraDevice != null) {
            CamLog.m3d(CameraConstants.TAG, "[CineZoom] setCineZoomListener enable : " + enable);
            CameraProxy cameraProxy;
            if (FunctionProperties.getSupportedHal() == 2) {
                CineZoomCallback cineZoomCallback;
                cameraProxy = this.mCameraDevice;
                if (enable) {
                    cineZoomCallback = this.mCineZoomCallback;
                }
                cameraProxy.setCineZoomCallback(cineZoomCallback);
                return;
            }
            cameraProxy = this.mCameraDevice;
            if (enable) {
                cineZoomStateListener = this.mCineZoomListener;
            }
            cameraProxy.setCineZoomListener(cineZoomStateListener);
        }
    }

    public boolean isZoomAvailable() {
        if (this.mCineZoomManager != null) {
            return this.mCineZoomManager.isCenterZoomAvailable();
        }
        return true;
    }

    public boolean isFocusEnableCondition() {
        boolean result = super.isFocusEnableCondition();
        if (this.mCineZoomManager != null) {
            return result & this.mCineZoomManager.isFocusAvailable();
        }
        return result;
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(false);
        }
    }

    public void onZoomHide() {
        super.onZoomHide();
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(true);
        }
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mCineZoomManager == null) {
            return true;
        }
        this.mCineZoomManager.setCineZoomLayoutVisibility(true);
        return true;
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(false);
        }
        return true;
    }

    public void onShowAEBar() {
        super.onShowAEBar();
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(false);
        }
    }

    public void onHideAEBar() {
        super.onHideAEBar();
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL) && this.mCinemaFilterManager != null && !this.mCinemaFilterManager.isCinemaLUTVisible() && this.mCineZoomManager != null) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(true);
        }
    }

    public void onVideoPauseClicked() {
        super.onVideoPauseClicked();
        if (this.mCineZoomManager != null && getCameraState() == 7) {
            this.mCineZoomManager.onVideoPauseClicked();
        }
    }

    public void onVideoResumeClicked() {
        super.onVideoResumeClicked();
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.onVideoResumeClicked();
        }
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    public void changeRequesterForZoom() {
        if (this.mParamUpdater != null && this.mZoomManager != null) {
            int initZoomValue = 0;
            if (!isCineZoomGuideShowing()) {
                initZoomValue = this.mZoomManager.getZoomValue();
            }
            this.mParamUpdater.addRequester("zoom", Integer.toString(initZoomValue), false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_JOG_ZOOM, Integer.toString(0), false, true);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        setCineZoomListener(false);
    }

    public void onPauseBefore() {
        setCineZoomListener(false);
        super.onPauseBefore();
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        setCineZoomListener(true);
    }

    public void doTouchAFInRecording() {
        if (!isCineZoomGuideShowing()) {
            super.doTouchAFInRecording();
        }
    }

    protected boolean isCineZoomGuideShowing() {
        return this.mCineZoomManager == null ? false : this.mCineZoomManager.isCineZoomButtonSelected();
    }

    public boolean checkQuickButtonAvailable() {
        if (!super.checkQuickButtonAvailable() || this.mCineZoomManager == null || this.mCineZoomManager.isCineZooming()) {
            return false;
        }
        return true;
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(!doByAction);
        }
    }

    protected boolean isFingerDetectionSupportedMode() {
        return false;
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        showDoubleCamera(true);
    }

    protected boolean isAvailableSteadyCam() {
        return !isUHDmode();
    }

    public String getShotMode() {
        return CameraConstants.MODE_CINEMA;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mCineZoomManager != null) {
            this.mCineZoomManager.init();
        }
    }

    public String getRecordingType() {
        if (FunctionProperties.isSupportedLogProfile()) {
            return CameraConstants.VIDEO_CINEMA_TYPE;
        }
        if (this.mCinemaFilterManager == null || this.mCinemaFilterManager.getCurrentLUTNum() == 1) {
            return CameraConstants.VIDEO_CINE_HDR10_TYPE;
        }
        return CameraConstants.VIDEO_CINE_HDR10_EFFECT_ON_TYPE;
    }

    public int getModeColumn() {
        if (FunctionProperties.isSupportedLogProfile()) {
            return 23;
        }
        if (this.mCinemaFilterManager == null || this.mCinemaFilterManager.getCurrentLUTNum() == 1) {
            return 26;
        }
        return 27;
    }

    public boolean isQuickClipShowingCondition() {
        return super.isQuickClipShowingCondition() && this.mCameraState != 5;
    }

    protected void setQuickClipListListener() {
        if (this.mQuickClipManager != null && isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipListListener(new C03573());
        }
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_cheese_shutter_or_timer:
                case C0088R.id.indicator_item_hdr_or_flash:
                    return true;
                case C0088R.id.indicator_item_steady:
                    return isAvailableSteadyCam();
            }
        }
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
                return true;
        }
        return false;
    }

    public void childSettingMenuClicked(String key, String value, int clickedType) {
        if (checkModuleValidate(207)) {
            super.childSettingMenuClicked(key, value, clickedType);
            if (FunctionProperties.isSupportedHDR10() && !this.mSteadyChangedWithReopenCamera && Setting.KEY_VIDEO_STEADY.equals(key) && this.mCameraDevice != null && this.mParamUpdater != null) {
                if (this.mCameraStartUpThread == null || !this.mCameraStartUpThread.isAlive()) {
                    this.mGet.setCameraChanging(1);
                    this.mParamUpdater.updateAllParameters(this.mCameraDevice.getParameters());
                    stopPreview();
                    closeCamera();
                    startCameraDevice(2);
                    this.mIsRestartInCine = true;
                    this.mSteadyChangedWithReopenCamera = true;
                    return;
                }
                CamLog.m11w(CameraConstants.TAG, "Already Restarting Camera, return.");
                return;
            }
            return;
        }
        CamLog.m11w(CameraConstants.TAG, "childSettingMenuClicked, app finishing or recording. return.");
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        if (this.mIsRestartInCine) {
            this.mIsRestartInCine = false;
            this.mGet.removeCameraChanging(1);
            setCineZoomListener(false);
            setCineZoomListener(true);
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
        super.oneShotPreviewCallbackDoneAfter();
        if (this.mSteadyChangedWithReopenCamera && this.mFocusManager != null) {
            this.mSteadyChangedWithReopenCamera = false;
            this.mFocusManager.initializeFocusManagerByForce();
            this.mFocusManager.initAFView();
            if (!isRecordingState()) {
                this.mFocusManager.registerCallback();
            }
        }
    }

    protected void restoreSteadyCamSetting() {
        if (!this.mSteadyChangedWithReopenCamera) {
            super.restoreSteadyCamSetting();
        }
    }

    public boolean onVideoShutterClickedBefore() {
        return !this.mSteadyChangedWithReopenCamera && super.onVideoShutterClickedBefore();
    }

    protected boolean onReviewThumbnailClicked(int waitType) {
        if (super.onReviewThumbnailClicked(waitType)) {
            return true;
        }
        if (waitType == 1 || !FunctionProperties.isSupportedHDR10()) {
            return false;
        }
        setPreviewCoverVisibility(0, false);
        return false;
    }
}
