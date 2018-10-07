package com.lge.camera.managers;

import android.os.SystemClock;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;

public class FlipManager extends ManagerInterfaceImpl {
    private boolean isFlipPicturepbyHW = false;
    private boolean isFlipVideobyHW = false;

    public FlipManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public boolean isPreProcessPictureFlip() {
        return this.isFlipPicturepbyHW;
    }

    public boolean isPreProcessVideoFlip() {
        return this.isFlipVideobyHW;
    }

    public void setPreProcessSupported(boolean isSupportedPicture, boolean isSupportedVideo) {
        this.isFlipPicturepbyHW = isSupportedPicture;
        this.isFlipVideobyHW = isSupportedVideo;
    }

    public boolean isNeedFlip(int cameraID) {
        if ((cameraID == 1 || cameraID == 2) && "off".equals(this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION))) {
            return true;
        }
        return false;
    }

    public CameraParameters setPictureFlipParam(int cameraID, CameraParameters param, int degree) {
        if (isPreProcessPictureFlip()) {
            if (isNeedFlip(cameraID)) {
                CameraDeviceUtils.setSnapshotPictureFlip(param, degree, true);
            } else {
                CameraDeviceUtils.setSnapshotPictureFlip(param, 0, false);
            }
        }
        return param;
    }

    public CameraParameters setVideoFlipParam(int cameraID, CameraParameters param, int degree) {
        if (isPreProcessVideoFlip()) {
            if (isNeedFlip(cameraID)) {
                CameraDeviceUtils.setVideoFlip(param, degree, true, false);
            } else {
                CameraDeviceUtils.setVideoFlip(param, 0, false, false);
            }
        }
        return param;
    }

    public CameraParameters setForceVideoFlipParam(int cameraID, CameraParameters param, int degree, boolean isOn, boolean isInverse) {
        if (isPreProcessVideoFlip() && (cameraID == 1 || cameraID == 2)) {
            CameraDeviceUtils.setVideoFlip(param, degree, isOn, isInverse);
        }
        return param;
    }

    public byte[] checkPostProcessAndMakeJpegFlip(int cameraID, int degree, byte[] jpegData, ExifInterface exif) {
        if (!isPreProcessPictureFlip() && isNeedFlip(cameraID)) {
            return makeFlippedJpegData(jpegData, exif, degree);
        }
        return jpegData;
    }

    public CameraParameters setFlipParam(CameraParameters param, int degree, boolean isOn) {
        CameraDeviceUtils.setSnapshotPictureFlip(param, 0, isOn);
        return param;
    }

    public byte[] makeFlippedJpegData(byte[] jpegData, ExifInterface exif, int exifOrientation) {
        if (exif == null) {
            return jpegData;
        }
        long start = SystemClock.elapsedRealtime();
        CamLog.m7i(CameraConstants.TAG, "jpeg Process time ");
        byte[] convertJpeg = jpegData;
        byte[] convertThumbnail = exif.getThumbnail();
        if (convertThumbnail != null) {
            exif.setCompressedThumbnail(BitmapManagingUtil.makeFlipImage(convertThumbnail, true, exifOrientation));
        }
        convertJpeg = BitmapManagingUtil.makeFlipImage(jpegData, true, exifOrientation);
        CamLog.m7i(CameraConstants.TAG, "jpeg Process time " + (SystemClock.elapsedRealtime() - start));
        return convertJpeg;
    }
}
