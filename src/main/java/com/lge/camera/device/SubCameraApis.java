package com.lge.camera.device;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.camera.device.ICameraCallback.CameraErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ICameraCallback.CameraShutterCallback;

public class SubCameraApis extends CameraHolder {
    public void startPreview() {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.startPreview();
        }
    }

    public void startRecordingPreview(Surface recorderSurface) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.startRecordingPreview(recorderSurface);
        }
    }

    public void stopPreview() {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.stopPreview();
        }
    }

    public void setParameters(CameraParameters p) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setParameters(p);
        }
    }

    public CameraParameters getParameters() {
        if (this.mCameraDevice != null) {
            return this.mCameraDevice.getParameters();
        }
        return null;
    }

    public void setPreviewDisplay(SurfaceHolder st) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setPreviewDisplay(st);
        }
    }

    public void setPreviewTexture(SurfaceTexture st) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setPreviewTexture(st);
        }
    }

    public void takePicture(Handler handler, CameraShutterCallback shutter, CameraPictureCallback raw, CameraPictureCallback postview, CameraPictureCallback jpeg) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.takePicture(handler, shutter, raw, postview, jpeg);
        }
    }

    public void setOneShotPreviewDataCallback(Handler handler, CameraPreviewDataCallback cb) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setOneShotPreviewDataCallback(handler, cb);
        }
    }

    public void setErrorCallback(Handler handler, CameraErrorCallback cb) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setErrorCallback(handler, cb);
        }
    }
}
