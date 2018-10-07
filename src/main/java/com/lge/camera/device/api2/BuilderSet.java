package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Handler;
import android.util.Range;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualUtil;
import java.util.HashMap;

public class BuilderSet extends BuilderSetBase {
    private static final int HFR_PREVIEW_FPS = 60;
    public static final int PREVIEW_SOLUTION_MAX_FRAME_CNT = 1;
    public static final boolean USE_MANUAL_EXPOSURE = false;
    private int mContrastValue = -1;
    private float[][] mCurrentCurve = ((float[][]) null);
    private int mEV;
    private boolean mIsFaceDetectionOn = false;
    private boolean mIsFlashForcedOn = false;
    private Boolean mIsHFRPreviewMode = Boolean.valueOf(false);
    private boolean mIsManualMode = false;
    protected int mPreviewCbSolution = 0;
    protected int mPreviewSolutionList = 0;
    private HashMap<Object, Object> mRequestBeforeStopPreviewMap;
    private TonemapCurve mSetTc = null;
    private Size mThumbnailSize = null;

    public BuilderSet(Parameters2 params, CameraCharacteristics charateristic) {
        super(params, charateristic);
    }

    public void setInfoFromParams(Builder builder, Parameters2 paramsNew) {
        int i = 0;
        if (paramsNew != null) {
            int i2;
            this.mShotMode = paramsNew.get(ParamConstants.KEY_APP_SHOT_MODE);
            boolean z = this.mShotMode != null && this.mShotMode.contains("manual");
            this.mIsManualMode = z;
            this.mPictureSize = paramsNew.getPictureSize();
            if (this.mPictureSize != null) {
                this.mRatio = ((float) this.mPictureSize.getWidth()) / ((float) this.mPictureSize.getHeight());
            }
            this.mThumbnailSize = Camera2Util.getThumbnailSize(getRatio());
            if ("on".equals(paramsNew.get(ParamConstants.KEY_APP_APPLY_PREVIEW_CB_SOLUTION))) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            this.mPreviewCbSolution = i2;
            setContrast(paramsNew.getContrast());
            setZoom(paramsNew.getZoom());
            setAreas(paramsNew);
            this.mParameters = paramsNew;
            if (builder != null) {
                applyShotMode(builder);
                applyZoom(builder);
                applyFocusMode(builder, paramsNew.getFocusMode());
                applyEV(builder, paramsNew.getExposureCompensation(), false);
                applyAutoExposureLock(builder, paramsNew.getAutoExposureLock());
                applyPreviewFpsRange(builder);
                applyBeautyParam(builder);
                applyOutfocusBlurParam(builder);
                applyAutoContrastParam(builder);
                applyLightFrame(builder);
                applyFilmParam(builder);
                applyColorCorrectionGains(builder);
                applyWB(builder, paramsNew.getInt("lg-wb"));
                applyISOAndShutterSpeed(builder, paramsNew);
                applyManualFocusValue(builder, paramsNew.get(ParamConstants.MANUAL_FOCUS_STEP));
                applyContrast(builder);
                applyCineEffect(builder);
                applySolution(builder, this.mPreviewSolutionList, 1);
                Key key = ParamConstants.KEY_FOCUS_PEAKING_MODE;
                if ("on".equals(this.mParameters.get(ParamConstants.KEY_FOCUS_PEAKING))) {
                    i = 1;
                }
                applyBuilder(builder, key, Integer.valueOf(i));
                applyBuilder(builder, ParamConstants.KEY_HDR_SET_MODE, Integer.valueOf(paramsNew.getInt("hdr-mode")));
                applyBuilder(builder, ParamConstants.KEY_AE_METERING_MODE, Integer.valueOf(1));
                applyFlash(builder);
            }
        }
    }

    public void applyColorCorrectionGains(Builder builder) {
        if (builder != null && this.mParameters != null && this.mParameters.getColorCorrectionGains() != null) {
            builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, this.mParameters.getColorCorrectionGains());
        }
    }

    private void applyBeautyParam(Builder builder) {
        if (this.mParameters != null && builder != null) {
            if (this.mParameters.isBeautySupported() == 0 || "on".equals(this.mParameters.get(ParamConstants.KEY_DRAWING_STICKER))) {
                applyBuilder(builder, ParamConstants.KEY_BEAUTY_VALUE, Integer.valueOf(0));
            } else {
                applyBuilder(builder, ParamConstants.KEY_BEAUTY_VALUE, Integer.valueOf(this.mParameters.getInt(ParamConstants.KEY_BEAUTY_LEVEL, 0)));
            }
        }
    }

    private void applyOutfocusBlurParam(Builder builder) {
        if (this.mParameters != null && builder != null) {
            if (this.mParameters.isOutfocusSupported() == 0) {
                applyBuilder(builder, ParamConstants.KEY_OUTFOCUS_BLUR_LEVEL, Integer.valueOf(0));
                return;
            }
            CamLog.m7i(CameraConstants.TAG, " outfocus blur : " + this.mParameters.getInt(ParamConstants.KEY_OUTFOCUS_LEVEL, 0));
            applyBuilder(builder, ParamConstants.KEY_OUTFOCUS_BLUR_LEVEL, Integer.valueOf(this.mParameters.getInt(ParamConstants.KEY_OUTFOCUS_LEVEL, 0)));
        }
    }

    private void applyAutoContrastParam(Builder builder) {
        if (this.mParameters != null && builder != null) {
            if (this.mParameters.getInt(ParamConstants.KEY_APP_AUTO_CONTRAST, 0) != 1) {
                applyBuilder(builder, ParamConstants.KEY_AUTOCONTRAST_LEVEL, Integer.valueOf(0));
                return;
            }
            CamLog.m7i(CameraConstants.TAG, " auto contrast : " + this.mParameters.getInt(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, 200));
            applyBuilder(builder, ParamConstants.KEY_AUTOCONTRAST_LEVEL, Integer.valueOf(this.mParameters.getInt(ParamConstants.KEY_APP_AUTO_CONTRAST_LEVEL, 200)));
        }
    }

    private void applyFilmParam(Builder builder) {
        if (this.mParameters != null && builder != null) {
            if ("true".equals(this.mParameters.get(ParamConstants.KEY_FILM_ENABLE))) {
                CamLog.m7i(CameraConstants.TAG, "film Type : " + this.mParameters.get(ParamConstants.KEY_FILM_TYPE));
                applyBuilder(builder, ParamConstants.KEY_FILM_FILTER_TYPE, this.mParameters.get(ParamConstants.KEY_FILM_TYPE));
                applyBuilder(builder, ParamConstants.KEY_FILM_FILTER_STRENGTH, Integer.valueOf(this.mParameters.getFilterStrength()));
                return;
            }
            applyBuilder(builder, ParamConstants.KEY_FILM_FILTER_TYPE, null);
            applyBuilder(builder, ParamConstants.KEY_FILM_FILTER_STRENGTH, Integer.valueOf(-1));
        }
    }

    public void applyEVResetMode(Builder builder, int value) {
        if (builder != null) {
            CamLog.m3d(CameraConstants.TAG, "applyEVResetMode mode = " + value);
            builder.set(ParamConstants.KEY_EV_RESET_MODE, Integer.valueOf(value));
        }
    }

    public void applyFlipMode(Builder builder) {
        if (this.mParameters != null && builder != null) {
            String flipMode = this.mParameters.get(ParamConstants.KEY_QC_SNAPSHOT_PICTURE_FLIP);
            if (flipMode != null) {
                CamLog.m3d(CameraConstants.TAG, "applyFlipMode flipMode = " + flipMode + " / flipKey : " + ParamConstants.KEY_SNAP_FLIP.toString());
                if (ParamConstants.FLIP_MODE_H.equals(flipMode)) {
                    applyBuilder(builder, ParamConstants.KEY_SNAP_FLIP, Integer.valueOf(1));
                } else if (ParamConstants.FLIP_MODE_V.equals(flipMode)) {
                    applyBuilder(builder, ParamConstants.KEY_SNAP_FLIP, Integer.valueOf(2));
                } else if (ParamConstants.FLIP_MODE_VH.equals(flipMode)) {
                    applyBuilder(builder, ParamConstants.KEY_SNAP_FLIP, Integer.valueOf(3));
                } else {
                    applyBuilder(builder, ParamConstants.KEY_SNAP_FLIP, Integer.valueOf(0));
                }
            }
        }
    }

    public void applyVideoHDR(Builder builder) {
        if (builder != null && this.mParameters != null) {
            String videoHDRMode = this.mParameters.get(ParamConstants.KEY_VIDEO_HDR_MODE);
            CamLog.m7i(CameraConstants.TAG, "apply videoHDRMode : " + videoHDRMode);
            if ("on".equals(videoHDRMode)) {
                applyBuilder(builder, ParamConstants.KEY_VIDEO_HDR, Integer.valueOf(1));
            } else {
                applyBuilder(builder, ParamConstants.KEY_VIDEO_HDR, Integer.valueOf(0));
            }
        }
    }

    public void applyHDR10(Builder builder) {
        if (builder != null && this.mParameters != null) {
            if ("on".equals(this.mParameters.get("hdr10"))) {
                applyBuilder(builder, ParamConstants.KEY_QC_HDR10_MODE, Byte.valueOf((byte) 2));
                CamLog.m7i(CameraConstants.TAG, "apply " + ParamConstants.KEY_QC_HDR10_MODE.getName() + " as 2");
                setRequestBeforeStopPreview(ParamConstants.KEY_QC_HDR10_MODE, Byte.valueOf((byte) 5));
                return;
            }
            applyBuilder(builder, ParamConstants.KEY_QC_HDR10_MODE, Byte.valueOf((byte) 0));
            CamLog.m7i(CameraConstants.TAG, "apply " + ParamConstants.KEY_QC_HDR10_MODE.getName() + " as 0");
        }
    }

    public void applyCineEffect(Builder builder) {
        if (builder != null && this.mParameters != null && ParamUtils.convertShotMode(CameraConstants.MODE_CINEMA).equals(this.mShotMode)) {
            String value = this.mParameters.get(ParamConstants.KEY_CINEMA_MODE);
            CamLog.m7i(CameraConstants.TAG, "apply CineEffect : " + value);
            applyBuilder(builder, ParamConstants.KEY_CINE_MODE, value);
            if (!ParamConstants.CINEMA_OFF.equals(value)) {
                applyBuilder(builder, ParamConstants.KEY_CINE_LUT_LEVEL, this.mParameters.get(ParamConstants.KEY_CINEMA_LUT));
                applyBuilder(builder, ParamConstants.KEY_CINE_VIGNETTE_LEVEL, this.mParameters.get(ParamConstants.KEY_CINEMA_VIGNETTE));
            }
        }
    }

    public void applySteadyCamMode(Builder builder) {
        String steadyCamMode = this.mParameters.get(ParamConstants.KEY_STEADY_CAM);
        CamLog.m3d(CameraConstants.TAG, "updateSteadyCam steadyCamMode = " + steadyCamMode);
        if (steadyCamMode != null && builder != null && Camera2Util.isSteadyCamSupported(this.mCharacteristics)) {
            if ("on".equals(steadyCamMode)) {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, Integer.valueOf(1));
                setRequestBeforeStopPreview(ParamConstants.KEY_EIS_END_STREAM, Byte.valueOf((byte) 1));
                return;
            }
            builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, Integer.valueOf(0));
        }
    }

    public void applyAutoExposureLock(Builder builder, boolean isLock) {
        if (builder != null) {
            if (this.mForcedAELock) {
                builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.TRUE);
            } else {
                builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(isLock));
            }
        }
    }

    public boolean isHFRPreviewMode() {
        return this.mIsHFRPreviewMode.booleanValue();
    }

    public void setHFRPreviewMode(boolean isPreview) {
        this.mIsHFRPreviewMode = Boolean.valueOf(isPreview);
    }

    public void applyPreviewFpsRange(Builder builder) {
        if (builder != null) {
            int[] fps = new int[2];
            this.mParameters.getPreviewFpsRange(fps);
            CamLog.m7i(CameraConstants.TAG, "mIsHFRPreviewMode " + this.mIsHFRPreviewMode + " min Fps : " + fps[0]);
            if (this.mIsHFRPreviewMode.booleanValue()) {
                if (fps[0] > 60) {
                    fps[0] = 60;
                }
            } else if (ParamConstants.VALUE_BINNING_MODE.equals(this.mParameters.get(ParamConstants.KEY_BINNING_PARAM))) {
                CamLog.m7i(CameraConstants.TAG, "binning mode, so min fps set to 5 forcely");
                fps[0] = 5;
                fps[1] = 30;
            }
            Range<Integer> currentFpsRange = (Range) builder.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);
            if (((Integer) currentFpsRange.getLower()).intValue() != fps[0] || ((Integer) currentFpsRange.getUpper()).intValue() != fps[1]) {
                CamLog.m7i(CameraConstants.TAG, "change fps range min : " + fps[0] + " / max : " + fps[1]);
                builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range(Integer.valueOf(fps[0]), Integer.valueOf(fps[1])));
            }
        }
    }

    public void applyISOAndShutterSpeed(Builder builder, Parameters2 paramsNew) {
        if (builder != null && paramsNew != null && this.mIsManualMode) {
            String valueISO = paramsNew.get("iso");
            String valueSS = paramsNew.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL);
            if (valueISO != null && valueSS != null) {
                if (isAutoInManualMode(paramsNew)) {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                    return;
                }
                builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.TRUE);
                builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(0));
                applyManualVirtualExposureValue(builder, false);
            }
        }
    }

    public boolean applyManualVirtualExposureValue(Builder builder, boolean isTakingPicture) {
        if (builder == null || this.mParameters == null || !this.mIsManualMode) {
            return false;
        }
        String strISO = this.mParameters.get("iso");
        String strExposureTimeMilliSec = this.mParameters.get(ParamConstants.KEY_MANUAL_SHUTTER_SPEED_ZSL);
        if (!(strISO == null || strExposureTimeMilliSec == null)) {
            if (!isAutoInManualMode(this.mParameters)) {
                double exposureTimeSec = (double) (Float.parseFloat(strExposureTimeMilliSec) / 1000.0f);
                double aperture = Math.floor(Double.parseDouble(this.mParameters.get(ParamConstants.KEY_F_NUMBER)) * 10.0d) / 10.0d;
                double maxExposureTimeSec = Camera2Util.convertAvailableFPS(exposureTimeSec);
                double currentEV = Camera2Util.log2(Math.pow(aperture, 2.0d) / exposureTimeSec);
                double baseEV = Camera2Util.log2(Math.pow(aperture, 2.0d) / maxExposureTimeSec);
                int isoCompensation = Integer.parseInt(strISO);
                CamLog.m3d(CameraConstants.TAG, "- Manual - origin - ISO :" + isoCompensation + ", exposure-time :" + (1000.0d * exposureTimeSec));
                if (Double.compare(currentEV, baseEV) < 0) {
                    if (isTakingPicture) {
                        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(ManualUtil.convertShutterSpeedMilliToNano(strExposureTimeMilliSec)));
                        builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(Integer.parseInt(strISO)));
                        CamLog.m3d(CameraConstants.TAG, "- Manual - takingpicture - ISO :" + strISO + ", exposure-time :" + strExposureTimeMilliSec);
                    } else {
                        isoCompensation = (int) (((double) Integer.parseInt(strISO)) * Math.pow(2.0d, baseEV - currentEV));
                        strExposureTimeMilliSec = String.valueOf(1000.0d * maxExposureTimeSec);
                        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(ManualUtil.convertShutterSpeedMilliToNano(strExposureTimeMilliSec)));
                        builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(isoCompensation));
                        CamLog.m3d(CameraConstants.TAG, "- Manual - after - ISO :" + isoCompensation + ", exposure-time :" + strExposureTimeMilliSec + ", EV diff:" + (baseEV - currentEV));
                    }
                    return true;
                }
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, Long.valueOf(ManualUtil.convertShutterSpeedMilliToNano(strExposureTimeMilliSec)));
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, Integer.valueOf(isoCompensation));
                return false;
            }
        }
        return false;
    }

    public void applyEV(Builder builder, int value, boolean oneTimeReq) {
        if (builder != null) {
            CamLog.m3d(CameraConstants.TAG, "EV = " + value);
            if (oneTimeReq) {
                builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(value));
                return;
            }
            this.mEV = value;
            applyEV(builder);
        }
    }

    public void getCurrentContrast(TotalCaptureResult result, Handler handler) {
        if (result != null && this.mContrastValue == 0) {
            try {
                TonemapCurve currentCurve = (TonemapCurve) result.get(CaptureResult.TONEMAP_CURVE);
                if (currentCurve != null) {
                    final float[][] newCurrentCurve = new float[3][];
                    this.mCurrentCurve = new float[3][];
                    for (int i = 0; i <= 2; i++) {
                        newCurrentCurve[i] = new float[(currentCurve.getPointCount(i) * 2)];
                        currentCurve.copyColorCurve(i, newCurrentCurve[i], 0);
                    }
                    handler.post(new Runnable() {
                        public void run() {
                            if (BuilderSet.this.mContrastValue == 0) {
                                BuilderSet.this.mCurrentCurve = newCurrentCurve;
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setContrast(int contrastValue) {
        this.mContrastValue = contrastValue;
        CamLog.m3d(CameraConstants.TAG, "mContrastValue : " + this.mContrastValue);
        if (this.mCurrentCurve != null) {
            if (this.mContrastValue == -1) {
                this.mSetTc = null;
                this.mCurrentCurve = (float[][]) null;
            } else if (this.mContrastValue < 0 || this.mContrastValue > 100 || this.mContrastValue == 0) {
                this.mSetTc = null;
            } else {
                float[][] newValues = new float[3][];
                float contrast = ((float) this.mContrastValue) / 100.0f;
                CamLog.m3d(CameraConstants.TAG, "contrast : " + contrast);
                for (int i = 0; i < this.mCurrentCurve.length; i++) {
                    float[] array = new float[this.mCurrentCurve[i].length];
                    System.arraycopy(this.mCurrentCurve[i], 0, array, 0, array.length);
                    for (int j = 0; j < array.length; j++) {
                        if (j < array.length / 2) {
                            array[j] = array[j] - contrast;
                        }
                        if (j >= array.length / 2) {
                            array[j] = array[j] + contrast;
                        }
                        if (array[j] < 0.0f) {
                            array[j] = 0.0f;
                        }
                        if (array[j] > 1.0f) {
                            array[j] = 1.0f;
                        }
                    }
                    newValues[i] = array;
                }
                this.mSetTc = new TonemapCurve(newValues[0], newValues[1], newValues[2]);
            }
        }
    }

    public void applyContrast(Builder builder) {
        if (builder != null) {
            if (this.mSetTc == null) {
                builder.set(CaptureRequest.TONEMAP_MODE, Integer.valueOf(1));
                return;
            }
            builder.set(CaptureRequest.TONEMAP_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.TONEMAP_CURVE, this.mSetTc);
        }
    }

    public void applyWB(Builder builder, int value) {
        if (builder != null && this.mParameters != null && this.mParameters.getColorCorrectionGains() == null) {
            if (value == 0) {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(1));
                builder.set(ParamConstants.KEY_QTI_AWB_CCT, Integer.valueOf(0));
                return;
            }
            builder.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(0));
            if (value < 0) {
                value = 0;
            }
            try {
                Integer awbCCT = Integer.valueOf(value);
                CamLog.m3d(CameraConstants.TAG, "[WB CCT] set : " + awbCCT);
                builder.set(ParamConstants.KEY_QTI_AWB_CCT, awbCCT);
            } catch (IllegalArgumentException e) {
                if (((Integer) builder.get(CaptureRequest.COLOR_CORRECTION_MODE)).intValue() == 0) {
                    Integer[] color = new Integer[3];
                    Camera2Util.getWhiteBalancedColor(color, value);
                    RggbChannelVector gains = new RggbChannelVector(1.0f + (((float) color[2].intValue()) / 255.0f), 1.0f, 1.0f, 1.0f + (((float) color[0].intValue()) / 255.0f));
                    CamLog.m3d(CameraConstants.TAG, "red = " + color[0] + ", green = " + color[1] + ", blue = " + color[2]);
                    builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, gains);
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "WB ignored");
            }
        }
    }

    public void applyFlashForcedOn(Builder builder, boolean on) {
        if (builder != null) {
            CamLog.m3d(CameraConstants.TAG, "applyFlashForcedOn " + on);
            if (((Integer) builder.get(CaptureRequest.CONTROL_AE_MODE)).intValue() == 0 && on) {
                CamLog.m3d(CameraConstants.TAG, "ae off flash firing");
                builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(1));
                return;
            }
            this.mIsFlashForcedOn = on;
            applyFlash(builder);
        }
    }

    public void applyFaceDetection(Builder builder, boolean on) {
        if (builder != null) {
            CamLog.m3d(CameraConstants.TAG, "applyFaceDetection " + on);
            this.mIsFaceDetectionOn = on;
            applyFaceDetection(builder);
        }
    }

    public void applyFaceDetection(Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(this.mIsFaceDetectionOn ? 1 : 0));
        }
    }

    public void applyCommonSettings(Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
            builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(this.mControlAFMode));
            builder.set(CaptureRequest.EDGE_MODE, Integer.valueOf(2));
            applyBuilder(builder, ParamConstants.KEY_FOCUS_PEAKING_MODE, Integer.valueOf("on".equals(this.mParameters.get(ParamConstants.KEY_FOCUS_PEAKING)) ? 1 : 0));
            applyBuilder(builder, ParamConstants.KEY_HDR_SET_MODE, Integer.valueOf(this.mParameters.getInt("hdr-mode")));
            CamLog.m7i(CameraConstants.TAG, " mRecordMode  " + this.mRecordMode);
            applyBuilder(builder, ParamConstants.KEY_RECORD_MODE, Integer.valueOf(this.mRecordMode));
            applyAFRegions(builder);
            applyAERegions(builder);
            applyAutoExposureLock(builder, this.mParameters.getAutoExposureLock());
            applyShotMode(builder);
            applyFaceDetection(builder);
            applyEV(builder);
            applyZoom(builder);
            applyBeautyParam(builder);
            applyOutfocusBlurParam(builder);
            applyAutoContrastParam(builder);
            applyFilmParam(builder);
            applyLightFrame(builder);
            applyContrast(builder);
            applySolution(builder, this.mPreviewSolutionList, 1);
            applyPreviewFpsRange(builder);
            applyWB(builder, this.mParameters.getInt("lg-wb"));
            applySettingsForManualMode(builder);
            applyHistogram(builder);
            applyBuilder(builder, ParamConstants.KEY_AE_METERING_MODE, Integer.valueOf(1));
            if (this.mRecordMode == 1) {
                applySteadyCamMode(builder);
            }
        }
    }

    public void applySettingsForManualMode(Builder builder) {
        if (builder != null && this.mParameters != null && this.mIsManualMode) {
            applyISOAndShutterSpeed(builder, this.mParameters);
            applyEV(builder, this.mParameters.getExposureCompensation(), false);
            applyWB(builder, this.mParameters.getInt("lg-wb"));
            applyManualFocusValue(builder, this.mParameters.get(ParamConstants.MANUAL_FOCUS_STEP));
        }
    }

    private void applyManualFocusValue(Builder builder, String value) {
        if (builder != null && this.mParameters != null && !"-1".equals(value) && !"auto".equals(value)) {
            float valueFloat = getFocusLensDistanceFromUserValue(value);
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, Float.valueOf(valueFloat));
            CamLog.m3d(CameraConstants.TAG, "focus lens distance request : " + valueFloat);
        }
    }

    public void applyFlash(Builder builder) {
        if (builder != null && this.mParameters != null) {
            String mode = this.mParameters.getFlashMode();
            CamLog.m3d(CameraConstants.TAG, "applyFlash " + mode + " mIsFlashForcedOn " + this.mIsFlashForcedOn);
            if (!applyFlashTimer(builder, mode)) {
                if (((Integer) builder.get(CaptureRequest.CONTROL_AE_MODE)).intValue() != 0) {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                    if ("on".equals(mode) || this.mIsFlashForcedOn) {
                        builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(3));
                    } else if ("off".equals(mode)) {
                        builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
                    } else if (ParamConstants.FLASH_MODE_TORCH.equals(mode)) {
                        builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
                    }
                } else if ("off".equals(mode)) {
                    builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
                } else if (ParamConstants.FLASH_MODE_TORCH.equals(mode)) {
                    builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
                }
            }
        }
    }

    private boolean applyFlashTimer(Builder builder, String modeValue) {
        if (builder == null || modeValue == null) {
            return false;
        }
        if (!ParamConstants.FLASH_MODE_TIMER_ON.equals(modeValue) && !ParamConstants.FLASH_MODE_TIMER_OFF.equals(modeValue)) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "applyFlash KEY_FLASH_TIMER : " + modeValue);
        if (((Integer) builder.get(CaptureRequest.CONTROL_AE_MODE)).intValue() != 0) {
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
        }
        if (ParamConstants.FLASH_MODE_TIMER_ON.equals(modeValue)) {
            builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
            applyBuilder(builder, ParamConstants.KEY_FLASH_TIMER, Integer.valueOf(1));
        } else if (ParamConstants.FLASH_MODE_TIMER_OFF.equals(modeValue)) {
            builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            applyBuilder(builder, ParamConstants.KEY_FLASH_TIMER, Integer.valueOf(0));
        }
        return true;
    }

    public void applyLightFrame(Builder builder) {
        if (builder != null && this.mParameters != null) {
            int lightFrameTime = this.mParameters.getLightFrameTime();
            CamLog.m3d(CameraConstants.TAG, "[lightFrame] remain time value : " + lightFrameTime);
            applyBuilder(builder, ParamConstants.KEY_HAL_LIGHTFRAME_TIME, Integer.valueOf(lightFrameTime));
        }
    }

    public void applyEV(Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(this.mEV));
        }
    }

    public void applySettingForJpeg(Builder builder) {
        if (builder != null && this.mParameters != null) {
            String rotation = this.mParameters.get(ParamConstants.KEY_ROTATION);
            if (rotation != null) {
                builder.set(CaptureRequest.JPEG_ORIENTATION, Integer.valueOf(rotation));
            }
            builder.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, this.mThumbnailSize);
            builder.set(CaptureRequest.JPEG_GPS_LOCATION, this.mLocation);
            builder.set(CaptureRequest.JPEG_QUALITY, Byte.valueOf((byte) 97));
            applyFlipMode(builder);
        }
    }

    public void applySettingForCpp(Builder builder, boolean enable) {
        if (builder != null) {
            if (enable) {
                builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(2));
                builder.set(CaptureRequest.EDGE_MODE, Integer.valueOf(2));
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(2));
                builder.set(CaptureRequest.TONEMAP_MODE, Integer.valueOf(2));
                return;
            }
            builder.set(ParamConstants.KEY_NO_REPROC, Integer.valueOf(1));
            builder.set(CaptureRequest.EDGE_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(0));
        }
    }

    public boolean setPointZoom(Rect rect) {
        if (rect == null) {
            this.mPointZoomCropRect = null;
            return false;
        }
        if (this.mPointZoomCropRect == null) {
            this.mPointZoomCropRect = new Rect(rect);
        } else {
            this.mPointZoomCropRect.set(rect);
        }
        return true;
    }

    public void applyHistogram(Builder builder) {
        if (builder != null) {
            Byte disable = Byte.valueOf((byte) 0);
            Byte enable = Byte.valueOf((byte) 1);
            Key key = ParamConstants.KEY_QC_HISTOGRAM;
            if (!this.mIsManualMode) {
                enable = disable;
            }
            applyBuilder(builder, key, enable);
        }
    }

    public void applyPanoramaCapture(Builder builder, long exposureTime, int sensorSensitivity) {
        if (builder == null) {
            CamLog.m11w(CameraConstants.TAG, "applyPanoramaCapture, builder is null. return.");
            return;
        }
        try {
            builder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(1));
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, ZERO_WEIGHT_3A_REGION);
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, ZERO_WEIGHT_3A_REGION);
            builder.set(CaptureRequest.CONTROL_AWB_LOCK, Boolean.TRUE);
            builder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            builder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            builder.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.TRUE);
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(0));
        } catch (IllegalArgumentException e) {
            if (CamLog.isTagExceptionLogOn()) {
                e.printStackTrace();
            }
        }
    }

    public int getPreviewSolutionList() {
        return this.mPreviewSolutionList;
    }

    public void setPreviewSolution(int enabledSolutionList) {
        this.mPreviewSolutionList = enabledSolutionList;
    }

    public void applySolution(Builder builder, int enabledSolutionList, int frameCount) {
        if (builder != null) {
            CamLog.m7i(CameraConstants.TAG, " enabledSolution " + enabledSolutionList + " max frameCount " + frameCount + " apply previe CB ? " + this.mPreviewCbSolution);
            applyBuilder(builder, ParamConstants.KEY_ENALBED_SOLUTION_PREVIEWCB, Integer.valueOf(this.mPreviewCbSolution));
            applyBuilder(builder, ParamConstants.KEY_ENALBED_SOLUTIONS, Integer.valueOf(enabledSolutionList));
            applyBuilder(builder, ParamConstants.KEY_SOLUTION_MAX_FRAME_COUNT, Integer.valueOf(frameCount));
        }
    }

    public void applyShotMode(Builder builder) {
        if (builder != null) {
            String shotMode = this.mShootMode == 2 ? ParamUtils.convertShotMode(CameraConstants.MODE_BURST) : this.mShotMode;
            CamLog.m7i(CameraConstants.TAG, " shotMode " + shotMode);
            applyBuilder(builder, ParamConstants.KEY_UI_SHOT_MODE, shotMode);
        }
    }

    public void applyTakePictureState(Builder builder, boolean isStartTakePic) {
        if (builder != null) {
            applyBuilder(builder, ParamConstants.KEY_STATE_TAKE_PIUCTURE, isStartTakePic ? new Integer(1) : new Integer(0));
        }
    }

    public void reset() {
        this.mPreviewSolutionList = 0;
        this.mPreviewCbSolution = 0;
        this.mIsFlashForcedOn = false;
        this.mIsFaceDetectionOn = false;
        this.mIsHFRPreviewMode = Boolean.valueOf(false);
        this.mContrastValue = -1;
        this.mSetTc = null;
        this.mCurrentCurve = (float[][]) null;
        this.mShotMode = null;
        this.mIsSuperZoom = false;
        this.mRecordMode = 0;
        if (this.mRequestBeforeStopPreviewMap != null) {
            this.mRequestBeforeStopPreviewMap.clear();
        }
    }

    public void setSuperZoomParam(Parameters2 params) {
        if (params != null) {
            params.set(ParamConstants.KEY_APP_SOLUTION_SUPERZOOM, this.mIsSuperZoom ? 1 : 0);
        }
    }

    private void setRequestBeforeStopPreview(Object key, Object value) {
        if (this.mRequestBeforeStopPreviewMap == null) {
            this.mRequestBeforeStopPreviewMap = new HashMap();
        }
        this.mRequestBeforeStopPreviewMap.put(key, value);
    }

    public boolean applyBuilderBeforeStopPreview(Builder builder) {
        if (this.mRequestBeforeStopPreviewMap == null || this.mRequestBeforeStopPreviewMap.isEmpty() || builder == null) {
            return false;
        }
        for (Object key : this.mRequestBeforeStopPreviewMap.keySet()) {
            Object value = this.mRequestBeforeStopPreviewMap.get(key);
            builder.set((Key) key, value);
            CamLog.m3d(CameraConstants.TAG, "apply Key : " + ((Key) key).getName() + ", Value : " + value);
        }
        return true;
    }
}
