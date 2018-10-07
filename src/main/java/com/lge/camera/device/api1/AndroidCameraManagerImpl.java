package com.lge.camera.device.api1;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.camera.app.VideoRecorder;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraCallbackForwards.AFCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.AFMoveCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.CameraErrorCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.CameraHistogramDataCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.CameraOpenErrorCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.FaceDetectionCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.OneShotCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.PictureCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.ShutterCallbackForward;
import com.lge.camera.device.CameraDeviceUtils;
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
import com.lge.hardware.LGCamera.LGParameters;
import com.lge.hardware.LGCamera.ObtDataListener;
import com.lge.hardware.LGCamera.ProxyData;
import com.lge.hardware.LGCamera.ProxyDataListener;
import java.io.IOException;
import java.util.List;

public class AndroidCameraManagerImpl implements CameraManager, ProxyDataListener {
    private static final int AUTO_FOCUS = 301;
    private static final int CANCEL_AUTO_FOCUS = 302;
    private static final int CLOSE_SECOND_CAMERA = 8;
    private static final int GET_PARAMETERS = 202;
    private static final int LOCK = 5;
    private static final int LOW_DISTANCE = 20;
    private static final int OPEN_CAMERA = 1;
    private static final int RECONNECT = 3;
    private static final int REFRESH_CAMERA = 6;
    private static final int REFRESH_PARAMETERS = 203;
    private static final int RELEASE = 2;
    private static final int SET_AUTO_FOCUS_MOVE_CALLBACK = 303;
    private static final int SET_DISPLAY_ORIENTATION = 501;
    private static final int SET_ERROR_CALLBACK = 464;
    private static final int SET_FACE_DETECTION_LISTENER = 461;
    private static final int SET_ONE_SHOT_PREVIEW_CALLBACK = 105;
    private static final int SET_PARAMETERS = 201;
    private static final int SET_PREVIEW_DISPLAY_ASYNC = 104;
    private static final int SET_PREVIEW_TEXTURE_ASYNC = 101;
    private static final int SET_SECOND_CAMERA = 7;
    private static final int SET_SUPER_ZOOM_WITH_SYNC = 305;
    private static final int START_FACE_DETECTION = 462;
    private static final int START_PREVIEW_ASYNC = 102;
    private static final int STOP_FACE_DETECTION = 463;
    private static final int STOP_PREVIEW = 103;
    private static final int SWITCH_CAMERA_FOR_IN_AND_OUT = 9;
    private static final int UNLOCK = 4;
    private Camera mCamera;
    private CameraHandler mCameraHandler;
    int mCameraId;
    private int mCnt_low = 0;
    private int mCnt_normal = 0;
    private LGCamera mLGCamera;
    private LGCamera mLGCameraPrev;
    private LGParameters mLGParameters;
    private CameraParameters mParameters;
    private Parameters mParametersBackup;
    private boolean mParametersIsDirty;
    private CameraParameters mParamsToSet;
    private IOException mReconnectIOException;

    public class AndroidCameraProxyImpl implements CameraProxy {
        private AndroidCameraProxyImpl() {
            if (AndroidCameraManagerImpl.this.mCamera == null) {
                throw new AssertionError();
            }
        }

        public Object getCamera() {
            return AndroidCameraManagerImpl.this.mCamera;
        }

        public void release(boolean releaseMessage) {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(2);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
            if (releaseMessage) {
                AndroidCameraManagerImpl.this.mCameraHandler.removeCallbacksAndMessages(null);
                AndroidCameraManagerImpl.this.mCameraHandler.getLooper().quit();
            }
        }

        public boolean reconnect(Handler handler, CameraOpenErrorCallback cb) {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(3);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
            CameraOpenErrorCallback cbforward = CameraOpenErrorCallbackForward.getNewInstance(handler, cb);
            if (AndroidCameraManagerImpl.this.mReconnectIOException == null) {
                return true;
            }
            if (cbforward != null) {
                cbforward.onReconnectionFailure(AndroidCameraManagerImpl.this);
            }
            return false;
        }

        public void setCineZoom(CameraParameters params, int command, int mZoomSpeed, RectF guideRectF) {
            if (AndroidCameraManagerImpl.this.mLGParameters == null || guideRectF == null || params == null || AndroidCameraManagerImpl.this.mCamera == null || AndroidCameraManagerImpl.this.mLGCamera == null) {
                CamLog.m3d(CameraConstants.TAG, "mLGParameters or guideRectF is null");
                return;
            }
            AndroidCameraManagerImpl.this.mLGParameters.setCineZoomParameters((Parameters) params.getParameters(), command, guideRectF.left, guideRectF.top, guideRectF.width(), guideRectF.height(), mZoomSpeed, 0);
            AndroidCameraManagerImpl.this.mParametersIsDirty = true;
        }

        public void setCineZoomListener(CineZoomStateListener listener) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                AndroidCameraManagerImpl.this.mLGCamera.setCineZoomStateListener(listener);
            }
        }

        public void unlock() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(4);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public void lock() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(5);
        }

        public void refreshCamera() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(6);
        }

        public void setSecondCamera(LGCamera lgcamera) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(7, lgcamera).sendToTarget();
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public void closeSecondCamera() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(8);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public void switchCameraForInAndOut() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(9);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public void setPreviewTexture(SurfaceTexture surfaceTexture) {
            CamLog.m3d(CameraConstants.TAG, "-hybrid- setPreviewTexture");
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(101, surfaceTexture).sendToTarget();
        }

        public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
            CamLog.m3d(CameraConstants.TAG, "-hybrid- setPreviewDisplay surfaceHolder = " + surfaceHolder);
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(104, surfaceHolder).sendToTarget();
        }

        public void startPreview() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(102);
        }

        public void preparePreview(CameraParameters parameters) {
        }

        public void stopPreview() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(103);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
        }

        public void autoFocus(Handler handler, CameraAFCallback cb) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(301, AFCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void cancelAutoFocus() {
            AndroidCameraManagerImpl.this.mCameraHandler.removeMessages(301);
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(302);
        }

        public void setAutoFocusMoveCallback(Handler handler, CameraAFMoveCallback cb) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(303, AFMoveCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void takePicture(Handler handler, CameraShutterCallback shutter, CameraPictureCallback raw, CameraPictureCallback post, CameraPictureCallback jpeg) {
            AndroidCameraManagerImpl.this.mCameraHandler.requestTakePicture(ShutterCallbackForward.getNewInstance(handler, this, shutter), PictureCallbackForward.getNewInstance(handler, this, raw), PictureCallbackForward.getNewInstance(handler, this, post), PictureCallbackForward.getNewInstance(handler, this, jpeg));
        }

        public void takePictureDirect(Handler handler, CameraShutterCallback shutter, CameraPictureCallback raw, CameraPictureCallback post, CameraPictureCallback jpeg) {
            try {
                CamLog.m3d(CameraConstants.TAG, "#### take picture directly - start");
                if (AndroidCameraManagerImpl.this.mCamera == null) {
                    throw new RuntimeException("Camera instance is null.");
                }
                AndroidCameraManagerImpl.this.mCamera.takePicture(ShutterCallbackForward.getNewInstance(handler, this, shutter), PictureCallbackForward.getNewInstance(handler, this, raw), PictureCallbackForward.getNewInstance(handler, this, post), PictureCallbackForward.getNewInstance(handler, this, jpeg));
                CamLog.m3d(CameraConstants.TAG, "#### take picture directly - end");
            } catch (RuntimeException e) {
                CamLog.m5e(CameraConstants.TAG, "take picture failed.");
                throw e;
            }
        }

        public void setDisplayOrientation(int degrees) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_DISPLAY_ORIENTATION, degrees, 0).sendToTarget();
        }

        public void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback cb) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_FACE_DETECTION_LISTENER, FaceDetectionCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void startFaceDetection() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl.START_FACE_DETECTION);
        }

        public void stopFaceDetection() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(AndroidCameraManagerImpl.STOP_FACE_DETECTION);
        }

        public void setBacklightDetectionCallback(Handler handler, final CameraBacklightDetectionCallback callback) {
            CameraMetaDataCallback cb;
            if (callback == null) {
                cb = null;
            } else {
                cb = new CameraMetaDataCallback() {
                    public void onCameraMetaData(byte[] data, Camera camera) {
                        boolean isDetected = true;
                        if (data != null && data.length > 0) {
                            if (data[0] != (byte) 1) {
                                isDetected = false;
                            }
                            callback.onDetected(isDetected);
                        }
                    }
                };
            }
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    AndroidCameraManagerImpl.this.mLGCamera.setHdrMetadataCb(cb);
                } catch (NoSuchMethodError e) {
                    CamLog.m4d(CameraConstants.TAG, "setBacklightDetectionCallback ", e);
                }
            }
        }

        public void setZoomChangeCallback(ZoomChangeCallback zoomChangeCallback) {
        }

        public void setCineZoomCallback(CineZoomCallback cineZoomCallback) {
        }

        public void setErrorCallback(Handler handler, CameraErrorCallback cb) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(AndroidCameraManagerImpl.SET_ERROR_CALLBACK, CameraErrorCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void removeCallbacks() {
            if (AndroidCameraManagerImpl.this.mCameraHandler != null) {
                AndroidCameraManagerImpl.this.mCameraHandler.removeCallbacksAndMessages(null);
            }
        }

        public void setParameters(CameraParameters params) {
            setParameters(params, null);
        }

        public void setParameters(CameraParameters params, ParamQueue paramQueue) {
            if (params == null) {
                CamLog.m9v(CameraConstants.TAG, "null parameters in setParameters()");
                return;
            }
            if (!(paramQueue == null || paramQueue.isParamQueueEmpty())) {
                synchronized (AndroidCameraManagerImpl.this.mCameraHandler.mSyncObject) {
                    paramQueue.setParameters(params);
                }
            }
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(201, params.flatten()).sendToTarget();
        }

        public CameraParameters getParameters() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(202);
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
            return AndroidCameraManagerImpl.this.mParameters;
        }

        public void refreshParameters() {
            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(203);
        }

        public List<Area> getMultiWindowFocusArea() {
            if (AndroidCameraManagerImpl.this.mCamera == null || AndroidCameraManagerImpl.this.mLGCamera == null) {
                return null;
            }
            LGParameters params = AndroidCameraManagerImpl.this.mLGCamera.getLGParameters();
            if (params != null) {
                return params.getMultiWindowFocusAreas();
            }
            return null;
        }

        public void setOneShotPreviewDataCallback(Handler handler, CameraPreviewDataCallback cb) {
            AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(105, OneShotCallbackForward.getNewInstance(handler, this, cb)).sendToTarget();
        }

        public void setPreviewSurfaceToTarget(boolean set) {
        }

        public CameraParameters setNightandHDRorAuto(CameraParameters parameters, String mode, boolean isRecoding) {
            synchronized (AndroidCameraManagerImpl.this.mCameraHandler.mSyncObject) {
                CamLog.m3d(CameraConstants.TAG, "setNightandHDRorAuto START mode=" + mode + " video=" + isRecoding);
                if (AndroidCameraManagerImpl.this.mCamera == null || AndroidCameraManagerImpl.this.mLGCamera == null || AndroidCameraManagerImpl.this.mLGParameters == null || parameters == null) {
                    CamLog.m3d(CameraConstants.TAG, "exit setNightandHDRorAuto");
                    parameters = null;
                } else {
                    String flattenParameters = parameters.flatten();
                    if (!(flattenParameters == null || flattenParameters.length() == 0 || AndroidCameraManagerImpl.this.mParametersBackup == null)) {
                        AndroidCameraManagerImpl.this.mParametersBackup.unflatten(flattenParameters);
                    }
                    Parameters changedParams = null;
                    try {
                        changedParams = AndroidCameraManagerImpl.this.mLGParameters.setNightandHDRorAuto((Parameters) parameters.getParameters(), mode, isRecoding);
                    } catch (NoSuchMethodError e) {
                        CamLog.m3d(CameraConstants.TAG, "fail setNightandHDRorAuto=" + e);
                    } catch (NumberFormatException e2) {
                        CamLog.m3d(CameraConstants.TAG, "fail setNightandHDRorAuto=" + e2);
                    } catch (RuntimeException e3) {
                        CamLog.m3d(CameraConstants.TAG, "fail setNightandHDRorAuto=" + e3);
                    }
                    if (changedParams != null) {
                        AndroidCameraManagerImpl.this.mParameters = new Parameters1(changedParams);
                    }
                    CamLog.m3d(CameraConstants.TAG, "setNightandHDRorAuto END");
                    parameters.setParameters(changedParams);
                }
            }
            return parameters;
        }

        public void setNightandHDRorAutoSync(final CameraParameters parameters, final String mode, final boolean isRecoding) {
            CamLog.m3d(CameraConstants.TAG, "setNightandHDRorAutoSync START");
            AndroidCameraManagerImpl.this.mCameraHandler.post(new Runnable() {
                public void run() {
                    AndroidCameraProxyImpl.this.setNightandHDRorAuto(parameters, mode, isRecoding);
                    if (isRecoding) {
                        VideoRecorder.setWaitStartRecoding(false);
                    }
                }
            });
            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
            CamLog.m3d(CameraConstants.TAG, "setNightandHDRorAutoSync END");
        }

        public void restoreParameters(CameraParameters parameters) {
            synchronized (AndroidCameraManagerImpl.this.mCameraHandler.mSyncObject) {
                CamLog.m3d(CameraConstants.TAG, "restoreParameters START parameters=" + parameters);
                String flattenParameters;
                if (parameters == null) {
                    flattenParameters = AndroidCameraManagerImpl.this.mParametersBackup.flatten();
                    if (!(flattenParameters == null || AndroidCameraManagerImpl.this.mParameters == null)) {
                        AndroidCameraManagerImpl.this.mParameters.unflatten(flattenParameters);
                    }
                    setParameters(AndroidCameraManagerImpl.this.mParameters);
                } else {
                    flattenParameters = AndroidCameraManagerImpl.this.mParametersBackup.flatten();
                    if (!(flattenParameters == null || parameters == null)) {
                        parameters.unflatten(flattenParameters);
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "restoreParameters END");
            }
        }

        public CameraParameters setSuperZoom(CameraParameters parameters) {
            CamLog.m3d(CameraConstants.TAG, "setSuperZoom");
            if (AndroidCameraManagerImpl.this.mCamera == null || AndroidCameraManagerImpl.this.mLGCamera == null || AndroidCameraManagerImpl.this.mLGParameters == null) {
                CamLog.m3d(CameraConstants.TAG, "exit setSuperZoom");
                return null;
            }
            CamLog.m3d(CameraConstants.TAG, "setSuperZoom: KEY_ZOOM  = " + parameters.get("zoom"));
            Parameters changedParams = null;
            synchronized (AndroidCameraManagerImpl.this.mCameraHandler.mSyncObject) {
                try {
                    changedParams = AndroidCameraManagerImpl.this.mLGParameters.setSuperZoom((Parameters) parameters.getParameters());
                } catch (NoSuchMethodError e) {
                    CamLog.m3d(CameraConstants.TAG, "fail setSuperZoom=" + e);
                } catch (NumberFormatException e2) {
                    CamLog.m3d(CameraConstants.TAG, "fail setSuperZoom=" + e2);
                } catch (RuntimeException e3) {
                    CamLog.m3d(CameraConstants.TAG, "fail setSuperZoom=" + e3);
                }
                if (changedParams != null) {
                    AndroidCameraManagerImpl.this.mParameters = new Parameters1(changedParams);
                }
            }
            parameters.setParameters(changedParams);
            return parameters;
        }

        public void setSuperZoomSync(CameraParameters parameters) {
            CamLog.m3d(CameraConstants.TAG, "setSuperZoomSync");
            if (AndroidCameraManagerImpl.this.mCamera == null || AndroidCameraManagerImpl.this.mLGCamera == null || AndroidCameraManagerImpl.this.mLGParameters == null) {
                CamLog.m3d(CameraConstants.TAG, "exit setSuperZoomSync");
            } else {
                AndroidCameraManagerImpl.this.mCameraHandler.obtainMessage(305, parameters).sendToTarget();
            }
        }

        public void setLowlightDetectionCallback(Handler handler, final CameraLowlightDetectionCallback callback) {
            CameraMetaDataCallback cb;
            if (callback == null) {
                cb = null;
            } else {
                cb = new CameraMetaDataCallback() {
                    public void onCameraMetaData(byte[] data, Camera camera) {
                        boolean isDetected = true;
                        if (data != null && data.length > 0) {
                            if (data[0] != (byte) 1) {
                                isDetected = false;
                            }
                            callback.onDetected(isDetected);
                        }
                    }
                };
            }
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    AndroidCameraManagerImpl.this.mLGCamera.setFlashMetadataCb(cb);
                } catch (NoSuchMethodError e) {
                    CamLog.m4d(CameraConstants.TAG, "fail setFlashMetadataCb = ", e);
                }
            }
        }

        public void setOutFocusCallback(OutFocusCallback outfocusCallback) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    LGCamera.class.getDeclaredMethod("setOutfocusMetadataCb", new Class[]{CameraMetaDataCallback.class}).invoke(AndroidCameraManagerImpl.this.mLGCamera, new Object[]{outfocusCallback});
                } catch (Exception e) {
                    CamLog.m6e(CameraConstants.TAG, "fail setOutfocusMetadataCb = ", e);
                }
            }
        }

        public void startRecordingPreview(Surface surface) {
            startPreview();
        }

        public void setRecordSurfaceToTarget(boolean set) {
        }

        public void setRecordStreamForSpecialMode(boolean isStart) {
        }

        public void setForStopRecording() {
        }

        public void setManualCameraMetadataCb(LGCameraMetaDataCallback cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    AndroidCameraManagerImpl.this.mLGCamera.setManualCameraMetadataCb(cb);
                } catch (NoSuchMethodError e) {
                    CamLog.m4d(CameraConstants.TAG, "fail setManualCameraMetadataCb = ", e);
                }
            }
        }

        public void setOpticZoomMetadataCb(CameraMetaDataCallback cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    LGCamera.class.getDeclaredMethod("setDualCamMetadataCb", new Class[]{CameraMetaDataCallback.class}).invoke(AndroidCameraManagerImpl.this.mLGCamera, new Object[]{cb});
                } catch (Exception e) {
                    CamLog.m4d(CameraConstants.TAG, "fail setOpticZoomMetadataCb = ", e);
                }
            }
        }

        public void setObtDataListener(ObtDataListener cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    CamLog.m7i(CameraConstants.TAG, "-focus- setObtDataListener " + cb);
                    AndroidCameraManagerImpl.this.mLGCamera.setObtDataListener(cb);
                } catch (NoSuchMethodError ne) {
                    CamLog.m5e(CameraConstants.TAG, "-focus- setObtDataListener NoSuchMethodError" + ne);
                } catch (Exception e) {
                    CamLog.m5e(CameraConstants.TAG, "-focus- setObtDataListener Exception" + e);
                }
            }
        }

        public void setEVCallbackDataListener(EVCallbackListener cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    CamLog.m7i(CameraConstants.TAG, "-AE control- setEVCallbackDataListener " + cb);
                    AndroidCameraManagerImpl.this.mLGCamera.setEVCallbackDataListener(cb);
                } catch (NoSuchMethodError ne) {
                    CamLog.m5e(CameraConstants.TAG, "-AE control- setEVCallbackDataListener NoSuchMethodError" + ne);
                } catch (Exception e) {
                    CamLog.m5e(CameraConstants.TAG, "-AE control- setEVCallbackDataListener Exception" + e);
                }
            }
        }

        public void setFingerDetectionDataListener(FingerDetectionDataListener cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    CamLog.m7i(CameraConstants.TAG, "-focus- setFingerDetectionDataListener " + cb);
                    AndroidCameraManagerImpl.this.mLGCamera.setFingerDetectionDataListener(cb);
                } catch (NoSuchMethodError ne) {
                    CamLog.m5e(CameraConstants.TAG, "-focus- setFingerDetectionDataListener NoSuchMethodError" + ne);
                } catch (Exception e) {
                    CamLog.m5e(CameraConstants.TAG, "-focus- setFingerDetectionDataListener Exception" + e);
                }
            }
        }

        public void setHistogramDataCallback(Handler handler, CameraHistogramDataCallback cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    AndroidCameraManagerImpl.this.mLGCamera.setHistogramMode(CameraHistogramDataCallbackForward.getNewInstance(handler, this, cb));
                } catch (Exception e) {
                    CamLog.m3d(CameraConstants.TAG, "setHistogramDataCallback failed!");
                }
            }
        }

        public CameraInfomation getCameraInfo() {
            CameraInfo info = new CameraInfo();
            AndroidCameraManagerImpl.this.mCamera;
            Camera.getCameraInfo(AndroidCameraManagerImpl.this.mCameraId, info);
            return CameraInfomation.createCameraInfo(info);
        }

        public void setLongshot(boolean enable) {
            try {
                Class.forName("android.hardware.Camera").getDeclaredMethod("setLongshot", new Class[]{Boolean.TYPE}).invoke(AndroidCameraManagerImpl.this.mCamera, new Object[]{Boolean.valueOf(enable)});
                Log.d(CameraConstants.TAG, "#### [setLongshot] invoke success - enable : " + enable);
            } catch (Exception e) {
                CamLog.m4d(CameraConstants.TAG, "setLongshot setLongshot error : ", e);
            }
        }

        public void addRawImageCallbackBuffer(byte[] buffer) {
            if (AndroidCameraManagerImpl.this.mCamera != null) {
                AndroidCameraManagerImpl.this.mCamera.addRawImageCallbackBuffer(buffer);
            } else {
                CamLog.m5e(CameraConstants.TAG, "mCamera is null. error.");
            }
        }

        public void setGPSlocation(Location loc) {
        }

        public boolean isLowLightDetected(CameraParameters parameters) {
            return CameraDeviceUtils.isLowLuminance(parameters, false);
        }

        public boolean isLowFps() {
            return false;
        }

        public CameraParameters convertParameter(Parameters param) {
            return new Parameters1(param);
        }

        public void setImageDataCallback(int imageType, CameraImageCallback imageCb) {
        }

        public void setImageMetaCallback(CameraImageMetaCallback meatCb) {
        }

        public SizeF getSensorPhysicalSize(float focalLength, float horiFov, float vertFov) {
            return new SizeF((2.0f * focalLength) * ((float) Math.tan(Math.toRadians((double) (horiFov / 2.0f)))), (2.0f * focalLength) * ((float) Math.tan(Math.toRadians((double) (vertFov / 2.0f)))));
        }

        public Size getSensorPixelArraySize(CameraParameters parameters, boolean isMtkChipset) {
            int maxPictureSizeIndex = 0;
            List<Size> supported = parameters.getSupportedPictureSizes();
            if (supported == null) {
                return new Size(0, 0);
            }
            if (isMtkChipset) {
                maxPictureSizeIndex = supported.size() - 1;
            }
            return (Size) supported.get(maxPictureSizeIndex);
        }

        public Size getSensorActiveArrarySize(CameraParameters parameters, boolean isMtkChipset) {
            int maxPictureSizeIndex = 0;
            List<Size> supported = parameters.getSupportedPictureSizes();
            if (supported == null) {
                return new Size(0, 0);
            }
            if (isMtkChipset) {
                maxPictureSizeIndex = supported.size() - 1;
            }
            return (Size) supported.get(maxPictureSizeIndex);
        }

        public float getHorizontalViewAngle(CameraParameters parameters) {
            return parameters != null ? parameters.getHorizontalViewAngle() : 60.02726f;
        }

        public float getVerticalViewAngle(CameraParameters parameters) {
            return parameters != null ? parameters.getVerticalViewAngle() : 46.849403f;
        }

        public float getFocalLength(CameraParameters parameters) {
            return parameters != null ? parameters.getFocalLength() : 4.03f;
        }

        public float getCameraFnumber(CameraParameters parameters) {
            if (parameters == null) {
                return 0.0f;
            }
            String aperture = parameters.get(ParamConstants.KEY_F_NUMBER);
            if (aperture != null) {
                try {
                    if (!(aperture.equals("0") || ModelProperties.isFakeExif())) {
                        return (float) (Math.floor(((double) Float.parseFloat(aperture)) * 10.0d) / 10.0d);
                    }
                } catch (Exception e) {
                    return 0.0f;
                }
            }
            throw new Exception();
        }

        public void updateRequestCapture(long exposureTime, int sensorSensitivity) {
        }

        public void setLuxIndexMetadata(LGCameraMetaDataCallback cb) {
            if (AndroidCameraManagerImpl.this.mLGCamera != null) {
                try {
                    AndroidCameraManagerImpl.this.mLGCamera.setLuxIndexMetadataCb(cb);
                } catch (NoSuchMethodError e) {
                    CamLog.m4d(CameraConstants.TAG, "fail setNightVisionMetadataCb = ", e);
                }
            }
        }

        public void stopBurstShot() {
        }
    }

    private class CameraHandler extends Handler {
        public Object mSyncObject = new Object();

        CameraHandler(Looper looper) {
            super(looper);
        }

        public void requestTakePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback postView, PictureCallback jpeg) {
            final ShutterCallback shutterCallback = shutter;
            final PictureCallback pictureCallback = raw;
            final PictureCallback pictureCallback2 = postView;
            final PictureCallback pictureCallback3 = jpeg;
            post(new Runnable() {
                public void run() {
                    try {
                        CamLog.m3d(CameraConstants.TAG, "#### take picture");
                        AndroidCameraManagerImpl.this.mCamera.takePicture(shutterCallback, pictureCallback, pictureCallback2, pictureCallback3);
                    } catch (RuntimeException e) {
                        CamLog.m5e(CameraConstants.TAG, "take picture failed.");
                        if (AndroidCameraManagerImpl.this.mCameraHandler != null) {
                            CamLog.m5e(CameraConstants.TAG, "getParameters");
                            AndroidCameraManagerImpl.this.mCameraHandler.sendEmptyMessage(202);
                            AndroidCameraManagerImpl.this.mCameraHandler.waitDone();
                            CameraHandler.this.printParamLog(AndroidCameraManagerImpl.this.mParameters);
                        }
                        throw e;
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
                AndroidCameraManagerImpl.this.mCameraHandler.post(unlockRunnable);
                try {
                    waitDoneLock.wait();
                } catch (InterruptedException e) {
                    CamLog.m9v(CameraConstants.TAG, "waitDone interrupted");
                    return false;
                }
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:113:0x04b6  */
        public void handleMessage(android.os.Message r11) {
            /*
            r10 = this;
            r9 = 0;
            r8 = 1;
            r5 = r11.what;	 Catch:{ RuntimeException -> 0x0022 }
            switch(r5) {
                case 1: goto L_0x0099;
                case 2: goto L_0x0144;
                case 3: goto L_0x0163;
                case 4: goto L_0x0189;
                case 5: goto L_0x01a2;
                case 6: goto L_0x01bb;
                case 7: goto L_0x01fe;
                case 8: goto L_0x0217;
                case 9: goto L_0x025b;
                case 101: goto L_0x02a5;
                case 102: goto L_0x02df;
                case 103: goto L_0x02f8;
                case 104: goto L_0x02c2;
                case 105: goto L_0x03c1;
                case 201: goto L_0x0039;
                case 202: goto L_0x0072;
                case 203: goto L_0x03b2;
                case 301: goto L_0x0311;
                case 302: goto L_0x032e;
                case 303: goto L_0x0340;
                case 305: goto L_0x03d7;
                case 461: goto L_0x036a;
                case 462: goto L_0x0380;
                case 463: goto L_0x0391;
                case 464: goto L_0x039c;
                case 501: goto L_0x0356;
                default: goto L_0x0007;
            };	 Catch:{ RuntimeException -> 0x0022 }
        L_0x0007:
            r5 = new java.lang.RuntimeException;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = "Invalid CameraProxy message=";
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r11.what;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0022 }
            r5.<init>(r6);	 Catch:{ RuntimeException -> 0x0022 }
            throw r5;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x0022:
            r1 = move-exception;
            r5 = r11.what;
            r6 = 201; // 0xc9 float:2.82E-43 double:9.93E-322;
            if (r5 != r6) goto L_0x0456;
        L_0x0029:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;
            r5 = r5.mCamera;
            if (r5 == 0) goto L_0x0456;
        L_0x0031:
            r5 = "CameraApp";
            r6 = "SET_PARAMETERS Exception : ";
            com.lge.camera.util.CamLog.m6e(r5, r6, r1);
        L_0x0038:
            return;
        L_0x0039:
            r6 = r10.mSyncObject;	 Catch:{ RuntimeException -> 0x0022 }
            monitor-enter(r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ all -> 0x006f }
            r7 = 1;
            r5.mParametersIsDirty = r7;	 Catch:{ all -> 0x006f }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ all -> 0x006f }
            r7 = r5.mParamsToSet;	 Catch:{ all -> 0x006f }
            r5 = r11.obj;	 Catch:{ all -> 0x006f }
            r5 = (java.lang.String) r5;	 Catch:{ all -> 0x006f }
            r7.unflatten(r5);	 Catch:{ all -> 0x006f }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ all -> 0x006f }
            r5 = r5.mParamsToSet;	 Catch:{ all -> 0x006f }
            r10.printParamLog(r5);	 Catch:{ all -> 0x006f }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ all -> 0x006f }
            r7 = r5.mCamera;	 Catch:{ all -> 0x006f }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ all -> 0x006f }
            r5 = r5.mParamsToSet;	 Catch:{ all -> 0x006f }
            r5 = r5.getParameters();	 Catch:{ all -> 0x006f }
            r5 = (android.hardware.Camera.Parameters) r5;	 Catch:{ all -> 0x006f }
            r7.setParameters(r5);	 Catch:{ all -> 0x006f }
            monitor-exit(r6);	 Catch:{ all -> 0x006f }
            goto L_0x0038;
        L_0x006f:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x006f }
            throw r5;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x0072:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mParametersIsDirty;	 Catch:{ RuntimeException -> 0x0022 }
            if (r5 == 0) goto L_0x0038;
        L_0x007a:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = new com.lge.camera.device.api1.Parameters1;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r7.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r7.getLGParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r7.getParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r5.mParameters = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 0;
            r5.mParametersIsDirty = r6;	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0099:
            r3 = com.lge.camera.constants.FunctionProperties.getSupportedHal();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = "#### OPEN_CAMERA START halConfig=";
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.append(r3);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0022 }
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            if (r3 != r8) goto L_0x0129;
        L_0x00b7:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = new com.lge.hardware.LGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r11.arg1;	 Catch:{ RuntimeException -> 0x0022 }
            r8 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
            r6.<init>(r7, r8);	 Catch:{ RuntimeException -> 0x0022 }
            r5.mLGCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x00c5:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getCamera();	 Catch:{ RuntimeException -> 0x0022 }
            r5.mCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getLGParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r5.mLGParameters = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getLGParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r5.mParametersBackup = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            if (r5 == 0) goto L_0x0136;
        L_0x00fe:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 1;
            r5.mParametersIsDirty = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mParamsToSet;	 Catch:{ RuntimeException -> 0x0022 }
            if (r5 != 0) goto L_0x0120;
        L_0x010c:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = new com.lge.camera.device.api1.Parameters1;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r7.mLGParameters;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r7.getParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r5.mParamsToSet = r6;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x0120:
            r5 = "CameraApp";
            r6 = "#### OPEN_CAMERA END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0129:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = new com.lge.hardware.LGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r11.arg1;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r5.mLGCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x00c5;
        L_0x0136:
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            if (r5 == 0) goto L_0x0120;
        L_0x013a:
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r11.arg1;	 Catch:{ RuntimeException -> 0x0022 }
            r5.onDeviceOpenFailure(r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0120;
        L_0x0144:
            r5 = "CameraApp";
            r6 = "#### RELEASE START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.release();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 0;
            r5.mCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### RELEASE END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0163:
            r5 = "CameraApp";
            r6 = "#### RECONNECT START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 0;
            r5.mReconnectIOException = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ IOException -> 0x0182 }
            r5 = r5.mCamera;	 Catch:{ IOException -> 0x0182 }
            r5.reconnect();	 Catch:{ IOException -> 0x0182 }
        L_0x0179:
            r5 = "CameraApp";
            r6 = "#### RECONNECT END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0182:
            r2 = move-exception;
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5.mReconnectIOException = r2;	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0179;
        L_0x0189:
            r5 = "CameraApp";
            r6 = "#### UNLOCK START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.unlock();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### UNLOCK END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x01a2:
            r5 = "CameraApp";
            r6 = "#### LOCK START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.lock();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### LOCK END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x01bb:
            r5 = "CameraApp";
            r6 = "#### REFRESH_CAMERA START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getCamera();	 Catch:{ RuntimeException -> 0x0022 }
            r5.mCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = "Refreshed camera = ";
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r7 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r7 = r7.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0022 }
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 1;
            r5.mParametersIsDirty = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### REFRESH_CAMERA END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x01fe:
            r5 = "CameraApp";
            r6 = "#### SET_SECOND_CAMERA START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (com.lge.hardware.LGCamera) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6.mLGCameraPrev = r5;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### SET_SECOND_CAMERA END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0217:
            r5 = "CameraApp";
            r6 = "#### CLOSE_SECOND_CAMERA START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mLGCameraPrev;	 Catch:{ RuntimeException -> 0x0022 }
            if (r5 == 0) goto L_0x0252;
        L_0x0226:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mLGCameraPrev;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.getCamera();	 Catch:{ RuntimeException -> 0x0022 }
            if (r5 == 0) goto L_0x0252;
        L_0x0232:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mLGCameraPrev;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.getCamera();	 Catch:{ RuntimeException -> 0x0022 }
            r5.stopPreview();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mLGCameraPrev;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.getCamera();	 Catch:{ RuntimeException -> 0x0022 }
            r5.release();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 0;
            r5.mLGCameraPrev = r6;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x0252:
            r5 = "CameraApp";
            r6 = "#### CLOSE_SECOND_CAMERA END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x025b:
            r5 = "CameraApp";
            r6 = "#### SWITCH_CAMERA_FOR_IN_AND_OUT START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r4 = r5.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCameraPrev;	 Catch:{ RuntimeException -> 0x0022 }
            r5.mLGCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5.mLGCameraPrev = r4;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getCamera();	 Catch:{ RuntimeException -> 0x0022 }
            r5.mCamera = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.mLGCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.getLGParameters();	 Catch:{ RuntimeException -> 0x0022 }
            r5.mLGParameters = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 1;
            r5.mParametersIsDirty = r6;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### SWITCH_CAMERA_FOR_IN_AND_OUT END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x02a5:
            r5 = "CameraApp";
            r6 = "#### SET_PREVIEW_TEXTURE_ASYNC";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ IOException -> 0x02bb }
            r6 = r5.mCamera;	 Catch:{ IOException -> 0x02bb }
            r5 = r11.obj;	 Catch:{ IOException -> 0x02bb }
            r5 = (android.graphics.SurfaceTexture) r5;	 Catch:{ IOException -> 0x02bb }
            r6.setPreviewTexture(r5);	 Catch:{ IOException -> 0x02bb }
            goto L_0x0038;
        L_0x02bb:
            r1 = move-exception;
            r5 = new java.lang.RuntimeException;	 Catch:{ RuntimeException -> 0x0022 }
            r5.<init>(r1);	 Catch:{ RuntimeException -> 0x0022 }
            throw r5;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x02c2:
            r5 = "CameraApp";
            r6 = "#### SET_PREVIEW_DISPLAY_ASYNC";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ IOException -> 0x02d8 }
            r6 = r5.mCamera;	 Catch:{ IOException -> 0x02d8 }
            r5 = r11.obj;	 Catch:{ IOException -> 0x02d8 }
            r5 = (android.view.SurfaceHolder) r5;	 Catch:{ IOException -> 0x02d8 }
            r6.setPreviewDisplay(r5);	 Catch:{ IOException -> 0x02d8 }
            goto L_0x0038;
        L_0x02d8:
            r1 = move-exception;
            r5 = new java.lang.RuntimeException;	 Catch:{ RuntimeException -> 0x0022 }
            r5.<init>(r1);	 Catch:{ RuntimeException -> 0x0022 }
            throw r5;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x02df:
            r5 = "CameraApp";
            r6 = "[Time Info][6]#### START_PREVIEW_ASYNC START";
            android.util.Log.i(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.startPreview();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "[Time Info][6]#### START_PREVIEW_ASYNC END";
            android.util.Log.i(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x02f8:
            r5 = "CameraApp";
            r6 = "#### STOP_PREVIEW START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.stopPreview();	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "#### STOP_PREVIEW END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0311:
            r5 = "CameraApp";
            r6 = "#### AUTO_FOCUS";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = "CameraApp";
            r6 = "TIME CHECK : Auto focus [START] - autoFocus()";
            android.util.Log.d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (android.hardware.Camera.AutoFocusCallback) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6.autoFocus(r5);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x032e:
            r5 = "CameraApp";
            r6 = "#### CANCEL_AUTO_FOCUS";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.cancelAutoFocus();	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0340:
            r5 = "CameraApp";
            r6 = "#### SET_AUTO_FOCUS_MOVE_CALLBACK";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (android.hardware.Camera.AutoFocusMoveCallback) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6.setAutoFocusMoveCallback(r5);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0356:
            r5 = "CameraApp";
            r6 = "#### SET_DISPLAY_ORIENTATION";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r11.arg1;	 Catch:{ RuntimeException -> 0x0022 }
            r5.setDisplayOrientation(r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x036a:
            r5 = "CameraApp";
            r6 = "#### SET_FACE_DETECTION_LISTENER";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (android.hardware.Camera.FaceDetectionListener) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6.setFaceDetectionListener(r5);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0380:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ Exception -> 0x038b }
            r5 = r5.mCamera;	 Catch:{ Exception -> 0x038b }
            r5.startFaceDetection();	 Catch:{ Exception -> 0x038b }
            goto L_0x0038;
        L_0x038b:
            r1 = move-exception;
            r1.printStackTrace();	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0391:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5.stopFaceDetection();	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x039c:
            r5 = "CameraApp";
            r6 = "#### SET_ERROR_CALLBACK";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (android.hardware.Camera.ErrorCallback) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6.setErrorCallback(r5);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x03b2:
            r5 = "CameraApp";
            r6 = "#### REFRESH_PARAMETERS";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = 1;
            r5.mParametersIsDirty = r6;	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x03c1:
            r5 = "CameraApp";
            r6 = "#### SET_ONE_SHOT_PREVIEW_CALLBACK";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r5.mCamera;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = r11.obj;	 Catch:{ RuntimeException -> 0x0022 }
            r5 = (android.hardware.Camera.PreviewCallback) r5;	 Catch:{ RuntimeException -> 0x0022 }
            r6.setOneShotPreviewCallback(r5);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x03d7:
            r5 = "CameraApp";
            r6 = "#### SET_SUPER_ZOOM_WITH_SYNC START";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            r0 = 0;
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
            r6 = r5.mLGParameters;	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
            r5 = r11.obj;	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
            r5 = (com.lge.camera.device.CameraParameters) r5;	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
            r5 = r5.getParameters();	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
            r5 = (android.hardware.Camera.Parameters) r5;	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
            r0 = r6.setSuperZoom(r5);	 Catch:{ NoSuchMethodError -> 0x0408, NumberFormatException -> 0x0422, RuntimeException -> 0x043c }
        L_0x03f3:
            if (r0 == 0) goto L_0x03ff;
        L_0x03f5:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ RuntimeException -> 0x0022 }
            r6 = new com.lge.camera.device.api1.Parameters1;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>(r0);	 Catch:{ RuntimeException -> 0x0022 }
            r5.mParameters = r6;	 Catch:{ RuntimeException -> 0x0022 }
        L_0x03ff:
            r5 = "CameraApp";
            r6 = "#### SET_SUPER_ZOOM_WITH_SYNC END";
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x0038;
        L_0x0408:
            r1 = move-exception;
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = "fail setSuperZoom=";
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.append(r1);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0022 }
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x03f3;
        L_0x0422:
            r1 = move-exception;
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = "fail setSuperZoom=";
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.append(r1);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0022 }
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x03f3;
        L_0x043c:
            r1 = move-exception;
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0022 }
            r6.<init>();	 Catch:{ RuntimeException -> 0x0022 }
            r7 = "fail setSuperZoom=";
            r6 = r6.append(r7);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.append(r1);	 Catch:{ RuntimeException -> 0x0022 }
            r6 = r6.toString();	 Catch:{ RuntimeException -> 0x0022 }
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ RuntimeException -> 0x0022 }
            goto L_0x03f3;
        L_0x0456:
            r5 = r11.what;
            r6 = 2;
            if (r5 == r6) goto L_0x04ae;
        L_0x045b:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;
            r5 = r5.mCamera;
            if (r5 == 0) goto L_0x04ae;
        L_0x0463:
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x04a5 }
            r6.<init>();	 Catch:{ Exception -> 0x04a5 }
            r7 = "#### RuntimeException occured, RELEASE CAMERA! message = ";
            r6 = r6.append(r7);	 Catch:{ Exception -> 0x04a5 }
            r7 = r11.what;	 Catch:{ Exception -> 0x04a5 }
            r6 = r6.append(r7);	 Catch:{ Exception -> 0x04a5 }
            r6 = r6.toString();	 Catch:{ Exception -> 0x04a5 }
            com.lge.camera.util.CamLog.m3d(r5, r6);	 Catch:{ Exception -> 0x04a5 }
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;	 Catch:{ Exception -> 0x04a5 }
            r5 = r5.mCamera;	 Catch:{ Exception -> 0x04a5 }
            r5.release();	 Catch:{ Exception -> 0x04a5 }
        L_0x0486:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;
            r5.mCamera = r9;
        L_0x048b:
            r5 = "CameraApp";
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "Skip throw exception : ";
            r6 = r6.append(r7);
            r6 = r6.append(r1);
            r6 = r6.toString();
            com.lge.camera.util.CamLog.m11w(r5, r6);
            goto L_0x0038;
        L_0x04a5:
            r2 = move-exception;
            r5 = "CameraApp";
            r6 = "Fail to release the camera.";
            com.lge.camera.util.CamLog.m5e(r5, r6);
            goto L_0x0486;
        L_0x04ae:
            r5 = com.lge.camera.device.api1.AndroidCameraManagerImpl.this;
            r5 = r5.mCamera;
            if (r5 != 0) goto L_0x048b;
        L_0x04b6:
            r5 = "CameraApp";
            r6 = "Cannot handle message, mCamera is null.";
            com.lge.camera.util.CamLog.m11w(r5, r6);
            goto L_0x0038;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api1.AndroidCameraManagerImpl.CameraHandler.handleMessage(android.os.Message):void");
        }

        private void printParamLog(CameraParameters param) {
            int i = 0;
            if (param != null) {
                String[] paramNames = new String[]{"shutter-speed", ParamConstants.KEY_MANUAL_ISO, ParamConstants.KEY_MANUAL_MODE_RESET, "lg-wb", ParamConstants.MANUAL_FOCUS_STEP, "flash-mode", ParamConstants.KEY_STEADY_CAM, ParamConstants.KEY_VIEW_MODE, "picture-size", "zoom", ParamConstants.KEY_PREVIEW_SIZE, "beautyshot", ParamConstants.KEY_BEAUTY_LEVEL, ParamConstants.KEY_RELIGHTING_LEVEL, ParamConstants.KEY_LIGHTFRMAE_TIME, ParamConstants.KEY_OUTFOCUS, ParamConstants.KEY_OUTFOCUS_LEVEL, ParamConstants.KEY_JPEG_QUALITY, ParamConstants.KEY_FILM_ENABLE, ParamConstants.KEY_FILM_TYPE, ParamConstants.KEY_FILM_STRENGTH, "hdr-mode", ParamConstants.KEY_VIDEO_HDR_MODE, ParamConstants.KEY_FOCUS_PEAKING, ParamConstants.KEY_OPTIC_CAM_INIT, "iso", ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL, ParamConstants.KEY_VIDEO_HDR_MODE, ParamConstants.KEY_CINEMA_MODE, ParamConstants.KEY_JOG_ZOOM, ParamConstants.KEY_NIGHTVISION_PARAM, ParamConstants.KEY_BINNING_PARAM, ParamConstants.KEY_LGE_CAMERA};
                StringBuilder log = new StringBuilder("== SET_PARAMETERS start \n");
                int length = paramNames.length;
                while (i < length) {
                    String name = paramNames[i];
                    log.append("SET_PARAMETERS : " + name + " : " + param.get(name) + "\n");
                    i++;
                }
                log.append("SET_PARAMETERS : KEY_EV : " + param.getExposureCompensation() + "\n");
                log.append("SET_PARAMETERS : FOCUS_MODE : " + param.getFocusMode() + "\n");
                if (FunctionProperties.isSupportedOpticZoom() && !FunctionProperties.isSameResolutionOpticZoom()) {
                    log.append("SET_PARAMETERS : picture-size-wide : " + param.get(ParamConstants.KEY_PICTURE_SIZE_WIDE) + "\n");
                }
                log.append("== SET_PARAMETERS end");
                CamLog.m3d(CameraConstants.TAG, log.toString());
            }
        }
    }

    public AndroidCameraManagerImpl() {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        this.mCameraHandler = new CameraHandler(ht.getLooper());
    }

    public CameraProxy cameraOpen(Handler handler, int cameraId, CameraOpenErrorCallback callback) {
        this.mCameraId = cameraId;
        this.mCameraHandler.obtainMessage(1, cameraId, 0, CameraOpenErrorCallbackForward.getNewInstance(handler, callback)).sendToTarget();
        this.mCameraHandler.waitDone();
        if (this.mCamera != null) {
            return new AndroidCameraProxyImpl();
        }
        return null;
    }

    public void setLGProxyDataListener(boolean set) {
        if (this.mLGCamera != null) {
            this.mCnt_low = 0;
            this.mCnt_normal = 0;
            try {
                this.mLGCamera.setProxyDataListener(null);
                CamLog.m7i(CameraConstants.TAG, "setLGProxyDataListener " + set);
                if (set) {
                    this.mLGCamera.setProxyDataListener(this);
                }
            } catch (NoSuchMethodError ne) {
                CamLog.m5e(CameraConstants.TAG, "setLGProxyDataListener NoSuchMethodError" + ne);
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, "setLGProxyDataListener Exception" + e);
            }
        }
    }

    public void onDataListen(ProxyData data, Camera camera) {
        if (data != null) {
            int distance = data.val;
            CamLog.m7i(CameraConstants.TAG, "[APLUS] check distance : " + distance);
            if (distance < 0 || distance >= 20) {
                if (this.mCnt_normal + 1 >= Integer.MAX_VALUE) {
                    this.mCnt_normal -= this.mCnt_low;
                    this.mCnt_low = 0;
                }
                this.mCnt_normal++;
                return;
            }
            if (this.mCnt_low + 1 >= Integer.MAX_VALUE) {
                this.mCnt_low -= this.mCnt_normal;
                this.mCnt_normal = 0;
            }
            this.mCnt_low++;
        }
    }

    public boolean isLowDistance() {
        boolean isLow;
        CamLog.m7i(CameraConstants.TAG, "[APLUS] check low : " + this.mCnt_low + ", normal : " + this.mCnt_normal);
        if (this.mCnt_low > this.mCnt_normal) {
            isLow = true;
        } else {
            isLow = false;
        }
        this.mCnt_low = 0;
        this.mCnt_normal = 0;
        return isLow;
    }

    public void setParamToBackup(String key, Object value) {
        synchronized (this.mCameraHandler.mSyncObject) {
            if (this.mParametersBackup != null) {
                if (value instanceof String) {
                    this.mParametersBackup.set(key, (String) value);
                } else {
                    this.mParametersBackup.set(key, ((Integer) value).intValue());
                }
            }
        }
    }

    public void setCameraOpsModuleBridge(CameraOpsModuleBridge callbacks) {
    }
}
