package com.lge.camera.app.ext;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.ScaleGestureDetector;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule.MediaRecorderListener;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.components.StartRecorderInfo;
import com.lge.camera.components.TextureScaleListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.CameraSecondHolder;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraShutterCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.file.MediaSaveService.OnLocalSaveListener;
import com.lge.camera.file.SupportedExif;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.IntentBroadcastUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.Utils;
import java.io.IOException;

public class MultiGLViewExpand extends MultiGLView {
    protected final int OUT_OF_SCREEN = 10000;
    protected PictureCallback mCroppedPictureCallbackFront = new C03953();
    private int mHalfLCDDistance = 0;
    protected boolean mIsSingleViewScreenRecording;
    private int mLcdHeight = 0;
    private int mLcdWidth = 0;
    protected CameraPictureCallback mPictureCallbackFront = new C03942();
    private ScaleGestureDetector mScaleDetector = null;
    protected CameraShutterCallback mShutterCallbackFront = new C03931();

    /* renamed from: com.lge.camera.app.ext.MultiGLViewExpand$1 */
    class C03931 implements CameraShutterCallback {
        C03931() {
        }

        public void onShutter(CameraProxy camera) {
            MultiGLViewExpand.this.access$300(true, true, false);
        }
    }

    /* renamed from: com.lge.camera.app.ext.MultiGLViewExpand$2 */
    class C03942 extends CameraPictureCallback {
        C03942() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            MultiGLViewExpand.this.mPictureCallback.onPictureTaken(data, null, null);
            CameraSecondHolder.subinstance().stopPreview();
            CameraSecondHolder.subinstance().startPreview();
        }
    }

    /* renamed from: com.lge.camera.app.ext.MultiGLViewExpand$3 */
    class C03953 implements PictureCallback {
        C03953() {
        }

        public void onPictureTaken(byte[] data, Camera arg1) {
            MultiGLViewExpand.this.mCroppedPictureCallback.onPictureTaken(data, null, null);
            CameraSecondHolder.subinstance().stopPreview();
            CameraSecondHolder.subinstance().startPreview();
        }
    }

    public MultiGLViewExpand(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeBefore() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - onResumeBefore");
        calculateLcdSize();
        this.mScaleDetector = new ScaleGestureDetector(this.mGet.getAppContext(), new TextureScaleListener(this.mHalfLCDDistance));
        if (this.mScaleDetector == null) {
            CamLog.m3d(CameraConstants.TAG, "mScaleDetector is null");
        }
        super.onResumeBefore();
    }

    public void onResumeAfter() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - onResumeAfter");
        super.onResumeAfter();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        CamLog.m3d(CameraConstants.TAG, "MultiView - onPauseBefore");
        if (this.mRenderThread != null) {
            if (!(getMVState() == 4 || getMVState() == 8)) {
                this.mRenderThread.stopThread();
            }
            if (getMVState() == 4 || getMVState() == 8) {
                this.mRenderThread.setRequiredToFinishAfterSaving(true);
            }
        }
        if (this.mTextureViewMV != null) {
            this.mTextureViewMV.setTranslationY(10000.0f);
            this.mTextureViewMV.setVisibility(8);
            CamLog.m3d(CameraConstants.TAG, "mTextureViewMV gone");
        }
    }

    public void onPauseAfter() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - onPauseAfter");
        super.onPauseAfter();
        if (!(getMVState() == 4 || getMVState() == 8)) {
            release();
            setMVState(1);
        }
        this.mSTLatch = null;
        this.mCollageSTLatch = null;
    }

    private void calculateLcdSize() {
        DisplayMetrics outMetrics = Utils.getWindowRealMatics(this.mGet.getActivity());
        this.mLcdWidth = outMetrics.widthPixels;
        this.mLcdHeight = outMetrics.heightPixels;
        this.mHalfLCDDistance = (this.mLcdWidth + this.mLcdHeight) / 4;
        CamLog.m3d(CameraConstants.TAG, "calculateLcdSize = " + this.mLcdWidth + "x" + this.mLcdHeight);
    }

    protected void doTakePicture() {
        CamLog.m3d(CameraConstants.TAG, "doTakePicture");
        if (checkModuleValidate(223)) {
            CamLog.m3d(CameraConstants.TAG, "MultiView takePicture isTouchShot = " + AppControlUtil.isTouchShotOnMultiView());
            this.mSnapShotChecker.removeBurstState(1);
            this.mSnapShotChecker.setSnapShotState(3);
            if (!this.mSnapShotChecker.checkMultiShotState(4)) {
                setParameterBeforeTakePicture(updateDeviceParameter(), this.mSnapShotChecker.checkMultiShotState(4), true);
            }
            if (this.mSnapShotChecker.checkMultiShotState(4) && this.mSnapShotChecker.isBurstCaptureStarted() && this.mReviewThumbnailManager != null) {
                updateBurstCount(false, false);
                this.mReviewThumbnailManager.startBurstCaptureEffect(true);
                return;
            }
            return;
        }
        this.mSnapShotChecker.setSnapShotState(0);
    }

    protected void afterStopRecording() {
        CamLog.m3d(CameraConstants.TAG, "-rec- afterStopRecording");
        if (this.mIsStoppingRecord) {
            if (isPaused()) {
                setMVState(1);
            } else if ((getMVState() & 32) != 0) {
                setMVState(getMVState() | 2);
            } else {
                setMVState(2);
            }
            if (CameraSecondHolder.isSecondCameraOpened() && isMultiviewFrameShot() && !this.mIsSingleViewScreenRecording) {
                CameraSecondHolder.subinstance().getCameraDevice().setRecordSurfaceToTarget(false);
            }
            super.afterStopRecording();
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "abnormal recording stop!!! return");
    }

    protected void takePictureMV() {
        this.mSnapShotChecker.setSnapShotState(3);
        if (AppControlUtil.isTouchShotOnMultiView()) {
            if (this.mTouchedView == 0) {
                this.mCameraDevice.takePicture(this.mHandler, this.mShutterCallback, this.mRAWPictureCallback, null, this.mPictureCallback);
            } else if (this.mTouchedView == 1) {
                CameraSecondHolder.subinstance().takePicture(this.mHolderHandler, this.mShutterCallbackFront, null, null, this.mPictureCallbackFront);
            } else {
                CameraSecondHolder.subinstance().takePicture(this.mHolderHandler, this.mShutterCallbackFront, null, null, this.mPictureCallbackFront);
            }
            AppControlUtil.setTouchShotOnMultiView(false);
            return;
        }
        takePictureInternal();
    }

    protected void takePictureSingle(CameraParameters param) {
        CamLog.m3d(CameraConstants.TAG, "takePictureSingle");
        if (param != null) {
            this.mCameraDevice.setNightandHDRorAuto(param, "mode_normal", false);
        }
        if (this.mCameraDevice != null) {
            this.mSnapShotChecker.setSnapShotState(3);
            this.mCameraDevice.takePicture(this.mHandler, this.mShutterCallback, this.mRAWPictureCallback, null, this.mPictureCallback);
        }
    }

    protected void takeFrameShotPicture() {
    }

    protected void takePictureInternal() {
        CamLog.m3d(CameraConstants.TAG, "MultiView - takePictureInternal");
        this.mRenderThread.takeGLView();
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                MultiGLViewExpand.this.access$300(true, true, false);
            }
        });
    }

    protected void stopRecorder() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- stopRecorder");
        if (isPaused()) {
            CamLog.m3d(CameraConstants.TAG, "-th- stopRecorder in onPause");
            this.mIsStoppingRecord = false;
        } else {
            this.mIsStoppingRecord = true;
        }
        if (this.mRenderThread != null) {
            this.mRenderThread.stopRecorder();
        }
        if ("on".equals(getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT))) {
            stopRecorder(false);
        } else {
            stopRecorder(true);
        }
        AudioUtil.setUseBuiltInMicForRecording(getAppContext(), false);
        DebugUtil.setStartTime("[+] MV: Final FreezePreview is Done");
    }

    public void sendLDBIntentOnAfterStopRecording() {
        if (CameraConstants.MODE_SNAP.equals(getShotMode()) || !"on".equals(getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT)) || getMVState() == 4) {
            super.access$3700();
        }
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        String multiViewIntervalType = LdbConstants.LDB_MULTIVIEW_INTERVAL_MODE_SINGLE;
        if (LdbUtil.isMultiViewIntervalMode()) {
            multiViewIntervalType = LdbConstants.LDB_MULTIVIEW_INTERVAL_MODE_INTERVAL;
            LdbUtil.setMultiViewIntervalMode(false);
        }
        extraStr = extraStr + "multiViewIntervalType=" + multiViewIntervalType + ";";
        String multiViewSequentialType = LdbConstants.LDB_MULTIVIEW_SEQUENTIAL_MODE_SEQUENTIAL;
        if (!isMultiviewFrameShot()) {
            multiViewSequentialType = LdbConstants.LDB_MULTIVIEW_SEQUENTIAL_MODE_SIMULTANEOUS;
        }
        extraStr = extraStr + "multiViewSequentialType=" + multiViewSequentialType + ";";
        return extraStr + "multiViewLayout=" + getMultiviewLayout() + ";";
    }

    public String getMultiviewLayout() {
        String layout = "";
        switch (getLayoutIndex()) {
            case 0:
                return LdbConstants.LDB_MULTIVIEW_LAYOUT_SINGLE_TXT;
            case 1:
                return LdbConstants.LDB_MULTIVIEW_LAYOUT_SPLIT_TXT;
            case 2:
                return LdbConstants.LDB_MULTIVIEW_LAYOUT_TRIPLE1_TXT;
            case 3:
                return LdbConstants.LDB_MULTIVIEW_LAYOUT_TRIPLE2_TXT;
            case 4:
                return LdbConstants.LDB_MULTIVIEW_LAYOUT_QUAD_TXT;
            default:
                return layout;
        }
    }

    protected Uri saveImageInBackground(byte[] data, byte[] extraExif, boolean useDB, boolean needCrop) {
        Uri uri = null;
        String dir = getCurDir();
        long curTime = System.currentTimeMillis();
        String fileName = makeFileName(0, getCurStorage(), dir, false, getSettingValue(Setting.KEY_MODE)) + ".jpg";
        FileNamer.get().addFileNameInSaving(dir + fileName);
        SupportedExif supportedExif = new SupportedExif(this.mCameraCapabilities.isFocusAreaSupported(), this.mCameraCapabilities.isFlashSupported(), this.mCameraCapabilities.isWBSupported(), this.mCameraCapabilities.isMeteringAreaSupported(), this.mCameraCapabilities.isZoomSupported(), true, true, true);
        byte[] convertJpeg = data;
        if (this.mCameraDevice != null || this.mGet.isPaused()) {
            CameraParameters parameters;
            if (this.mCameraDevice != null) {
                parameters = this.mCameraDevice.getParameters();
                if (this.mCameraCapabilities.isFlashSupported()) {
                    parameters.setFlashMode("off");
                }
            } else {
                parameters = null;
            }
            String picSize = getCurPictureSize();
            CamLog.m3d(CameraConstants.TAG, "picSize = " + picSize);
            int[] pictureSize = Utils.sizeStringToArray(picSize);
            ExifInterface exif = Exif.createExif(convertJpeg, pictureSize[0], pictureSize[1], parameters, this.mLocationServiceManager.getCurrentLocation(), this.mCameraDegree, -1, supportedExif, CameraConstants.MODE_MULTIVIEW.equals(getShotMode()) ? (short) 11 : (short) 14);
            updateThumbnail(exif, this.mCameraDegree, false);
            if (!saveThumb(data, extraExif, convertJpeg, dir, fileName, exif, pictureSize)) {
                return null;
            }
            if (needCrop) {
                BitmapManagingUtil.saveCroppedImage(dir, fileName, exif);
            }
            if (useDB) {
                uri = FileManager.registerImageUri(this.mGet.getAppContext().getContentResolver(), dir, fileName, curTime, this.mLocationServiceManager.getCurrentLocation(), this.mCameraDegree, Exif.getExifSize(exif), false);
            }
            FileNamer.get().removeFileNameInSaving(dir + fileName);
            CamLog.m3d(CameraConstants.TAG, "Jpeg uri = " + uri);
            this.mGet.onNewItemAdded(uri, 5, null);
            checkStorage();
            return uri;
        }
        CamLog.m11w(CameraConstants.TAG, "mCameraDevice is null, so return");
        return null;
    }

    private boolean saveThumb(byte[] data, byte[] extraExif, byte[] convertJpeg, String dir, String fileName, ExifInterface exif, int[] pictureSize) {
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
                            CamLog.m3d(CameraConstants.TAG, "re compress thumbnail - quality: " + quality);
                            Exif.updateThumbnail(exif, convertJpeg, pictureSize[0], pictureSize[1], quality);
                            break;
                        case 2:
                            CamLog.m3d(CameraConstants.TAG, "remove thumbnail");
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

    protected int getSaveImageDegree() {
        return CameraDeviceUtils.getJpegOrientation(this.mGet.getActivity(), Utils.restoreDegree(this.mGet.getAppContext().getResources(), getOrientationDegree()), this.mCameraId);
    }

    public boolean isZoomAvailable() {
        return false;
    }

    public boolean isZoomAvailable(boolean checkRecordingState) {
        return false;
    }

    protected void setLayoutForRecording(int viewType) {
        if (this.mRenderThread != null && this.mRenderThread.mMultiViewRecorder != null) {
            this.mRenderThread.mMultiViewRecorder.updateLayoutForRecording(viewType);
        }
    }

    public int getVideoOrientation() {
        return CameraDeviceUtils.getMultiviewOrientationHint((getDisplayOrientation() + getOrientationDegree()) % 360);
    }

    protected void doRunnableStartRecorder(StartRecorderInfo info) {
        CamLog.m3d(CameraConstants.TAG, "-rec- doRunnableStartRecorder");
        setLayoutForRecording(getLayoutIndex());
        startGLTextureRecording(info);
    }

    protected void startGLTextureRecording(StartRecorderInfo info) {
        if (!checkModuleValidate(15) || this.mCameraDevice == null || this.mRecordingUIManager == null || this.mAudioZoomManager == null || this.mStorageManager == null || this.mLocationServiceManager == null || info == null) {
            this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
            restoreRecorderToIdleOnUIThread();
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-rec- startGLTextureRecording");
        setMicPath();
        setupVideoRecorderForScreenRec(info);
        this.mRenderThread.setInputRecordSurface(VideoRecorder.getSurface(true, 0));
        if (VideoRecorder.isInitialized()) {
            MediaRecorderListener listener = new MediaRecorderListener();
            VideoRecorder.setInfoListener(listener);
            VideoRecorder.setErrorListener(listener);
            IntentBroadcastUtil.blockAlarmInRecording(this.mGet.getActivity(), true);
            if (VideoRecorder.start()) {
                if (AudioUtil.isWiredHeadsetHasMicOn()) {
                    showHeadsetRecordingToastPopup();
                }
                AudioUtil.setAllSoundCaseMute(getAppContext(), true);
                setCameraState(6);
                setRecordingUILocation();
                updateRecordingTime();
                this.mOrientationAtStartRecording = getActivity().getResources().getConfiguration().orientation;
                startHeatingWarning(true);
                this.mStartRecorderInfo = null;
                this.mRenderThread.startRecorder(this.mIsSingleViewScreenRecording);
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (MultiGLViewExpand.this.mRecordingUIManager != null && MultiGLViewExpand.this.mCaptureButtonManager != null && !MultiGLViewExpand.this.mGet.isPaused() && MultiGLViewExpand.this.mCameraState == 6) {
                            MultiGLViewExpand.this.mRecordingUIManager.updateVideoTime(1, SystemClock.uptimeMillis());
                            if (CameraConstants.MODE_MULTIVIEW.equals(MultiGLViewExpand.this.getShotMode()) || CameraConstants.MODE_SQUARE_SPLICE.equals(MultiGLViewExpand.this.getShotMode())) {
                                if (MultiGLViewExpand.this.isMultiviewFrameShot()) {
                                    MultiGLViewExpand.this.mRecordingUIManager.setRec3sec(true);
                                } else {
                                    MultiGLViewExpand.this.mRecordingUIManager.setRec3sec(false);
                                    MultiGLViewExpand.this.mCaptureButtonManager.changeButtonByMode(2);
                                    MultiGLViewExpand.this.mCaptureButtonManager.changeExtraButton(0, 1);
                                    MultiGLViewExpand.this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
                                }
                            }
                            MultiGLViewExpand.this.mRecordingUIManager.initLayout();
                            MultiGLViewExpand.this.mRecordingUIManager.updateRecStatusIcon();
                            MultiGLViewExpand.this.mRecordingUIManager.show(MultiGLViewExpand.this.access$1600());
                            MultiGLViewExpand.this.keepScreenOn();
                        }
                    }
                });
                CamLog.m3d(CameraConstants.TAG, "-rec-  mIsRecordStarted = true");
                return;
            }
            restoreRecorderToIdle();
            this.mRecordingUIManager.hide();
            this.mToastManager.showShortToast(getActivity().getString(isMiracastState() ? C0088R.string.error_video_recording_during_miracast : C0088R.string.error_occurred));
            return;
        }
        this.mToastManager.showShortToast(getActivity().getString(C0088R.string.error_write_file));
        restoreRecorderToIdle();
    }

    protected void setRecordingUILocation() {
    }

    private void setupVideoRecorderForScreenRec(StartRecorderInfo info) {
        VideoRecorder.setMaxDuration(this.mLimitRecordingDuration);
        VideoRecorder.setMaxFileSize(this.mLimitRecordingSize, this.mStorageManager.getFreeSpace(info.mStorageType), info.mStorageType);
        VideoRecorder.setFileName(info.mFileName);
        VideoRecorder.setFilePath(info.mOutFilePath);
        VideoRecorder.setOrientationHint(getVideoOrientation());
        Location location = this.mLocationServiceManager != null ? this.mLocationServiceManager.getCurrentLocation() : null;
        Camera camera = null;
        if (FunctionProperties.getSupportedHal() != 2) {
            camera = (Camera) this.mCameraDevice.getCamera();
        }
        VideoRecorder.init(camera, this.mCameraId, info.mVideoSize, location, 2, info.mVideoFps, 0, getRecordingType(), false, info.mVideoFlipType);
    }

    public boolean onShutterBottomButtonLongClickListener() {
        CamLog.m3d(CameraConstants.TAG, "burst shot is not supported on multiview");
        return true;
    }

    protected void saveImage(byte[] data, byte[] extraExif) {
        saveImage(data, extraExif, false);
    }

    protected void saveImage(byte[] data, byte[] extraExif, boolean needCrop) {
        saveImage(data, extraExif, true, needCrop);
    }

    protected void saveImage(byte[] data, byte[] extraExif, boolean useDB, boolean needCrop) {
        if (this.mCameraDevice == null && !this.mGet.isPaused()) {
            CamLog.m11w(CameraConstants.TAG, "MultiView - mCameraDevice is null, so return");
        } else if (this.mGet.getMediaSaveService() == null || !this.mGet.getMediaSaveService().isQueueFull()) {
            String dir = getCurDir();
            final byte[] bArr = data;
            final byte[] bArr2 = extraExif;
            final boolean z = useDB;
            final boolean z2 = needCrop;
            this.mGet.getMediaSaveService().processLocal(new OnLocalSaveListener() {
                public void onPreExecute() {
                }

                public void onPostExecute(Uri uri) {
                    if (uri != null) {
                        MultiGLViewExpand.this.mGet.requestNotifyNewMediaonActivity(uri, MultiGLViewExpand.this.checkModuleValidate(128));
                    }
                }

                public Uri onLocalSave(String dir, String fileName) {
                    return MultiGLViewExpand.this.saveImageInBackground(bArr, bArr2, z, z2);
                }
            }, dir, getFileName(dir));
        }
    }

    protected void changeLayout() {
        this.mMultiviewFrame.changeLayout();
    }

    protected void changeLayout(int viewMode) {
        this.mMultiviewFrame.changeLayout(viewMode, true);
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    public void setQuickButtonIndex(int id, int index) {
        if (id != C0088R.id.quick_button_multi_view_layout) {
            super.setQuickButtonIndex(id, index);
        }
    }

    protected void changeRequester() {
        if (this.mParamUpdater != null) {
            this.mParamUpdater.addRequester(ParamConstants.KEY_STEADY_CAM, "off", false, true);
            if (this.mUseDualRecorder) {
                this.mParamUpdater.setParamValue(ParamConstants.KEY_DUAL_RECORDER, "1");
            }
        }
    }

    protected String getCurPictureSize() {
        return this.MULTIVIEW_IMAGE_SIZE_FINAL;
    }

    public boolean checkPreviewCoverVisibilityForRecording() {
        return false;
    }
}
