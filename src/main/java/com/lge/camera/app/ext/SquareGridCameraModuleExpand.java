package com.lge.camera.app.ext;

import android.view.MotionEvent;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.managers.ext.GridCameraInitGuideManager;
import com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase.InitGuideListener;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class SquareGridCameraModuleExpand extends SquareGridCameraModule implements InitGuideListener {
    GridCameraInitGuideManager mIniGuideManager = new GridCameraInitGuideManager(this);

    public void init() {
        super.init();
        this.mIniGuideManager.setInitGuideListener(this);
        if (!SharedPreferenceUtilBase.getGridShotFirstGuide(this.mGet.getAppContext())) {
            this.mIniGuideManager.initLayout();
        }
    }

    public SquareGridCameraModuleExpand(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mIniGuideManager);
    }

    public String getShotMode() {
        return CameraConstants.MODE_SQUARE_GRID;
    }

    public String getRecordingType() {
        return CameraConstants.VIDEO_GRID_TYPE;
    }

    protected void setQuickclipUri() {
    }

    public void reloadList() {
    }

    public boolean isSquareGalleryBtn() {
        return false;
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    protected void setBtimapImageToAnimManager(ExifInterface exif) {
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    public void onShutterTopButtonLongClickListener() {
    }

    protected void setVideoLimitSize() {
        this.mLimitRecordingDuration = CameraConstants.MULTIVIEW_LIMIT_RECORDING_DURATION;
    }

    protected void updateShutterButtonsOnVideoStopClicked() {
    }

    protected void changeCommandButtons() {
    }

    protected void updateButtonsOnVideoStopClicked() {
        super.updateButtonsOnVideoStopClicked();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setShutterButtonVisibility(8, 3, false);
        }
        if (this.mReviewThumbnailManager != null) {
            this.mReviewThumbnailManager.setThumbnailVisibility(8);
        }
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot() || this.mAdvancedFilmManager == null) {
            return false;
        }
        if (this.mCameraId == 1) {
            if (this.mCapturedCount != 4 || this.mIsRetakeMode) {
                this.mAdvancedFilmManager.setSelfieMenuVisibility(true);
                this.mAdvancedFilmManager.setMenuEnable(true);
                if (this.mExtraPrevewUIManager != null) {
                    this.mExtraPrevewUIManager.setAllButtonsEnable(true);
                }
            } else {
                this.mAdvancedFilmManager.setSelfieMenuVisibility(false, true);
                this.mAdvancedFilmManager.setSelfieOptionVisibility(false, false, true);
                this.mExtraPrevewUIManager.hide(false, true, true);
            }
        }
        return true;
    }

    public boolean doBackKey() {
        CamLog.m3d(CameraConstants.TAG, "doBackKey");
        if (this.mIniGuideManager != null && this.mIniGuideManager.getGuideLayouVisiblity()) {
            this.mIniGuideManager.removeViews();
            return true;
        } else if (this.mAdvancedFilmManager != null && (isBarVisible(1) || this.mAdvancedFilmManager.getSelfieFilterVisibility())) {
            CamLog.m3d(CameraConstants.TAG, "front film menu");
            this.mAdvancedFilmManager.setSelfieOptionVisibility(false, true);
            return true;
        } else if (this.mGet.isHelpListVisible()) {
            this.mHandler.sendEmptyMessage(49);
            return true;
        } else {
            if (isTimerShotCountdown() && this.mIsRecordButtonClicked) {
                this.mIsRecordButtonClicked = false;
            }
            if (isProgressDialogVisible()) {
                CamLog.m3d(CameraConstants.TAG, "doBackKey EXIT - ignore back key.");
                return true;
            } else if (processBackKeyOnRecording() || closeModeAndSettingMenu()) {
                return true;
            } else {
                return super.doBackKey();
            }
        }
    }

    public void onOkButtonClicked(boolean isChecked) {
        this.mIniGuideManager.removeViews();
        if (isChecked) {
            SharedPreferenceUtilBase.saveGridShotFirstGuide(this.mGet.getAppContext(), true);
        }
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if ((this.mGridPostviewManager == null || !this.mGridPostviewManager.getPostviewPreviewDummyVisibility()) && !this.mIsRendererStarting) {
            return super.onTouchEvent(event);
        }
        CamLog.m3d(CameraConstants.TAG, "touch event returned");
        return true;
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (this.mCapturedCount != 4 || this.mGridPostviewManager == null || !this.mGridPostviewManager.getPostviewPreviewDummyVisibility()) {
            return;
        }
        if (isFocusLock() || isAELock()) {
            this.mFocusManager.hideAEAFText();
            this.mFocusManager.hideFocus();
        }
    }

    public boolean isGridPostViesShowing() {
        return this.mGridPostviewManager.getPostviewPreviewDummyVisibility();
    }

    protected boolean closeModeAndSettingMenu() {
        if (this.mCapturedCount == 4 && this.mGet.isCameraChanging()) {
            return true;
        }
        if (this.mGet.isModeMenuVisible()) {
            this.mGet.hideModeMenu(true, false);
            return true;
        } else if (!this.mGet.isSettingMenuVisible()) {
            return false;
        } else {
            this.mGet.removeSettingMenu(true, false);
            return true;
        }
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
            case 11:
                CamLog.m3d(CameraConstants.TAG, "CAM_INTERVALSHOT");
                if (this.mSnapShotChecker.checkMultiShotState(2)) {
                    CamLog.m3d(CameraConstants.TAG, "CAM_INTERVALSHOT and checker");
                    if (this.mIsRetakeMode) {
                        this.mIsRetakeMode = false;
                        this.mGridPostviewManager.setHighlightView(this.mCapturedCount, false);
                    }
                }
                super.onShutterLargeButtonClicked();
                this.mGestureShutterManager.resetGestureCaptureType();
                return;
            case 12:
                CamLog.m3d(CameraConstants.TAG, "CAM_SHUTTER_ONLY");
                onCameraShutterButtonClicked();
                return;
            default:
                super.onShutterLargeButtonClicked();
                return;
        }
    }

    public int getShutterButtonType() {
        return FunctionProperties.isSupportedCollageRecording() ? 3 : 4;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && this.mGridPostviewManager != null && this.mCapturedCount == 4 && !this.mGridPostviewManager.isImageOnly()) {
            AudioUtil.setAudioFocus(this.mGet.getActivity(), true);
        }
    }

    public boolean isSquareInitGuideShowing() {
        return this.mIniGuideManager == null ? false : this.mIniGuideManager.getGuideLayouVisiblity();
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        if (this.mCameraState != 9) {
            super.doRunnableStartRecorder(info);
        }
    }

    public boolean isCollageProgressing() {
        return this.mIsCollageStarted;
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- onPauseBefore");
        if (this.mIsRecordButtonClicked && !isTimerShotCountdown()) {
            onVideoStopClicked(true, false);
            this.mIsRecordingCanceled = true;
        }
        super.onPauseBefore();
    }

    public String getCurDir() {
        if ((this.mCapturedCount < 4 || (this.mCapturedCount == 4 && this.mIsRetakeMode)) && this.mIsRecordButtonClicked) {
            return getAppContext().getFilesDir().getAbsolutePath();
        }
        return super.getCurDir();
    }

    public int getCurStorage() {
        if ((this.mCapturedCount < 4 || (this.mCapturedCount == 4 && this.mIsRetakeMode)) && this.mIsRecordButtonClicked) {
            return 0;
        }
        return super.getCurStorage();
    }

    public boolean isHALSignatureCaptureMode() {
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_hdr_or_flash:
                    return true;
            }
        }
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
            case C0088R.id.indicator_item_hdr_or_flash:
                return true;
        }
        return false;
    }
}
