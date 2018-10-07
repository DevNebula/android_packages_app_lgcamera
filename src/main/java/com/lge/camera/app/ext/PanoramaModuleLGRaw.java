package com.lge.camera.app.ext;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;

public class PanoramaModuleLGRaw extends PanoramaModuleLGNormal {
    protected int mCurISO;
    protected ExifInterface mExif;
    protected PictureCallback mPictureCallbackForExif;
    protected HandlerRunnable mStartPanoAfterTakePictureRunnable;

    /* renamed from: com.lge.camera.app.ext.PanoramaModuleLGRaw$1 */
    class C04571 implements PictureCallback {
        C04571() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            CamLog.m3d(CameraConstants.TAG, "onPictureTaken here, read exif");
            if (PanoramaModuleLGRaw.this.isPaused()) {
                CamLog.m11w(CameraConstants.TAG, "Picture callback for exif, activity is paused. return.");
                return;
            }
            if (data != null) {
                PanoramaModuleLGRaw.this.mExif = Exif.readExif(data);
            }
            PanoramaModuleLGRaw.this.createExifParam();
            PanoramaModuleLGRaw.this.mGet.postOnUiThread(PanoramaModuleLGRaw.this.mStartPanoAfterTakePictureRunnable);
        }
    }

    public PanoramaModuleLGRaw(ActivityBridge activityBridge) {
        super(activityBridge);
        this.mExif = null;
        this.mPictureCallbackForExif = new C04571();
        this.mStartPanoAfterTakePictureRunnable = new HandlerRunnable(this) {
            public void handleRun() {
                if (PanoramaModuleLGRaw.this.isPaused()) {
                    CamLog.m11w(CameraConstants.TAG, "startPanoAftertakePicture, activity is paused. return.");
                } else {
                    PanoramaModuleLGRaw.this.startPanoAfterTakePicture();
                }
            }
        };
        this.mCurISO = 100;
        this.mInputType = 2;
    }

    public void startPanorama() {
        boolean z;
        if (FunctionProperties.isSupportedZSL(this.mCameraId) && FunctionProperties.isSupportedFastShot()) {
            z = true;
        } else {
            z = false;
        }
        sREAD_EXIF = z;
        this.mNRmode = 0;
        this.mEdgeEnhancementMode = 0;
        super.startPanorama();
    }

    public void startPanoramaExtraProceed() {
        if (FunctionProperties.getSupportedHal() == 2) {
            startPanoAfterTakePicture();
        } else if (sREAD_EXIF) {
            Camera camera = (Camera) this.mCameraDevice.getCamera();
            if (camera != null) {
                camera.takePicture(null, null, this.mPictureCallbackForExif);
            }
        } else {
            startPanoAfterTakePicture();
        }
    }

    public void createExifParam() {
        if (this.mExif == null) {
            CamLog.m11w(CameraConstants.TAG, "mExif is null.");
            return;
        }
        Integer iso_val = this.mExif.getTagIntValue(ExifInterface.TAG_ISO_SPEED_RATINGS);
        if (iso_val == null) {
            CamLog.m11w(CameraConstants.TAG, "iso_val is null.");
            return;
        }
        this.mCurISO = iso_val.intValue();
        setFilterParameter(this.mCurISO);
        CamLog.m11w(CameraConstants.TAG, "mExif iso_val : " + iso_val);
    }

    public void stopPanorama(boolean needSaving, boolean stop360) {
        this.mGet.removePostRunnable(this.mStartPanoAfterTakePictureRunnable);
        super.stopPanorama(needSaving, stop360);
    }

    protected ExifInterface readExif() {
        return this.mExif != null ? this.mExif : super.readExif();
    }
}
