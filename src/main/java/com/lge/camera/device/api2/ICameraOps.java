package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader.OnImageAvailableListener;

public interface ICameraOps {
    void captureYuvFlash();

    int doAutoFocus(boolean z);

    int doCancelAutoFocus();

    void doJpegCapture(boolean z) throws ApiFailureException;

    OnImageAvailableListener getOnFullFrameImageAvailableListener();

    OnImageAvailableListener getOnJpegImageAvailableListener();

    OnImageAvailableListener getOnPreviewImageAvailableListener();

    OnImageAvailableListener getOnRawImageAvailableListener();

    boolean isAFSupported();

    boolean isFlashRequired();

    int lockExposure(boolean z);

    void onFaceDetionOnOff(boolean z);

    void onMetaAvailable(TotalCaptureResult totalCaptureResult, boolean z);

    void onPreviewCaptureFailed(CaptureRequest captureRequest, CaptureFailure captureFailure);

    int preCapture();

    void setJogZoom(int i);

    void setPointZoom(Rect rect);

    void startZoomAction();

    void stopZoomAction();

    void takePicture() throws ApiFailureException;
}
