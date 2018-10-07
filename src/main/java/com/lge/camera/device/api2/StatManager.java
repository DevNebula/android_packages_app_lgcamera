package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.hardware.Camera.Area;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraCallbackForwards.AFCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.AFMoveCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.FaceDetectionCallbackForward;
import com.lge.camera.device.CameraCallbackForwards.OneShotCallbackForward;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.ICameraCallback.CameraBacklightDetectionCallback;
import com.lge.camera.device.ICameraCallback.CameraHistogramDataCallback;
import com.lge.camera.device.ICameraCallback.CameraLowlightDetectionCallback;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.camera.device.LGCameraMetaDataCallback;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CamLog.TraceTag;
import com.lge.camera.util.ManualUtil;
import com.lge.hardware.LGCamera.EVCallbackListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class StatManager extends CaptureCallback {
    private static final int FACEDETECTION_OFF_FRAME_CNT = 90;
    private static final int FIRST_FRAME_NUMBER = -1;
    private static final int FIRST_FRAME_WAIT_COUNT = 10;
    CameraBacklightDetectionCallback mBacklightDetectionCallback = null;
    private BuilderSet mBuilderSet = null;
    AFCallbackForward mCameraAFCallback = null;
    AFMoveCallbackForward mCameraAFMoveCallback = null;
    private CameraHistogramDataCallback mCameraHistogramDataCallback = null;
    private ICameraOps mCameraOps = null;
    EVCallbackListener mEVCallback = null;
    private float mEVLuxIndex = 0.0f;
    private long mExposureTime = 0;
    FaceDetectionCallbackForward mFaceDetectionCallback = null;
    private int mFaceDetectionOffCnt = 0;
    private int mFirstFrameWaitCount;
    private Handler mHandler = null;
    private boolean mIsBackLightDetected = false;
    private boolean mIsEnableMeta = false;
    private boolean mIsHDRModeOff = false;
    private boolean mIsLowLightDetected = false;
    private LGCameraMetaDataCallback mLGManualMetaCallback = null;
    CameraLowlightDetectionCallback mLowlightDetectionCallback = null;
    private float mLuxIndex = 0.0f;
    private LGCameraMetaDataCallback mLuxIndexMetaCallback = null;
    private List<Area> mMWAreaList = null;
    private final Object mMWAreaLock = new Object();
    private OneShotCallbackForward mOneShotPreviewCallback = null;
    OutFocusCallback mOutFocusCallback = null;
    private int mPreShootMode = 1;
    private int mPrevFocusState = 0;

    public StatManager(Handler handler, ICameraOps cameraOps) {
        CamLog.m3d(CameraConstants.TAG, "Creating StatManager");
        this.mCameraOps = cameraOps;
        this.mHandler = handler;
    }

    public synchronized void setEnableMeta(boolean isEnable) {
        this.mIsEnableMeta = isEnable;
    }

    public synchronized void setDisableHDRMode(boolean isDisable) {
        this.mIsHDRModeOff = isDisable;
    }

    public void reset() {
        this.mFirstFrameWaitCount = 0;
        this.mIsEnableMeta = false;
    }

    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        if (this.mIsEnableMeta) {
            if (this.mOneShotPreviewCallback != null) {
                long syncFrame = 0;
                try {
                    syncFrame = ((Long) result.get(CaptureResult.SYNC_FRAME_NUMBER)).longValue();
                } catch (Exception e) {
                    if (CamLog.isTagExceptionLogOn()) {
                        e.printStackTrace();
                    }
                }
                if (syncFrame == -1 || this.mFirstFrameWaitCount >= 10) {
                    CamLog.m3d(CameraConstants.TAG, "onCaptureCompleted FirstFrame Wait Count : " + this.mFirstFrameWaitCount);
                    CamLog.traceEnd(TraceTag.OPTIONAL, "HAL3_setRepeatingRequest_to_oneshot", 1003);
                    this.mOneShotPreviewCallback.onPreviewFrame(null);
                    setOneShotPreviewCallback(null);
                } else {
                    this.mFirstFrameWaitCount++;
                }
            }
            boolean isFocused = false;
            try {
                if (this.mBuilderSet.getShootMode() != 2) {
                    checkFocusCallback(result);
                    checkFaceCallback(result);
                    checkLowLightCallback(result);
                    checkBackLightCallback(result);
                    checkLGManualModedata(result);
                    checkMWFocusArea(result);
                    checkEVRestMode(result);
                    checkOutfocusOutput(result);
                    checkLuxIndexData(result);
                    checkExposureTime(result);
                    isFocused = isFocused(result);
                    checkNeedSensorModuleUpdate(result);
                }
            } catch (Exception e2) {
                if (CamLog.isTagExceptionLogOn()) {
                    e2.printStackTrace();
                }
            }
            checkShotModeChanged(this.mBuilderSet.getShootMode());
            this.mCameraOps.onMetaAvailable(result, isFocused);
        }
    }

    public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
        CamLog.m3d(CameraConstants.TAG, "PreviewCaptureSession - onCaptureFailed " + failure.getReason());
        this.mCameraOps.onPreviewCaptureFailed(request, failure);
    }

    private void checkFocusCallback(TotalCaptureResult result) {
        Integer focusMode = (Integer) result.get(CaptureResult.CONTROL_AF_MODE);
        Integer focusState = (Integer) result.get(CaptureResult.CONTROL_AF_STATE);
        if (focusMode != null && focusState != null) {
            if (this.mCameraAFCallback != null) {
                doAFCallback(focusMode.intValue(), focusState.intValue());
            }
            if (this.mCameraAFMoveCallback != null) {
                doAFMoveCallback(focusMode.intValue(), focusState.intValue());
            }
            if (this.mPrevFocusState != focusState.intValue()) {
                CamLog.m3d(CameraConstants.TAG, "Focus state = " + Camera2Converter.getStringFocusState(focusState.intValue()) + " (" + focusState + ")");
                this.mPrevFocusState = focusState.intValue();
            }
        }
    }

    private void doAFCallback(int focusMode, int focusState) {
        if (this.mPrevFocusState != focusState) {
            if (focusState == 4) {
                CamLog.m3d(CameraConstants.TAG, "focus success");
                this.mCameraAFCallback.onAutoFocus(true);
                this.mCameraAFCallback = null;
            } else if (focusState == 5) {
                CamLog.m3d(CameraConstants.TAG, "focus fail");
                this.mCameraAFCallback.onAutoFocus(false);
                this.mCameraAFCallback = null;
            }
        }
    }

    private void doAFMoveCallback(int focusMode, int focusState) {
        if (focusMode != 4 && focusMode != 3) {
            return;
        }
        if ((this.mPrevFocusState == 0 || this.mPrevFocusState == 2 || this.mPrevFocusState == 6) && focusState == 1) {
            this.mCameraAFMoveCallback.onAutoFocusMoving(true);
            if (this.mFaceDetectionCallback != null) {
                this.mCameraOps.onFaceDetionOnOff(true);
            }
            this.mFaceDetectionOffCnt = 0;
        } else if (this.mPrevFocusState != 1 && this.mPrevFocusState != 0) {
        } else {
            if (focusState == 2 || focusState == 6 || focusState == 4 || focusState == 5) {
                this.mCameraAFMoveCallback.onAutoFocusMoving(false);
            }
        }
    }

    private void checkFaceCallback(TotalCaptureResult result) {
        if (this.mBuilderSet != null && this.mFaceDetectionCallback != null && ((Integer) result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE)).intValue() != 0) {
            Face[] faces = (Face[]) result.get(CaptureResult.STATISTICS_FACES);
            if (this.mCameraOps.isAFSupported() && faces.length == 0) {
                this.mFaceDetectionOffCnt++;
                if (this.mFaceDetectionOffCnt > 90) {
                    this.mCameraOps.onFaceDetionOnOff(false);
                    this.mFaceDetectionOffCnt = 0;
                }
            }
            this.mFaceDetectionCallback.onFaceDetection(faces, this.mBuilderSet.getActiveArraySize(), this.mBuilderSet.getPictureSize(), this.mBuilderSet.getZoomRatio());
        }
    }

    private void checkLowLightCallback(TotalCaptureResult result) {
        boolean z = true;
        try {
            Integer isLowLight = (Integer) result.get(ParamConstants.KEY_LOW_LIGHT);
            if (isLowLight == null) {
                z = false;
            } else if (Integer.compare(1, isLowLight.intValue()) != 0) {
                z = false;
            }
            this.mIsLowLightDetected = z;
        } catch (IllegalArgumentException ex) {
            if (CamLog.isTagExceptionLogOn()) {
                ex.printStackTrace();
            }
        }
        if (((Integer) result.get(CaptureResult.CONTROL_AE_MODE)).intValue() == 0) {
            this.mIsLowLightDetected = false;
        }
        if (this.mLowlightDetectionCallback != null) {
            this.mLowlightDetectionCallback.onDetected(this.mIsLowLightDetected);
        }
    }

    private void checkBackLight(TotalCaptureResult result) {
        if (this.mCameraOps != null) {
            if (this.mCameraOps.isFlashRequired() || this.mIsHDRModeOff) {
                this.mIsBackLightDetected = false;
                return;
            }
            Integer backlightStatus = null;
            try {
                backlightStatus = (Integer) result.get(ParamConstants.KEY_BACK_LIGHT_DETECTION);
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
            if (backlightStatus != null) {
                boolean z;
                if (Integer.compare(1, backlightStatus.intValue()) == 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.mIsBackLightDetected = z;
            }
        }
    }

    private void checkBackLightCallback(TotalCaptureResult result) {
        checkBackLight(result);
        if (this.mBacklightDetectionCallback != null) {
            this.mBacklightDetectionCallback.onDetected(this.mIsBackLightDetected);
        }
    }

    private void checkLGManualModedata(CaptureResult result) {
        if (this.mCameraHistogramDataCallback != null) {
            int[] histogramStats = (int[]) result.get(ParamConstants.QCAMERA3_HISTOGRAM_STATS);
            if (histogramStats != null) {
                this.mCameraHistogramDataCallback.onCameraData(histogramStats, null);
            }
        }
        if (this.mLGManualMetaCallback != null && this.mBuilderSet != null) {
            float ev = 0.0f;
            try {
                Float tmpEv = (Float) result.get(ParamConstants.KEY_EV_RESULT);
                if (tmpEv != null) {
                    ev = tmpEv.floatValue();
                }
            } catch (IllegalArgumentException ex) {
                if (CamLog.isTagExceptionLogOn()) {
                    ex.printStackTrace();
                }
            }
            Integer sensitivity = (Integer) result.get(CaptureResult.SENSOR_SENSITIVITY);
            float exposureTime = ManualUtil.convertShutterSpeedNanoToSec(result.get(CaptureResult.SENSOR_EXPOSURE_TIME) + "");
            float focusDistance = this.mBuilderSet.getFocusUserValueFromFocusDistance(((Float) result.get(CaptureResult.LENS_FOCUS_DISTANCE)).floatValue());
            int wb = 3800;
            try {
                Integer wbInteger = (Integer) result.get(ParamConstants.KEY_QTI_AWB_CCT_RESULT);
                if (wbInteger != null) {
                    wb = wbInteger.intValue();
                }
            } catch (IllegalArgumentException ex2) {
                wb = 3800;
                if (CamLog.isTagExceptionLogOn()) {
                    ex2.printStackTrace();
                }
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putFloat(0, ev);
            byteBuffer.putFloat(4, (float) sensitivity.intValue());
            byteBuffer.putFloat(8, exposureTime);
            byteBuffer.putFloat(12, (float) wb);
            byteBuffer.putFloat(16, focusDistance);
            this.mLGManualMetaCallback.onCameraMetaData(byteBuffer.array());
        }
    }

    private void checkMWFocusArea(TotalCaptureResult result) {
        synchronized (this.mMWAreaLock) {
            try {
                int[] area = (int[]) result.get(ParamConstants.KEY_MWFOCUS_AREA);
                if (area != null) {
                    this.mMWAreaList = new ArrayList();
                    for (int i = 0; i < area.length; i += 5) {
                        this.mMWAreaList.add(new Area(new Rect(area[i], area[i + 1], area[i + 2], area[i + 3]), area[i + 4]));
                    }
                }
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkOutfocusOutput(TotalCaptureResult result) {
        if (this.mOutFocusCallback != null) {
            try {
                Integer data = (Integer) result.get(ParamConstants.KEY_OUTFOCUS_RESULT);
                if (data != null) {
                    this.mOutFocusCallback.onOutFocusResult(data.intValue());
                }
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkEVRestMode(TotalCaptureResult result) {
        if (this.mEVCallback != null && Math.abs(this.mEVLuxIndex - this.mLuxIndex) >= 15.0f) {
            this.mEVCallback.onDataListen(null, null);
        }
    }

    private void checkShotModeChanged(int shotMode) {
        if (this.mFaceDetectionCallback != null) {
            if (this.mPreShootMode == 1 && shotMode == 2) {
                this.mCameraOps.onFaceDetionOnOff(false);
                this.mFaceDetectionOffCnt = 0;
            } else if (this.mPreShootMode == 2 && shotMode == 1) {
                this.mCameraOps.onFaceDetionOnOff(true);
                this.mFaceDetectionOffCnt = 0;
            }
        }
        this.mPreShootMode = shotMode;
    }

    private boolean isFocused(TotalCaptureResult result) {
        int afState = ((Integer) result.get(CaptureResult.CONTROL_AF_STATE)).intValue();
        if (afState == 4 || afState == 2) {
            return true;
        }
        return false;
    }

    public List<Area> getMWFocusAreas() {
        Throwable th;
        synchronized (this.mMWAreaLock) {
            List<Area> areaList = null;
            if (this.mMWAreaList != null) {
                List<Area> areaList2 = new ArrayList();
                try {
                    for (Area area : this.mMWAreaList) {
                        areaList2.add(area);
                    }
                    areaList = areaList2;
                } catch (Throwable th2) {
                    th = th2;
                    areaList = areaList2;
                    throw th;
                }
            }
            try {
                return areaList;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void setBuilder(BuilderSet builderSet) {
        this.mBuilderSet = builderSet;
    }

    public void setCallback(Runnable action) {
        if (this.mHandler.getLooper().isCurrentThread()) {
            action.run();
        }
        this.mHandler.post(action);
    }

    public void setLGManualModedataCb(final LGCameraMetaDataCallback cb) {
        setCallback(new Runnable() {
            public void run() {
                StatManager.this.mLGManualMetaCallback = cb;
            }
        });
    }

    public void setCameraHistogramDataCallback(final CameraHistogramDataCallback cb) {
        setCallback(new Runnable() {
            public void run() {
                StatManager.this.mCameraHistogramDataCallback = cb;
            }
        });
    }

    public void setOneShotPreviewCallback(final OneShotCallbackForward cb) {
        setCallback(new Runnable() {
            public void run() {
                StatManager.this.mOneShotPreviewCallback = cb;
            }
        });
    }

    public void setAutoFocusCallback(final AFCallbackForward cb) {
        setCallback(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "setAutoFocusCallback: " + cb);
                StatManager.this.mCameraAFCallback = cb;
            }
        });
    }

    public void setAutoFocusMoveCallback(final AFMoveCallbackForward cb) {
        setCallback(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "setAutoFoucsMoveCallback: " + cb);
                StatManager.this.mCameraAFMoveCallback = cb;
            }
        });
    }

    public void setFaceDetectionCallback(final FaceDetectionCallbackForward cb) {
        setCallback(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "setFaceDetectionCallback: " + cb);
                StatManager.this.mFaceDetectionCallback = cb;
            }
        });
    }

    public void setBacklightDetectionCallback(final CameraBacklightDetectionCallback cb) {
        setCallback(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "setBacklightDetectionCallback: " + cb);
                StatManager.this.mBacklightDetectionCallback = cb;
            }
        });
    }

    public void setLowlightDetectionCallback(final CameraLowlightDetectionCallback cb) {
        setCallback(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "setLowlightDetectionCallback: " + cb);
                StatManager.this.mLowlightDetectionCallback = cb;
            }
        });
    }

    public void setEVCallbackDataListener(final EVCallbackListener cb) {
        setCallback(new Runnable() {
            public void run() {
                CamLog.m3d(CameraConstants.TAG, "setEVCallbackDataListener: " + cb);
                StatManager.this.mEVCallback = cb;
                StatManager.this.mEVLuxIndex = StatManager.this.mEVCallback == null ? 0.0f : StatManager.this.mLuxIndex;
            }
        });
    }

    public boolean isLowLightDetected() {
        return this.mIsLowLightDetected;
    }

    public boolean isLowFps() {
        return this.mExposureTime > 50000000;
    }

    public boolean isHDRDetected() {
        return this.mIsBackLightDetected;
    }

    public void setLuxIndexMetadata(final LGCameraMetaDataCallback cb) {
        setCallback(new Runnable() {
            public void run() {
                StatManager.this.mLuxIndexMetaCallback = cb;
            }
        });
    }

    public void setOutFocusCallback(final OutFocusCallback cb) {
        setCallback(new Runnable() {
            public void run() {
                StatManager.this.mOutFocusCallback = cb;
            }
        });
    }

    private void checkLuxIndexData(TotalCaptureResult result) {
        Float luxIndex = null;
        try {
            luxIndex = (Float) result.get(ParamConstants.KEY_LUX_INDEX);
        } catch (IllegalArgumentException e) {
            if (CamLog.isTagExceptionLogOn()) {
                e.printStackTrace();
            }
        }
        if (luxIndex != null) {
            this.mLuxIndex = luxIndex.floatValue();
            if (this.mLuxIndexMetaCallback != null) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putFloat(0, this.mLuxIndex);
                this.mLuxIndexMetaCallback.onCameraMetaData(byteBuffer.array());
            }
        }
    }

    private void checkExposureTime(TotalCaptureResult result) {
        this.mExposureTime = ((Long) result.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue();
    }

    private void checkNeedSensorModuleUpdate(TotalCaptureResult result) {
        if (CameraDeviceUtils.sCameraModuleType < 0) {
            try {
                Integer eepromInfo = (Integer) result.get(ParamConstants.KEY_MODULE_TYPE);
                if (eepromInfo != null) {
                    CamLog.m3d(CameraConstants.TAG, "[ModuleType] KEY_MODULE_TYPE : " + eepromInfo);
                    CameraDeviceUtils.sCameraModuleType = eepromInfo.intValue();
                }
            } catch (Exception e) {
                CameraDeviceUtils.sCameraModuleType = 1;
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }
}
