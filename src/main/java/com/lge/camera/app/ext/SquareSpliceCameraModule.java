package com.lge.camera.app.ext;

import android.view.KeyEvent;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.managers.ext.SpliceInitGuideManager;
import com.lge.camera.managers.ext.SpliceViewCameraAngleListener;
import com.lge.camera.managers.ext.SquareCameraInitGuideManagerBase.InitGuideListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class SquareSpliceCameraModule extends SquareSpliceCameraModuleBase implements InitGuideListener, SpliceViewCameraAngleListener {
    SpliceInitGuideManager mIniGuideManager = new SpliceInitGuideManager(this);
    protected boolean mIsFinishCondition = false;
    protected int mTouchedViewIndex = -1;

    /* renamed from: com.lge.camera.app.ext.SquareSpliceCameraModule$8 */
    class C05188 implements onQuickClipListListener {
        C05188() {
        }

        public void onListOpend() {
            SquareSpliceCameraModule.this.access$700(false);
            SquareSpliceCameraModule.this.access$800();
            if (SquareSpliceCameraModule.this.mHandler != null && SquareSpliceCameraModule.this.isMenuShowing(4)) {
                SquareSpliceCameraModule.this.mHandler.sendEmptyMessage(84);
            }
            SquareSpliceCameraModule.this.mSplicePostViewManager.setQuickClipDrawerOpend(true);
            if (SquareSpliceCameraModule.this.isRearCamera()) {
                SquareSpliceCameraModule.this.setFilmStrengthButtonVisibility(false, false);
                SquareSpliceCameraModule.this.access$600(false);
            }
        }

        public void onListClosed() {
            if (!SquareSpliceCameraModule.this.isTimerShotCountdown()) {
                if (SquareSpliceCameraModule.this.isSplicePostViewVisible()) {
                    SquareSpliceCameraModule.this.mSplicePostViewManager.setQuickClipDrawerOpend(false);
                } else {
                    SquareSpliceCameraModule.this.access$700(true);
                }
                SquareSpliceCameraModule.this.access$800();
                if (SquareSpliceCameraModule.this.isRearCamera()) {
                    SquareSpliceCameraModule.this.setFilmStrengthButtonVisibility(true, false);
                    SquareSpliceCameraModule.this.access$900();
                }
            }
        }
    }

    public SquareSpliceCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        this.mTopPreviewTargetCamId = 0;
        this.mBottomPreviewTargetCamId = 1;
        this.mSpliceViewImageImportManager.setSpliceViewListener(this);
        this.mSpliceViewImageImportManager.setSpliceManagerInterface(this);
        this.mSpliceViewImageImportManager.setSpliceManagerAngleListener(this);
        this.mSplicePostViewManager.setSplicePostViewListener(this);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        this.mIniGuideManager.setInitGuideListener(this);
        if (!SharedPreferenceUtilBase.getDualShotFirstGuide(this.mGet.getAppContext())) {
            this.mIniGuideManager.initLayout();
        }
        this.mSplicePostViewManager.resetSaveButtonClickedFlag();
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mSpliceViewImageImportManager);
        this.mManagerList.add(this.mIniGuideManager);
    }

    public String getShotMode() {
        return CameraConstants.MODE_SQUARE_SPLICE;
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (this.mIsImportedImage && !this.mSplicePostViewManager.isPostviewVisible() && (getMVState() & 4) == 0) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).setPrevCameraId();
            MultiViewFrame.setGuidePhotoLoad();
            ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).setGuidePhotoView(this.mSpliceViewImageImportManager.getReverseState() ? 1 : 0, this.mGet.getOrientationDegree());
            if (this.mFrameShotFilePath.size() > 0) {
                this.mFrameShotFilePath.clear();
                this.mVideoDegree.clear();
            }
            takeFrameShotPicture();
            this.mSpliceViewImageImportManager.showImportButton(true);
            this.mSpliceViewImageImportManager.showSwapAndRotateButton(true);
        }
    }

    public void onResumeAfter() {
        CamLog.m7i(CameraConstants.TAG, "in");
        super.onResumeAfter();
        if (this.mSpliceViewImageImportManager != null) {
            this.mSpliceViewImageImportManager.showImportLayout(true);
            if (!isMultiviewFrameShot()) {
                this.mSpliceViewImageImportManager.showImportButton(false);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
            }
            if (this.mFrameShotProgressCount > 0 && !this.mIsImportedImage) {
                this.mSpliceViewImageImportManager.showImportButton(false);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
            }
        }
        if (this.mSplicePostViewManager != null) {
            this.mSplicePostViewManager.setSaveButtonClickedFlag(false);
        }
        if (this.mSplicePostViewManager != null && this.mSplicePostViewManager.isPostviewVisible()) {
            this.mSplicePostViewManager.onResumeAfter();
            this.mSplicePostViewManager.resetSaveButtonClickedFlag();
            setCmdButtonsVisibility(false);
            if (this.mIsImportedImage) {
                this.mFrameShotProgressCount = 2;
            }
        }
        if (this.mSpliceViewImageImportManager != null && this.mFrameShotProgressCount == 0) {
            this.mSpliceViewImageImportManager.resetGalleryButtonClicked();
        }
        this.mIsCollageShared = false;
        if (this.mLayoutRecreated) {
            this.mLayoutRecreated = false;
            if (this.mSpliceViewImageImportManager != null && this.mIsImportedImage) {
                ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).setPrevCameraId();
                ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).setGuidePhotoView(this.mSpliceViewImageImportManager.getReverseState() ? 1 : 0, this.mGet.getOrientationDegree());
                takeFrameShotPicture();
                this.mSpliceViewImageImportManager.updateDualCameraIcon();
                this.mTopPreviewTargetCamId = 0;
                this.mBottomPreviewTargetCamId = 1;
            }
        }
        if (!(this.mMultiviewFrame == null || this.mMultiViewLayoutList == null || this.mLayoutRecreated || this.mIsImportedImage)) {
            if (SecureImageUtil.isSecureCamera()) {
                ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).resetCameraForced();
                this.mTopPreviewTargetCamId = 0;
                this.mBottomPreviewTargetCamId = 1;
            }
            this.mMultiviewFrame.changeLayout(1, false);
        }
        if (this.mFrameShotProgressCount < 2 && this.mSpliceViewImageImportManager != null) {
            this.mSpliceViewImageImportManager.showDualCamLayoutAll(true);
        }
        CamLog.m7i(CameraConstants.TAG, "out");
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (isPostviewVisible()) {
            return true;
        }
        return super.onShutterUp(keyCode, event);
    }

    public boolean onShutterDown(int keyCode, KeyEvent event) {
        if (isPostviewVisible()) {
            return true;
        }
        return super.onShutterDown(keyCode, event);
    }

    public void setSwapBitmapReady() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareSpliceCameraModule.this.mSpliceViewImageImportManager.doShowSwapAnim();
            }
        });
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mRenderThread != null && this.mRenderThread.isRequiredToFinishAfterSaving()) {
            this.mSplicePostViewManager.releaseMediaPlayer();
            this.mSplicePostViewManager.removeViews();
            setButtonsVisibilityForPostView(true);
        }
        if (this.mSplicePostViewManager != null) {
            this.mSplicePostViewManager.setSaveButtonClickedFlag(true);
        }
        if (SecureImageUtil.isSecureCamera() && this.mFrameShotProgressCount == 1) {
            onCancel();
        }
    }

    public boolean isImportedImage() {
        return this.mIsImportedImage;
    }

    public boolean isSpliceViewImporteImage() {
        return isImportedImage();
    }

    public String getRecordingType() {
        return CameraConstants.VIDEO_SPLICE_TYPE;
    }

    public boolean onVolumeKeyLongPressed(int keyCode, KeyEvent event) {
        return true;
    }

    public boolean doBackKey() {
        CamLog.m3d(CameraConstants.TAG, "doBackKey");
        if (this.mIniGuideManager != null && this.mIniGuideManager.getGuideLayouVisiblity()) {
            this.mIniGuideManager.removeViews();
            return true;
        } else if (!this.mGet.isAnimationShowing() || (this.mFrameShotMaxCount != 0 && isFrameShot())) {
            return super.doBackKey();
        } else {
            this.mIsFinishCondition = true;
            return true;
        }
    }

    public void onOkButtonClicked(boolean isChecked) {
        this.mIniGuideManager.removeViews();
        if (isChecked) {
            SharedPreferenceUtilBase.saveDualShotFirstGuide(this.mGet.getAppContext(), true);
        }
    }

    public void onOrientationChanged(int orientation, boolean isFirst) {
        super.onOrientationChanged(orientation, isFirst);
        if (this.mIsMultiviewRecording) {
            setRecordingUILocation();
        }
    }

    protected boolean isSplicePostViewVisible() {
        return isPostviewVisible();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && this.mSplicePostViewManager != null && this.mSplicePostViewManager.isPostviewVisible() && this.mSplicePostViewManager.getPostviewType() != 0) {
            AudioUtil.setAudioFocus(this.mGet.getActivity(), true);
        }
    }

    public boolean isSquareInitGuideShowing() {
        return this.mIniGuideManager == null ? false : this.mIniGuideManager.getGuideLayouVisiblity();
    }

    public boolean onCameraShutterButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "match camera shutter clicked.");
        if (isPostviewVisible()) {
            CamLog.m3d(CameraConstants.TAG, "return cause by postview visible");
            return false;
        }
        checkTimerShotWorking();
        return super.onCameraShutterButtonClicked();
    }

    public void onTakePictureBefore() {
        if (this.mSpliceViewImageImportManager != null) {
            if (isFrameShot()) {
                this.mSpliceViewImageImportManager.setCurrentImportState();
                this.mSpliceViewImageImportManager.processForTimerShot(true);
                this.mSpliceViewImageImportManager.showImportLayout(true);
            } else if (isTimerShotCountdown()) {
                this.mSpliceViewImageImportManager.showImportLayout(false);
            }
        }
        super.onTakePictureBefore();
    }

    private void checkTimerShotWorking() {
        try {
            if (this.mGet != null && !"0".equals(this.mGet.getCurSettingValue(Setting.KEY_TIMER))) {
                if (isFrameShot()) {
                    int cameraId = getCameraArray()[this.mFrameShotProgressCount];
                    if (this.mSpliceViewImageImportManager != null && this.mIsImportedImage && this.mSpliceViewImageImportManager.getReverseState()) {
                        cameraId = getCameraArray()[0];
                        CamLog.m3d(CameraConstants.TAG, "getReverseState set cameraId :" + cameraId);
                    }
                    if (1 == cameraId || 3 == cameraId) {
                        if (this.mTimerManager != null) {
                            CamLog.m3d(CameraConstants.TAG, "isFrameShot Front camera flash off");
                            this.mTimerManager.enableFlashNoti(false);
                            return;
                        }
                        return;
                    } else if (this.mTimerManager != null) {
                        CamLog.m3d(CameraConstants.TAG, "isFrameShot Rear camera flash on");
                        this.mTimerManager.enableFlashNoti(true);
                        return;
                    } else {
                        return;
                    }
                }
                int firstCameraId = getCameraArray()[0];
                int secondCameraId = getCameraArray()[1];
                if (firstCameraId == 0 || 2 == firstCameraId || secondCameraId == 0 || 2 == secondCameraId) {
                    if (this.mTimerManager != null) {
                        CamLog.m3d(CameraConstants.TAG, "Rear camera flash on");
                        this.mTimerManager.enableFlashNoti(true);
                    }
                } else if (this.mTimerManager != null) {
                    CamLog.m3d(CameraConstants.TAG, "All front camera flash off");
                    this.mTimerManager.enableFlashNoti(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onTakePictureAfter() {
        if (this.mTimerManager != null) {
            this.mTimerManager.enableFlashNoti(true);
        }
        if (this.mSpliceViewImageImportManager != null) {
            if (isFrameShot()) {
                if (this.mIsTimerShotCanceled) {
                    this.mSpliceViewImageImportManager.processForTimerShotCancel();
                    this.mIsTimerShotCanceled = false;
                } else {
                    this.mSpliceViewImageImportManager.processForTimerShot(false);
                }
            }
            this.mSpliceViewImageImportManager.showImportLayout(true);
        }
        super.onTakePictureAfter();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mSplicePostViewManager != null && this.mSplicePostViewManager.isPostviewVisible()) {
            this.mSplicePostViewManager.releaseMediaPlayer();
            this.mSplicePostViewManager.removeViews();
        }
        resetImportedImage();
        this.mIsCollageShared = false;
        MultiViewFrameBase.resetGenTex();
    }

    public void resetStatus() {
        if (isFrameShot() && this.mFrameShotProgressCount > 0) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "reset status");
                    SquareSpliceCameraModule.this.onCancel();
                }
            });
        }
    }

    public void onSwitchDualCamera(int viewIdx, int camId) {
        int currentCamId = getCameraArray()[viewIdx];
        if (this.mMultiviewFrame.getLayoutListCountMVFrame() != 0 && !isAnimationShowing() && currentCamId != camId) {
            this.mGet.movePreviewOutOfWindow(true);
            this.mTouchedViewIndex = viewIdx;
            this.mGet.getCurPreviewBlurredBitmap(this.mLcdSize[1] / 10, this.mLcdSize[0] / 10, 13, false, false);
            this.mGet.startCameraSwitchingAnimation(this.mSpliceViewImageImportManager.isWideAngle(camId) ? 3 : 2);
            ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).changeCamera(viewIdx, camId, this.mGet.getOrientationDegree());
            this.mMultiviewFrame.changeLayout(1, false);
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCameraIcon();
                }
            });
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SquareSpliceCameraModule.this.setPreviewCoverVisibility(4, true);
                    SquareSpliceCameraModule.this.mTouchedViewIndex = -1;
                    SquareSpliceCameraModule.this.mGet.setAnimationShowing(false);
                    if (SquareSpliceCameraModule.this.mIsFinishCondition) {
                        SquareSpliceCameraModule.this.doBackKey();
                        SquareSpliceCameraModule.this.mIsFinishCondition = false;
                    }
                }
            }, 300);
        }
    }

    protected void processGestureForSplice(float x, float y) {
        if (this.mMultiviewFrame.getLayoutListCountMVFrame() != 0) {
            this.mGet.movePreviewOutOfWindow(true);
            final int viewIndex = ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getTouchedViewIndex(x, y);
            this.mTouchedViewIndex = viewIndex;
            this.mGet.getCurPreviewBlurredBitmap(this.mLcdSize[1] / 10, this.mLcdSize[0] / 10, 13, false, false);
            if (this.mFrameShotProgressCount != 0 || viewIndex != 1 || !isFrameShot() || this.mSpliceViewImageImportManager.getReverseState()) {
                this.mGet.startCameraSwitchingAnimation(1);
                int curCamId = getCameraArray()[viewIndex];
                int targetCamId = getNextCamId(viewIndex, curCamId);
                CamLog.m3d(CameraConstants.TAG, "viewIndex = " + viewIndex + ", curCamId = " + curCamId + ", targetCamId = " + targetCamId);
                ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).changeCamera(viewIndex, targetCamId, this.mGet.getOrientationDegree());
                this.mMultiviewFrame.changeLayout(1, false);
                if (viewIndex == 0) {
                    this.mTopPreviewTargetCamId = curCamId;
                } else {
                    this.mBottomPreviewTargetCamId = curCamId;
                }
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCameraIcon();
                        SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(viewIndex, true);
                    }
                });
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        SquareSpliceCameraModule.this.setPreviewCoverVisibility(4, true);
                        SquareSpliceCameraModule.this.mTouchedViewIndex = -1;
                        SquareSpliceCameraModule.this.mGet.setAnimationShowing(false);
                        if (SquareSpliceCameraModule.this.mIsFinishCondition) {
                            SquareSpliceCameraModule.this.doBackKey();
                            SquareSpliceCameraModule.this.mIsFinishCondition = false;
                        }
                    }
                }, 450);
            }
        }
    }

    public int getSpliceViewIndex() {
        return this.mTouchedViewIndex;
    }

    private int getNextCamId(int viewIndex, int curCamId) {
        int nextCamId = viewIndex == 0 ? curCamId == this.mTopPreviewTargetCamId ? getCameraArray()[1] : this.mTopPreviewTargetCamId : curCamId == this.mBottomPreviewTargetCamId ? getCameraArray()[0] : this.mBottomPreviewTargetCamId;
        if (isAllRearCameraID(curCamId, nextCamId)) {
            CamLog.m11w(CameraConstants.TAG, "CameraArray() is all REAR");
            nextCamId = 1;
        }
        if (!isAllFrontCameraID(curCamId, nextCamId)) {
            return nextCamId;
        }
        CamLog.m11w(CameraConstants.TAG, "CameraArray() is all FRONT");
        return 0;
    }

    private boolean isAllFrontCameraID(int curCamId, int nextCamId) {
        if (curCamId == 1 && (nextCamId == 1 || nextCamId == 3)) {
            return true;
        }
        if (curCamId == 3 && (nextCamId == 3 || nextCamId == 1)) {
            return true;
        }
        return false;
    }

    private boolean isAllRearCameraID(int curCamId, int nextCamId) {
        if (curCamId == 0 && (nextCamId == 0 || nextCamId == 2)) {
            return true;
        }
        if (curCamId == 2 && (nextCamId == 2 || nextCamId == 0)) {
            return true;
        }
        return false;
    }

    protected void doAfterFrameShot() {
        super.doAfterFrameShot();
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (SquareSpliceCameraModule.this.mFrameShotProgressCount == 1) {
                    if (SquareSpliceCameraModule.this.mSpliceViewImageImportManager.getReverseState()) {
                        SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
                        SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, false);
                    } else {
                        SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, false);
                        SquareSpliceCameraModule.this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, true);
                    }
                }
                SquareSpliceCameraModule.this.mSpliceViewImageImportManager.showDualCamLayoutAll(true);
            }
        });
    }

    public void removeSpliceDimColor() {
        this.mMultiViewManager.clearCapturedImageView();
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }

    protected void setQuickClipListListener() {
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipListListener(new C05188());
        }
    }
}
