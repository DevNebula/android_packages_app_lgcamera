package com.lge.camera.device;

import android.hardware.Camera;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.os.Handler;
import android.util.Size;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.hardware.LGCamera.CameraMetaDataCallback;
import java.nio.ByteBuffer;

public interface ICameraCallback {

    public interface CameraPreviewDataCallback {
        void onPreviewFrame(byte[] bArr, CameraProxy cameraProxy);
    }

    public interface CameraImageCallback {
        void onImageData(Image image);
    }

    public interface CameraShutterCallback {
        void onShutter(CameraProxy cameraProxy);
    }

    public static abstract class CameraPictureCallback {
        public abstract void onPictureTaken(byte[] bArr, byte[] bArr2, CameraProxy cameraProxy);

        public void onPictureTakenBefore(byte[] data, byte[] extraExif) {
        }
    }

    public static abstract class OutFocusCallback implements CameraMetaDataCallback {
        private static final int OUTFOCUS_META_CALLBACK_LOW_LIGHT = 1;
        private static final int OUTFOCUS_META_CALLBACK_NO_FACE = 2;

        public abstract void onOutFocusResult(int i);

        public void onOutFocusCaptureResult(OutfocusCaptureResult outfocusResult) {
        }

        public void onCameraMetaData(byte[] data, Camera camera) {
            if (data != null && data.length > 0) {
                int type = data[0];
                switch (type) {
                    case 1:
                        onOutFocusResult(4);
                        return;
                    case 2:
                        onOutFocusResult(1);
                        return;
                    default:
                        onOutFocusResult(type);
                        return;
                }
            }
        }
    }

    public static abstract class CameraPictureCallbackForDng extends CameraPictureCallback {
        public abstract void onPictureTaken(DngCreator dngCreator, ByteBuffer byteBuffer, Size size);
    }

    public interface CameraOpenErrorCallback {
        void onCameraDisabled(int i);

        void onDeviceOpenFailure(int i);

        void onReconnectionFailure(CameraManagerBase cameraManagerBase);
    }

    public interface CameraErrorCallback {
        void onError(int i, CameraProxy cameraProxy);
    }

    public interface CameraBacklightDetectionCallback {
        void onDetected(boolean z);
    }

    public interface CameraLowlightDetectionCallback {
        void onDetected(boolean z);
    }

    public interface CineZoomCallback {
        void onCineZoom(int i);
    }

    public interface CameraImageMetaCallback {
        void onImageMetaData(TotalCaptureResult totalCaptureResult);
    }

    public interface CameraAFCallback {
        void onAutoFocus(boolean z, CameraProxy cameraProxy);
    }

    public interface CameraAFMoveCallback {
        void onAutoFocusMoving(boolean z, CameraProxy cameraProxy);
    }

    public interface CameraFaceDetectionCallback {
        void onFaceDetection(FaceCommon[] faceCommonArr, CameraProxy cameraProxy);
    }

    public interface CameraHistogramDataCallback {
        void onCameraData(int[] iArr, CameraProxy cameraProxy);
    }

    public interface ZoomChangeCallback {
        void onZoomChange(int i);
    }

    CameraProxy cameraOpen(Handler handler, int i, CameraOpenErrorCallback cameraOpenErrorCallback);
}
