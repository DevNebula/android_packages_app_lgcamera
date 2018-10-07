package com.lge.camera.managers;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.util.CamLog;

public class DirectCallbackManager {
    private CameraProxy mCameraDevice = null;
    private int mHeight;
    private boolean mIsApi2;
    private int mMaxPreviewBufferCnt = 0;
    private int mPreviewBufferIndex = 0;
    private byte[][] mPreviewBuffers = ((byte[][]) null);
    private int mWidth;

    public boolean configPreviewCallback(CameraProxy cameraDevice, int maxCnt) {
        boolean z = false;
        if (cameraDevice == null) {
            return false;
        }
        CameraParameters parameters = cameraDevice.getParameters();
        if (parameters == null) {
            return false;
        }
        Size previewSize = parameters.getPreviewSize();
        if (previewSize == null) {
            return false;
        }
        this.mCameraDevice = cameraDevice;
        this.mWidth = previewSize.getWidth();
        this.mHeight = previewSize.getHeight();
        this.mMaxPreviewBufferCnt = maxCnt;
        this.mPreviewBufferIndex = 0;
        if (FunctionProperties.getSupportedHal() == 2) {
            z = true;
        }
        this.mIsApi2 = z;
        this.mPreviewBuffers = (byte[][]) null;
        return true;
    }

    public boolean setPreviewDataCallback(Object cb) {
        if (this.mIsApi2) {
            if (cb == null || (cb instanceof CameraImageCallback)) {
                setPreviewCallbackApi2((CameraImageCallback) cb);
                return true;
            }
            CamLog.m5e(CameraConstants.TAG, "Abnormal function call!");
            return false;
        } else if (cb == null || (cb instanceof CameraPreviewDataCallback)) {
            setPreviewCallbackApi1((CameraPreviewDataCallback) cb);
            return true;
        } else {
            CamLog.m5e(CameraConstants.TAG, "Abnormal function call!");
            return false;
        }
    }

    public boolean setPreviewDataCallbackWithBuffer(final CameraPreviewDataCallback cb) {
        if (this.mIsApi2 || this.mCameraDevice == null) {
            return false;
        }
        Camera camera = (Camera) this.mCameraDevice.getCamera();
        if (camera == null) {
            return false;
        }
        if (cb != null) {
            camera.setPreviewCallbackWithBuffer(new PreviewCallback() {
                public void onPreviewFrame(byte[] arg0, Camera arg1) {
                    cb.onPreviewFrame(arg0, DirectCallbackManager.this.mCameraDevice);
                }
            });
        } else {
            camera.addCallbackBuffer(null);
            camera.setPreviewCallback(null);
        }
        return true;
    }

    private void setPreviewCallbackApi1(final CameraPreviewDataCallback cb) {
        if (this.mCameraDevice != null && this.mMaxPreviewBufferCnt >= 1) {
            Camera camera = (Camera) this.mCameraDevice.getCamera();
            if (camera == null) {
                return;
            }
            if (cb != null) {
                setPreviewCallbackBuffer();
                camera.addCallbackBuffer(this.mPreviewBuffers[this.mPreviewBufferIndex]);
                camera.setPreviewCallbackWithBuffer(new PreviewCallback() {
                    public void onPreviewFrame(byte[] arg0, Camera arg1) {
                        cb.onPreviewFrame(arg0, DirectCallbackManager.this.mCameraDevice);
                    }
                });
                return;
            }
            camera.addCallbackBuffer(null);
            camera.setPreviewCallback(null);
        }
    }

    public void addCallbackBuffer(byte[] callbackBuffer) {
        if (!this.mIsApi2 && this.mCameraDevice != null) {
            Camera camera = (Camera) this.mCameraDevice.getCamera();
            if (camera != null) {
                camera.addCallbackBuffer(callbackBuffer);
            }
        }
    }

    public void addCallbackBuffer() {
        if (!this.mIsApi2 && this.mCameraDevice != null && this.mPreviewBuffers != null && this.mPreviewBuffers[this.mPreviewBufferIndex] != null && this.mMaxPreviewBufferCnt >= 1) {
            this.mPreviewBufferIndex = (this.mPreviewBufferIndex + 1) % this.mMaxPreviewBufferCnt;
            Camera camera = (Camera) this.mCameraDevice.getCamera();
            if (camera != null) {
                camera.addCallbackBuffer(this.mPreviewBuffers[this.mPreviewBufferIndex]);
            }
        }
    }

    private void setPreviewCallbackApi2(CameraImageCallback cb) {
        if (this.mCameraDevice != null) {
            this.mCameraDevice.setImageDataCallback(0, cb);
        }
    }

    public void release() {
        this.mCameraDevice = null;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mMaxPreviewBufferCnt = 0;
        this.mPreviewBufferIndex = 0;
        this.mIsApi2 = true;
        this.mPreviewBuffers = (byte[][]) null;
    }

    public boolean isConfigured() {
        return this.mCameraDevice != null;
    }

    private void setPreviewCallbackBuffer() {
        if (this.mPreviewBuffers == null && this.mMaxPreviewBufferCnt > 0) {
            this.mPreviewBuffers = new byte[this.mMaxPreviewBufferCnt][];
            for (int i = 0; i < this.mMaxPreviewBufferCnt; i++) {
                this.mPreviewBuffers[i] = new byte[((int) (((double) (this.mWidth * this.mHeight)) * 1.5d))];
            }
        }
    }
}
