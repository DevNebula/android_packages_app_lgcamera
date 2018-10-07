package com.lge.camera.device;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.device.ICameraCallback.CameraAFMoveCallback;
import com.lge.camera.device.ICameraCallback.CameraBacklightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraFaceDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ICameraCallback.CameraShutterCallback;
import com.lge.camera.device.ICameraCallback.CineZoomCallback;
import com.lge.camera.device.ICameraCallback.ZoomChangeCallback;
import com.lge.hardware.LGCamera;

public interface CameraManagerBase {

    public interface CameraProxyBase {
        void autoFocus(Handler handler, CameraAFCallback cameraAFCallback);

        void cancelAutoFocus();

        void closeSecondCamera();

        Object getCamera();

        CameraParameters getParameters();

        void lock();

        void preparePreview(CameraParameters cameraParameters);

        boolean reconnect(Handler handler, CameraOpenErrorCallback cameraOpenErrorCallback);

        void refreshCamera();

        void refreshParameters();

        void release(boolean z);

        void removeCallbacks();

        void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cameraAFMoveCallback);

        void setBacklightDetectionCallback(Handler handler, CameraBacklightDetectionCallback cameraBacklightDetectionCallback);

        void setCineZoom(CameraParameters cameraParameters, int i, int i2, RectF rectF);

        void setCineZoomCallback(CineZoomCallback cineZoomCallback);

        void setDisplayOrientation(int i);

        void setErrorCallback(Handler handler, CameraErrorCallback cameraErrorCallback);

        void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback cameraFaceDetectionCallback);

        void setForStopRecording();

        void setOneShotPreviewDataCallback(Handler handler, CameraPreviewDataCallback cameraPreviewDataCallback);

        void setParameters(CameraParameters cameraParameters);

        void setParameters(CameraParameters cameraParameters, ParamQueue paramQueue);

        void setPreviewDisplay(SurfaceHolder surfaceHolder);

        void setPreviewSurfaceToTarget(boolean z);

        void setPreviewTexture(SurfaceTexture surfaceTexture);

        void setRecordStreamForSpecialMode(boolean z);

        void setRecordSurfaceToTarget(boolean z);

        void setSecondCamera(LGCamera lGCamera);

        void setZoomChangeCallback(ZoomChangeCallback zoomChangeCallback);

        void startFaceDetection();

        void startPreview();

        void startRecordingPreview(Surface surface);

        void stopFaceDetection();

        void stopPreview();

        void switchCameraForInAndOut();

        void takePicture(Handler handler, CameraShutterCallback cameraShutterCallback, CameraPictureCallback cameraPictureCallback, CameraPictureCallback cameraPictureCallback2, CameraPictureCallback cameraPictureCallback3);

        void takePictureDirect(Handler handler, CameraShutterCallback cameraShutterCallback, CameraPictureCallback cameraPictureCallback, CameraPictureCallback cameraPictureCallback2, CameraPictureCallback cameraPictureCallback3);

        void unlock();
    }

    CameraProxy cameraOpen(Handler handler, int i, CameraOpenErrorCallback cameraOpenErrorCallback);
}
