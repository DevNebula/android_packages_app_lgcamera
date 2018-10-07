package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.hardware.Camera.Area;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.params.MeteringRectangle;
import android.location.Location;
import android.util.MathUtils;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import java.util.List;

public class BuilderSetBase {
    private static final float MAX_FOCUS_SUPPORT_VALUE = 9.9f;
    private static final float MAX_FOCUS_USER_VALUE = 60.0f;
    public static final MeteringRectangle[] ZERO_WEIGHT_3A_REGION = new MeteringRectangle[]{new MeteringRectangle(0, 0, 0, 0, 0)};
    protected MeteringRectangle[] mAERegions = ZERO_WEIGHT_3A_REGION;
    protected CameraCharacteristics mCharacteristics = null;
    protected int mControlAFMode;
    protected MeteringRectangle[] mFocusRegions = ZERO_WEIGHT_3A_REGION;
    protected boolean mForcedAELock = false;
    protected boolean mIsSuperZoom = false;
    protected Location mLocation;
    protected Parameters2 mParameters = null;
    protected Size mPictureSize;
    protected Rect mPointZoomCropRect = null;
    protected float mRatio;
    protected int mRecordMode = 0;
    protected Rect mSensorInfoActiveArraySize = null;
    protected int mShootMode;
    public String mShotMode = null;
    protected Rect mZoomCropRect = null;
    protected float mZoomRatio = 1.0f;
    protected int[] mZoomRatioList;

    public BuilderSetBase(Parameters2 params, CameraCharacteristics charateristic) {
        this.mCharacteristics = charateristic;
        if (this.mCharacteristics != null) {
            Camera2Util.setSupportedThumbnailSizeList(charateristic);
            this.mSensorInfoActiveArraySize = (Rect) this.mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            this.mZoomRatioList = Camera2Util.getZoomRatioList(this.mCharacteristics);
        }
        this.mParameters = params;
        this.mZoomCropRect = getScaleCropRegion(params.getZoom());
    }

    public Rect getActiveArraySize() {
        return this.mSensorInfoActiveArraySize;
    }

    public Size getPictureSize() {
        return this.mPictureSize;
    }

    public float getRatio() {
        return this.mRatio;
    }

    public void setShootMode(String value) {
        if (value == null) {
            value = this.mShotMode;
        }
        this.mShootMode = Camera2Converter.getShootMode(value);
        CamLog.m7i(CameraConstants.TAG, " Solution Shoot mode " + this.mShotMode);
    }

    public int getShootMode() {
        return this.mShootMode;
    }

    public void setLocation(Location loc) {
        this.mLocation = loc;
    }

    public void setSuperZoomMode(boolean isSuperZoom) {
        this.mIsSuperZoom = isSuperZoom;
    }

    public void setZoom(int zoomValue) {
        CamLog.m3d(CameraConstants.TAG, "applyZoom : requested value = " + zoomValue);
        Rect rect = getScaleCropRegion(zoomValue);
        if (rect != null && this.mZoomCropRect != null) {
            this.mZoomCropRect.set(rect);
        }
    }

    private Rect getScaleCropRegion(int zoomValue) {
        CamLog.m3d(CameraConstants.TAG, "getScaleCropRegion");
        if (this.mSensorInfoActiveArraySize == null) {
            return null;
        }
        int oWidth = this.mSensorInfoActiveArraySize.width();
        int oHeight = this.mSensorInfoActiveArraySize.height();
        if (this.mZoomRatioList == null || zoomValue >= this.mZoomRatioList.length || zoomValue < 0) {
            return new Rect(0, 0, oWidth, oHeight);
        }
        this.mZoomRatio = ((float) this.mZoomRatioList[zoomValue]) / 100.0f;
        int nWidth = (int) Math.floor((double) (((float) oWidth) / this.mZoomRatio));
        int nHeight = (int) Math.floor((double) (((float) oHeight) / this.mZoomRatio));
        int x0 = (oWidth - nWidth) / 2;
        int y0 = (oHeight - nHeight) / 2;
        return new Rect(x0, y0, x0 + nWidth, y0 + nHeight);
    }

    public float getZoomRatio() {
        return this.mZoomRatio;
    }

    public void setAreas(Parameters2 paramsNew) {
        if (paramsNew != null) {
            List<Area> newFocusArea = paramsNew.getFocusAreas();
            if (newFocusArea == null) {
                this.mFocusRegions = ZERO_WEIGHT_3A_REGION;
            } else if (!newFocusArea.equals(this.mParameters.getFocusAreas())) {
                this.mFocusRegions = Camera2Util.getMeteringRectFromArea(newFocusArea, this.mZoomCropRect);
            }
            List<Area> newMeteringArea = paramsNew.getMeteringAreas();
            if (newMeteringArea == null) {
                this.mAERegions = ZERO_WEIGHT_3A_REGION;
            } else if (!newMeteringArea.equals(this.mParameters.getMeteringAreas())) {
                this.mAERegions = Camera2Util.getMeteringRectFromArea(newMeteringArea, this.mZoomCropRect);
            }
        }
    }

    public void applyFocusMode(Builder builder, String focusMode) {
        this.mControlAFMode = Camera2Converter.getFocusMode(focusMode);
        CamLog.m3d(CameraConstants.TAG, "focusMode : " + focusMode + ",  request meta : " + this.mControlAFMode);
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(this.mControlAFMode));
            if (this.mControlAFMode == 4) {
                applyBuilder(builder, ParamConstants.KEY_MWFOCUS_MODE, Integer.valueOf(1));
            } else {
                applyBuilder(builder, ParamConstants.KEY_MWFOCUS_MODE, Integer.valueOf(0));
            }
            applyAFRegions(builder);
            applyAERegions(builder);
        }
    }

    public void applyAFRegions(Builder builder) {
        if (builder != null && this.mControlAFMode != 0) {
            if (this.mControlAFMode == 1) {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, this.mFocusRegions);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, ZERO_WEIGHT_3A_REGION);
            }
        }
    }

    public void applyAERegions(Builder builder) {
        if (builder != null) {
            if (this.mControlAFMode == 1 || this.mControlAFMode == 0) {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, this.mAERegions);
            } else {
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, ZERO_WEIGHT_3A_REGION);
            }
        }
    }

    public void applyZoom(Builder builder) {
        if (builder != null) {
            if (this.mPointZoomCropRect != null) {
                builder.set(CaptureRequest.SCALER_CROP_REGION, this.mPointZoomCropRect);
                return;
            }
            builder.set(CaptureRequest.SCALER_CROP_REGION, this.mZoomCropRect);
            applyBuilder(builder, ParamConstants.KEY_ZOOM_RATIO, Float.valueOf(getZoomRatio()));
        }
    }

    public void applySettingsForAutoFocus(Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
            applyAFRegions(builder);
            applyAERegions(builder);
        }
    }

    public void applySettingsForLockExposure(Builder builder) {
        if (builder != null) {
            this.mForcedAELock = true;
            builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.TRUE);
        }
    }

    public void applySettingsForUnlockExposure(Builder builder) {
        if (builder != null) {
            this.mForcedAELock = false;
            builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.FALSE);
        }
    }

    public void applySettingsForUnlockFocus(Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
        }
    }

    public void applySettingsForPrecapture(Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(1));
        }
    }

    public void setRecordMode(int recordMode) {
        this.mRecordMode = recordMode;
    }

    public boolean isRecordMode() {
        return this.mRecordMode == 1;
    }

    public boolean isAutoInManualMode(Parameters2 paramsNew) {
        if (paramsNew == null) {
            return false;
        }
        return "auto".equals(paramsNew.get("iso"));
    }

    public float getFocusLensDistanceFromUserValue(String value) {
        return (MAX_FOCUS_SUPPORT_VALUE * (MAX_FOCUS_USER_VALUE - Float.parseFloat(value))) / MAX_FOCUS_USER_VALUE;
    }

    public float getFocusUserValueFromFocusDistance(float focusDistance) {
        return ((MAX_FOCUS_SUPPORT_VALUE - MathUtils.min(MAX_FOCUS_SUPPORT_VALUE, focusDistance)) * MAX_FOCUS_USER_VALUE) / MAX_FOCUS_SUPPORT_VALUE;
    }

    public void applyBuilder(Builder builder, Object key, Object value) {
        try {
            builder.set((Key) key, value);
        } catch (IllegalArgumentException e) {
            if (CamLog.isTagExceptionLogOn()) {
                e.printStackTrace();
            }
        }
    }
}
