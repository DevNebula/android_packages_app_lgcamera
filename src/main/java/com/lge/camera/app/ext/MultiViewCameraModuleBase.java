package com.lge.camera.app.ext;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule.MediaRecorderListener;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.ArcProgress;
import com.lge.camera.components.AudioData;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.file.FileNamer;
import com.lge.camera.managers.GestureViewManager;
import com.lge.camera.managers.ext.MultiViewLayoutSelectionManager;
import com.lge.camera.managers.ext.MultiViewManager;
import com.lge.camera.managers.ext.MultiViewModuleInterface;
import com.lge.camera.managers.ext.SplicePostViewManager;
import com.lge.camera.managers.ext.SpliceViewImageImportManager;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.LinkedList;

public class MultiViewCameraModuleBase extends MultiGLViewExpand implements MultiViewModuleInterface {
    public static final boolean USE_MULTIVIEW_SWITCHING = false;
    private final int MIN_SWIPE_DISTANCE = 100;
    private final float NOT_PRESSED = -1.0f;
    LinkedList<AudioData>[] mAudioBuffer = null;
    private int mCameraIdForReverse = -1;
    protected int mCurDegree = 0;
    protected int[] mFrameShotCameraIdOrder = null;
    protected ArrayList<String> mFrameShotFilePath = null;
    protected int mFrameShotMaxCount = 3;
    protected boolean mFrameShotProgress = false;
    protected int mFrameShotProgressCount = 0;
    Bitmap mImportedBitmap = null;
    protected Uri mImportedUri;
    protected boolean mIsCapturingPreview = false;
    protected boolean mIsPinch = false;
    protected boolean mIsRecordingCanceled = false;
    protected boolean mIsTakingPicture = false;
    protected boolean mIsTimerShotCanceled = false;
    protected MultiViewLayoutSelectionManager mLayoutSelectionManager = null;
    protected MultiViewManager mMultiViewManager;
    long[] mPlayerTimeStamp = null;
    private float mPosX = -1.0f;
    private float mPosY = -1.0f;
    protected SplicePostViewManager mSplicePostViewManager = new SplicePostViewManager(this);
    protected SpliceViewImageImportManager mSpliceViewImageImportManager = new SpliceViewImageImportManager(this);
    Object mSynchAudioBuffer = new Object();
    Object mSynchVideoPlayer = new Object();
    protected ArrayList<Integer> mVideoDegree = null;
    protected boolean mVisibleForPostView = false;

    public MultiViewCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
        setLCDSizeForMultiLayout();
    }

    public String getShotMode() {
        return CameraConstants.MODE_MULTIVIEW;
    }

    protected boolean isHandlerSwitchingModule() {
        if (FunctionProperties.useWideRearAsDefault()) {
            this.mGet.setCameraChanging(1);
        }
        return false;
    }

    public void init() {
        this.mMultiViewManager = new MultiViewManager(this);
        super.init();
        initializeMultiviewModule();
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mIsRecordingCanceled = false;
        this.mIsMultiviewRecording = false;
        this.mIsStoppingRecord = false;
        if (MultiViewFrame.initializeStaticVariables(false)) {
            initializeMultiviewModule();
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                if (this.mSplicePostViewManager != null) {
                    this.mSplicePostViewManager.releaseMediaPlayer();
                    this.mSplicePostViewManager.removeViews();
                }
                setButtonsVisibilityForPostView(true);
                this.mSpliceViewImageImportManager.showImportLayout(true);
            }
        }
        if (this.mSplicePostViewManager == null || !(this.mSplicePostViewManager == null || this.mSplicePostViewManager.isPostviewVisible())) {
            this.mCaptureButtonManager.changeButtonByMode(1);
        }
    }

    protected void resetFrameShotProgress() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- resetFrameShotProgress");
        this.mFrameShotProgressCount = 0;
        this.mFrameShotFilePath.clear();
        this.mVideoDegree.clear();
    }

    private void initializeMultiviewModule() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- initializeMultiviewModule");
        this.mGet.movePreviewOutOfWindow(true);
        MultiViewFrame.resetCapturedTexture();
        this.mFrameShotFilePath = new ArrayList();
        this.mVideoDegree = new ArrayList();
        this.mFrameShotProgress = false;
        resetFrameShotProgress();
        makeMultiviewLayout();
    }

    protected void onChangeModuleBefore() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- onChangeModuleBefore");
        super.onChangeModuleBefore();
        if (this.mMultiViewManager != null) {
            this.mMultiViewManager.setFrameShotGuideVisibility(false);
        }
        MultiViewFrame.resetCapturedTexture();
        if (this.mMultiviewFrame != null) {
            this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
        }
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- onPauseBefore");
        if (this.mIsMultiviewRecording) {
            if (isMultiviewFrameShot()) {
                cancelRecording();
            } else {
                this.mIsRecordingCanceled = true;
                this.mIsMultiviewRecording = false;
            }
        }
        if (!SecureImageUtil.isSecureCamera() && this.mIsImportedImage) {
            this.mFrameShotProgressCount--;
            if (this.mFrameShotProgressCount < 0) {
                this.mFrameShotProgressCount = 0;
            }
        }
        this.mGet.setPreviewVisibility(4);
        super.onPauseBefore();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- onDestroy");
        super.onDestroy();
        this.mGet.movePreviewOutOfWindow(false);
        this.mMultiViewManager = null;
        this.mFrameShotFilePath = null;
        this.mVideoDegree = null;
    }

    protected boolean onSurfaceDestroyed(SurfaceHolder holder) {
        CamLog.m3d(CameraConstants.TAG, "onSurfaceDestroyed. mCameraDevice is null ? " + (this.mCameraDevice == null));
        if (!(this.mCameraDevice == null || isModuleChanging())) {
            this.mCameraDevice.stopPreview();
            this.mCameraDevice.setPreviewDisplay(null);
        }
        return true;
    }

    protected void initializeSettingMenus() {
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FRAME_GRID, "off", false);
        setSpecificSettingValueAndDisable("tracking-af", "off", false);
        setDefaultSettingValueAndDisable(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId), false);
        setDefaultSettingValueAndDisable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId), false);
        setSpecificSettingValueAndDisable(Setting.KEY_VIDEO_STEADY, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSettingMenuEnable(Setting.KEY_TIMER, true);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FINGER_DETECTION, "off", false);
        setSettingMenuEnable(Setting.KEY_SIGNATURE, true);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- restoreSettingMenus");
        restoreFlashSetting();
        restoreSettingValue("hdr-mode");
        restoreSettingValue(Setting.KEY_FRAME_GRID);
        restoreSettingValue("tracking-af");
        restoreSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), this.mCameraId));
        restoreSettingValue(Setting.KEY_VIDEO_STEADY);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_FINGER_DETECTION);
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_FULLVISION);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected void onCameraSwitchingStart() {
        super.onCameraSwitchingStart();
        CameraSecondHolder.subinstance().stopPreview();
        CameraSecondHolder.subinstance().release();
        CamLog.m3d(CameraConstants.TAG, "onCameraSwitchingStart");
    }

    protected void setParamFirstIndividual(CameraParameters parameters) {
        parameters.set("picture-size", getLiveSnapShotSize(parameters, "1280x720"));
        if (FunctionProperties.useWideRearAsDefault()) {
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, "2560x1440");
        } else if (FunctionProperties.isSupportedCollageRecording()) {
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, "1280x720");
        } else {
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, "1080x1080");
        }
        parameters.set(ParamConstants.KEY_VIDEO_SIZE, "1280x720");
        parameters.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, "8000,24000");
    }

    protected void setParamSecondIndividual(CameraParameters parameters) {
        parameters.set("picture-size", getLiveSnapShotSize(parameters, "1280x960"));
        if (Utils.getLCDsize(this.mGet.getAppContext(), true)[1] < CameraConstants.MENU_TYPE_FULL_SCREEN_GUIDE) {
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, "960x720");
        } else {
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, "1280x960");
        }
        parameters.set(ParamConstants.KEY_VIDEO_SIZE, "1280x960");
        parameters.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, "12000,24000");
        this.mBeautyStrength = Integer.parseInt(SharedPreferenceUtil.getBeautyLevel(this.mGet.getAppContext(), "front")) * 10;
        parameters.set(ParamConstants.KEY_BEAUTY_LEVEL, "" + this.mBeautyStrength);
        parameters.set("beautyshot", "on");
        parameters.set(ParamConstants.KEY_MULTISHOT, this.mIsMultiviewRecording ? "on" : "off");
    }

    protected void setRecordingParamOnFront(String value) {
        CameraParameters param = CameraSecondHolder.subinstance().getParameters();
        if (param == null) {
            CamLog.m5e(CameraConstants.TAG, "param is null");
            return;
        }
        String fps;
        String multiShotVal = "";
        if ("true".equals(value)) {
            fps = "24000,24000";
            multiShotVal = "on";
        } else {
            fps = "12000,24000";
            multiShotVal = "off";
        }
        param.set("recording-hint", value);
        param.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, fps);
        param.set(ParamConstants.KEY_BEAUTY_LEVEL, "" + this.mBeautyStrength);
        param.set("beautyshot", "on");
        param.set(ParamConstants.KEY_MULTISHOT, multiShotVal);
        param.set(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()));
        CamLog.m3d(CameraConstants.TAG, "set fps range on front = " + fps + ", value = " + value + ", dual_shot : " + multiShotVal);
        CameraSecondHolder.subinstance().setParameters(CameraSecondHolder.subinstance().getParameters());
        if ("true".equals(value)) {
            CamLog.m3d(CameraConstants.TAG, "recording is started");
            setupPreview(param);
            setMicPath();
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "recording is finished");
    }

    public int getVideoOrientation() {
        return CameraDeviceUtils.getMultiviewOrientationHint((getDisplayOrientation() + getOrientationDegree()) % 360);
    }

    public boolean isMultiviewFrameShot() {
        if ("on".equals(getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT))) {
            return true;
        }
        return false;
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        CamLog.m3d(CameraConstants.TAG, "-rec- doRunnableStartRecorder");
        if (!isMultiviewFrameShot()) {
            super.doRunnableStartRecorder(info);
        } else if (this.mIsSingleViewScreenRecording) {
            this.mCurDegree = this.mGet.getOrientationDegree();
            CamLog.m3d(CameraConstants.TAG, "-degree- mCurDegree : " + this.mCurDegree);
            super.doRunnableStartRecorder(info);
        } else {
            frameRecording(info);
        }
    }

    public int getRecordedCameraIdForReverse() {
        return this.mCameraIdForReverse;
    }

    protected int getCameraDeviceId() {
        int cameraId;
        if (!isMultiviewFrameShot()) {
            cameraId = 2;
        } else if (this.mFrameShotCameraIdOrder.length <= this.mFrameShotProgressCount) {
            CamLog.m3d(CameraConstants.TAG, "ERROR - mFrameShotCameraIdOrder.length = " + this.mFrameShotCameraIdOrder.length + " mFrameShotProgressCount = " + this.mFrameShotProgressCount);
            if (FunctionProperties.useWideRearAsDefault()) {
                return 2;
            }
            return 0;
        } else {
            int curCameraIndex = this.mSpliceViewImageImportManager.getReverseState() ? this.mFrameShotCameraIdOrder[0] : this.mFrameShotCameraIdOrder[this.mFrameShotProgressCount];
            if (this.mSpliceViewImageImportManager.getReverseState()) {
                this.mCameraIdForReverse = curCameraIndex;
            }
            CamLog.m3d(CameraConstants.TAG, "-rec- curCameraIndex : " + curCameraIndex);
            if (curCameraIndex == 4) {
                return 4;
            }
            cameraId = FunctionProperties.useWideRearAsDefault() ? (curCameraIndex == 1 || curCameraIndex == 3) ? 1 : 2 : (curCameraIndex == 1 || curCameraIndex == 3) ? 1 : 0;
        }
        return cameraId;
    }

    protected Camera getCameraDeviceOnMultiview(int cameraDeviceId) {
        return (Camera) (cameraDeviceId != 1 ? this.mCameraDevice.getCamera() : CameraSecondHolder.subinstance().getCameraDevice().getCamera());
    }

    private void initVideoRecorder(StartRecorderInfo info) {
        int cameraId = getCameraDeviceId();
        Camera camera = null;
        if (FunctionProperties.getSupportedHal() != 2) {
            camera = getCameraDeviceOnMultiview(cameraId);
        }
        CamLog.m3d(CameraConstants.TAG, "-multiview- cameraId = " + cameraId);
        if (FunctionProperties.getSupportedHal() != 2) {
            if (FunctionProperties.useWideRearAsDefault()) {
                if (cameraId == 2) {
                    VideoRecorder.setWaitStartRecoding(true);
                }
            } else if (cameraId == 0) {
                VideoRecorder.setWaitStartRecoding(true);
            }
        }
        VideoRecorder.init(true, camera, cameraId, info.mVideoSize, this.mLocationServiceManager != null ? this.mLocationServiceManager.getCurrentLocation() : null, info.mPurpose, info.mVideoFps, info.mVideoBitrate, getRecordingType(), false, getCameraIndex(cameraId), info.mVideoFlipType);
        if (FunctionProperties.useWideRearAsDefault()) {
            if (cameraId == 2) {
                VideoRecorder.resetWaitStartRecoding();
            }
        } else if (cameraId == 0) {
            VideoRecorder.resetWaitStartRecoding();
        }
    }

    private int getCameraIndex(int cameraId) {
        return cameraId == 1 ? 1 : 0;
    }

    private void frameRecording(StartRecorderInfo info) {
        CamLog.m3d(CameraConstants.TAG, "-multiview- frameRecording");
        int videoOrientation = getVideoOrientation();
        VideoRecorder.setOrientationHint(videoOrientation);
        if (this.mRenderThread != null) {
            this.mRenderThread.setOrientationHint(videoOrientation);
            this.mCurDegree = this.mGet.getOrientationDegree();
            CamLog.m3d(CameraConstants.TAG, "-degree- mCurDegree : " + this.mCurDegree);
            VideoRecorder.setMaxDuration(CameraConstants.MULTIVIEW_LIMIT_RECORDING_DURATION);
            VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(info.mStorageType), info.mStorageType);
            VideoRecorder.setFileName(info.mFileName);
            VideoRecorder.setFilePath(info.mOutFilePath);
            initVideoRecorder(info);
            if (VideoRecorder.isInitialized()) {
                MediaRecorderListener listener = new MediaRecorderListener();
                VideoRecorder.setInfoListener(listener);
                VideoRecorder.setErrorListener(listener);
                this.mRecordingUIManager.initLayout();
                IntentBroadcastUtil.blockAlarmInRecording(this.mGet.getActivity(), true);
                if (VideoRecorder.start()) {
                    if (AudioUtil.isWiredHeadsetHasMicOn()) {
                        showHeadsetRecordingToastPopup();
                    }
                    AudioUtil.setAllSoundCaseMute(getAppContext(), true);
                    updateDeviceParameter();
                    setCameraState(6);
                    this.mRecordingUIManager.setRec3sec(true);
                    this.mRecordingUIManager.updateVideoTime(1, SystemClock.uptimeMillis());
                    this.mRecordingUIManager.updateRecStatusIcon();
                    this.mRecordingUIManager.show(access$1600());
                    setBatteryIndicatorVisibility(true);
                    setRecordingUILocation();
                    updateRecordingTime();
                    checkThemperatureOnRecording(true);
                    this.mOrientationAtStartRecording = getActivity().getResources().getConfiguration().orientation;
                    startHeatingWarning(true);
                    keepScreenOn();
                    this.mStartRecorderInfo = null;
                    return;
                }
                restoreRecorderToIdle();
                this.mRecordingUIManager.hide();
                this.mToastManager.showShortToast(getActivity().getString(isMiracastState() ? C0088R.string.error_video_recording_during_miracast : C0088R.string.error_occurred));
                return;
            }
            this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
            restoreRecorderToIdle();
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-multiview- mRenderThread is null");
    }

    protected void setRecordingUILocation() {
        this.mMultiViewManager.setRecordingUILocation(((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getRecordingUILocation(this.mSpliceViewImageImportManager.getReverseState() ? 0 : this.mFrameShotProgressCount, this.mGet.getAppContext()), isMultiviewFrameShot());
    }

    public void videoRecorderRelease() {
        CamLog.m3d(CameraConstants.TAG, "videoRecorderRelease");
        int cameraId = getCameraDeviceId();
        if (this.mCameraDevice == null || !CameraSecondHolder.isSecondCameraOpened()) {
            CamLog.m3d(CameraConstants.TAG, "mCameraDevice is null");
        } else if (FunctionProperties.getSupportedHal() == 2) {
            try {
                if (!(isMultiviewFrameShot() || this.mRenderThread == null)) {
                    this.mRenderThread.stopRecorder();
                }
                VideoRecorder.release(null);
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "VideoRecorder stop error! ", e);
            }
        } else if (getCameraDeviceOnMultiview(cameraId) != null) {
            try {
                if (!(isMultiviewFrameShot() || this.mRenderThread == null)) {
                    this.mRenderThread.stopRecorder();
                }
                VideoRecorder.release(null);
            } catch (Exception e2) {
                CamLog.m6e(CameraConstants.TAG, "VideoRecorder stop error! ", e2);
            }
        }
    }

    public void onVideoStopClickedBefore() {
        super.onVideoStopClickedBefore();
        if (this.mQuickButtonManager != null && (getMVState() & 32) != 0) {
            this.mQuickButtonManager.setVisibility(100, 4, false);
        } else if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.updateButton(100);
        }
    }

    private boolean processBackKeyOnRecording() {
        if (!this.mIsMultiviewRecording) {
            return false;
        }
        if (isMultiviewFrameShot()) {
            CamLog.m3d(CameraConstants.TAG, "-th- doBackKey Recording state getMVState() = " + getMVState());
            if ((getMVState() & 32) != 0) {
                setMVState(1);
            }
            cancelRecording();
            this.mGet.enableConeMenuIcon(31, true);
            if (this.mFrameShotProgressCount == 0) {
                this.mFrameShotProgress = false;
            }
        } else if (this.mRecordingUIManager == null || this.mRecordingUIManager.checkMinRecTime()) {
            this.mCaptureButtonManager.changeButtonByMode(1);
            onShutterStopButtonClicked();
        } else {
            CamLog.m7i(CameraConstants.TAG, "Video recording time is too short.");
            return true;
        }
        if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            return true;
        }
        this.mSpliceViewImageImportManager.showDualCamLayoutAll(true);
        return true;
    }

    public String makeFileName(int useType, int storage, String dir, boolean useThread, String shotMode) {
        if (!this.mFrameShotProgress || useType != 1) {
            return FileNamer.get().getFileNewName(getAppContext(), useType, storage, dir, false, shotMode);
        }
        String filePath = CameraConstants.MULTIVIEW_FILE_NAME + this.mFrameShotProgressCount;
        CamLog.m3d(CameraConstants.TAG, "-cp- makeFileName add video file");
        this.mFrameShotFilePath.add(dir + filePath + ".mp4");
        this.mCurDegree = this.mGet.getOrientationDegree();
        this.mVideoDegree.add(Integer.valueOf(this.mCurDegree));
        CamLog.m3d(CameraConstants.TAG, "-degree- add degree = " + this.mCurDegree + " videoDegree size = " + this.mVideoDegree.size());
        return filePath;
    }

    protected boolean closeModeAndSettingMenu() {
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

    public boolean doBackKey() {
        CamLog.m3d(CameraConstants.TAG, "doBackKey");
        if (this.mGet.isHelpListVisible()) {
            this.mHandler.sendEmptyMessage(49);
            return true;
        } else if (processBackKeyOnRecording()) {
            if (this.mQuickClipManager != null) {
                this.mQuickClipManager.doBackMultishot();
            }
            setQuickClipIcon(true, true);
            return true;
        } else if (isTimerShotCountdown()) {
            CamLog.m3d(CameraConstants.TAG, "doBackKey isTimerShotCountdown");
            processTimerForSplice();
            setMVState(1);
            if (this.mQuickClipManager != null) {
                this.mQuickClipManager.doBackMultishot();
            }
            setQuickClipIcon(true, true);
            return super.doBackKey();
        } else if (!this.mFrameShotProgress && (getMVState() & 16) == 0) {
            return super.doBackKey();
        } else {
            CamLog.m3d(CameraConstants.TAG, "-th- doBackKey capture progress");
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).releaseGuidePhotoView();
                resetImportedImage();
                this.mSpliceViewImageImportManager.hidePrePostLayout();
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(1, false);
                this.mSpliceViewImageImportManager.setPrePostBitmap(null);
            }
            cancelFrameShot();
            MultiViewFrame.resetCapturedTexture();
            this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
            setQuickClipIcon(true, true);
            if (!isTimerShotCountdown()) {
                return true;
            }
            this.mMultiViewManager.setMultiGuideTextVisibility(true);
            return super.doBackKey();
        }
    }

    private void processTimerForSplice() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            this.mSpliceViewImageImportManager.showDualCamLayoutAll(true);
            if (isMultiviewFrameShot()) {
                this.mIsTimerShotCanceled = true;
            }
        }
    }

    public boolean isQuickClipShowingCondition() {
        if ((getMVState() & 32) == 0 && (getMVState() & 16) == 0) {
            return true;
        }
        return false;
    }

    protected void cancelRecording() {
        CamLog.m3d(CameraConstants.TAG, "-cp- cancelRecording mFrameShotProgressCount = " + this.mFrameShotProgressCount);
        onVideoStopClicked(true, false);
        this.mIsRecordingCanceled = true;
        if (this.mFrameShotFilePath.size() > this.mFrameShotProgressCount) {
            this.mFrameShotFilePath.remove(this.mFrameShotProgressCount);
            this.mVideoDegree.remove(this.mFrameShotProgressCount);
        }
        this.mIsMultiviewRecording = false;
    }

    protected void cancelFrameShot() {
        boolean z = true;
        this.mFrameShotProgress = false;
        resetFrameShotProgress();
        setMVState(1);
        initMultiviewGuide();
        if (this.mReviewThumbnailManager != null) {
            GestureViewManager gestureViewManager = this.mReviewThumbnailManager;
            if (getUri() == null) {
                z = false;
            }
            gestureViewManager.setEnabled(z);
        }
        setQuickClipIcon(false, false);
    }

    public void changeLayoutOnMultiview(String layoutType) {
        CamLog.m3d(CameraConstants.TAG, "changeLayoutOnMultiview layoutType = " + layoutType);
        resetFrameShotProgress();
        this.mFrameShotProgress = false;
        this.mMultiviewFrame.changeLayout(getLayoutIndex(layoutType), false);
        setMultiLayoutDegree(this.mGet.getOrientationDegree());
        cancelFrameShot();
        MultiViewFrame.resetCapturedTexture();
        this.mMultiviewFrame.restoreVertex(this.mGet.getOrientationDegree());
        this.mQuickButtonManager.setButtonIndex(C0088R.id.quick_button_multi_view_layout, getLayoutIndex());
        if (this.mMultiViewManager != null) {
            this.mMultiViewManager.activateFirstGuideLayout();
        }
    }

    private void setCapturedImageViewPositions() {
        int viewSize = ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getFrameShotMaxCount();
        for (int i = 0; i < viewSize; i++) {
            this.mMultiViewManager.setCaptureImgPosition(i, ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getViewRect(i));
        }
        Rect emptyRect = new Rect(0, 0, 0, 0);
        if (viewSize == 2) {
            this.mMultiViewManager.setCaptureImgPosition(2, emptyRect);
            this.mMultiViewManager.setCaptureImgPosition(3, emptyRect);
        } else if (viewSize == 3) {
            this.mMultiViewManager.setCaptureImgPosition(3, emptyRect);
        }
    }

    private void updateFrameShotProgressInfo() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- updateFrameShotProgressInfo");
        this.mMultiViewManager.setGuideTextLocation(((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getGuideTextLocation((int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.multi_guide_text_width), (int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.multi_guide_text_height)));
        this.mMultiViewManager.setDrawableArray(((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getDrawableArray());
        this.mFrameShotMaxCount = ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getFrameShotMaxCount();
        this.mFrameShotCameraIdOrder = ((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getCurCameraIdArray();
        this.mMultiViewManager.setCenterPoint(((MultiViewLayout) this.mMultiViewLayoutList.get(getLayoutIndex())).getCenterPoint());
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mMultiViewManager);
    }

    protected void setVideoLimitSize() {
        if (isMultiviewFrameShot()) {
            this.mLimitRecordingDuration = CameraConstants.MULTIVIEW_LIMIT_RECORDING_DURATION;
        } else {
            this.mLimitRecordingDuration = 0;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.mPosX = event.getX();
                this.mPosY = event.getY();
                this.mIsPinch = false;
                break;
            case 1:
                if (!(this.mPosX == -1.0f || this.mPosY == -1.0f)) {
                    int distance = ((int) (Math.abs(event.getX() - this.mPosX) + Math.abs(event.getY() - this.mPosY))) / 2;
                    if (!(this.mIsPinch || this.mIsTakingPicture || isModeMenuVisible() || isSettingMenuVisible() || isHelpListVisible() || this.mQuickClipManager.isOpened() || Utils.checkSystemUIArea(this.mGet.getAppContext(), (int) this.mPosX, (int) this.mPosY) || distance <= 100 || isAnimationShowing() || !checkModuleValidate(192) || this.mMultiviewFrame == null)) {
                        if (!CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
                            this.mMultiviewFrame.processGesture(this.mPosX, this.mPosY, this.mGet.getOrientationDegree());
                        } else if (this.mGestureManager.gestureDetected(event)) {
                            this.mGet.setGestureType(this.mGestureManager.getGestureFlickingType());
                            processGestureForSplice(this.mPosX, this.mPosY);
                        }
                    }
                    this.mPosX = -1.0f;
                    this.mPosY = -1.0f;
                    break;
                }
            case 2:
                if (event.getPointerCount() > 1) {
                    this.mIsPinch = true;
                    break;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    protected void processGestureForSplice(float x, float y) {
    }

    protected int getLayoutIndex() {
        return getSettingIndex(Setting.KEY_MULTIVIEW_LAYOUT);
    }

    protected void multiviewMakingCollageEnd() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CamLog.m3d(CameraConstants.TAG, "-cp- multiviewMakingCollageEnd");
                if (MultiViewCameraModuleBase.this.mDialogManager != null && MultiViewCameraModuleBase.this.mDialogManager.isProgressDialogVisible()) {
                    MultiViewCameraModuleBase.this.mDialogManager.onDismissRotateDialog();
                    if (CameraConstants.MODE_SQUARE_SPLICE.equals(MultiViewCameraModuleBase.this.getShotMode()) && MultiViewCameraModuleBase.this.mSplicePostViewManager != null && MultiViewCameraModuleBase.this.isMultiviewFrameShot()) {
                        if (MultiViewCameraModuleBase.this.mSplicePostViewManager != null) {
                            MultiViewCameraModuleBase.this.mSplicePostViewManager.removeViews();
                        }
                        MultiViewCameraModuleBase.this.resetImportedImageOnUiThread();
                        MultiViewCameraModuleBase.this.setButtonsVisibilityForPostView(true);
                        MultiViewCameraModuleBase.this.mSpliceViewImageImportManager.showImportLayout(true);
                    }
                }
                MultiViewCameraModuleBase.this.mGet.enableConeMenuIcon(31, true);
                MultiViewFrame.resetCapturedTexture();
                if (MultiViewCameraModuleBase.this.mMultiviewFrame != null) {
                    MultiViewCameraModuleBase.this.mMultiviewFrame.restoreVertex(MultiViewCameraModuleBase.this.mGet.getOrientationDegree());
                }
                MultiViewCameraModuleBase.this.initMultiviewGuide();
                MultiViewCameraModuleBase.this.restoreCleanViewForSelfTimer(500);
            }
        });
    }

    protected void resetImportedImageOnUiThread() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    try {
                        MultiViewCameraModuleBase.this.resetImportedImage();
                        if (MultiViewCameraModuleBase.this.mMultiviewFrame != null && MultiViewCameraModuleBase.this.mIsImportedImage) {
                            MultiViewCameraModuleBase.this.mMultiviewFrame.restoreVertex(MultiViewCameraModuleBase.this.mGet.getOrientationDegree());
                        }
                        if (MultiViewCameraModuleBase.this.mMultiViewLayoutList != null) {
                            ((MultiViewLayout) MultiViewCameraModuleBase.this.mMultiViewLayoutList.get(MultiViewCameraModuleBase.this.getLayoutIndex())).releaseGuidePhotoView();
                        }
                        if (MultiViewCameraModuleBase.this.mSpliceViewImageImportManager != null) {
                            MultiViewCameraModuleBase.this.mSpliceViewImageImportManager.resetPrePostLayout();
                        }
                        MultiViewCameraModuleBase.this.mIsCollageShared = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void multiviewFrameReady() {
        super.multiviewFrameReady();
        CamLog.m3d(CameraConstants.TAG, "multiviewFrameReady");
        if (this.mMultiViewManager == null) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- create multiview manager again.");
            this.mMultiViewManager = new MultiViewManager(this);
        }
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (MultiViewCameraModuleBase.this.mMultiViewManager == null || MultiViewCameraModuleBase.this.mGet.isPaused() || MultiViewCameraModuleBase.this.mGet.getActivity().isFinishing()) {
                    CamLog.m3d(CameraConstants.TAG, "-multiview- do not proceed to multiview operation");
                    return;
                }
                if (MultiViewCameraModuleBase.this.mFrameShotProgressCount == 0) {
                    MultiViewCameraModuleBase.this.initMultiviewGuide();
                } else if (!MultiViewCameraModuleBase.this.mSplicePostViewManager.isPostviewVisible()) {
                    MultiViewCameraModuleBase.this.mMultiViewManager.restorePreCondition();
                }
                MultiViewCameraModuleBase.this.mMultiviewFrameReady = true;
            }
        });
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

    protected void initMultiviewGuide() {
        boolean z = false;
        CamLog.m3d(CameraConstants.TAG, "-multiview- initMultiviewGuide");
        if (this.mMultiViewManager != null) {
            this.mMultiViewManager.initGuideLayout();
            updateFrameShotProgressInfo();
            this.mMultiViewManager.initGuideText();
            this.mMultiViewManager.activateFirstGuideLayout();
            this.mMultiViewManager.setMultiviewGuideBg(getLayoutIndex());
            setCapturedImageViewPositions();
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode()) && this.mSpliceViewImageImportManager != null) {
                this.mSpliceViewImageImportManager.initLayout();
                this.mSpliceViewImageImportManager.updateDualCamLayoutShowing(0, true);
                SpliceViewImageImportManager spliceViewImageImportManager = this.mSpliceViewImageImportManager;
                if (!isMultiviewFrameShot()) {
                    z = true;
                }
                spliceViewImageImportManager.updateDualCamLayoutShowing(1, z);
            }
        }
    }

    protected void restoreCleanViewForSelfTimer(long delay) {
        if (getMVState() != 16) {
            super.access$4000(delay);
        }
    }

    protected void doCleanViewAfterStopRecording() {
        if ((getMVState() & 32) == 0) {
            super.doCleanViewAfterStopRecording();
        }
    }

    protected void updateButtonsOnVideoStopClicked() {
        if ((getMVState() & 32) == 0 && (!isMultiviewFrameShot() || this.mFrameShotProgressCount == this.mFrameShotMaxCount)) {
            super.updateButtonsOnVideoStopClicked();
        } else if (this.mBackButtonManager != null) {
            this.mBackButtonManager.setBackButton(false);
        }
    }

    protected void enableControls(boolean enable, boolean changeColor) {
        setCaptureButtonEnable(enable, getShutterButtonType());
        if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.refreshButtonEnable(100, enable, changeColor);
        }
        if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
            setCaptureButtonEnable(false, 1);
        }
        if (FunctionProperties.isSupportedConeUI()) {
            this.mGet.setConeClickable(enable);
        }
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    public void playRecordingSound(boolean start) {
        if (start || this.mFrameShotProgressCount == this.mFrameShotMaxCount - 1 || (getMVState() & 32) == 0) {
            super.playRecordingSound(start);
        }
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mLayoutSelectionManager != null) {
            this.mLayoutSelectionManager.setVisibility(false);
        }
        if (this.mMultiViewManager != null) {
            this.mMultiViewManager.setFrameShotGuideVisibility(false);
        }
    }

    public void setQuickButtonSelected(int id, boolean selected) {
        if (id != C0088R.id.quick_button_multi_view_layout) {
            super.setQuickButtonSelected(id, selected);
        } else if (this.mQuickButtonManager != null) {
            this.mQuickButtonManager.setPressed(id, selected);
        }
    }

    public boolean isAvailableToChangeAutoModeByWatch() {
        if (this.mFrameShotProgress || (getMVState() & 48) != 0) {
            return false;
        }
        return super.isAvailableToChangeAutoModeByWatch();
    }

    protected void doOnStartPreviewResult(boolean result) {
        super.doOnStartPreviewResult(result);
        updateFrameShotProgressInfo();
    }

    public boolean isMultiviewIntervalShot() {
        if ((getMVState() & 48) != 0) {
            return true;
        }
        return false;
    }

    public String getRecordingType() {
        if (CameraConstants.MODE_SNAP.equals(getShotMode())) {
            return CameraConstants.VIDEO_SNAP_TYPE;
        }
        return CameraConstants.VIDEO_MULTIVIEW_TYPE;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mMultiViewManager != null) {
            this.mMultiViewManager.onDestroy();
            this.mMultiViewManager.init();
            initMultiviewGuide();
        }
    }

    public void setCameraChangingOnSnap(boolean isChanging) {
        this.mGet.setCameraChangingOnSnap(isChanging);
    }

    protected void resetImportedImage() {
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(getShotMode())) {
            this.mIsImportedImage = false;
            this.mImportedUri = null;
            if (!(this.mImportedBitmap == null || this.mImportedBitmap.isRecycled())) {
                this.mImportedBitmap.recycle();
            }
            if (this.mSpliceViewImageImportManager != null) {
                this.mSpliceViewImageImportManager.showImportButton(true);
                this.mSpliceViewImageImportManager.showSwapAndRotateButton(false);
                this.mSpliceViewImageImportManager.setReverseState(false);
                this.mSpliceViewImageImportManager.changeImportButtonLayoutLocation();
            }
            if (this.mMultiviewFrame != null) {
                this.mMultiviewFrame.setRotateState(0);
            }
        }
    }

    protected String getLDBNonSettingString() {
        return super.getLDBNonSettingString() + "snap_mode=Multi";
    }

    public void setButtonsVisibilityForPostView(boolean visible) {
        CamLog.m3d(CameraConstants.TAG, "visible : " + visible);
        this.mVisibleForPostView = visible;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                int visibility = MultiViewCameraModuleBase.this.mVisibleForPostView ? 0 : 8;
                MultiViewCameraModuleBase.this.mQuickButtonManager.setVisibility(100, visibility, false);
                if (!MultiViewCameraModuleBase.this.isMenuShowing(1)) {
                    MultiViewCameraModuleBase.this.mCaptureButtonManager.setShutterButtonVisibility(visibility, MultiViewCameraModuleBase.this.getShutterButtonType(), false);
                    MultiViewCameraModuleBase.this.mReviewThumbnailManager.setThumbnailVisibility(visibility, false, true);
                }
                if (MultiViewCameraModuleBase.this.mVisibleForPostView) {
                    MultiViewCameraModuleBase.this.mBackButtonManager.show();
                } else {
                    MultiViewCameraModuleBase.this.mBackButtonManager.hide();
                }
            }
        });
    }

    public boolean getSpliceviewReverseState() {
        return getReverseState();
    }
}
