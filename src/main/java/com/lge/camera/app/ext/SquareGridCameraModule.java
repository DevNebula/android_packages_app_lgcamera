package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Message;
import android.support.p000v4.view.PointerIconCompat;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.ArcProgress;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.GestureViewManager;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.managers.ext.GridCameraPostviewManager;
import com.lge.camera.managers.ext.GridCameraPostviewManagerBase.GridContentsInfo;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SettingKeyWrapper;
import java.io.File;

public class SquareGridCameraModule extends SquareGridCameraModuleBase {

    /* renamed from: com.lge.camera.app.ext.SquareGridCameraModule$1 */
    class C04901 implements onQuickClipListListener {
        C04901() {
        }

        public void onListOpend() {
            SquareGridCameraModule.this.pauseShutterless();
            SquareGridCameraModule.this.access$200(false);
            if (SquareGridCameraModule.this.mGridPostviewManager != null) {
                SquareGridCameraModule.this.mGridPostviewManager.setQuickClipDrawerOpened(true);
            }
            if (SquareGridCameraModule.this.mGridPostviewManager != null && SquareGridCameraModule.this.mIsRetakeMode && SquareGridCameraModule.this.mCapturedCount == 4) {
                SquareGridCameraModule.this.setButtonsVisibilityForPostView(false);
                SquareGridCameraModule.this.mIsRetakeMode = false;
                SquareGridCameraModule.this.mGridPostviewManager.hideAllHighrightViews();
            }
            SquareGridCameraModule.this.access$100(false);
            if (SquareGridCameraModule.this.mGridPostviewManager != null) {
                SquareGridCameraModule.this.mGridPostviewManager.setPostviewButtonsVisibility(false);
            }
        }

        public void onListClosed() {
            if (!SquareGridCameraModule.this.isTimerShotCountdown()) {
                SquareGridCameraModule.this.resumeShutterless();
                if (SquareGridCameraModule.this.mCapturedCount < 4) {
                    SquareGridCameraModule.this.access$200(true);
                }
                if (SquareGridCameraModule.this.mGridPostviewManager != null) {
                    SquareGridCameraModule.this.mGridPostviewManager.setQuickClipDrawerOpened(false);
                    if (SquareGridCameraModule.this.mCapturedCount == 4) {
                        SquareGridCameraModule.this.mGridPostviewManager.setPostviewButtonsVisibility(true);
                    }
                }
                if (SquareGridCameraModule.this.mGridPostviewManager != null && !SquareGridCameraModule.this.mGridPostviewManager.getPostviewPreviewDummyVisibility()) {
                    SquareGridCameraModule.this.access$300();
                }
            }
        }
    }

    public SquareGridCameraModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        CamLog.m3d(CameraConstants.TAG, "grid mode init.");
        this.mCapturedCount = 0;
        this.mIsRetakeMode = false;
        this.mIsSavingImage = false;
        this.mGridPostviewManager.setPostviewListener(this);
        this.mGridPostviewManager.resetSaveButtonClickedFlag();
    }

    protected void setQuickClipListListener() {
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.setQuickClipListListener(new C04901());
        }
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mGridPostviewManager);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (!FunctionProperties.isSupportedCollageRecording()) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
        resetCollageTexture();
        this.mIsRecordingCanceled = false;
        this.mIsCollageShared = false;
        if (this.mCapturedCount == 4) {
            if (this.mIsRetakeMode) {
                setButtonsVisibilityForPostView(true);
            } else {
                setButtonsVisibilityForPostView(false);
                if (this.mGridPostviewManager != null) {
                    this.mGridPostviewManager.hideAllHighrightViews();
                }
            }
        } else if (!this.mIsRetakeMode) {
            this.mGridPostviewManager.setHighlightView(this.mCapturedCount, false);
        }
        if (this.mGridPostviewManager != null && !this.mGridPostviewManager.getPostviewVisibility()) {
            this.mGridPostviewManager.showPostViewLayout(true);
        }
    }

    public void onPauseAfter() {
        boolean z = false;
        super.onPauseAfter();
        if (this.mRenderThread != null && this.mIsCollageStarted) {
            this.mRenderThread.setRequiredToFinishAfterSaving(true);
            if (this.mGridPostviewManager != null) {
                CamLog.m3d(CameraConstants.TAG, "show cover");
                this.mGridPostviewManager.showPostviewCover(true);
            }
        }
        if (!(this.mRenderThread == null || this.mRenderThread.isRequiredToFinishAfterSaving())) {
            release();
        }
        if (this.mIsRetakeMode) {
            this.mIsRetakeMode = false;
            if (this.mCapturedCount != 4) {
                z = true;
            }
            setButtonsVisibilityForPostView(z);
        }
        if (this.mCameraId == 1) {
            this.mAdvancedFilmManager.setMenuEnable(true);
            this.mExtraPrevewUIManager.setAllButtonsEnable(true);
        }
    }

    protected void changeInitParameters() {
        ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        String previewSize = "1080x1080";
        if (this.mCameraId != 1) {
            if (listPref != null) {
                previewSize = listPref.getExtraInfo(1);
            } else {
                previewSize = "1080x1080";
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, previewSize);
            this.mParamUpdater.setParamValue("picture-size", "2104x2104");
            if (isOpticZoomSupported(null) && !FunctionProperties.isSameResolutionOpticZoom()) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_PICTURE_SIZE_WIDE, "720x720");
                CamLog.m3d(CameraConstants.TAG, "[opticzoom] KEY_PICTURE_SIZE_WIDE 720x720");
            }
            this.mParamUpdater.removeRequester(ParamConstants.KEY_BEAUTY_LEVEL);
            this.mParamUpdater.removeRequester("beautyshot");
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, "1080x1080");
            return;
        }
        if (listPref != null) {
            previewSize = listPref.getExtraInfo(1);
        } else {
            previewSize = "720x720";
        }
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, previewSize);
        this.mParamUpdater.setParamValue("picture-size", "1920x1920");
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, "1080x1080");
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setDefaultSettingValueAndDisable(Setting.KEY_SQUARE_PICTURE_SIZE, false);
        setDefaultSettingValueAndDisable(Setting.KEY_SQUARE_VIDEO_SIZE, false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreSettingValue(Setting.KEY_SQUARE_PICTURE_SIZE);
        restoreSettingValue(Setting.KEY_SQUARE_VIDEO_SIZE);
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
    }

    protected void saveImage(byte[] data, byte[] extraExif) {
    }

    protected void saveImage(byte[] data, byte[] extraExif, boolean needCrop) {
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mIsRetakeMode || this.mCapturedCount != 4) {
            return super.onCameraShutterButtonClicked();
        }
        return false;
    }

    public void takePictureByTimer(int type) {
        if (this.mGridPostviewManager != null) {
            this.mGridPostviewManager.setQuickClipDrawerOpened(false);
            if (this.mCapturedCount == 4) {
                this.mGridPostviewManager.setPostviewButtonsVisibility(true);
            }
        }
        super.takePictureByTimer(type);
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        boolean z = false;
        super.onPictureTakenCallback(data, extraExif, camera);
        ExifInterface exif = Exif.readExif(data);
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (this.mIsRetakeMode) {
            if (this.mRetakeViewIndex == 0) {
                this.mCameraDegree = getSaveImageDegree();
            }
            this.mGridPostviewManager.setImageBitmapToGrid(this.mRetakeViewIndex, bm, isRearCamera(), Exif.getOrientation(exif), true, "", false);
            this.mRetakeViewIndex = -1;
            this.mIsRetakeMode = false;
        } else {
            if (this.mCapturedCount == 0) {
                this.mCameraDegree = getSaveImageDegree();
            }
            this.mCapturedCount++;
            this.mGridPostviewManager.setImageBitmapToGrid(this.mCapturedCount - 1, bm, isRearCamera(), Exif.getOrientation(exif), true, "", false);
            if (this.mCapturedCount > 4) {
                this.mCapturedCount = 4;
            }
        }
        if (this.mCapturedCount == 4) {
            setButtonsVisibilityForPostView(false);
            access$2600(true, true, getShareContentType());
            this.mGridPostviewManager.setVideoContents();
            this.mGestureShutterManager.resetGestureCaptureType();
            CamLog.m3d(CameraConstants.TAG, "max count, picture callback");
            access$2700();
        }
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.rollbackStatus();
        }
        GestureViewManager gestureViewManager = this.mReviewThumbnailManager;
        if (getUri() != null) {
            z = true;
        }
        gestureViewManager.setEnabled(z);
    }

    public void notifyNewMedia(final Uri uri, final boolean updateThumbnail) {
        CamLog.m3d(CameraConstants.TAG, "grid view notifyNewMedia : URI = " + uri);
        new Thread() {
            public void run() {
                FileManager.broadcastNewMedia(SquareGridCameraModule.this.mGet.getAppContext(), uri);
                SquareGridCameraModule.this.mReviewThumbnailManager.setLaunchGalleryAvailable(true);
                if (SquareGridCameraModule.this.mGet.isLGUOEMCameraIntent()) {
                    SquareGridCameraModule.this.mGet.postOnUiThread(SquareGridCameraModule.this.mChangeToAttachModule);
                } else if (updateThumbnail && SquareGridCameraModule.this.mReviewThumbnailManager != null) {
                    SquareGridCameraModule.this.mReviewThumbnailManager.doAfterCaptureProcess(uri, false);
                }
                if (SquareGridCameraModule.this.mIsCollageShared) {
                    SquareGridCameraModule.this.launchQuickClipSharedApp(uri);
                }
                SquareGridCameraModule.this.mIsCollageShared = false;
                SquareGridCameraModule.this.mIsSaveBtnClicked = false;
                SquareGridCameraModule.this.resetCollageTexture();
                SquareGridCameraModule.this.mIsCollageStarted = false;
                SquareGridCameraModule.this.hidePostviewCover();
                SquareGridCameraModule.this.mCapturedCount = 0;
                SquareGridCameraModule.this.mGridPostviewManager.resetSaveButtonClickedFlag();
                SquareGridCameraModule.this.mGestureShutterManager.resetGestureCaptureType();
                SquareGridCameraModule.this.mGridPostviewManager.setQuickClipDrawerOpened(false);
                AudioUtil.setAudioFocus(SquareGridCameraModule.this.mGet.getActivity(), false);
                SquareGridCameraModule.this.startSelfieEngineUiThread();
                SquareGridCameraModule.this.mGet.postOnUiThread(new HandlerRunnable(SquareGridCameraModule.this) {
                    public void handleRun() {
                        if (SquareGridCameraModule.this.mDialogManager != null && SquareGridCameraModule.this.mDialogManager.isProgressDialogVisible()) {
                            SquareGridCameraModule.this.mDialogManager.onDismissRotateDialog();
                        }
                        SquareGridCameraModule.this.setButtonsVisibilityForPostView(true);
                    }
                }, 1000);
            }
        }.start();
    }

    private void startSelfieEngineUiThread() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareGridCameraModule.this.access$2900();
            }
        });
    }

    private void resetCollageTexture() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (SquareGridCameraModule.this.mRenderThread == null) {
                    SquareGridCameraModule.this.mGridPostviewManager.releaseCollageTexture();
                    if (!SquareGridCameraModule.this.mGet.isPaused()) {
                        SquareGridCameraModule.this.mGridPostviewManager.setupCollageTextureView();
                    }
                }
            }
        });
    }

    protected void updateRecordingUi() {
        this.mRecordingUIManager.setRec3sec(true);
        super.updateRecordingUi();
        RotateTextView tv = (RotateTextView) this.mGet.findViewById(C0088R.id.arc_progress_text);
        RotateLayout v = (RotateLayout) this.mGet.findViewById(C0088R.id.arc_progress_rotate_layout);
        ((RelativeLayout) this.mGet.findViewById(C0088R.id.rec_time_indicator)).setVisibility(4);
        tv.setVisibility(0);
        v.setVisibility(0);
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mIsRecordingCanceled) {
            this.mIsRecordingCanceled = false;
        } else {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    Bitmap bm;
                    if (SquareGridCameraModule.this.mIsRetakeMode) {
                        SquareGridCameraModule.this.deleteTempFile();
                        SquareGridCameraModule.this.mTempFilePath = SquareGridCameraModule.this.getTempFilePath();
                        if (!new File(SquareGridCameraModule.this.mTempFilePathRetake).renameTo(new File(SquareGridCameraModule.this.mTempFilePath))) {
                            CamLog.m3d(CameraConstants.TAG, "file rename failed");
                        }
                        bm = ThumbnailUtils.createVideoThumbnail(SquareGridCameraModule.this.mTempFilePath, 2);
                        if (SquareGridCameraModule.this.mRetakeViewIndex == 0) {
                            SquareGridCameraModule.this.mCameraDegree = SquareGridCameraModule.this.getSaveImageDegree();
                        }
                        SquareGridCameraModule.this.mGridPostviewManager.setImageBitmapToGrid(SquareGridCameraModule.this.mRetakeViewIndex, bm, SquareGridCameraModule.this.isRearCamera(), SquareGridCameraModule.this.mCurDegree, false, SquareGridCameraModule.this.mTempFilePath, SquareGridCameraModule.this.isFilmRunning());
                        if (SquareGridCameraModule.this.mCapturedCount == 4) {
                            SquareGridCameraModule.this.mGridPostviewManager.addTextureView(SquareGridCameraModule.this.mRetakeViewIndex);
                            SquareGridCameraModule.this.mGridPostviewManager.setVideoContents();
                        }
                        SquareGridCameraModule.this.mRetakeViewIndex = -1;
                        SquareGridCameraModule.this.mIsRetakeMode = false;
                        if (SquareGridCameraModule.this.mCapturedCount == 4) {
                            SquareGridCameraModule.this.access$2600(true, true, SquareGridCameraModule.this.getShareContentType());
                            SquareGridCameraModule.this.setButtonsVisibilityForPostView(false);
                            return;
                        }
                        return;
                    }
                    bm = ThumbnailUtils.createVideoThumbnail(SquareGridCameraModule.this.mTempFilePath, 2);
                    if (SquareGridCameraModule.this.mCapturedCount == 0) {
                        SquareGridCameraModule.this.mCameraDegree = SquareGridCameraModule.this.getSaveImageDegree();
                    }
                    SquareGridCameraModule squareGridCameraModule = SquareGridCameraModule.this;
                    squareGridCameraModule.mCapturedCount++;
                    if (SquareGridCameraModule.this.mCapturedCount > 4) {
                        SquareGridCameraModule.this.mCapturedCount = 4;
                    }
                    SquareGridCameraModule.this.mGridPostviewManager.setImageBitmapToGrid(SquareGridCameraModule.this.mCapturedCount - 1, bm, SquareGridCameraModule.this.isRearCamera(), SquareGridCameraModule.this.mCurDegree, false, SquareGridCameraModule.this.mTempFilePath, SquareGridCameraModule.this.isFilmRunning());
                    if (SquareGridCameraModule.this.mCapturedCount == 4) {
                        SquareGridCameraModule.this.access$2600(true, true, SquareGridCameraModule.this.getShareContentType());
                        SquareGridCameraModule.this.setButtonsVisibilityForPostView(false);
                        SquareGridCameraModule.this.mGridPostviewManager.setVideoContents();
                    }
                }
            });
        }
    }

    protected void doShowDoubleCamera() {
        if (this.mCapturedCount == 4) {
            boolean z = this.mIsRetakeMode && !this.mAdvancedFilmManager.getSelfieFilterVisibility();
            showDoubleCamera(z);
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    CamLog.m3d(CameraConstants.TAG, "double camera showing");
                    SquareGridCameraModule.this.access$2700();
                    if (SquareGridCameraModule.this.mCameraId == 1 && SquareGridCameraModule.this.mIsRetakeMode) {
                        SquareGridCameraModule.this.access$2900();
                    }
                }
            }, 200);
            return;
        }
        super.doShowDoubleCamera();
    }

    private String getShareContentType() {
        String contentType = null;
        if (this.mGridPostviewManager != null) {
            GridContentsInfo[] contentsInfo = this.mGridPostviewManager.getGridContentsInfo();
            if (!(contentsInfo == null || contentsInfo.length == 0)) {
                contentType = "image/*";
                for (GridContentsInfo info : contentsInfo) {
                    if (info.contentsType == 2) {
                        contentType = "video/*";
                        break;
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "contentType>>" + contentType);
            }
        }
        return contentType;
    }

    protected boolean isFilmRunning() {
        return this.mAdvancedFilmManager != null && this.mAdvancedFilmManager.isRunningFilmEmulator();
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        switch (msg.what) {
            case 71:
                if (this.mRecordingUIManager != null) {
                    ArcProgress arcProgress = this.mRecordingUIManager.getArcProgress();
                    if (arcProgress != null) {
                        arcProgress.updateArcProgress(3);
                    }
                }
                return true;
            default:
                return super.mainHandlerHandleMessage(msg);
        }
    }

    protected void stopRecorder() {
        if (this.mAdvancedFilmManager != null) {
            this.mAdvancedFilmManager.stopRecorder(false);
        }
        if (isSupportedQuickClip()) {
            this.mQuickClipManager.rollbackStatus();
        }
        setQuickClipIcon(true, true);
        stopRecorder(false);
        this.mIsRecordButtonClicked = false;
    }

    public void onCameraSwitchingStart() {
        release();
        this.mGridPostviewManager.releaseCollageTexture();
        super.onCameraSwitchingStart();
    }

    public void onCameraSwitchingEnd() {
        boolean z = false;
        if (this.mCameraId != 1) {
            this.mAdvancedFilmManager.setSelfieMenuVisibility(false, true);
            this.mAdvancedFilmManager.setSelfieOptionVisibility(false, false, true);
        }
        if (this.mIsRetakeMode && this.mCapturedCount == 4) {
            setButtonsVisibilityForPostView(true);
            this.mGridPostviewManager.setPostviewPreviewDummyVisibility(false);
        }
        if (!(this.mGridPostviewManager == null || this.mGridPostviewManager.getPostviewVisibility() || isMenuShowing(4))) {
            this.mGridPostviewManager.showPostViewLayout(true);
        }
        super.onCameraSwitchingEnd();
        GestureViewManager gestureViewManager = this.mReviewThumbnailManager;
        if (getUri() != null) {
            z = true;
        }
        gestureViewManager.setEnabled(z);
    }

    public boolean onHideMenu(int menuType) {
        boolean retValue = super.onHideMenu(menuType);
        this.mGridPostviewManager.showPostViewLayout(true);
        return retValue;
    }

    protected void startTimerShotByType(TimerType timerType, String timerValue) {
        if (this.mGestureShutterManager != null && this.mCapturedCount > 0 && this.mGestureShutterManager.getGestureCaptureType() == 2) {
            this.mIsRetakeMode = false;
            this.mRetakeViewIndex = -1;
            onCancel();
        }
        if (!(this.mGridPostviewManager == null || this.mGridPostviewManager.getPostviewVisibility())) {
            this.mGridPostviewManager.showPostViewLayout(true);
        }
        super.startTimerShotByType(timerType, timerValue);
        if (this.mGridPostviewManager != null) {
            this.mGridPostviewManager.setPostviewButtonsVisibility(false);
        }
        this.mBackButtonManager.show();
    }

    protected void processForCleanViewAfterInterval() {
        if (this.mCapturedCount == 4) {
            setButtonsVisibilityForPostView(false);
        } else {
            super.processForCleanViewAfterInterval();
        }
    }

    protected void onTakePictureBefore() {
        super.onTakePictureBefore();
        if (this.mCapturedCount == 4 && this.mIsRetakeMode) {
            if (this.mGridPostviewManager != null) {
                this.mGridPostviewManager.setPostviewButtonsVisibility(false);
            }
            this.mBackButtonManager.show();
        }
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        if (this.mCapturedCount == 4) {
            showDoubleCamera(false);
        }
        this.mWaitSavingDialogType = 0;
        this.mNeedProgressDuringCapture = 0;
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        if (this.mCapturedCount == 4) {
            setButtonsVisibilityForPostView(false);
            showDoubleCamera(false);
            if (this.mIsRetakeMode) {
                access$400(PointerIconCompat.TYPE_ZOOM_OUT, true, true);
                if (this.mGridPostviewManager != null) {
                    this.mGridPostviewManager.setPostviewButtonsVisibility(false);
                }
                this.mBackButtonManager.show();
            }
            if (this.mIndicatorManager != null) {
                this.mIndicatorManager.initIndicatorListAndLayout();
                setBatteryIndicatorVisibility(true);
                setTimerIndicatorVisibility(true);
                return;
            }
            return;
        }
        super.doCleanView(doByAction, false, saveState);
    }

    public void setCollageContentsSharedFlag() {
        this.mIsCollageShared = true;
        this.mGridPostviewManager.saveContents(false);
    }

    public boolean checkCollageContentsShareAvailable() {
        return this.mCapturedCount == 4 && !this.mIsSaveBtnClicked;
    }

    public boolean onRecordStartButtonClicked() {
        if (this.mIsSavingImage) {
            return false;
        }
        this.mIsRecordButtonClicked = true;
        if (!(this.mGridPostviewManager == null || this.mGridPostviewManager.getPostviewVisibility())) {
            this.mGridPostviewManager.showPostViewLayout(true);
        }
        return super.onRecordStartButtonClicked();
    }

    public void onVideoShutterClicked() {
        super.onVideoShutterClicked();
        if (this.mIsRetakeMode && this.mCapturedCount == 4) {
            this.mGridPostviewManager.setPostviewButtonsVisibility(false);
            this.mGridPostviewManager.setPostviewPreviewDummyVisibility(false);
            this.mBackButtonManager.show();
        }
    }

    protected void doPlayRecordingStopSound() {
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                SquareGridCameraModule.this.playRecordingSound(false);
            }
        }, this.mIsRecordingCanceled ? 300 : 0);
    }

    protected void deleteTempFile() {
        if (this.mIsRetakeMode) {
            File file = new File(getTempFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private String getTempFilePath() {
        String path = "";
        switch (this.mRetakeViewIndex) {
            case 0:
                return GridCameraPostviewManager.FIRST_VIDEO_LOCATION;
            case 1:
                return GridCameraPostviewManager.SECOND_VIDEO_LOCATION;
            case 2:
                return GridCameraPostviewManager.THIRD_VIDEO_LOCATION;
            case 3:
                return GridCameraPostviewManager.FOURTH_VIDEO_LOCATION;
            default:
                return path;
        }
    }

    protected void hidePostviewCover() {
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "hide cover, save button is visible ? " + SquareGridCameraModule.this.mGridPostviewManager.getPostviewButtonsVisibility());
                SquareGridCameraModule.this.mGridPostviewManager.showPostviewCover(false);
                if (SquareGridCameraModule.this.mDialogManager != null && SquareGridCameraModule.this.mDialogManager.isProgressDialogVisible()) {
                    SquareGridCameraModule.this.mDialogManager.onDismissRotateDialog();
                }
                SquareGridCameraModule.this.mIsSavingImage = false;
            }
        }, 300);
    }
}
