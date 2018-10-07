package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule.MediaRecorderListener;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PopoutModule extends PopoutModuleBase {
    protected static final int POPOUT_ALL_PREVIEW_AVAILABLE = 3;
    protected static final int POPOUT_NORMAL_PREVIEW_AVAILABLE = 1;
    protected static final int POPOUT_VIDEO_BIT_RATE = 4000000;
    protected static final int POPOUT_VIDEO_FRAME_RATE = 30;
    protected static final int POPOUT_WIDE_PREVIEW_AVAILABLE = 2;
    protected PopoutCaptureDataSyncThread mCaptureDataSyncThread;
    protected ExifInterface mExif;
    protected int mIsAllPreviewAvailable = 0;
    protected boolean mIsNormalCaptured = false;
    protected boolean mIsShowFpsLog = false;
    protected boolean mIsWideCaptured = false;
    protected byte[] mNormalViewCaptureData;
    protected CameraPictureCallback mPictureCallback2 = new C04713();
    protected byte[] mWideViewCaptureData;

    /* renamed from: com.lge.camera.app.ext.PopoutModule$1 */
    class C04691 extends Thread {
        C04691() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[popout] Wait saving picture");
            int waitCnt = 0;
            while (PopoutModule.this.mIsSavingPicture && waitCnt <= 50) {
                try {
                    C04691.sleep(100);
                    waitCnt++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.PopoutModule$2 */
    class C04702 implements CameraPreviewDataCallback {
        C04702() {
        }

        public void onPreviewFrame(byte[] data, CameraProxy camera) {
            CamLog.m3d(CameraConstants.TAG, "[popout] onPreviewFrame for wide camera");
            PopoutModule popoutModule = PopoutModule.this;
            popoutModule.mIsAllPreviewAvailable |= 2;
        }
    }

    /* renamed from: com.lge.camera.app.ext.PopoutModule$3 */
    class C04713 extends CameraPictureCallback {
        C04713() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            PopoutModule.this.onPictureTakenCallback2(data, camera);
        }
    }

    protected class PopoutCaptureDataSyncThread extends Thread {
        protected PopoutCaptureDataSyncThread() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[popout] PopoutCaptureDataSyncThread - start");
            int watingCount = 0;
            while (true) {
                try {
                    if (PopoutModule.this.mIsNormalCaptured && PopoutModule.this.mIsWideCaptured) {
                        PopoutModule.this.mIsSavingPicture = true;
                        PopoutModule.this.sendPopoutStillImage();
                        PopoutModule.this.mIsNormalCaptured = false;
                        PopoutModule.this.mIsWideCaptured = false;
                        return;
                    } else if (watingCount > 70) {
                        CamLog.m11w(CameraConstants.TAG, "[popout] Save fail");
                        return;
                    } else if (!PopoutModule.this.mGet.isPaused()) {
                        Thread.sleep(100);
                        watingCount++;
                    } else {
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public PopoutModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void sendPopoutStillImage() {
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mCaptureButtonManager.changeButtonByMode(1);
        setQuickButtonEnable(C0088R.id.quick_button_film_emulator, false, true);
    }

    public void onPauseAfter() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(15);
        }
        if (isPaused()) {
            setPictureSizeListAndSettingMenu(false);
        }
        this.mIsOriginalListBackuped = false;
        this.mGet.setPreviewCoverVisibility(0, false);
        super.onPauseAfter();
        if (this.mCaptureDataSyncThread != null) {
            try {
                this.mCaptureDataSyncThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mCaptureDataSyncThread = null;
        }
        if (this.mIsSavingPicture) {
            Thread waitSavingPictureThread = new C04691();
            waitSavingPictureThread.start();
            try {
                waitSavingPictureThread.join();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            this.mIsSavingPicture = false;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (!this.mGet.isAnimationShowing()) {
            this.mGet.movePreviewOutOfWindow(false);
        }
        this.mIsOriginalListBackuped = false;
    }

    protected boolean isCameraDeviceAvailable() {
        if (this.mCameraDevice == null || this.mCameraDevice.getCamera() == null) {
            CamLog.m3d(CameraConstants.TAG, "[popout] Normal camera is null, so return");
            return false;
        } else if (!this.mIsMultiPopoutMode || (CameraSecondHolder.subinstance() != null && this.mPopoutSurfaceView != null)) {
            return true;
        } else {
            CamLog.m3d(CameraConstants.TAG, "[popout] Wide camera is null, so return");
            return false;
        }
    }

    protected void startPreviewForNormalCamera(CameraParameters parameters, boolean stopPreview) {
        if (this.mCameraDevice != null) {
            CamLog.m7i(CameraConstants.TAG, "[popout] startPreviewForNormalCamera");
            if (stopPreview) {
                this.mCameraDevice.stopPreview();
            }
            this.mCameraDevice.setErrorCallback(this.mHandler, this.mErrorCallback);
            CameraParameters param = parameters;
            if (parameters == null) {
                param = this.mCameraDevice.getParameters();
            }
            if (!(this.mParamUpdater == null || param == null)) {
                this.mParamUpdater.updateAllParameters(param);
                Log.i(CameraConstants.TAG, "[Time Info][4] App Param setting End : Camera Parameter setting " + DebugUtil.interimCheckTime(true) + " ms");
                Log.i(CameraConstants.TAG, "[Time Info][5] Device Param setting Start : Device setting " + DebugUtil.interimCheckTime(false));
                param.set(ParamConstants.KEY_PREVIEW_SIZE, this.mNormalCameraPreviewSize);
                this.mCameraDevice.setParameters(param);
                Log.i(CameraConstants.TAG, "[Time Info][5] Device Param setting End : Device setting " + DebugUtil.interimCheckTime(true) + " ms");
                CamLog.m3d(CameraConstants.TAG, "camera 0 - paramPictureSize = " + param.get("picture-size") + ", paramPreviewSize = " + param.get(ParamConstants.KEY_PREVIEW_SIZE));
            }
            setDisplayOrientation(false);
            setOneShotPreviewCallback();
            Log.i(CameraConstants.TAG, "[Time Info][6] Device StartPreview Start : Driver Preview Operation " + DebugUtil.interimCheckTime(false));
            this.mCameraDevice.startPreview();
            setZoomCompensation(param);
        }
    }

    protected void startPreviewForWideCamera(CameraParameters parameters, boolean stopPreview) {
        if (this.mIsMultiPopoutMode && CameraSecondHolder.subinstance() != null) {
            CamLog.m7i(CameraConstants.TAG, "[popout] startPreviewForWideCamera");
            if (stopPreview) {
                CameraSecondHolder.subinstance().stopPreview();
            }
            CameraParameters param = parameters;
            if (param == null) {
                param = CameraSecondHolder.subinstance().getParameters();
                setParamForWideCamera(param);
            } else {
                param.set(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()));
                param.set(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_STR);
                param.set(ParamConstants.KEY_PREVIEW_SIZE, this.mWideCameraPreviewSize);
            }
            CamLog.m3d(CameraConstants.TAG, "[popout] Wide camera setParameters");
            CameraSecondHolder.subinstance().setParameters(param);
            setOneShotPreviewCallback2();
            CamLog.m3d(CameraConstants.TAG, "[popout] device1.startPreview");
            CameraSecondHolder.subinstance().startPreview();
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
    }

    protected void startPreview(CameraParameters params) {
        this.mIsAllPreviewAvailable = 0;
        boolean isStopPrevigew = ModelProperties.isMTKChipset();
        startPreviewForWideCamera(params, isStopPrevigew);
        startPreviewForNormalCamera(params, isStopPrevigew);
    }

    protected void startPreviewDone() {
        CamLog.m3d(CameraConstants.TAG, "[popout] startPreviewDone : start");
        if (checkModuleValidate(1)) {
            this.mGet.setPreviewVisibility(0);
            if (this.mFocusManager != null && isFocusEnableCondition()) {
                this.mFocusManager.registerCallback();
            }
            CamLog.m3d(CameraConstants.TAG, "QuickClip setLayout");
            if (isSupportedQuickClip() && this.mQuickClipManager != null) {
                this.mQuickClipManager.setLayout();
            }
            if (FunctionProperties.isSupportedSwitchingAnimation()) {
                ColorUtil.createRS(getAppContext());
            }
            CamLog.m3d(CameraConstants.TAG, "[popout] startPreviewDone : end");
        }
    }

    protected void stopPreview() {
        super.stopPreview();
        if (this.mIsMultiPopoutMode && CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().stopPreview();
        }
    }

    protected void closeCamera() {
        CamLog.m3d(CameraConstants.TAG, "[popout] closeCamera");
        if (this.mPopoutSurfaceView != null) {
            this.mPopoutSurfaceView.setVisibility(8);
            this.mPopoutSurfaceView = null;
        }
        closeCamera2();
        super.closeCamera();
    }

    protected void closeCamera2() {
        if (this.mIsMultiPopoutMode) {
            CamLog.m3d(CameraConstants.TAG, "[popout] closeCamera2");
            if (CameraSecondHolder.subinstance() != null) {
                CameraSecondHolder.subinstance().setErrorCallback(null, null);
                CameraSecondHolder.subinstance().release();
            }
        }
    }

    protected void setParamForWideCamera(CameraParameters parameters) {
        if (parameters != null && this.mIsMultiPopoutMode) {
            parameters.set(ParamConstants.KEY_DUAL_RECORDER, 1);
            parameters.set(ParamConstants.KEY_LGE_CAMERA, 1);
            parameters.set("picture-size", this.mPictureSize);
            parameters.set(ParamConstants.KEY_PREVIEW_SIZE, this.mWideCameraPreviewSize);
            parameters.set("flash-mode", "off");
            parameters.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, MultimediaProperties.getCameraFPSRange(isRearCamera()));
            parameters.set(ParamConstants.KEY_ZSL, "off");
            if (getCameraState() == 5) {
                ListPreference listPref = getListPreference(getVideoSizeSettingKey());
                if (listPref != null) {
                    parameters.set(ParamConstants.KEY_VIDEO_SIZE, listPref.getValue());
                } else {
                    return;
                }
            }
            parameters.set(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_STR);
            parameters.set(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()));
        }
    }

    protected void setOneShotPreviewCallback2() {
        if (CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().setOneShotPreviewDataCallback(this.mHolderHandler, new C04702());
        }
    }

    protected void switchCamera() {
        if (checkModuleValidate(77)) {
            setPictureSizeListAndSettingMenu(false);
            if (this.mPopoutSurfaceView != null) {
                int distance = Utils.getDefaultDisplayHeight(getActivity()) + 300;
                CamLog.m3d(CameraConstants.TAG, "move popout preview out of window");
                this.mPopoutSurfaceView.setTranslationY((float) distance);
            }
            super.switchCamera();
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mIsMultiPopoutMode) {
            if (this.mIsAllPreviewAvailable != 3) {
                CamLog.m3d(CameraConstants.TAG, "[popout] All preview is not available : " + this.mIsAllPreviewAvailable);
                return false;
            }
        } else if (this.mIsAllPreviewAvailable == 0) {
            CamLog.m3d(CameraConstants.TAG, "[popout] All preview is not available : " + this.mIsAllPreviewAvailable);
            return false;
        }
        return super.onCameraShutterButtonClicked();
    }

    protected void doTakePicture() {
        if (this.mCaptureDataSyncThread == null) {
            this.mCaptureDataSyncThread = new PopoutCaptureDataSyncThread();
            this.mCaptureDataSyncThread.start();
            CamLog.m3d(CameraConstants.TAG, "[popout] doTakePicture");
            if (this.mIsMultiPopoutMode && CameraSecondHolder.subinstance() != null) {
                CamLog.m3d(CameraConstants.TAG, "[popout] doTakePicture - Wide camera");
                CameraSecondHolder.subinstance().takePicture(this.mHolderHandler, null, null, null, this.mPictureCallback2);
            }
            super.doTakePicture();
        }
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        CamLog.m3d(CameraConstants.TAG, "[popout] Normal view picture taken callback");
        this.mNormalViewCaptureData = data;
        this.mExif = Exif.readExif(data);
        this.mIsNormalCaptured = true;
        if (!this.mIsMultiPopoutMode) {
            this.mIsWideCaptured = true;
        }
    }

    protected void oneShotPreviewCallbackDone() {
        CamLog.m3d(CameraConstants.TAG, "[popout] oneShotPreviewCallbackDone");
        if (!checkModuleValidate(1)) {
            CamLog.m3d(CameraConstants.TAG, "[popout] device is released,so return");
        } else if (isCameraDeviceAvailable()) {
            this.mIsAllPreviewAvailable |= 1;
            if (this.mGet.isAnimationShowing()) {
                this.mGet.setPreviewVisibility(4);
            }
            refreshCameraParamForEngine();
            super.oneShotPreviewCallbackDone();
            this.mGet.movePreviewOutOfWindow(true);
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (PopoutModule.this.mPopoutSurfaceView != null) {
                        PopoutModule.this.mPopoutSurfaceView.setTranslationY(0.0f);
                    }
                    PopoutModule.this.mGet.setPreviewCoverVisibility(8, true, null, false, true);
                }
            }, 150);
        }
    }

    protected void onPictureTakenCallback2(byte[] data, CameraProxy camera) {
        CamLog.m3d(CameraConstants.TAG, "[popout] Wide view picture taken callback");
        this.mWideViewCaptureData = data;
        this.mIsWideCaptured = true;
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        String jpgFileName = filename + ".jpg";
        FileNamer.get().addFileNameInSaving(dir + jpgFileName);
        ExifInterface exif = this.mExif;
        int[] pictureSize = Utils.sizeStringToArray(this.mPictureSize);
        Exif.updateThumbnail(exif, data, pictureSize[0], pictureSize[1], 70);
        Exif.setSceneCaptureType(exif, (short) 12);
        int exifDegree = Exif.getOrientation(exif);
        updateThumbnail(exif, exifDegree, false);
        if (!saveThumb(data, extraExif, dir, jpgFileName, exif, pictureSize)) {
            return null;
        }
        String str = dir;
        String str2 = jpgFileName;
        Uri uri = FileManager.registerImageUri(this.mGet.getAppContext().getContentResolver(), str, str2, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, Exif.getExifSize(exif), false);
        checkSavedURI(uri);
        FileNamer.get().removeFileNameInSaving(dir + jpgFileName);
        this.mGet.onNewItemAdded(uri, 3, null);
        CamLog.m3d(CameraConstants.TAG, "[popout] Jpeg uri = " + uri);
        if (VideoRecorder.isRecording()) {
            File file = new File(dir + jpgFileName);
            if (file != null) {
                long savedFileSize = file.length();
                CamLog.m3d(CameraConstants.TAG, "[popout] changeMaxFileSize = " + savedFileSize);
                VideoRecorder.changeMaxFileSize(savedFileSize);
            }
        }
        checkStorage();
        return uri;
    }

    private boolean saveThumb(byte[] data, byte[] extraExif, String dir, String fileName, ExifInterface exif, int[] pictureSize) {
        int thumbRewriteCount = 0;
        boolean isThumbWrote = false;
        int quality = 70;
        while (!isThumbWrote) {
            thumbRewriteCount++;
            if (thumbRewriteCount > 3) {
                try {
                    this.mToastManager.showShortToast(this.mGet.getAppContext().getResources().getString(C0088R.string.saving_failure));
                    return false;
                } catch (IOException e) {
                    quality -= 20;
                    if (quality < 0) {
                        quality = 0;
                    }
                    switch (thumbRewriteCount) {
                        case 1:
                            CamLog.m3d(CameraConstants.TAG, "[popout] re compress thumbnail - quality: " + quality);
                            Exif.updateThumbnail(exif, data, pictureSize[0], pictureSize[1], quality);
                            break;
                        case 2:
                            CamLog.m3d(CameraConstants.TAG, "[popout] remove thumbnail");
                            exif.removeCompressedThumbnail();
                            break;
                        default:
                            return false;
                    }
                }
            }
            isThumbWrote = FileManager.writeJpegImageToFile(data, extraExif, dir, fileName, exif);
        }
        return true;
    }

    protected void doSaveImagePostExecute(Uri uri) {
        super.doSaveImagePostExecute(uri);
        if (!this.mGet.isPaused()) {
            startPreview(null);
        }
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        if (!isMenuShowing(3)) {
            this.mPopoutCameraManager.setEffectButtonVisibility(true);
            this.mPopoutCameraManager.setFrameVisibility(true);
        }
    }

    protected void setPreviewFpsRange(CameraParameters parameters, boolean isRecordingStarted) {
        super.setPreviewFpsRange(parameters, isRecordingStarted);
        String fps = SystemProperties.get(ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMCORDER, "15000,30000");
        if (CameraSecondHolder.subinstance() != null) {
            try {
                CameraParameters param = CameraSecondHolder.subinstance().getParameters();
                if (param != null) {
                    if (isRecordingStarted) {
                        param.setRecordingHint(true);
                    } else {
                        fps = MultimediaProperties.getCameraFPSRange(isRearCamera());
                    }
                    param.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, fps);
                    CameraSecondHolder.subinstance().setParameters(param);
                }
            } catch (RuntimeException e) {
                Log.e(CameraConstants.TAG, "[popout] - getParameters failed");
            }
        }
        CamLog.m3d(CameraConstants.TAG, "[Popout] setPreviewFpsRange for wide = " + fps);
    }

    protected boolean doSetParamForStartRecording(CameraParameters parameters, boolean recordStart, ListPreference listPref, String videoSize) {
        if (listPref == null || videoSize == null) {
            return false;
        }
        setContinuousFocus(parameters, true);
        if (parameters != null) {
            parameters.setMeteringAreas(null);
            if (parameters.isAutoExposureLockSupported()) {
                parameters.setAutoExposureLock(false);
            }
            if (parameters.isAutoWhiteBalanceLockSupported()) {
                parameters.setAutoWhiteBalanceLock(false);
            }
        }
        String previewSize = listPref.getExtraInfo(1);
        String screenSize = listPref.getExtraInfo(2);
        sVideoSize = Utils.sizeStringToArray(screenSize);
        this.mWideCameraPreviewSize = getPreviewSize(previewSize, screenSize, videoSize);
        this.mNormalCameraPreviewSize = this.mWideCameraPreviewSize;
        CamLog.m3d(CameraConstants.TAG, "[popout] sVideoSize  " + sVideoSize[0] + "x" + sVideoSize[1] + ", videoPreviewSize : " + this.mWideCameraPreviewSize);
        setMicPath();
        this.mParamUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, this.mNormalCameraPreviewSize, false, true);
        this.mParamUpdater.setParamValue("picture-size", this.mNormalCameraPreviewSize, false, true);
        this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, videoSize, false, true);
        setRecordingHint(parameters, "true", false);
        setPreviewFpsRange(parameters, recordStart);
        setCameraState(5);
        if (this.mCameraDevice == null || CameraSecondHolder.subinstance() == null) {
            CamLog.m7i(CameraConstants.TAG, "Camera is null");
            return false;
        }
        this.mCameraDevice.stopPreview();
        CameraSecondHolder.subinstance().stopPreview();
        releasePopoutEngine();
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                PopoutModule.this.mPopoutSurfaceView.setVisibility(8);
            }
        });
        this.mHandler.sendEmptyMessage(86);
        return true;
    }

    protected boolean doSetParamForStopRecording(CameraParameters parameters, boolean recordStart, String videoSize) {
        resetMicPath();
        setContinuousFocus(parameters, false);
        if (CameraSecondHolder.subinstance() != null) {
            try {
                CameraParameters param = CameraSecondHolder.subinstance().getParameters();
                if (param != null) {
                    param.setRecordingHint(false);
                    param.set("picture-size", this.mPictureSize);
                    param.set(ParamConstants.KEY_PREVIEW_SIZE, this.mWideCameraPreviewSize);
                    CameraSecondHolder.subinstance().setParameters(param);
                }
            } catch (RuntimeException e) {
                Log.e(CameraConstants.TAG, "[popout] - getParameters failed");
            }
        }
        this.mParamUpdater.setParamValue("picture-size", this.mPictureSize, false, true);
        setRecordingHint(parameters, "false", false);
        setPreviewFpsRange(parameters, recordStart);
        if (checkPreviewCoverVisibilityForRecording()) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    int[] lcdSize = Utils.getLCDsize(PopoutModule.this.getAppContext(), true);
                    PopoutModule.this.mGet.getCurPreviewBitmap((int) (((float) lcdSize[1]) * 0.5f), (int) (((float) lcdSize[0]) * 0.5f));
                    PopoutModule.this.mGet.setPreviewCoverVisibility(0, false, null, true, true);
                }
            });
        }
        return true;
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        if (!checkModuleValidate(15) || this.mCameraDevice == null || this.mRecordingUIManager == null || this.mAudioZoomManager == null || this.mStorageManager == null || this.mLocationServiceManager == null || info == null) {
            this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
            restoreRecorderToIdleOnUIThread();
            return;
        }
        info.mPurpose = 2;
        VideoRecorder.setMaxDuration(this.mLimitRecordingDuration);
        VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(info.mStorageType), info.mStorageType);
        VideoRecorder.setFileName(info.mFileName);
        VideoRecorder.setFilePath(info.mOutFilePath);
        VideoRecorder.setOrientationHint(getVideoOrientation());
        Camera camera = null;
        if (FunctionProperties.getSupportedHal() != 2) {
            camera = (Camera) this.mCameraDevice.getCamera();
        }
        VideoRecorder.init(camera, this.mCameraId, info.mVideoSize, this.mLocationServiceManager.getCurrentLocation(), info.mPurpose, 30.0d, POPOUT_VIDEO_BIT_RATE, getRecordingType(), false, info.mVideoFlipType);
        VideoRecorder.resetWaitStartRecoding();
        if (VideoRecorder.isInitialized()) {
            Surface surface = VideoRecorder.getSurface(false, 0);
            int[] videoSize = Utils.sizeStringToArray(info.mVideoSize);
            prepareReordingOnEngine(surface, videoSize[0], videoSize[1]);
            MediaRecorderListener listener = new MediaRecorderListener();
            VideoRecorder.setInfoListener(listener);
            VideoRecorder.setErrorListener(listener);
            IntentBroadcastUtil.blockAlarmInRecording(this.mGet.getActivity(), true);
            if (VideoRecorder.start()) {
                startRecordingOnEngine(false);
                if (AudioUtil.isWiredHeadsetHasMicOn()) {
                    showHeadsetRecordingToastPopup();
                }
                AudioUtil.setAllSoundCaseMute(getAppContext(), true);
                updateDeviceParameter();
                setCameraState(6);
                checkThemperatureOnRecording(true);
                this.mOrientationAtStartRecording = getActivity().getResources().getConfiguration().orientation;
                startHeatingWarning(true);
                this.mStartRecorderInfo = null;
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (PopoutModule.this.mRecordingUIManager != null) {
                            PopoutModule.this.mRecordingUIManager.initLayout();
                            PopoutModule.this.access$600();
                            PopoutModule.this.mRecordingUIManager.updateVideoTime(1, SystemClock.uptimeMillis());
                            PopoutModule.this.mRecordingUIManager.updateRecStatusIcon();
                            PopoutModule.this.mRecordingUIManager.show(PopoutModule.this.access$900());
                            PopoutModule.this.access$1100();
                            PopoutModule.this.keepScreenOn();
                        }
                    }
                });
                return;
            }
            restoreRecorderToIdleOnUIThread();
            this.mToastManager.showShortToast(getActivity().getString(isMiracastState() ? C0088R.string.error_video_recording_during_miracast : C0088R.string.error_occurred));
            return;
        }
        this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
        restoreRecorderToIdleOnUIThread();
    }

    protected void afterStopRecording() {
        if (TelephonyUtil.phoneInVTCall(getAppContext())) {
            CamLog.m7i(CameraConstants.TAG, "Return afterStoprecording because of VT call state");
        } else if (this.mCameraDevice != null && CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().stopPreview();
            super.stopPreview();
            int settingIndex = this.mGet.getSettingIndex(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            this.mNormalCameraPreviewSize = this.mNormalPreviewSizeList[settingIndex];
            this.mWideCameraPreviewSize = this.mWidePreviewSizeList[settingIndex];
            releasePopoutEngine();
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    PopoutModule.this.mPopoutSurfaceView.setVisibility(8);
                }
            });
            this.mHandler.sendEmptyMessage(86);
            if (this.mCheeseShutterManager != null) {
                this.mCheeseShutterManager.setCheeseShutterSetting("on".equals(this.mGet.getCurSettingValue(Setting.KEY_VOICESHUTTER)), true);
            }
            this.mHandler.sendEmptyMessage(9);
            this.mHandler.sendEmptyMessage(5);
            this.mSnapShotChecker.releaseSnapShotChecker();
            doCleanViewAfterStopRecording();
            showMenuButton(true);
            if (this.mPopoutCameraManager != null) {
                this.mPopoutCameraManager.setEffectButtonVisibility(true);
                this.mPopoutCameraManager.setFrameVisibility(true);
            }
            access$3700();
            checkThemperatureOnRecording(false);
            setCameraState(1);
            access$900(true);
        }
    }

    public boolean onVideoShutterClickedBefore() {
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        setPopoutLayoutVisibility(false);
        return true;
    }

    protected void setContentFrameLayoutParam(LayoutParams params) {
        int i = 0;
        super.setContentFrameLayoutParam(params);
        if (params != null) {
            if (this.mPopoutCameraManager != null) {
                this.mPopoutCameraManager.shiftEffectButtonLayout(params.height, params.topMargin);
            }
            float ratio = ((float) params.height) / ((float) params.width);
            this.mShiftUpTouchArea = 0;
            if (ratio > 1.9f) {
                this.mPreviewRatio = 3;
            } else if (ratio > 1.3f) {
                this.mShiftUpTouchArea = ((Utils.getLCDsize(getAppContext(), true)[0] - params.height) - (params.topMargin * 2)) / 2;
                if (ratio <= 1.7f) {
                    i = 1;
                }
                this.mPreviewRatio = i;
            } else {
                this.mPreviewRatio = 2;
            }
        }
    }

    protected void onChangePictureSize() {
        super.onChangePictureSize();
        if (ModelProperties.isMTKChipset()) {
            this.mGet.setPreviewCoverVisibility(0, false);
            startPreview(null);
        } else {
            refreshCameraParamForEngine();
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (PopoutModule.this.mGet.getPreviewCoverVisibility() == 0) {
                        CamLog.m3d(CameraConstants.TAG, "Preview cover is displaying, so remove it");
                        PopoutModule.this.mGet.setPreviewCoverVisibility(8, false);
                    }
                }
            }, 500);
        }
        this.mPopoutCameraManager.onChangePictureSize();
    }

    protected void prepareReordingOnEngine(Surface surface, int width, int height) {
    }

    protected void startRecordingOnEngine(boolean isFixedWidePreview) {
    }

    protected void refreshCameraParamForEngine() {
    }

    protected void onSurfaceChanged(SurfaceHolder holder, int width, int height) {
    }

    protected boolean onSurfaceDestroyed(SurfaceHolder holder) {
        return true;
    }

    protected void onPopoutPictureTaken(Bitmap mPreviewBitmap_origin) {
        CamLog.m7i(CameraConstants.TAG, "[popout] onSendStillImage");
        Bitmap mPreviewBitmap = mPreviewBitmap_origin;
        if (isSignatureEnableCondition()) {
            mPreviewBitmap = this.mGet.composeSignatureImage(mPreviewBitmap, Exif.getOrientation(this.mExif));
        }
        ByteArrayOutputStream mPreviewStream = new ByteArrayOutputStream();
        if (mPreviewBitmap != null) {
            DebugUtil.setStartTime("[1] Popout image: jpeg compression");
            mPreviewBitmap.compress(CompressFormat.JPEG, 100, mPreviewStream);
            DebugUtil.setEndTime("[1] Popout image: jpeg compression");
            access$300(mPreviewStream.toByteArray(), null);
            CamLog.m3d(CameraConstants.TAG, "[popout] - jpeg compression - DONE");
            try {
                mPreviewStream.close();
            } catch (Exception e) {
                Log.e(CameraConstants.TAG, "[popout] - mPreviewStream.close() failed");
            }
            mPreviewBitmap.recycle();
        }
        this.mIsSavingPicture = false;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                PopoutModule.this.onTakePictureAfter();
                if (!PopoutModule.this.mSnapShotChecker.checkMultiShotState(4)) {
                    if (PopoutModule.this.mFocusManager != null && PopoutModule.this.isFocusEnableCondition()) {
                        PopoutModule.this.mFocusManager.registerCallback(true);
                    }
                    if (!"on".equals(PopoutModule.this.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                        AudioUtil.setAudioFocus(PopoutModule.this.getAppContext(), false);
                    }
                }
                PopoutModule.this.access$1700();
                PopoutModule.this.mCaptureDataSyncThread = null;
                if (PopoutModule.this.mQuickClipManager != null && !PopoutModule.this.mIsShutterlessSelfieProgress) {
                    PopoutModule.this.mQuickClipManager.setAfterShot();
                }
            }
        });
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (TelephonyUtil.phoneInVTCall(getAppContext())) {
            if (getCameraState() == 6 || getCameraState() == 7) {
                CamLog.m7i(CameraConstants.TAG, "Receive the VT Call while recording a video");
                onVideoStopClicked(false, false);
            }
            stopPreview();
            closeCamera();
        }
    }

    protected int getLoopRecordingType() {
        return 0;
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public boolean isSupportedFilterMenu() {
        return false;
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }
}
