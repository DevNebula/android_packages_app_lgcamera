package com.lge.camera.device.api2;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.params.RggbChannelVector;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraCallbackForwards.AFCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.AFMoveCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.CameraErrorCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.CameraOpenErrorCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.FaceDetectionCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.OneShotCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.PictureCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.ShutterCallbackForward;
import com.lge.camera.device.CameraInfomation;
import com.lge.camera.device.CameraManager;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraOpsModuleBridge;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraAFCallback;
import com.lge.camera.device.ICameraCallback.CameraAFMoveCallback;
import com.lge.camera.device.ICameraCallback.CameraBacklightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraFaceDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraHistogramDataCallback;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.device.ICameraCallback.CameraLowlightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback;
import com.lge.camera.device.ICameraCallback.CameraPictureCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.device.ICameraCallback.CameraShutterCallback;
import com.lge.camera.device.ICameraCallback.CineZoomCallback;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.camera.device.ICameraCallback.ZoomChangeCallback;
import com.lge.camera.device.LGCameraMetaDataCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamQueue;
import com.lge.camera.util.CamLog;
import com.lge.hardware.LGCamera;
import com.lge.hardware.LGCamera.CameraMetaDataCallback;
import com.lge.hardware.LGCamera.CineZoomStateListener;
import com.lge.hardware.LGCamera.EVCallbackListener;
import com.lge.hardware.LGCamera.FingerDetectionDataListener;
import com.lge.hardware.LGCamera.ObtDataListener;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;

public class AndroidCameraManagerImpl2 implements CameraManager {
    private static final int AUTO_FOCUS = 301;
    private static final int CANCEL_AUTO_FOCUS = 302;
    private static final int ENABLE_SHUTTER_SOUND = 501;
    private static final int GET_PARAMETERS = 202;
    private static final int OPEN_CAMERA = 1;
    private static final int PREPARE_PREVIEW_ASYNC = 109;
    private static final int RECORDING_STOP = 900;
    private static final int RECORDING_STREAM = 901;
    private static final int REFRESH_PARAMETERS = 203;
    private static final int RELEASE = 2;
    private static final int SET_AUTO_FOCUS_MOVE_CALLBACK = 303;
    private static final int SET_BACK_LIGHT_DETECTION_LISTENER = 601;
    private static final int SET_CINE_ZOOM_CALLBACK = 307;
    private static final int SET_ERROR_CALLBACK = 464;
    private static final int SET_FACE_DETECTION_LISTENER = 461;
    private static final int SET_IMAGEDATA_CALLBACK = 108;
    private static final int SET_IMAGEMETA_CALLBACK = 107;
    private static final int SET_LOW_LIGHT_DETECTION_LISTENER = 602;
    private static final int SET_ONE_SHOT_PREVIEW_CALLBACK = 105;
    private static final int SET_OUTFOCUS_CALLBACK = 306;
    private static final int SET_PARAMETERS = 201;
    private static final int SET_PREVIEW_DISPLAY_ASYNC = 104;
    private static final int SET_PREVIEW_SURFACE = 802;
    private static final int SET_PREVIEW_TEXTURE_ASYNC = 101;
    private static final int SET_RECORD_SURFACE = 801;
    private static final int SET_SUPER_ZOOM_WITH_SYNC = 305;
    private static final int SET_ZOOM_CHANGE_CALLBACK = 304;
    private static final int START_FACE_DETECTION = 462;
    private static final int START_PREVIEW_ASYNC = 102;
    private static final int START_RECORD_PREVIEW_ASYNC = 106;
    private static final int STOP_BURST_SHOT = 110;
    private static final int STOP_FACE_DETECTION = 463;
    private static final int STOP_PREVIEW = 103;
    private static final int UPDATE_PANORAMA_REQUEST = 701;
    private CameraOps mCamera2;
    private final CameraHandler mCameraHandler;
    private final CaptureListener mCaptureListener = new C06881();
    private WeakReference<Context> mContext;
    private PictureCallbackForward mJpegPictureCallback = null;
    private CameraParameters mParameters;
    private CameraParameters mParametersBackup;
    private boolean mParametersIsDirty;
    private Parameters2 mParamsToSet;
    private PictureCallbackForward mRawPictureCallback = null;
    private ShutterCallbackForward mShutterCallback = null;

    /* renamed from: com.lge.camera.device.api2.AndroidCameraManagerImpl2$1 */
    class C06881 implements CaptureListener {
        C06881() {
        }

        public boolean isValidRawPictureCallback() {
            return AndroidCameraManagerImpl2.this.mRawPictureCallback != null;
        }

        public void onCaptureAvailable(byte[] capture, byte[] extraExif) {
            CamLog.m3d(CameraConstants.TAG, "onCaptureAvailable()");
            AndroidCameraManagerImpl2.this.mJpegPictureCallback.onPictureTaken(capture, extraExif, null);
        }

        public void onCaptureAvailable(DngCreator dngCreator, ByteBuffer byteBuffer, Size size) {
            CamLog.m3d(CameraConstants.TAG, "onCaptureAvailable() for dng buffer");
            AndroidCameraManagerImpl2.this.mRawPictureCallback.onPictureTaken(dngCreator, byteBuffer, size);
        }
    }

    public class AndroidCameraProxyImpl2 implements CameraProxy {
        /* synthetic */ AndroidCameraProxyImpl2(AndroidCameraManagerImpl2 x0, C06881 x1) {
            this();
        }

        private AndroidCameraProxyImpl2() {
            if (AndroidCameraManagerImpl2.this.mCamera2 == null) {
                throw new AssertionError();
            }
        }

        public Object getCamera() {
            return AndroidCameraManagerImpl2.this.mCamera2;
        }

        public void release(boolean releaseMessage) {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(2);
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
            if (releaseMessage) {
                AndroidCameraManagerImpl2.this.mCameraHandler.removeCallbacksAndMessages(null);
                AndroidCameraManagerImpl2.this.mCameraHandler.getLooper().quit();
            }
        }

        public void setRecordStreamForSpecialMode(boolean isStart) {
            CamLog.m7i(CameraConstants.TAG, "setRecordingStream " + isStart);
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.RECORDING_STREAM, Boolean.valueOf(isStart)).sendToTarget();
        }

        public void setForStopRecording() {
            CamLog.m7i(CameraConstants.TAG, "setForStopRecording");
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl2.RECORDING_STOP);
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
        }

        public boolean reconnect(Handler handler, CameraOpenErrorCallback cb) {
            return true;
        }

        public void unlock() {
        }

        public void lock() {
        }

        public void setPreviewTexture(SurfaceTexture surfaceTexture) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(101, surfaceTexture).sendToTarget();
        }

        public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(104, surfaceHolder).sendToTarget();
        }

        public void startPreview() {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(102);
        }

        public void preparePreview(CameraParameters parameters) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(109, parameters != null ? parameters.flatten() : null).sendToTarget();
        }

        public void startRecordingPreview(Surface recorderSurface) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(106, recorderSurface).sendToTarget();
        }

        public void setRecordSurfaceToTarget(boolean set) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.SET_RECORD_SURFACE, Boolean.valueOf(set)).sendToTarget();
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
        }

        public void stopPreview() {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(103);
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
        }

        public void autoFocus(Handler handler, CameraAFCallback cb) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(301, AFCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void cancelAutoFocus() {
            AndroidCameraManagerImpl2.this.mCameraHandler.removeMessages(301);
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(302);
        }

        public void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cb) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(303, AFMoveCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void takePicture(Handler handler, CameraShutterCallback shutter, CameraPictureCallback raw, CameraPictureCallback post, CameraPictureCallback jpeg) {
            AndroidCameraManagerImpl2.this.mCameraHandler.requestTakePicture(handler, ShutterCallbackForward.getNewInstance(handler, this, shutter), PictureCallbackForward.getNewInstance(handler, this, raw), null, PictureCallbackForward.getNewInstance(handler, this, jpeg));
        }

        public void setDisplayOrientation(int degrees) {
        }

        public void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback cb) {
            CamLog.m3d(CameraConstants.TAG, "setFaceDetectionCallback");
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.SET_FACE_DETECTION_LISTENER, FaceDetectionCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void startFaceDetection() {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl2.START_FACE_DETECTION);
        }

        public void stopFaceDetection() {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl2.STOP_FACE_DETECTION);
        }

        public void setBacklightDetectionCallback(Handler handler, CameraBacklightDetectionCallback callback) {
            CamLog.m3d(CameraConstants.TAG, "setBacklightDetectionCallback");
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.SET_BACK_LIGHT_DETECTION_LISTENER, callback).sendToTarget();
        }

        public void setLowlightDetectionCallback(Handler handler, CameraLowlightDetectionCallback callback) {
            CamLog.m3d(CameraConstants.TAG, "setLowlightDetectionCallback");
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.SET_LOW_LIGHT_DETECTION_LISTENER, callback).sendToTarget();
        }

        public void setZoomChangeCallback(ZoomChangeCallback zoomChangeCallback) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(304, zoomChangeCallback).sendToTarget();
        }

        public void setCineZoomCallback(CineZoomCallback cineZoomCallback) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(307, cineZoomCallback).sendToTarget();
        }

        public void setOutFocusCallback(OutFocusCallback outfocusCallback) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(306, outfocusCallback).sendToTarget();
        }

        public void setErrorCallback(Handler handler, CameraErrorCallback cb) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.SET_ERROR_CALLBACK, CameraErrorCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void setParameters(CameraParameters params) {
            if (params == null) {
                CamLog.m9v(CameraConstants.TAG, "null parameters in setParameters()");
            } else {
                AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(201, params.flatten()).sendToTarget();
            }
        }

        public void setParameters(CameraParameters params, ParamQueue paramQueue) {
            if (params == null) {
                CamLog.m9v(CameraConstants.TAG, "null parameters in setParameters()");
                return;
            }
            if (!(paramQueue == null || paramQueue.isParamQueueEmpty())) {
                synchronized (AndroidCameraManagerImpl2.this.mCameraHandler.mSyncObject) {
                    paramQueue.setParameters(params);
                }
            }
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(201, params.flatten()).sendToTarget();
        }

        public CameraParameters getParameters() {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(202);
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
            return AndroidCameraManagerImpl2.this.mParameters;
        }

        public void refreshParameters() {
            AndroidCameraManagerImpl2.this.mCameraHandler.sendEmptyMessage(203);
        }

        public List<Area> getMultiWindowFocusArea() {
            return AndroidCameraManagerImpl2.this.mCamera2.getMultiWindowFocusAreas();
        }

        public boolean isLowLightDetected(CameraParameters parameters) {
            return AndroidCameraManagerImpl2.this.mCamera2.isLowLightDetected();
        }

        public boolean isLowFps() {
            return AndroidCameraManagerImpl2.this.mCamera2.isLowFps();
        }

        public void setOneShotPreviewDataCallback(Handler handler, CameraPreviewDataCallback cb) {
            if (handler == null) {
                handler = AndroidCameraManagerImpl2.this.mCameraHandler;
            }
            AndroidCameraManagerImpl2.this.mCamera2.setOneShotPreviewCallback(OneShotCallbackForward.getNewInstance(handler, this, cb));
        }

        public void setPreviewSurfaceToTarget(boolean set) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.SET_PREVIEW_SURFACE, Boolean.valueOf(set)).sendToTarget();
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
        }

        public CameraParameters setNightandHDRorAuto(CameraParameters parameters, String mode, boolean isRecoding) {
            synchronized (AndroidCameraManagerImpl2.this.mCameraHandler.mSyncObject) {
                String flattenParameters = parameters.flatten();
                if (!(flattenParameters == null || flattenParameters.length() == 0 || AndroidCameraManagerImpl2.this.mParametersBackup == null)) {
                    AndroidCameraManagerImpl2.this.mParametersBackup.unflatten(flattenParameters);
                }
                Parameters2 param = (Parameters2) parameters;
                boolean isBinningMode = ParamConstants.VALUE_BINNING_MODE.equals(param.get(ParamConstants.KEY_BINNING_PARAM));
                if (!isRecoding || isBinningMode) {
                    parameters = AndroidCameraManagerImpl2.this.mCamera2.prepareTakePicture(param, mode);
                }
                setParameters(parameters);
            }
            return parameters;
        }

        public void restoreParameters(CameraParameters parameters) {
            synchronized (AndroidCameraManagerImpl2.this.mCameraHandler.mSyncObject) {
                CamLog.m3d(CameraConstants.TAG, "restoreParameters START parameters=" + parameters);
                String flattenParameters;
                if (parameters == null) {
                    flattenParameters = AndroidCameraManagerImpl2.this.mParametersBackup.flatten();
                    if (!(flattenParameters == null || AndroidCameraManagerImpl2.this.mParameters == null)) {
                        AndroidCameraManagerImpl2.this.mParameters.unflatten(flattenParameters);
                    }
                    setParameters(AndroidCameraManagerImpl2.this.mParameters);
                } else {
                    flattenParameters = AndroidCameraManagerImpl2.this.mParametersBackup.flatten();
                    if (!(flattenParameters == null || parameters == null)) {
                        parameters.unflatten(flattenParameters);
                    }
                }
                AndroidCameraManagerImpl2.this.mCamera2.restoreParameters();
                CamLog.m3d(CameraConstants.TAG, "restoreParameters END");
            }
        }

        public CameraParameters setSuperZoom(CameraParameters parameters) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(305, Integer.valueOf(parameters.getZoom())).sendToTarget();
            return parameters;
        }

        public void setSuperZoomSync(CameraParameters parameters) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(305, Integer.valueOf(parameters.getZoom())).sendToTarget();
        }

        public CameraInfomation getCameraInfo() {
            return CameraInfomation.createCameraInfo(AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics());
        }

        public void setManualCameraMetadataCb(LGCameraMetaDataCallback cb) {
            if (AndroidCameraManagerImpl2.this.mCamera2 == null) {
                CamLog.m11w(CameraConstants.TAG, "setManualCameraMetadataCb, camera is released, return");
            } else {
                AndroidCameraManagerImpl2.this.mCamera2.setLGManualModedataCb(cb);
            }
        }

        public void refreshCamera() {
        }

        public void setSecondCamera(LGCamera lgcamera) {
        }

        public void closeSecondCamera() {
        }

        public void switchCameraForInAndOut() {
        }

        public void takePictureDirect(Handler handler, CameraShutterCallback shutter, CameraPictureCallback raw, CameraPictureCallback postview, CameraPictureCallback jpeg) {
            AndroidCameraManagerImpl2.this.mShutterCallback = ShutterCallbackForward.getNewInstance(handler, this, shutter);
            AndroidCameraManagerImpl2.this.mJpegPictureCallback = PictureCallbackForward.getNewInstance(handler, this, jpeg);
            AndroidCameraManagerImpl2.this.mRawPictureCallback = PictureCallbackForward.getNewInstance(handler, this, raw);
            try {
                CamLog.m3d(CameraConstants.TAG, "#### take picture");
                AndroidCameraManagerImpl2.this.mCamera2.takePicture(AndroidCameraManagerImpl2.this.mShutterCallback, AndroidCameraManagerImpl2.this.mCaptureListener);
            } catch (ApiFailureException e) {
                e.printStackTrace();
            }
        }

        public void removeCallbacks() {
        }

        public void setNightandHDRorAutoSync(final CameraParameters parameters, final String mode, final boolean isRecoding) {
            CamLog.m3d(CameraConstants.TAG, "setNightandHDRorAutoSync START");
            AndroidCameraManagerImpl2.this.mCameraHandler.post(new Runnable() {
                public void run() {
                    AndroidCameraProxyImpl2.this.setNightandHDRorAuto(parameters, mode, isRecoding);
                }
            });
            AndroidCameraManagerImpl2.this.mCameraHandler.waitDone();
            CamLog.m3d(CameraConstants.TAG, "setNightandHDRorAutoSync END");
        }

        public void setGPSlocation(Location loc) {
            AndroidCameraManagerImpl2.this.mCamera2.setGPSlocation(loc);
        }

        public void setObtDataListener(ObtDataListener cb) {
        }

        public void setLongshot(boolean enable) {
        }

        public void setOpticZoomMetadataCb(CameraMetaDataCallback cb) {
        }

        public void setFingerDetectionDataListener(FingerDetectionDataListener cb) {
        }

        public void setEVCallbackDataListener(EVCallbackListener cb) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    AndroidCameraManagerImpl2.this.mCamera2.setEVCallbackDataListener(cb);
                } catch (Exception e) {
                    CamLog.m5e(CameraConstants.TAG, "-AE control- setEVCallbackDataListener Exception" + e);
                }
            }
        }

        public void setCineZoom(CameraParameters params, int command, int mZoomSpeed, RectF guideRectF) {
            AndroidCameraManagerImpl2.this.showNotImplemented("setCineZoom");
        }

        public void setCineZoomListener(CineZoomStateListener listener) {
            AndroidCameraManagerImpl2.this.showNotImplemented("setCineZoomListener");
        }

        public void setHistogramDataCallback(Handler handler, CameraHistogramDataCallback cb) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    AndroidCameraManagerImpl2.this.mCamera2.setCameraHistogramDataCallback(cb);
                } catch (Exception e) {
                    CamLog.m5e(CameraConstants.TAG, "setHistogramDataCallback failed!" + e);
                }
            }
        }

        public CameraParameters convertParameter(Parameters param) {
            return null;
        }

        public void setImageDataCallback(int imageType, CameraImageCallback imageCb) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(108, imageType, 0, imageCb).sendToTarget();
        }

        public void setImageMetaCallback(CameraImageMetaCallback meatCb) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(107, meatCb).sendToTarget();
        }

        public void addRawImageCallbackBuffer(byte[] buffer) {
        }

        public SizeF getSensorPhysicalSize(float focalLength, float horiFov, float vertFov) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    return (SizeF) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                } catch (IllegalStateException e) {
                    CamLog.m6e(CameraConstants.TAG, "getSensorPysicalSize fail!", e);
                }
            }
            return new SizeF(0.0f, 0.0f);
        }

        public Size getSensorPixelArraySize(CameraParameters parameters, boolean isMtkChipset) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    return (Size) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
                } catch (IllegalStateException e) {
                    CamLog.m6e(CameraConstants.TAG, "getSensorPixelArraySize fail!", e);
                }
            }
            return new Size(0, 0);
        }

        public Size getSensorActiveArrarySize(CameraParameters parameters, boolean isMtkChipset) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    Rect sensorActiveArray = (Rect) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    return new Size(sensorActiveArray.width(), sensorActiveArray.height());
                } catch (IllegalStateException e) {
                    CamLog.m6e(CameraConstants.TAG, "getSensorActiveArrarySize fail!", e);
                }
            }
            return new Size(0, 0);
        }

        public float getHorizontalViewAngle(CameraParameters parameters) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    return (float) ((2.0d * Math.atan2((double) (((SizeF) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)).getWidth() / 2.0f), (double) getFocalLength(null))) * 57.29577951308232d);
                } catch (IllegalStateException e) {
                    CamLog.m6e(CameraConstants.TAG, "getHorizontalViewAngle fail!", e);
                }
            }
            return 60.02726f;
        }

        public float getVerticalViewAngle(CameraParameters parameters) {
            if (AndroidCameraManagerImpl2.this.mCamera2 != null) {
                try {
                    return (float) ((2.0d * Math.atan2((double) (((SizeF) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)).getHeight() / 2.0f), (double) getFocalLength(null))) * 57.29577951308232d);
                } catch (IllegalStateException e) {
                    CamLog.m6e(CameraConstants.TAG, "getVerticalViewAngle fail!", e);
                }
            }
            return 46.849403f;
        }

        public float getFocalLength(CameraParameters parameters) {
            if (AndroidCameraManagerImpl2.this.mCamera2 == null) {
                return 4.03f;
            }
            try {
                float[] focalLengthArray = (float[]) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                if (focalLengthArray.length > 0) {
                    return focalLengthArray[0];
                }
                return 4.03f;
            } catch (IllegalStateException e) {
                CamLog.m6e(CameraConstants.TAG, "getFocalLength fail!", e);
                return 4.03f;
            }
        }

        public float getCameraFnumber(CameraParameters parameters) {
            if (AndroidCameraManagerImpl2.this.mCamera2 == null) {
                return 0.0f;
            }
            try {
                float[] apertures = (float[]) AndroidCameraManagerImpl2.this.mCamera2.getCameraCharacteristics().get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                if (apertures.length > 0) {
                    return apertures[0];
                }
                return 0.0f;
            } catch (IllegalStateException e) {
                CamLog.m6e(CameraConstants.TAG, "getFocalLength fail!", e);
                return 0.0f;
            }
        }

        public void updateRequestCapture(long exposureTime, int sensorSensitivity) {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl2.UPDATE_PANORAMA_REQUEST, sensorSensitivity, 0, Long.valueOf(exposureTime)).sendToTarget();
        }

        public void setLuxIndexMetadata(LGCameraMetaDataCallback cb) {
            AndroidCameraManagerImpl2.this.mCamera2.setLuxIndexMetadata(cb);
        }

        public void stopBurstShot() {
            AndroidCameraManagerImpl2.this.mCameraHandler.obtainMessage(110).sendToTarget();
        }
    }

    private class CameraHandler extends Handler {
        public Object mSyncObject = new Object();

        CameraHandler(Looper looper) {
            super(looper);
        }

        public void requestTakePicture(Handler handler, final ShutterCallbackForward shutter, final PictureCallbackForward raw, PictureCallbackForward postView, final PictureCallbackForward jpeg) {
            post(new Runnable() {
                public void run() {
                    AndroidCameraManagerImpl2.this.mShutterCallback = shutter;
                    AndroidCameraManagerImpl2.this.mRawPictureCallback = raw;
                    AndroidCameraManagerImpl2.this.mJpegPictureCallback = jpeg;
                    try {
                        CamLog.m3d(CameraConstants.TAG, "#### take picture");
                        AndroidCameraManagerImpl2.this.mCamera2.takePicture(AndroidCameraManagerImpl2.this.mShutterCallback, AndroidCameraManagerImpl2.this.mCaptureListener);
                    } catch (ApiFailureException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public boolean waitDone() {
            final Object waitDoneLock = new Object();
            Runnable unlockRunnable = new Runnable() {
                public void run() {
                    synchronized (waitDoneLock) {
                        waitDoneLock.notifyAll();
                    }
                }
            };
            synchronized (waitDoneLock) {
                AndroidCameraManagerImpl2.this.mCameraHandler.post(unlockRunnable);
                try {
                    waitDoneLock.wait();
                } catch (InterruptedException e) {
                    CamLog.m9v(CameraConstants.TAG, "waitDone interrupted");
                    return false;
                }
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:88:0x042f  */
        public void handleMessage(android.os.Message r10) {
            /*
            r9 = this;
            r8 = 0;
            r3 = r10.what;	 Catch:{ RuntimeException -> 0x0021 }
            switch(r3) {
                case 1: goto L_0x0038;
                case 2: goto L_0x00aa;
                case 101: goto L_0x00ce;
                case 102: goto L_0x0123;
                case 103: goto L_0x0159;
                case 104: goto L_0x00e4;
                case 105: goto L_0x0311;
                case 106: goto L_0x013c;
                case 107: goto L_0x0340;
                case 108: goto L_0x034f;
                case 109: goto L_0x00fa;
                case 110: goto L_0x03cc;
                case 201: goto L_0x0262;
                case 202: goto L_0x02a9;
                case 203: goto L_0x0302;
                case 301: goto L_0x01ad;
                case 302: goto L_0x01ca;
                case 303: goto L_0x01dc;
                case 304: goto L_0x0172;
                case 305: goto L_0x0327;
                case 306: goto L_0x019e;
                case 307: goto L_0x0188;
                case 461: goto L_0x01f2;
                case 462: goto L_0x0208;
                case 463: goto L_0x0214;
                case 464: goto L_0x024c;
                case 501: goto L_0x02f9;
                case 601: goto L_0x0220;
                case 602: goto L_0x0236;
                case 701: goto L_0x0360;
                case 801: goto L_0x039b;
                case 802: goto L_0x03d7;
                case 900: goto L_0x03c1;
                case 901: goto L_0x03ae;
                default: goto L_0x0006;
            };	 Catch:{ RuntimeException -> 0x0021 }
        L_0x0006:
            r3 = new java.lang.RuntimeException;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0021 }
            r4.<init>();	 Catch:{ RuntimeException -> 0x0021 }
            r5 = "Invalid CameraProxy message=";
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r5 = r10.what;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0021 }
            r3.<init>(r4);	 Catch:{ RuntimeException -> 0x0021 }
            throw r3;	 Catch:{ RuntimeException -> 0x0021 }
        L_0x0021:
            r0 = move-exception;
            r3 = r10.what;
            r4 = 201; // 0xc9 float:2.82E-43 double:9.93E-322;
            if (r3 != r4) goto L_0x03ea;
        L_0x0028:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;
            r3 = r3.mCamera2;
            if (r3 == 0) goto L_0x03ea;
        L_0x0030:
            r3 = "CameraApp";
            r4 = "SET_PARAMETERS Exception : ";
            com.lge.camera.util.CamLog.m6e(r3, r4, r0);
        L_0x0037:
            return;
        L_0x0038:
            r3 = "CameraApp";
            r4 = "#### OPEN_CAMERA START";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r4 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ ApiFailureException -> 0x0093 }
            r5 = new com.lge.camera.device.api2.CameraOps;	 Catch:{ ApiFailureException -> 0x0093 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ ApiFailureException -> 0x0093 }
            r3 = r3.mContext;	 Catch:{ ApiFailureException -> 0x0093 }
            r3 = r3.get();	 Catch:{ ApiFailureException -> 0x0093 }
            r3 = (android.content.Context) r3;	 Catch:{ ApiFailureException -> 0x0093 }
            r6 = r10.arg1;	 Catch:{ ApiFailureException -> 0x0093 }
            r5.<init>(r3, r9, r6);	 Catch:{ ApiFailureException -> 0x0093 }
            r4.mCamera2 = r5;	 Catch:{ ApiFailureException -> 0x0093 }
        L_0x0057:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            if (r3 == 0) goto L_0x009c;
        L_0x005f:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 1;
            r3.mParametersIsDirty = r4;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mParamsToSet;	 Catch:{ RuntimeException -> 0x0021 }
            if (r3 != 0) goto L_0x007c;
        L_0x006d:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.getParameters();	 Catch:{ RuntimeException -> 0x0021 }
            r3.mParamsToSet = r4;	 Catch:{ RuntimeException -> 0x0021 }
        L_0x007c:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.getParameters();	 Catch:{ RuntimeException -> 0x0021 }
            r3.mParametersBackup = r4;	 Catch:{ RuntimeException -> 0x0021 }
        L_0x008b:
            r3 = "CameraApp";
            r4 = "#### OPEN_CAMERA END";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0093:
            r0 = move-exception;
            r3 = "CameraApp";
            r4 = "Cannot create camera ops!";
            com.lge.camera.util.CamLog.m5e(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0057;
        L_0x009c:
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            if (r3 == 0) goto L_0x008b;
        L_0x00a0:
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r10.arg1;	 Catch:{ RuntimeException -> 0x0021 }
            r3.onDeviceOpenFailure(r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x008b;
        L_0x00aa:
            r3 = "CameraApp";
            r4 = "#### RELEASE START";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ ApiFailureException -> 0x00c9 }
            r3 = r3.mCamera2;	 Catch:{ ApiFailureException -> 0x00c9 }
            r3.closeDevice();	 Catch:{ ApiFailureException -> 0x00c9 }
        L_0x00ba:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 0;
            r3.mCamera2 = r4;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = "CameraApp";
            r4 = "#### RELEASE END";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x00c9:
            r0 = move-exception;
            r0.printStackTrace();	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x00ba;
        L_0x00ce:
            r3 = "CameraApp";
            r4 = "#### SET_PREVIEW_TEXTURE_ASYNC";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (android.graphics.SurfaceTexture) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setPreviewTexture(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x00e4:
            r3 = "CameraApp";
            r4 = "#### SET_PREVIEW_DISPLAY_ASYNC";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (android.view.SurfaceHolder) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setPreviewDisplay(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x00fa:
            r3 = "CameraApp";
            r4 = "#### PREPARE_PREVIEW_ASYNC START";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r2 = new com.lge.camera.device.api2.Parameters2;	 Catch:{ RuntimeException -> 0x0021 }
            r2.<init>();	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            if (r3 == 0) goto L_0x0111;
        L_0x010a:
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.String) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r2.unflatten(r3);	 Catch:{ RuntimeException -> 0x0021 }
        L_0x0111:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3.preparePreview(r2);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = "CameraApp";
            r4 = "#### PREPARE_PREVIEW_ASYNC END";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0123:
            r3 = "CameraApp";
            r4 = "#### START_PREVIEW_ASYNC START";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3.startPreview();	 Catch:{ RuntimeException -> 0x0021 }
            r3 = "CameraApp";
            r4 = "#### START_PREVIEW_ASYNC END";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x013c:
            r3 = "CameraApp";
            r4 = "#### START_RECORD_PREVIEW_ASYNC START";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (android.view.Surface) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.startPreviewForRecording(r3);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = "CameraApp";
            r4 = "#### START_RECORD_PREVIEW_ASYNC END";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0159:
            r3 = "CameraApp";
            r4 = "#### STOP_PREVIEW START";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3.stopPreview();	 Catch:{ RuntimeException -> 0x0021 }
            r3 = "CameraApp";
            r4 = "#### STOP_PREVIEW END";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0172:
            r3 = "CameraApp";
            r4 = "#### SET_ZOOM_CHANGE_LISTENER";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.ZoomChangeCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setZoomChangeCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0188:
            r3 = "CameraApp";
            r4 = "#### SET_ZOOM_CHANGE_LISTENER";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.CineZoomCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setCineZoomCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x019e:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.OutFocusCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setOutFocusCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x01ad:
            r3 = "CameraApp";
            r4 = "#### AUTO_FOCUS";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = "CameraApp";
            r4 = "TIME CHECK : Auto focus [START] - autoFocus()";
            android.util.Log.d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.CameraCallbackForwards.AFCallbackForward) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.autoFocus(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x01ca:
            r3 = "CameraApp";
            r4 = "#### CANCEL_AUTO_FOCUS";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3.cancelAutoFocus();	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x01dc:
            r3 = "CameraApp";
            r4 = "#### SET_AUTO_FOCUS_MOVE_CALLBACK";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.CameraCallbackForwards.AFMoveCallbackForward) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setAutoFocusMoveCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x01f2:
            r3 = "CameraApp";
            r4 = "#### SET_FACE_DETECTION_LISTENER";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.CameraCallbackForwards.FaceDetectionCallbackForward) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setFaceDetectionCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0208:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 1;
            r3.setFaceDetection(r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0214:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 0;
            r3.setFaceDetection(r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0220:
            r3 = "CameraApp";
            r4 = "#### SET_BACK_LIGHT_DETECTION_LISTENER";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.CameraBacklightDetectionCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setBacklightDetectionCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0236:
            r3 = "CameraApp";
            r4 = "#### SET_LOW_LIGHT_DETECTION_LISTENER";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.CameraLowlightDetectionCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setLowlightDetectionCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x024c:
            r3 = "CameraApp";
            r4 = "#### SET_ERROR_CALLBACK";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.CameraCallbackForwards.CameraErrorCallbackForward) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setErrorCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0262:
            r3 = "CameraApp";
            r4 = "#### SET_PARAMETERS";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 1;
            r3.mParametersIsDirty = r4;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mParamsToSet;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.String) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.unflatten(r3);	 Catch:{ RuntimeException -> 0x0021 }
            r4 = "CameraApp";
            r3 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0021 }
            r3.<init>();	 Catch:{ RuntimeException -> 0x0021 }
            r5 = "setParameters: ";
            r5 = r3.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.String) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r5.append(r3);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.toString();	 Catch:{ RuntimeException -> 0x0021 }
            com.lge.camera.util.CamLog.m3d(r4, r3);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.mParamsToSet;	 Catch:{ RuntimeException -> 0x0021 }
            r3.setParameters(r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x02a9:
            r3 = "CameraApp";
            r4 = "#### GET_PARAMETERS";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mParametersIsDirty;	 Catch:{ RuntimeException -> 0x0021 }
            if (r3 == 0) goto L_0x0037;
        L_0x02b8:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.getParameters();	 Catch:{ RuntimeException -> 0x0021 }
            r3.mParameters = r4;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mParameters;	 Catch:{ RuntimeException -> 0x0021 }
            if (r3 == 0) goto L_0x02f1;
        L_0x02cf:
            r3 = "CameraApp";
            r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0021 }
            r4.<init>();	 Catch:{ RuntimeException -> 0x0021 }
            r5 = "getParameters: ";
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r5 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r5 = r5.mParameters;	 Catch:{ RuntimeException -> 0x0021 }
            r5 = r5.flatten();	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0021 }
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
        L_0x02f1:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 0;
            r3.mParametersIsDirty = r4;	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x02f9:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = "ENABLE_SHUTTER_SOUND";
            r3.showNotImplemented(r4);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0302:
            r3 = "CameraApp";
            r4 = "#### REFRESH_PARAMETERS";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 1;
            r3.mParametersIsDirty = r4;	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0311:
            r3 = "CameraApp";
            r4 = "#### SET_ONE_SHOT_PREVIEW_CALLBACK";
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.CameraCallbackForwards.OneShotCallbackForward) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setOneShotPreviewCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0327:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = 1;
            r3.mParametersIsDirty = r4;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.Integer) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.intValue();	 Catch:{ RuntimeException -> 0x0021 }
            r4.setSuperZoom(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0340:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.CameraImageMetaCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setImageCaptureCallback(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x034f:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r5 = r10.arg1;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (com.lge.camera.device.ICameraCallback.CameraImageCallback) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r4.setImageCallbackListener(r5, r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x0360:
            r3 = "CameraApp";
            r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0021 }
            r4.<init>();	 Catch:{ RuntimeException -> 0x0021 }
            r5 = "UPDATE_PANORAMA_REQUEST : exposureTime";
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r5 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r5 = ", sensorSensitivity : ";
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r5 = r10.arg1;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r4.toString();	 Catch:{ RuntimeException -> 0x0021 }
            com.lge.camera.util.CamLog.m5e(r3, r4);	 Catch:{ RuntimeException -> 0x0021 }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.Long) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r6 = r3.longValue();	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.arg1;	 Catch:{ RuntimeException -> 0x0021 }
            r4.updateRequestParameter(r6, r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x039b:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.Boolean) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.booleanValue();	 Catch:{ RuntimeException -> 0x0021 }
            r4.setRecordSurface(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x03ae:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.Boolean) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.booleanValue();	 Catch:{ RuntimeException -> 0x0021 }
            r4.setRecordingStream(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x03c1:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3.setForStopRecording();	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x03cc:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3.stopBurstShot();	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x03d7:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ RuntimeException -> 0x0021 }
            r4 = r3.mCamera2;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r10.obj;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = (java.lang.Boolean) r3;	 Catch:{ RuntimeException -> 0x0021 }
            r3 = r3.booleanValue();	 Catch:{ RuntimeException -> 0x0021 }
            r4.setPreviewSurface(r3);	 Catch:{ RuntimeException -> 0x0021 }
            goto L_0x0037;
        L_0x03ea:
            r3 = r10.what;
            r4 = 2;
            if (r3 == r4) goto L_0x0427;
        L_0x03ef:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;
            r3 = r3.mCamera2;
            if (r3 == 0) goto L_0x0427;
        L_0x03f7:
            r3 = "CameraApp";
            r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x041e }
            r4.<init>();	 Catch:{ Exception -> 0x041e }
            r5 = "#### RuntimeException RELEASE CAMERA : ";
            r4 = r4.append(r5);	 Catch:{ Exception -> 0x041e }
            r4 = r4.append(r0);	 Catch:{ Exception -> 0x041e }
            r4 = r4.toString();	 Catch:{ Exception -> 0x041e }
            com.lge.camera.util.CamLog.m3d(r3, r4);	 Catch:{ Exception -> 0x041e }
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;	 Catch:{ Exception -> 0x041e }
            r3 = r3.mCamera2;	 Catch:{ Exception -> 0x041e }
            r3.closeDevice();	 Catch:{ Exception -> 0x041e }
        L_0x0418:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;
            r3.mCamera2 = r8;
        L_0x041d:
            throw r0;
        L_0x041e:
            r1 = move-exception;
            r3 = "CameraApp";
            r4 = "Fail to release the camera.";
            com.lge.camera.util.CamLog.m5e(r3, r4);
            goto L_0x0418;
        L_0x0427:
            r3 = com.lge.camera.device.api2.AndroidCameraManagerImpl2.this;
            r3 = r3.mCamera2;
            if (r3 != 0) goto L_0x041d;
        L_0x042f:
            r3 = "CameraApp";
            r4 = "Cannot handle message, mCamera is null.";
            com.lge.camera.util.CamLog.m11w(r3, r4);
            r0.printStackTrace();
            goto L_0x0037;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.AndroidCameraManagerImpl2.CameraHandler.handleMessage(android.os.Message):void");
        }
    }

    public AndroidCameraManagerImpl2(Context context) {
        this.mContext = new WeakReference(context);
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        this.mCameraHandler = new CameraHandler(ht.getLooper());
    }

    private void showNotImplemented(String funcName) {
        CamLog.m5e(CameraConstants.TAG, "[" + funcName + "]------------------------------------- Not Implemented ---");
    }

    public CameraProxy cameraOpen(Handler handler, int cameraId, CameraOpenErrorCallback callback) {
        this.mCameraHandler.obtainMessage(1, cameraId, 0, CameraOpenErrorCallbackForward.getNewInstance(handler, callback)).sendToTarget();
        this.mCameraHandler.waitDone();
        if (this.mCamera2 != null) {
            return new AndroidCameraProxyImpl2(this, null);
        }
        return null;
    }

    public void setLGProxyDataListener(boolean set) {
        showNotImplemented("setLGProxyDataListener");
    }

    public boolean isLowDistance() {
        showNotImplemented("isLowDistance");
        return false;
    }

    public void setParamToBackup(String key, Object value) {
        synchronized (this.mCameraHandler.mSyncObject) {
            if (this.mParametersBackup != null) {
                if (value instanceof String) {
                    this.mParametersBackup.set(key, (String) value);
                } else if (value instanceof RggbChannelVector) {
                    this.mParametersBackup.setColorCorrectionGains((RggbChannelVector) value);
                } else {
                    this.mParametersBackup.set(key, ((Integer) value).intValue());
                }
            }
        }
    }

    public void setCameraOpsModuleBridge(CameraOpsModuleBridge bridge) {
        if (this.mCamera2 != null) {
            this.mCamera2.setCameraOpsModuleBridge(bridge);
        }
    }
}
