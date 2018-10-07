package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
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
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.DebugUtil;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import com.lge.camera.util.XMPWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DualPopCameraModuleExpand extends DualPopCameraModule {
    protected DualpopCaptureDataSyncThread mCaptureDataSyncThread;
    byte[] mConvertWideData;
    protected CameraPictureCallback mPictureCallback2 = new C03664();

    /* renamed from: com.lge.camera.app.ext.DualPopCameraModuleExpand$3 */
    class C03653 implements CameraPreviewDataCallback {
        C03653() {
        }

        public void onPreviewFrame(byte[] data, CameraProxy camera) {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] onPreviewFrame for wide camera");
            DualPopCameraModuleExpand dualPopCameraModuleExpand = DualPopCameraModuleExpand.this;
            dualPopCameraModuleExpand.mIsAllPreviewAvailable |= 2;
            DualPopCameraModuleExpand.this.access$900(true);
        }
    }

    /* renamed from: com.lge.camera.app.ext.DualPopCameraModuleExpand$4 */
    class C03664 extends CameraPictureCallback {
        C03664() {
        }

        public void onPictureTaken(byte[] data, byte[] extraExif, CameraProxy camera) {
            DualPopCameraModuleExpand.this.onPictureTakenCallback2(data, camera);
        }
    }

    /* renamed from: com.lge.camera.app.ext.DualPopCameraModuleExpand$7 */
    class C03697 extends Thread {
        C03697() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Wait saving picture");
            int waitCnt = 0;
            while (DualPopCameraModuleExpand.this.mIsSavingPicture && waitCnt <= 50) {
                try {
                    C03697.sleep(100);
                    waitCnt++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class DualpopCaptureDataSyncThread extends Thread {
        protected DualpopCaptureDataSyncThread() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] DualpopCaptureDataSyncThread - start");
            int watingCount = 0;
            while (true) {
                try {
                    if (DualPopCameraModuleExpand.this.mIsNormalCaptured && DualPopCameraModuleExpand.this.mIsWideCaptured) {
                        DualPopCameraModuleExpand.this.mIsSavingPicture = true;
                        DualPopCameraModuleExpand.this.mIsNormalCaptured = false;
                        DualPopCameraModuleExpand.this.mIsWideCaptured = false;
                        DualPopCameraModuleExpand.this.onDualPoptPictureTaken();
                        return;
                    } else if (watingCount > 70) {
                        CamLog.m11w(CameraConstants.TAG, "[dualpop] Save fail");
                        return;
                    } else if (!DualPopCameraModuleExpand.this.mGet.isPaused()) {
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

    public DualPopCameraModuleExpand(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void onDualPoptPictureTaken() {
        if (isSignatureEnableCondition()) {
            this.mNormalViewCaptureData = this.mGet.composeSignatureImage(this.mNormalViewCaptureData, Exif.getOrientation(this.mExif));
            this.mWideViewCaptureData = this.mGet.composeSignatureImage(this.mWideViewCaptureData, Exif.getOrientation(this.mExif));
        }
        Bitmap bmWide = BitmapFactory.decodeByteArray(this.mWideViewCaptureData, 0, this.mWideViewCaptureData.length);
        this.mConvertWideData = getBytesFromBitmap(bmWide);
        saveImage(null, null);
        bmWide.recycle();
        this.mIsSavingPicture = false;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                DualPopCameraModuleExpand.this.access$100();
                if (!DualPopCameraModuleExpand.this.mSnapShotChecker.checkMultiShotState(4)) {
                    if (DualPopCameraModuleExpand.this.mFocusManager != null && DualPopCameraModuleExpand.this.isFocusEnableCondition()) {
                        DualPopCameraModuleExpand.this.mFocusManager.registerCallback(true);
                    }
                    if (!"on".equals(DualPopCameraModuleExpand.this.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                        AudioUtil.setAudioFocus(DualPopCameraModuleExpand.this.getAppContext(), false);
                    }
                }
                DualPopCameraModuleExpand.this.access$500();
                DualPopCameraModuleExpand.this.mCaptureDataSyncThread = null;
                if (DualPopCameraModuleExpand.this.mQuickClipManager != null && !DualPopCameraModuleExpand.this.mIsShutterlessSelfieProgress) {
                    DualPopCameraModuleExpand.this.mQuickClipManager.setAfterShot();
                }
            }
        });
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        try {
            stream.close();
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "[dualpop] - stream.close() failed");
        }
        return data;
    }

    protected Uri doSaveImage(byte[] data, byte[] extraExif, String dir, String filename) {
        Exception e;
        Throwable th;
        String jpgFileName = filename + ".jpg";
        String tempNormalDir = "/data/user/" + UserHandle.myUserId() + "/com.lge.camera/";
        String tempNormalFileName = "temp.jpg";
        FileNamer.get().addFileNameInSaving(dir + jpgFileName);
        ExifInterface exif = this.mExif;
        int[] pictureSize = Utils.sizeStringToArray(this.mPictureSize);
        Exif.updateThumbnail(exif, this.mNormalViewCaptureData, pictureSize[0], pictureSize[1], 70);
        Exif.setSceneCaptureType(exif, (short) 22);
        int exifDegree = Exif.getOrientation(exif);
        updateThumbnail(exif, exifDegree, false);
        if (!saveThumb(this.mNormalViewCaptureData, extraExif, tempNormalDir, tempNormalFileName, exif, pictureSize)) {
            return null;
        }
        File file = new File(tempNormalDir + tempNormalFileName);
        byte[] normalData = new byte[((int) file.length())];
        Closeable fis = null;
        try {
            Closeable fileInputStream = new FileInputStream(file);
            try {
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                dataInputStream.readFully(normalData);
                dataInputStream.close();
                FileUtil.closeSilently(fileInputStream);
                Object xmpNomalData = XMPWriter.insertDualPopXMP(normalData, (long) this.mConvertWideData.length);
                Object mergedDate = new byte[(xmpNomalData.length + this.mConvertWideData.length)];
                System.arraycopy(xmpNomalData, 0, mergedDate, 0, xmpNomalData.length);
                System.arraycopy(this.mConvertWideData, 0, mergedDate, xmpNomalData.length, this.mConvertWideData.length);
                try {
                    FileManager.writeFile(mergedDate, dir, jpgFileName);
                    String str = dir;
                    Uri uri = FileManager.registerImageUri(this.mGet.getAppContext().getContentResolver(), str, jpgFileName, System.currentTimeMillis(), this.mLocationServiceManager.getCurrentLocation(), exifDegree, Exif.getExifSize(exif), false);
                    checkSavedURI(uri);
                    FileNamer.get().removeFileNameInSaving(dir + jpgFileName);
                    this.mGet.onNewItemAdded(uri, 203, null);
                    FileManager.deleteFile(tempNormalDir + tempNormalFileName);
                    CamLog.m3d(CameraConstants.TAG, "[dualpop] Jpeg uri = " + uri);
                    checkStorage();
                    return uri;
                } catch (Exception e2) {
                    CamLog.m3d(CameraConstants.TAG, "[dualpop] exception " + e2);
                    FileManager.deleteFile(tempNormalDir + tempNormalFileName);
                    return null;
                }
            } catch (Exception e3) {
                e2 = e3;
                fis = fileInputStream;
                try {
                    CamLog.m3d(CameraConstants.TAG, "[dualpop] exception " + e2);
                    FileUtil.closeSilently(fis);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    FileUtil.closeSilently(fis);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fileInputStream;
                FileUtil.closeSilently(fis);
                throw th;
            }
        } catch (Exception e4) {
            e2 = e4;
            CamLog.m3d(CameraConstants.TAG, "[dualpop] exception " + e2);
            FileUtil.closeSilently(fis);
            return null;
        }
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
                            Exif.updateThumbnail(exif, data, pictureSize[0], pictureSize[1], quality);
                            CamLog.m3d(CameraConstants.TAG, "[dualpop] re compress thumbnail - quality: " + quality);
                            break;
                        case 2:
                            exif.removeCompressedThumbnail();
                            CamLog.m3d(CameraConstants.TAG, "[dualpop] remove thumbnail");
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

    protected void startPreviewForNormalCamera(CameraParameters parameters, boolean stopPreview) {
        if (this.mCameraDevice != null) {
            CamLog.m7i(CameraConstants.TAG, "[dualpop] startPreviewForNormalCamera");
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
                Log.i(CameraConstants.TAG, "[Time Info][4][dualpop]  App Param setting End : Camera Parameter setting " + DebugUtil.interimCheckTime(true) + " ms");
                Log.i(CameraConstants.TAG, "[Time Info][5][dualpop]  Device Param setting Start : Device setting " + DebugUtil.interimCheckTime(false));
                this.mCameraDevice.setParameters(param);
                Log.i(CameraConstants.TAG, "[Time Info][5][dualpop]  Device Param setting End : Device setting " + DebugUtil.interimCheckTime(true) + " ms");
                CamLog.m3d(CameraConstants.TAG, "[dualpop] camera 0 - paramPictureSize = " + param.get("picture-size") + ", paramPreviewSize = " + param.get(ParamConstants.KEY_PREVIEW_SIZE));
            }
            this.mCameraDevice.setPreviewDisplay(this.mGet.getSurfaceHolder());
            setDisplayOrientation(false);
            setOneShotPreviewCallback();
            Log.i(CameraConstants.TAG, "[Time Info][6][dualpop]  Device StartPreview Start : Driver Preview Operation " + DebugUtil.interimCheckTime(false));
            this.mCameraDevice.startPreview();
            setZoomCompensation(param);
        }
    }

    protected void startPreviewForWideCamera(CameraParameters parameters, boolean stopPreview) {
        if (CameraSecondHolder.subinstance() != null && !isPaused() && this.mSecondSurfaceView != null) {
            CamLog.m7i(CameraConstants.TAG, "[dualpop] startPreviewForWideCamera");
            if (stopPreview) {
                CameraSecondHolder.subinstance().stopPreview();
            }
            CameraParameters param = CameraSecondHolder.subinstance().getParameters();
            if (param == null) {
                CamLog.m7i(CameraConstants.TAG, "[dualpop] wide camera param is null");
                return;
            }
            setParamForWideCamera(param);
            CamLog.m3d(CameraConstants.TAG, "[dualpop] Wide camera setParameters");
            CameraSecondHolder.subinstance().setParameters(param);
            CameraSecondHolder.subinstance().setPreviewDisplay(this.mSecondSurfaceView.getHolder());
            setOneShotPreviewCallback2();
            CamLog.m3d(CameraConstants.TAG, "[dualpop] device1.startPreview");
            CameraSecondHolder.subinstance().startPreview();
        }
    }

    protected void oneShotPreviewCallbackDoneAfter() {
    }

    protected void startPreview(final CameraParameters params) {
        if (!isPaused()) {
            this.mIsAllPreviewAvailable = 0;
            boolean isStopPrevigew = ModelProperties.isMTKChipset();
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DualPopCameraModuleExpand.this.startPreviewForWideCamera(params, false);
                }
            }, 500);
            startPreviewForNormalCamera(params, isStopPrevigew);
        }
    }

    protected void startPreviewDone() {
        CamLog.m3d(CameraConstants.TAG, "[dualpop] startPreviewDone : start");
        if (checkModuleValidate(1)) {
            this.mGet.setPreviewVisibility(0);
            if (this.mFocusManager != null && isFocusEnableCondition()) {
                this.mFocusManager.registerCallback();
            }
            if (FunctionProperties.isSupportedSwitchingAnimation()) {
                ColorUtil.createRS(getAppContext());
            }
            CamLog.m3d(CameraConstants.TAG, "[dualpop] startPreviewDone : end");
        }
    }

    protected void stopPreview() {
        super.stopPreview();
        if (CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().stopPreview();
        }
    }

    protected void closeCamera() {
        CamLog.m3d(CameraConstants.TAG, "[dualpop] closeCamera");
        if (this.mSecondSurfaceView != null) {
            this.mSecondSurfaceView.setVisibility(8);
            this.mSecondSurfaceView = null;
        }
        closeCamera2();
        super.closeCamera();
    }

    protected void closeCamera2() {
        CamLog.m3d(CameraConstants.TAG, "[dualpop] closeCamera2");
        if (CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().setPreviewDisplay(null);
            CameraSecondHolder.subinstance().setErrorCallback(null, null);
            CameraSecondHolder.subinstance().release();
        }
    }

    protected void setParamForWideCamera(CameraParameters parameters) {
        parameters.set(ParamConstants.KEY_DUAL_RECORDER, 1);
        parameters.set(ParamConstants.KEY_LGE_CAMERA, 1);
        parameters.set("picture-size", this.mWidePictureSize);
        parameters.set(ParamConstants.KEY_PREVIEW_SIZE, this.mWideCameraPreviewSize);
        parameters.set("flash-mode", "off");
        parameters.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, MultimediaProperties.getCameraFPSRange(isRearCamera()));
        parameters.set(ParamConstants.KEY_ZSL, "off");
        parameters.set(ParamConstants.KEY_APP_OUTPUTS_TYPE, ParamConstants.OUTPUTS_PREVIEW_JPEG_STR);
        parameters.set(ParamConstants.KEY_APP_SHOT_MODE, ParamUtils.convertShotMode(getShotMode()));
    }

    protected void setOneShotPreviewCallback2() {
        if (CameraSecondHolder.subinstance() != null) {
            CameraSecondHolder.subinstance().setOneShotPreviewDataCallback(this.mHolderHandler, new C03653());
        }
    }

    protected void switchCamera() {
        if (checkModuleValidate(77)) {
            setPictureSizeListAndSettingMenu(false);
            if (this.mSecondSurfaceView != null) {
                int distance = Utils.getDefaultDisplayHeight(getActivity()) + 300;
                CamLog.m3d(CameraConstants.TAG, "move mSecondSurfaceView preview out of window");
                this.mSecondSurfaceView.setTranslationY((float) distance);
            }
            super.switchCamera();
        }
    }

    public boolean onCameraShutterButtonClicked() {
        if (this.mIsAllPreviewAvailable == 3) {
            return super.onCameraShutterButtonClicked();
        }
        CamLog.m3d(CameraConstants.TAG, "[dualpop] All preview is not available : " + this.mIsAllPreviewAvailable);
        return false;
    }

    protected void doTakePicture() {
        if (this.mCaptureDataSyncThread == null) {
            this.mCaptureDataSyncThread = new DualpopCaptureDataSyncThread();
            this.mCaptureDataSyncThread.start();
            CamLog.m3d(CameraConstants.TAG, "[dualpop] doTakePicture");
            if (CameraSecondHolder.subinstance() != null) {
                CamLog.m3d(CameraConstants.TAG, "[dualpop] doTakePicture - Wide camera");
                CameraSecondHolder.subinstance().takePicture(this.mHolderHandler, null, null, null, this.mPictureCallback2);
            }
            super.doTakePicture();
        }
    }

    protected void onPictureTakenCallback(byte[] data, byte[] extraExif, CameraProxy camera) {
        CamLog.m3d(CameraConstants.TAG, "[dualpop] Normal view picture taken callback");
        this.mNormalViewCaptureData = data;
        this.mExif = Exif.readExif(data);
        this.mIsNormalCaptured = true;
    }

    protected void oneShotPreviewCallbackDone() {
        CamLog.m3d(CameraConstants.TAG, "[dualpop] oneShotPreviewCallbackDone");
        if (!checkModuleValidate(1)) {
            CamLog.m3d(CameraConstants.TAG, "[dualpop] device is released,so return");
        } else if (isCameraDeviceAvailable()) {
            this.mIsAllPreviewAvailable |= 1;
            super.oneShotPreviewCallbackDone();
            if (this.mSecondSurfaceView != null) {
                this.mSecondSurfaceView.setTranslationY(10000.0f);
            }
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    DualPopCameraModuleExpand.this.mGet.movePreviewOutOfWindow(false);
                    DualPopCameraModuleExpand.this.mGet.setPreviewCoverVisibility(8, true, null, false, true);
                }
            }, 150);
        }
    }

    protected void onPictureTakenCallback2(byte[] data, CameraProxy camera) {
        CamLog.m3d(CameraConstants.TAG, "[dualpop] Wide view picture taken callback");
        this.mWideViewCaptureData = data;
        this.mIsWideCaptured = true;
    }

    protected void setPreviewFpsRange(CameraParameters parameters, boolean isRecordingStarted) {
        super.setPreviewFpsRange(parameters, isRecordingStarted);
        String fps = SystemProperties.get(ParamConstants.FPS_RANGE_SYSTEM_PROPERTY_CAMCORDER, "15000,30000");
        if (!isRecordingStarted) {
            fps = MultimediaProperties.getCameraFPSRange(isRearCamera());
        }
        if (CameraSecondHolder.subinstance() != null) {
            try {
                CameraParameters param = CameraSecondHolder.subinstance().getParameters();
                if (param != null) {
                    param.set(ParamConstants.KEY_PREVIEW_FPS_RANGE, fps);
                    CameraSecondHolder.subinstance().setParameters(param);
                }
            } catch (RuntimeException e) {
                Log.e(CameraConstants.TAG, "[dualpop] - getParameters failed");
            }
        }
        CamLog.m3d(CameraConstants.TAG, "[dualpop] setPreviewFpsRange for wide = " + fps);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mCaptureButtonManager.changeButtonByMode(12);
    }

    public void onShutterLargeButtonClicked() {
        if (checkModuleValidate(15) && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 12:
                    onCameraShutterButtonClicked();
                    return;
                default:
                    super.onShutterLargeButtonClicked();
                    return;
            }
        }
    }

    protected void doSaveImagePostExecute(Uri uri) {
        super.doSaveImagePostExecute(uri);
        startPreview(null);
    }

    protected void onChangePictureSize() {
        super.onChangePictureSize();
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (DualPopCameraModuleExpand.this.mGet.getPreviewCoverVisibility() == 0) {
                    CamLog.m3d(CameraConstants.TAG, "Preview cover is displaying, so remove it");
                    DualPopCameraModuleExpand.this.mGet.setPreviewCoverVisibility(8, false);
                }
            }
        }, 500);
    }

    public int getShutterButtonType() {
        return 4;
    }

    public String getShotMode() {
        return CameraConstants.MODE_DUAL_POP_CAMERA;
    }

    public void onPauseAfter() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(15);
        }
        if (isPaused()) {
            setPictureSizeListAndSettingMenu(false);
        }
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
            handleSavingPictureOnPause();
        }
    }

    private void handleSavingPictureOnPause() {
        Thread waitSavingPictureThread = new C03697();
        waitSavingPictureThread.start();
        try {
            waitSavingPictureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mIsSavingPicture = false;
    }
}
