package com.lge.camera.device;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.camera2.DngCreator;
import android.os.Handler;
import android.util.Size;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.device.ICameraCallback.CameraAFMoveCallback;
import com.lge.camera.device.ICameraCallback.CameraErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraFaceDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraHistogramDataCallback;
import com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraPictureCallbackForDng;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ICameraCallback.CameraShutterCallback;
import com.lge.hardware.LGCamera.CameraDataCallback;
import java.nio.ByteBuffer;

public class CameraCallbackForwards {

    public static class AFCallbackForward implements AutoFocusCallback {
        private final CameraAFCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        public static AFCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraAFCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new AFCallbackForward(handler, camera, cb);
        }

        private AFCallbackForward(Handler h, CameraProxy camera, CameraAFCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onAutoFocus(final boolean b, Camera camera) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    AFCallbackForward.this.mCallback.onAutoFocus(b, AFCallbackForward.this.mCamera);
                }
            });
        }

        public void onAutoFocus(final boolean b) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    AFCallbackForward.this.mCallback.onAutoFocus(b, AFCallbackForward.this.mCamera);
                }
            });
        }
    }

    public static class AFMoveCallbackForward implements AutoFocusMoveCallback {
        private final CameraAFMoveCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        public static AFMoveCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraAFMoveCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new AFMoveCallbackForward(handler, camera, cb);
        }

        private AFMoveCallbackForward(Handler h, CameraProxy camera, CameraAFMoveCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onAutoFocusMoving(final boolean moving, Camera camera) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    AFMoveCallbackForward.this.mCallback.onAutoFocusMoving(moving, AFMoveCallbackForward.this.mCamera);
                }
            });
        }

        public void onAutoFocusMoving(final boolean moving) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    AFMoveCallbackForward.this.mCallback.onAutoFocusMoving(moving, AFMoveCallbackForward.this.mCamera);
                }
            });
        }
    }

    public static class CameraErrorCallbackForward implements ErrorCallback {
        private final CameraErrorCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        public static CameraErrorCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraErrorCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new CameraErrorCallbackForward(handler, camera, cb);
        }

        private CameraErrorCallbackForward(Handler h, CameraProxy camera, CameraErrorCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onError(final int error, Camera camera) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraErrorCallbackForward.this.mCallback.onError(error, CameraErrorCallbackForward.this.mCamera);
                }
            });
        }
    }

    public static class CameraHistogramDataCallbackForward implements CameraDataCallback {
        private final CameraHistogramDataCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        public static CameraHistogramDataCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraHistogramDataCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new CameraHistogramDataCallbackForward(handler, camera, cb);
        }

        private CameraHistogramDataCallbackForward(Handler h, CameraProxy camera, CameraHistogramDataCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onCameraData(final int[] data, Camera camera) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraHistogramDataCallbackForward.this.mCallback.onCameraData(data, CameraHistogramDataCallbackForward.this.mCamera);
                }
            });
        }
    }

    public static class CameraOpenErrorCallbackForward implements CameraOpenErrorCallback {
        private final CameraOpenErrorCallback mCallback;
        private final Handler mHandler;

        public static CameraOpenErrorCallbackForward getNewInstance(Handler handler, CameraOpenErrorCallback cb) {
            if (handler == null || cb == null) {
                return null;
            }
            return new CameraOpenErrorCallbackForward(handler, cb);
        }

        private CameraOpenErrorCallbackForward(Handler h, CameraOpenErrorCallback cb) {
            this.mHandler = h;
            this.mCallback = cb;
        }

        public void onCameraDisabled(final int cameraId) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onCameraDisabled(cameraId);
                }
            });
        }

        public void onDeviceOpenFailure(final int cameraId) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onDeviceOpenFailure(cameraId);
                }
            });
        }

        public void onReconnectionFailure(final CameraManagerBase mgr) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CameraOpenErrorCallbackForward.this.mCallback.onReconnectionFailure(mgr);
                }
            });
        }
    }

    public static class FaceDetectionCallbackForward implements FaceDetectionListener {
        private final CameraFaceDetectionCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;
        private int mPreFaceCnt = -1;

        public static FaceDetectionCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraFaceDetectionCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new FaceDetectionCallbackForward(handler, camera, cb);
        }

        private FaceDetectionCallbackForward(Handler h, CameraProxy camera, CameraFaceDetectionCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onFaceDetection(final Face[] faces, Camera camera) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    FaceCommon[] faceCommon = new FaceCommon[faces.length];
                    for (int i = 0; i < faces.length; i++) {
                        faceCommon[i] = new FaceCommon(faces[i]);
                    }
                    FaceDetectionCallbackForward.this.mCallback.onFaceDetection(faceCommon, FaceDetectionCallbackForward.this.mCamera);
                }
            });
        }

        public void onFaceDetection(android.hardware.camera2.params.Face[] faces, Rect activeSizeRect, Size pictureSize, float zoomRatio) {
            if (this.mPreFaceCnt != 0 || this.mPreFaceCnt != faces.length) {
                this.mPreFaceCnt = faces.length;
                final android.hardware.camera2.params.Face[] faceArr = faces;
                final Rect rect = activeSizeRect;
                final Size size = pictureSize;
                final float f = zoomRatio;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        FaceCommon[] faceCommon = new FaceCommon[faceArr.length];
                        for (int i = 0; i < faceArr.length; i++) {
                            faceCommon[i] = new FaceCommon(faceArr[i], rect, size, f);
                        }
                        FaceDetectionCallbackForward.this.mCallback.onFaceDetection(faceCommon, FaceDetectionCallbackForward.this.mCamera);
                    }
                });
            }
        }
    }

    public static class OneShotCallbackForward implements PreviewCallback {
        private final CameraPreviewDataCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        public static OneShotCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraPreviewDataCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new OneShotCallbackForward(handler, camera, cb);
        }

        private OneShotCallbackForward(Handler h, CameraProxy camera, CameraPreviewDataCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onPreviewFrame(final byte[] data, Camera camera) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    OneShotCallbackForward.this.mCallback.onPreviewFrame(data, OneShotCallbackForward.this.mCamera);
                }
            });
        }

        public void onPreviewFrame(final byte[] data) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    OneShotCallbackForward.this.mCallback.onPreviewFrame(data, OneShotCallbackForward.this.mCamera);
                }
            });
        }
    }

    public static class PictureCallbackForward implements PictureCallback {
        private final CameraPictureCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        public static PictureCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraPictureCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new PictureCallbackForward(handler, camera, cb);
        }

        private PictureCallbackForward(Handler h, CameraProxy camera, CameraPictureCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onPictureTaken(final byte[] data, Camera camera) {
            this.mCallback.onPictureTakenBefore(data, null);
            this.mHandler.post(new Runnable() {
                public void run() {
                    PictureCallbackForward.this.mCallback.onPictureTaken(data, null, PictureCallbackForward.this.mCamera);
                }
            });
        }

        public void onPictureTaken(final byte[] data, final byte[] hiddenExif, Camera camera) {
            this.mCallback.onPictureTakenBefore(data, hiddenExif);
            this.mHandler.post(new Runnable() {
                public void run() {
                    PictureCallbackForward.this.mCallback.onPictureTaken(data, hiddenExif, PictureCallbackForward.this.mCamera);
                }
            });
        }

        public void onPictureTaken(final DngCreator dngCreator, final ByteBuffer byteBuffer, final Size size) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (PictureCallbackForward.this.mCallback instanceof CameraPictureCallbackForDng) {
                        ((CameraPictureCallbackForDng) PictureCallbackForward.this.mCallback).onPictureTaken(dngCreator, byteBuffer, size);
                    }
                }
            });
        }
    }

    public static class ShutterCallbackForward implements ShutterCallback {
        private final CameraShutterCallback mCallback;
        private final CameraProxy mCamera;
        private final Handler mHandler;

        /* renamed from: com.lge.camera.device.CameraCallbackForwards$ShutterCallbackForward$1 */
        class C06731 implements Runnable {
            C06731() {
            }

            public void run() {
                ShutterCallbackForward.this.mCallback.onShutter(ShutterCallbackForward.this.mCamera);
            }
        }

        public static ShutterCallbackForward getNewInstance(Handler handler, CameraProxy camera, CameraShutterCallback cb) {
            if (handler == null || camera == null || cb == null) {
                return null;
            }
            return new ShutterCallbackForward(handler, camera, cb);
        }

        private ShutterCallbackForward(Handler h, CameraProxy camera, CameraShutterCallback cb) {
            this.mHandler = h;
            this.mCamera = camera;
            this.mCallback = cb;
        }

        public void onShutter() {
            this.mHandler.post(new C06731());
        }
    }
}
