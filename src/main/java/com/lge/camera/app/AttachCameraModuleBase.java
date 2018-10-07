package com.lge.camera.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.FileManager;
import com.lge.camera.file.FileNamer;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.TelephonyUtil;
import com.lge.camera.util.Utils;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class AttachCameraModuleBase extends BeautyShotCameraModule {
    int mAttachRecordingDuration = 0;
    long mAttachRecordingSize = 0;
    AttachCaptureValues mCaptureParam = new AttachCaptureValues();
    boolean mIsFirstEntry = true;
    Uri mSavedUriFromOtherModule = null;

    public AttachCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        this.mGet.setCurrentConeMode(1, false);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mGet.isLGUOEMCameraIntent() && this.mSavedUriFromOtherModule != null) {
            notifyNewMedia(this.mSavedUriFromOtherModule, true);
            this.mSavedUriFromOtherModule = null;
        }
        if (this.mCaptureButtonManager == null) {
            return;
        }
        if (this.mGet.isVideoCaptureMode()) {
            this.mCaptureButtonManager.changeButtonByMode(14);
        } else {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
    }

    protected void onPostviewRelease() {
        if (!(this.mCameraDevice == null || isRearCamera())) {
            setFlashTorch(this.mCameraDevice.getParameters(), true, getSettingValue("flash-mode"), true);
        }
        if ((!this.mGet.isLGUOEMCameraIntent() || "mode_normal".equals(getSettingValue(Setting.KEY_MODE))) && checkModuleValidate(4)) {
            super.onPostviewRelease();
        }
        if (!this.mGet.isLGUOEMCameraIntent() || "mode_normal".equals(getSettingValue(Setting.KEY_MODE))) {
            startSelfieEngine();
            this.mSnapShotChecker.releaseAttachShotState();
            showDoubleCamera(true);
            return;
        }
        this.mGet.changeModule();
    }

    public void onQueueStatus(boolean full) {
        super.onQueueStatus(full);
        if (this.mSnapShotChecker.isAttachShotPictureTaken()) {
            setCaptureButtonEnable(false, 4);
        }
    }

    protected void saveImage(final byte[] data, final byte[] extraExif) {
        if (this.mCaptureParam == null || this.mCaptureParam.getTargetUri() == null) {
            new Thread(new Runnable() {
                public void run() {
                    CamLog.m3d(CameraConstants.TAG, "saveImage in attach camera module - start");
                    String markTime = FileNamer.get().getTakeTime(AttachCameraModuleBase.this.getSettingValue(Setting.KEY_MODE));
                    String dir = AttachCameraModuleBase.this.getCurDir();
                    String fileName = AttachCameraModuleBase.this.getFileNameByTime(true, dir, markTime) + ".jpg";
                    try {
                        if (!FileManager.writeJpegImageToFile(data, extraExif, dir, fileName, Exif.readExif(data))) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileNamer.get().removeFileNameInSaving(dir + fileName);
                    AttachCameraModuleBase.this.checkStorage();
                    AttachCameraModuleBase.this.notifyNewMedia(Uri.fromFile(new File(dir + fileName)), false);
                    AttachCameraModuleBase.this.mNeedProgressDuringCapture = 0;
                    CamLog.m3d(CameraConstants.TAG, "saveImage in attach camera module - end");
                }
            }).start();
        } else {
            super.saveImage(data, extraExif);
        }
    }

    public void setCaptureButtonEnable(boolean enable, int type) {
        if (!enable || !this.mSnapShotChecker.isSnapShotProcessing()) {
            if (!TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || enable || type != 1 || isVideoCaptureMode()) {
                super.setCaptureButtonEnable(enable, 4);
            }
        }
    }

    protected void onChangeModuleAfter() {
        super.onChangeModuleAfter();
        if (this.mCheeseShutterManager != null && this.mGet.isVideoCaptureMode()) {
            setVoiceShutter(false, 0);
        }
    }

    protected void doPhoneStateListenerAction(int state) {
        super.doPhoneStateListenerAction(state);
        if (this.mGet.isVideoCaptureMode()) {
            setVoiceShutter(false, 0);
            if (state == 0) {
                if (!checkModuleValidate(192)) {
                    return;
                }
                if (MDMUtil.allowMicrophone()) {
                    setCaptureButtonEnable(true, 4);
                } else {
                    setCaptureButtonEnable(false, 4);
                }
            } else if (state == 1 && getCameraState() != 6 && getCameraState() != 7) {
                setCaptureButtonEnable(false, 4);
            }
        }
    }

    public boolean onRecordStartButtonClicked() {
        if (isVideoCaptureMode()) {
            return super.onRecordStartButtonClicked();
        }
        return false;
    }

    protected void updateShutterButtonsOnVideoStopClicked() {
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.changeButtonByMode(14);
        }
    }

    public void onVideoPauseClicked() {
        super.onVideoPauseClicked();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
        }
    }

    public void onVideoResumeClicked() {
        super.onVideoResumeClicked();
        if (this.mCaptureButtonManager != null) {
            this.mCaptureButtonManager.setExtraButtonVisibility(8, 1);
        }
    }

    protected void initializeAfterCameraOpen() {
        super.initializeAfterCameraOpen();
        if (this.mGet.isLGUOEMCameraIntent() && this.mIsFirstEntry) {
            this.mGet.showModeMenu(true);
            setQuickButtonIndex(C0088R.id.quick_button_mode, 1);
            setQuickButtonSelected(C0088R.id.quick_button_mode, true);
        }
    }

    public boolean attatchMediaOnPostview(Uri uri, int mediaType) {
        if (!checkModuleValidate(13)) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "uri = " + (uri != null ? uri.toString() : null) + ", mediaType = " + mediaType);
        if (mediaType == 0) {
            doAttachImage(uri);
        } else if (mediaType == 1) {
            doAttachVideo(uri);
        }
        return true;
    }

    public int getRequestedStorage(Uri targetUri) {
        if (targetUri == null) {
            CamLog.m7i(CameraConstants.TAG, "TargetUri is null.");
            return 0;
        }
        String requestedFilePath = targetUri.getPath();
        String externalDir = getStorageDir(1);
        String internalDir = getStorageDir(0);
        if (externalDir != null && requestedFilePath.contains(externalDir)) {
            return 1;
        }
        if (internalDir == null || !requestedFilePath.contains(internalDir)) {
            return 0;
        }
        return 0;
    }

    public void doAttachToTargetUri(Uri savedImageUri) {
        CamLog.m3d(CameraConstants.TAG, "doAttachToTargetUri()");
        this.mCaptureParam.preProcessSaveUri(savedImageUri);
        if (checkStorage(0, getRequestedStorage(this.mCaptureParam.getTargetUri()))) {
            try {
                if (this.mCaptureParam.getTargetUri().equals(Media.EXTERNAL_CONTENT_URI)) {
                    CamLog.m3d(CameraConstants.TAG, "URL Is Not correct we will return URI :" + this.mCaptureParam.getTargetUri().getPath());
                    sendResultIntent(savedImageUri);
                    return;
                }
                if (!writeTargetUri(savedImageUri, this.mCaptureParam.getTargetUri())) {
                    setResultAndFinish(0);
                }
                if (ModelProperties.isRemoveOrgFile() || this.mCaptureParam.hasTargetUri()) {
                    FileManager.deleteFile(this.mGet.getAppContext(), savedImageUri);
                }
                CamLog.m3d(CameraConstants.TAG, "doAttach OK");
                setResultAndFinish(-1);
            } catch (Exception e) {
                CamLog.m3d(CameraConstants.TAG, "outputStream error" + e);
                setResultAndFinish(0);
            }
        }
    }

    public void doAttachImage(Uri savedImageUri) {
        CamLog.m3d(CameraConstants.TAG, "doAttachNoCrop()");
        if (this.mCaptureParam.getTargetUri() != null) {
            doAttachToTargetUri(savedImageUri);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "doAttach target uri is Null");
        if (savedImageUri == null) {
            CamLog.m3d(CameraConstants.TAG, "doAttach mSavedImageUri null!");
            setResultAndFinish(0);
            return;
        }
        int sampleSizeWidth;
        int sampleSizeHeight;
        if (this.mCameraDevice != null) {
            sampleSizeWidth = this.mCameraDevice.getParameters().getPictureSize().getWidth() / 16;
            sampleSizeHeight = this.mCameraDevice.getParameters().getPictureSize().getHeight() / 16;
        } else {
            int[] pictureSize = Utils.sizeStringToArray(getSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId)));
            sampleSizeWidth = pictureSize[0] / 16;
            sampleSizeHeight = pictureSize[1] / 16;
        }
        Bitmap orgBmp = BitmapManagingUtil.loadScaledBitmap(this.mGet.getActivity().getContentResolver(), savedImageUri.toString(), sampleSizeWidth, sampleSizeHeight);
        if (orgBmp == null) {
            CamLog.m3d(CameraConstants.TAG, "LoadBitmap fail!");
            setResultAndFinish(0);
            return;
        }
        String filePath = FileUtil.getRealPathFromURI(this.mGet.getActivity(), savedImageUri);
        if (filePath != null) {
            try {
                File file = new File(filePath);
                if (file != null && file.exists() && file.delete()) {
                    CamLog.m3d(CameraConstants.TAG, "File deleted successfully");
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "delete file failed : ", e);
            }
        }
        Intent intent = new Intent("inline-data").putExtra("data", orgBmp);
        intent.setFlags(1);
        CamLog.m3d(CameraConstants.TAG, "doAttach OK");
        setResultAndFinish(-1, intent);
        orgBmp.recycle();
    }

    public boolean writeTargetUri(Uri savedImageUri, Uri targetUri) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        Closeable fis = null;
        try {
            FileOutputStream fos = (FileOutputStream) getActivity().getContentResolver().openOutputStream(targetUri);
            if (fos == null) {
                CamLog.m3d(CameraConstants.TAG, "outputStream null! cancel");
                FileUtil.closeSilently(null);
                return false;
            }
            String filePath = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), savedImageUri);
            if (filePath == null) {
                CamLog.m3d(CameraConstants.TAG, "filePath null! cancel");
                FileUtil.closeSilently(null);
                return false;
            }
            Closeable fis2 = new FileInputStream(new File(filePath));
            try {
                FileChannel inChannel = fis2.getChannel();
                if (inChannel != null) {
                    inChannel.transferTo(0, inChannel.size(), fos.getChannel());
                    FileUtil.closeSilently(fis2);
                    fis = fis2;
                    return true;
                }
                FileUtil.closeSilently(fis2);
                fis = fis2;
                return false;
            } catch (FileNotFoundException e3) {
                e = e3;
                fis = fis2;
                try {
                    CamLog.m6e(CameraConstants.TAG, "readImageData FileNotFoundException : ", e);
                    FileUtil.closeSilently(fis);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    FileUtil.closeSilently(fis);
                    throw th;
                }
            } catch (IOException e4) {
                e2 = e4;
                fis = fis2;
                CamLog.m6e(CameraConstants.TAG, "readImageData IO error : ", e2);
                FileUtil.closeSilently(fis);
                return false;
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                FileUtil.closeSilently(fis);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            CamLog.m6e(CameraConstants.TAG, "readImageData FileNotFoundException : ", e);
            FileUtil.closeSilently(fis);
            return false;
        } catch (IOException e6) {
            e2 = e6;
            CamLog.m6e(CameraConstants.TAG, "readImageData IO error : ", e2);
            FileUtil.closeSilently(fis);
            return false;
        }
    }

    public boolean writeOutputStreamUri(Uri savedImageUri, OutputStream outputStream) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        if (outputStream == null) {
            CamLog.m7i(CameraConstants.TAG, "Output stream is null.");
            return false;
        }
        Closeable fis = null;
        try {
            String filePath = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), savedImageUri);
            if (filePath == null) {
                CamLog.m3d(CameraConstants.TAG, "filePath null! cancel");
                FileUtil.closeSilently(null);
                return false;
            }
            File file = new File(filePath);
            Closeable fis2 = new FileInputStream(file);
            try {
                byte[] data = new byte[((int) file.length())];
                if (fis2.read(data) != -1) {
                    outputStream.write(data);
                    FileUtil.closeSilently(fis2);
                    return true;
                }
                FileUtil.closeSilently(fis2);
                fis = fis2;
                return false;
            } catch (FileNotFoundException e3) {
                e = e3;
                fis = fis2;
                try {
                    CamLog.m6e(CameraConstants.TAG, "readImageData FileNotFoundException : ", e);
                    FileUtil.closeSilently(fis);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    FileUtil.closeSilently(fis);
                    throw th;
                }
            } catch (IOException e4) {
                e2 = e4;
                fis = fis2;
                CamLog.m6e(CameraConstants.TAG, "readImageData IO error : ", e2);
                FileUtil.closeSilently(fis);
                return false;
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                FileUtil.closeSilently(fis);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            CamLog.m6e(CameraConstants.TAG, "readImageData FileNotFoundException : ", e);
            FileUtil.closeSilently(fis);
            return false;
        } catch (IOException e6) {
            e2 = e6;
            CamLog.m6e(CameraConstants.TAG, "readImageData IO error : ", e2);
            FileUtil.closeSilently(fis);
            return false;
        }
    }

    public void sendResultIntent(Uri savedImageUri) {
        if (savedImageUri != null) {
            Bundle newExtras = new Bundle();
            newExtras.putParcelable("output", savedImageUri);
            CamLog.m3d(CameraConstants.TAG, "mSavedImageUri: " + savedImageUri);
            Intent intent = new Intent();
            intent.setData(savedImageUri);
            intent.putExtras(newExtras);
            setResultAndFinish(-1, intent);
            return;
        }
        setResultAndFinish(0);
    }

    public void setResultAndFinish(int resultCode, Intent data) {
        Activity activity = this.mGet.getActivity();
        if (activity != null) {
            activity.setResult(resultCode, data);
            activity.finish();
        }
    }

    public void setResultAndFinish(int resultCode) {
        Activity activity = this.mGet.getActivity();
        if (activity != null) {
            activity.setResult(resultCode);
            activity.finish();
        }
    }

    public void doAttachVideo(Uri savedVideoUri) {
        int resultCode;
        Intent resultIntent = new Intent();
        if (savedVideoUri != null) {
            CamLog.m3d(CameraConstants.TAG, "attached file uri:" + savedVideoUri);
            if (!(this.mCaptureParam.getTargetUri() == null || this.mGet.isMMSIntent())) {
                doAttachToTargetUri(savedVideoUri);
            }
            resultCode = -1;
            resultIntent.setData(savedVideoUri);
            resultIntent.setFlags(1);
        } else {
            CamLog.m3d(CameraConstants.TAG, "attached file uri is null");
            resultCode = 0;
        }
        this.mGet.getActivity().setResult(resultCode, resultIntent);
        this.mGet.getActivity().finish();
    }

    public boolean isShutterZoomSupported() {
        if (isVideoCameraIntent() || isVideoCaptureMode()) {
            return false;
        }
        return isRearCamera();
    }

    public boolean isJogZoomAvailable() {
        if (isPostviewShowing()) {
            return false;
        }
        return super.isJogZoomAvailable();
    }

    protected boolean isAudioRecordingAvailable() {
        return false;
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (!checkModuleValidate(128)) {
            this.mRecordingUIManager.setProgresbarView(false);
        }
    }

    public void onZoomHide() {
        super.onZoomHide();
        if (!checkModuleValidate(128) && isNeedProgressBar()) {
            this.mRecordingUIManager.setProgresbarView(true);
        }
    }

    protected void setVideoLimitSize() {
        super.setVideoLimitSize();
        this.mLimitRecordingDuration = this.mAttachRecordingDuration;
        this.mLimitRecordingSize = this.mAttachRecordingSize;
    }

    public void onSnapShotButtonClicked() {
    }

    public boolean onShutterUp(int keyCode, KeyEvent event) {
        if (this.mGet.isVideoCaptureMode() && checkModuleValidate(192)) {
            return true;
        }
        return super.onShutterUp(keyCode, event);
    }

    public boolean onShutterDown(int keyCode, KeyEvent event) {
        if (this.mGet.isVideoCaptureMode() && checkModuleValidate(192)) {
            return true;
        }
        return super.onShutterDown(keyCode, event);
    }

    public void onCameraKeyUp(KeyEvent event) {
        if (!this.mGet.isVideoCaptureMode() || !checkModuleValidate(192)) {
            super.onCameraKeyUp(event);
        }
    }

    public boolean isPlayRingMode() {
        boolean z;
        Intent intent = getActivity().getIntent();
        String str = CameraConstants.TAG;
        StringBuilder append = new StringBuilder().append("isCallPlayRingMode? = ");
        if (intent.getIntExtra("playRing", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        CamLog.m3d(str, append.append(z).toString());
        if (intent.getIntExtra("playRing", 0) == 1) {
            return true;
        }
        return false;
    }

    public boolean doBackKey() {
        if (this.mTimerManager != null && (this.mTimerManager.isTimerShotCountdown() || this.mTimerManager.getIsGesureTimerShotProgress())) {
            this.mIsGoingToPostview = false;
            this.mSnapShotChecker.releaseAttachShotState();
        }
        return super.doBackKey();
    }

    public boolean doMenuKey() {
        if (isMMSIntent() && this.mGet.isVideoCaptureMode()) {
            return true;
        }
        return super.doMenuKey();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        this.mSnapShotChecker.releaseAttachShotState();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        this.mIsGoingToPostview = false;
    }

    public String getShotMode() {
        if (isRearCamera() || !FunctionProperties.isSupportedBeautyShot()) {
            return "mode_normal";
        }
        return "mode_beauty=" + this.mBeautyManager.getBeautyStrength();
    }

    protected void showSceneIndicator(boolean show) {
        if (!isVideoCaptureMode()) {
            super.showSceneIndicator(show);
        }
    }

    protected void setHDRMetaDataCallback(String hdrValue) {
        if (!isVideoCaptureMode()) {
            super.setHDRMetaDataCallback(hdrValue);
        }
    }

    public boolean isSupportedQuickClip() {
        return false;
    }

    public int getShutterButtonType() {
        return 4;
    }

    protected boolean isFastShotSupported() {
        return false;
    }
}
