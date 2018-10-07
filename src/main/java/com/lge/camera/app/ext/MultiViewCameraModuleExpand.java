package com.lge.camera.app.ext;

import android.view.KeyEvent;
import android.view.MotionEvent;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.managers.ext.MultiViewLayoutSelectionManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;

public class MultiViewCameraModuleExpand extends MultiViewCameraModule {

    /* renamed from: com.lge.camera.app.ext.MultiViewCameraModuleExpand$5 */
    class C04155 implements Runnable {
        C04155() {
        }

        public void run() {
            if (MultiViewCameraModuleExpand.this.mFrameShotProgressCount == 1 && CameraConstants.MODE_SQUARE_SPLICE.equals(MultiViewCameraModuleExpand.this.getShotMode())) {
                MultiViewCameraModuleExpand.this.mCameraDegree = MultiViewCameraModuleExpand.this.getSaveImageDegree();
            }
            MultiViewCameraModuleExpand.this.doAfterFrameShot();
        }
    }

    public MultiViewCameraModuleExpand(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        this.mLayoutSelectionManager = new MultiViewLayoutSelectionManager(this);
        super.init();
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            this.mManagerList.add(this.mLayoutSelectionManager);
        }
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        super.doRunnableStartRecorder(info);
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibility(false);
        }
    }

    protected boolean doStartTimerShot(TimerType timerType) {
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibility(false);
        }
        return super.doStartTimerShot(timerType);
    }

    public void onTakePictureBefore() {
        if (this.mMultiViewManager != null && "on".equals(getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT))) {
            this.mMultiViewManager.setFrameShotGuideVisibility(true);
        }
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibilityChildMenu(false, false);
            this.mLayoutSelectionManager.setEnabled(false);
        }
        super.onTakePictureBefore();
    }

    protected void onTakePictureAfter() {
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setEnabled(true);
        }
        super.onTakePictureAfter();
    }

    protected void hideManagerForSelfTimer(boolean keepState) {
        super.hideManagerForSelfTimer(false);
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        super.doCleanView(doByAction, useAnimation, saveState);
        if (!this.mGet.isModuleChanging()) {
            if (doByAction) {
                this.mLayoutSelectionManager.setVisibility(false);
            } else {
                this.mLayoutSelectionManager.setVisibility(true);
            }
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        if (this.mMultiViewManager != null) {
            this.mMultiViewManager.setFrameShotGuideVisibility(false);
        }
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibility(false);
        }
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL)) {
            return true;
        }
        if (this.mMultiViewManager != null && "on".equals(getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT))) {
            this.mMultiViewManager.setFrameShotGuideVisibility(true);
        }
        if (this.mLayoutSelectionManager == null) {
            return true;
        }
        this.mLayoutSelectionManager.setVisibility(true);
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 1:
                if (this.mLayoutSelectionManager.isOpen()) {
                    this.mLayoutSelectionManager.setVisibilityChildMenu(false, true);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void afterPreviewCaptured() {
        super.afterPreviewCaptured();
        if (this.mMultiviewFrame.isCapturingPreview() && !"on".equals(getSettingValue(Setting.KEY_VOICESHUTTER))) {
            AudioUtil.setAudioFocus(getAppContext(), false);
        }
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (MultiViewCameraModuleExpand.this.mLayoutSelectionManager != null) {
                    MultiViewCameraModuleExpand.this.mLayoutSelectionManager.setVisibility(true);
                }
            }
        });
    }

    protected void multiviewMakingCollageEnd() {
        super.multiviewMakingCollageEnd();
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (MultiViewCameraModuleExpand.this.mLayoutSelectionManager != null) {
                    MultiViewCameraModuleExpand.this.mLayoutSelectionManager.setVisibility(true);
                    MultiViewCameraModuleExpand.this.mLayoutSelectionManager.setEnabled(true);
                }
            }
        });
    }

    protected void changeCommandButtons() {
        if (!isMultiviewFrameShot()) {
            super.changeCommandButtons();
        }
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        super.enableControls(enable, changeColor);
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setEnabled(enable);
        }
    }

    public boolean doBackKey() {
        if (this.mLayoutSelectionManager != null && this.mLayoutSelectionManager.doBackKey()) {
            CamLog.m3d(CameraConstants.TAG, "doBackKey aborted by layout selector - ignore back key");
            return true;
        } else if (this.mIsTakingPicture || isProgressDialogVisible() || this.mIsCapturingPreview) {
            CamLog.m3d(CameraConstants.TAG, "doBackKey EXIT - ignore back key.");
            return true;
        } else if (closeModeAndSettingMenu()) {
            return true;
        } else {
            return super.doBackKey();
        }
    }

    public void onPauseBefore() {
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.removeView();
        }
        super.onPauseBefore();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager = null;
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        if (this.mLayoutSelectionManager == null) {
            return true;
        }
        this.mLayoutSelectionManager.setVisibility(true);
        return true;
    }

    protected void doTakePicture() {
        if (this.mMultiviewFrame.isCapturingPreview() || isProgressDialogVisible() || isRotateDialogVisible() || this.mGet.isModuleChanging() || !checkModuleValidate(223)) {
            CamLog.m3d(CameraConstants.TAG, "-cp- Do not start capture : preview is still capturing");
            return;
        }
        DebugUtil.setStartTime("[1] MV image: shutter pressed ~ takeFrameShotPicture");
        super.doTakePicture();
        if (isMultiviewFrameShot()) {
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                this.mSpliceViewImageImportManager.showImportButton(false);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
            }
            takeFrameShotPicture();
            return;
        }
        this.mCameraDegree = getSaveImageDegree();
        this.mRenderThread.setCollageImageOnState(true);
        this.mRenderThread.takeGLView();
        access$300(true, true, false);
        showDialog(5);
    }

    public boolean onVolumeKeyLongPressed(int keyCode, KeyEvent event) {
        if (isMultiviewFrameShot()) {
            doIntervalFrameShotMV(true);
        }
        return true;
    }

    public boolean isFocusEnableCondition() {
        return false;
    }

    protected void doAfterFrameShot() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (MultiViewCameraModuleExpand.this.mLayoutSelectionManager != null) {
                    MultiViewCameraModuleExpand.this.mLayoutSelectionManager.setEnabled(true);
                }
            }
        });
        this.mFrameShotProgressCount++;
        CamLog.m3d(CameraConstants.TAG, "-multiview- doAfterFrameShot mFrameShotProgressCount = " + this.mFrameShotProgressCount);
        if (this.mFrameShotCameraIdOrder == null || this.mFrameShotProgressCount != this.mFrameShotCameraIdOrder.length) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (MultiViewCameraModuleExpand.this.mFrameShotProgressCount != MultiViewCameraModuleExpand.this.mFrameShotMaxCount) {
                        MultiViewCameraModuleExpand.this.mMultiViewManager.setMultiGuideTextVisibility(true);
                        MultiViewCameraModuleExpand.this.mMultiViewManager.updateGuide();
                        if ((MultiViewCameraModuleExpand.this.getMVState() & 16) != 0) {
                            MultiViewCameraModuleExpand.this.doIntervalFrameShotMV(true);
                        } else if ((MultiViewCameraModuleExpand.this.getMVState() & 32) != 0) {
                            MultiViewCameraModuleExpand.this.doIntervalFrameShotMV(false);
                        } else {
                            MultiViewCameraModuleExpand.this.mGet.enableConeMenuIcon(31, true);
                        }
                    }
                }
            });
            return;
        }
        this.mQuickButtonManager.setEnable(100, false);
        afterLastFrameShot();
    }

    protected void setRecordingHint(CameraParameters parameters, String value, boolean setParam) {
        this.mParamUpdater.setParameters(parameters, ParamConstants.KEY_PREVIEW_SIZE, "1280x720");
        this.mParamUpdater.setParameters(parameters, "picture-size", "1280x720");
        super.setRecordingHint(parameters, value, setParam);
    }

    protected boolean doSetParamForStopRecording(CameraParameters parameters, boolean recordStart, String videoSize) {
        CamLog.m3d(CameraConstants.TAG, "-rec- doSetParamForStopRecording");
        int cameraId = getCameraDeviceId();
        if (!(cameraId == 1 || cameraId == 3)) {
            setRecordingHint(parameters, "false", false);
        }
        return true;
    }

    protected void takeFrameShotPicture() {
        CamLog.m3d(CameraConstants.TAG, "-cp- takeFrameShotPicture");
        DebugUtil.setEndTime("[1] MV image: shutter pressed ~ takeFrameShotPicture");
        DebugUtil.setStartTime("[+] MV: Final FreezePreview is Done");
        this.mFrameShotProgress = true;
        this.mIsTakingPicture = true;
        if (this.mFrameShotProgressCount == this.mFrameShotMaxCount) {
            resetFrameShotProgress();
        }
        if (this.mFrameShotProgressCount == 0) {
            this.mCameraDegree = this.mIsImportedImage ? 90 : getSaveImageDegree();
            CamLog.m3d(CameraConstants.TAG, "-multiview- mCameraDegree = " + this.mCameraDegree);
        }
        this.mMultiViewManager.setCapturedTag(this.mFrameShotProgressCount);
        doScreenCapture();
        new Thread(new C04155()).start();
        access$300(true, true, false);
    }

    protected void afterStopRecording() {
        CamLog.m3d(CameraConstants.TAG, "-rec- afterStopRecording");
        super.afterStopRecording();
        this.mIsMultiviewRecording = false;
        if (this.mIsRecordingCanceled) {
            CamLog.m3d(CameraConstants.TAG, "-rec- recording cancel");
            this.mIsRecordingCanceled = false;
            if (this.mFrameShotProgressCount == 0 && CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                this.mSpliceViewImageImportManager.showImportButton(true);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
            } else if (this.mFrameShotProgressCount == 1 && CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) && this.mIsImportedImage) {
                this.mSpliceViewImageImportManager.showImportButton(true);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(true);
            }
            doShowSpliceLayout();
        } else {
            setRecordingParamOnFront("false");
            if (isMultiviewFrameShot()) {
                capturePreview(null);
                if (this.mFrameShotProgressCount == 1 && CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                    this.mCameraDegree = getVideoOrientation();
                }
                doAfterFrameShot();
            }
            startPreviewSecond();
            showDualCameraIconAfterRecording();
        }
        resetMicPath();
        if (this.mLayoutSelectionManager == null) {
            return;
        }
        if (isMultiviewIntervalShot()) {
            this.mLayoutSelectionManager.setVisibility(false);
        } else {
            this.mLayoutSelectionManager.setVisibility(true);
        }
    }

    protected void showDualCameraIconAfterRecording() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            boolean isShow = (isMultiviewFrameShot() && this.mFrameShotProgressCount == 2) ? false : true;
            this.mSpliceViewImageImportManager.showDualCamLayoutAll(isShow);
            doShowSpliceLayout();
        }
    }

    protected void doShowSpliceLayout() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) && !this.mSpliceViewImageImportManager.getSpliceLayoutVisibility()) {
            this.mSpliceViewImageImportManager.showImportLayout(true);
        }
    }

    public void onShutterTopButtonClickListener() {
        super.onShutterTopButtonClickListener();
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibility(false);
        }
    }

    protected String getLiveSnapShotSize(CameraParameters parameters, String value) {
        CamLog.m3d(CameraConstants.TAG, "");
        String pictureSize = super.getLiveSnapShotSize(parameters, value);
        if (!ModelProperties.isMTKChipset() && pictureSize != null) {
            return pictureSize;
        }
        if ("1280x720".equals(value)) {
            return "1080x1080";
        }
        return "1280x960".equals(value) ? "1280x960" : value;
    }
}
