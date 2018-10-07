package com.lge.camera.solution;

import android.graphics.Rect;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.media.Image;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.api2.Parameters2;
import com.lge.camera.util.CamLog;

public class SolutionManager {
    private static int FULL_SOLUTION_MASK = -1;
    private static SolutionManager sSolutionManager;
    private int mCameraId;
    private boolean mIsEnabledAppShotSolution = false;
    private boolean mIsRecording = false;
    private boolean mIsSolutionOpened = false;
    private TotalCaptureResult mLastCaptureResult = null;
    private int mShootMode;
    SolutionParameters[] mSolutionParameters = new SolutionParameters[3];
    SolutionPickEngine mSolutionPickEngine = new SolutionPickEngine();

    public native void Close();

    public native int[] Configure(SolutionParameters solutionParameters, Image image);

    public native void Open(int i);

    public native byte[] Process(SolutionParameters solutionParameters, Image[] imageArr, SolutionPickResult solutionPickResult);

    public static SolutionManager getInstance() {
        if (sSolutionManager == null) {
            sSolutionManager = new SolutionManager();
        }
        return sSolutionManager;
    }

    private SolutionManager() {
        if (FunctionProperties.isSupportedShotSolution()) {
            System.loadLibrary("LGCameraSolution-jni");
        }
        SolutionParsingUtil.parseConfig();
        this.mSolutionParameters[0] = new SolutionParameters();
        this.mSolutionParameters[1] = new SolutionParameters();
    }

    public void openSolutions(int cameraId) {
        if (!this.mIsSolutionOpened) {
            this.mCameraId = cameraId;
            if (FunctionProperties.isSupportedShotSolution()) {
                Open(checkSolutionOpen());
            }
            this.mIsSolutionOpened = true;
        }
    }

    public void closeSolutions() {
        if (this.mIsSolutionOpened) {
            if (FunctionProperties.isSupportedShotSolution()) {
                Close();
            }
            this.mIsSolutionOpened = false;
        }
    }

    public int getMultiFrameCount(TotalCaptureResult result, Parameters2 params) {
        if (result == null || this.mSolutionParameters[1] == null) {
            return 0;
        }
        updateParameters(result, this.mSolutionParameters[1], 1, null);
        return this.mSolutionPickEngine.getSolutionFrameCount(this.mSolutionParameters[1]);
    }

    public int getSolutionFrameCount(int solutionType) {
        return this.mSolutionPickEngine.getFrameCount(solutionType);
    }

    public SolutionPickResult checkSnapShotSolutions(TotalCaptureResult result, int shootMode, Parameters2 params) {
        if (result == null || this.mSolutionParameters[0] == null) {
            return null;
        }
        setShootMode(shootMode);
        updateParameters(result, this.mSolutionParameters[0], 0, params);
        return this.mSolutionPickEngine.checkSolution(this.mSolutionParameters[0], 0);
    }

    public synchronized SolutionPickResult checkPreviewSolutions(TotalCaptureResult result, Parameters2 params) {
        SolutionPickResult checkSolution;
        updateParameters(result, this.mSolutionParameters[1], 1, params);
        if (this.mIsRecording) {
            checkSolution = this.mSolutionPickEngine.checkSolution(this.mSolutionParameters[1], 2);
        } else {
            checkSolution = this.mSolutionPickEngine.checkSolution(this.mSolutionParameters[1], 1);
        }
        return checkSolution;
    }

    public int[] configureSolutions(Image image) {
        if (!FunctionProperties.isSupportedShotSolution() || image == null) {
            return null;
        }
        return Configure(this.mSolutionParameters[0], image);
    }

    public byte[] processSolutions(TotalCaptureResult[] resultArray, Image[] imageArray, SolutionPickResult pickResult) {
        if (!FunctionProperties.isSupportedShotSolution() || resultArray == null || imageArray == null) {
            return null;
        }
        return Process(this.mSolutionParameters[0], imageArray, pickResult);
    }

    public void updateParameters(TotalCaptureResult result, SolutionParameters solutionParams, int currentMode, Parameters2 params) {
        if (solutionParams != null) {
            try {
                solutionParams.setCameraId(this.mCameraId);
                solutionParams.setShootMode(this.mShootMode);
                updateUserSettingParam(solutionParams, params, currentMode);
                updateTotalCaptureParam(solutionParams, result, currentMode);
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateUserSettingParam(SolutionParameters solutionParams, Parameters2 params, int currentMode) throws IllegalArgumentException {
        boolean z = true;
        if (solutionParams != null && params != null) {
            boolean z2;
            if (currentMode == 0) {
                solutionParams.setFilmEmulatorMode("true".equals(params.get(ParamConstants.KEY_FILM_ENABLE)));
                solutionParams.setSignatureMode("on".equals(params.get(ParamConstants.KEY_SIGNATURE_ENABLE)));
            }
            Size size = params.getVideoSize();
            if (size != null) {
                solutionParams.setVideoSize(size.getWidth(), size.getHeight());
            }
            int[] fps = new int[2];
            params.getPreviewFpsRange(fps);
            solutionParams.setFpsValue(fps[1]);
            solutionParams.setBeautyEnabled(params.isBeautySupported());
            solutionParams.setOutFocusEnabled(params.isOutfocusSupported());
            solutionParams.setFlash(params.get(ParamConstants.KEY_FLASH_FIRE));
            if ("0".equals(params.get("hdr-mode"))) {
                z2 = false;
            } else {
                z2 = true;
            }
            solutionParams.setHDRMode(z2);
            if (params.getInt(ParamConstants.KEY_APP_SOLUTION_SUPERZOOM, 0) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            solutionParams.setZoomEnabled(z2);
            if (this.mShootMode == 16) {
                solutionParams.setNightShotEnabled("on".equals(params.get(ParamConstants.KEY_MANUAL_NOISE_REDUCTION)));
            } else {
                solutionParams.setNightShotEnabled(true);
            }
            String shotMode = params.get(ParamConstants.KEY_APP_SHOT_MODE);
            if (shotMode == null || !shotMode.contains("smart_cam")) {
                solutionParams.setAutoContrastEnabled(true);
                solutionParams.setAutoContrastForTextEnabled(false);
            } else {
                if (params.getInt(ParamConstants.KEY_APP_AUTO_CONTRAST) != 1) {
                    z = false;
                }
                solutionParams.setAutoContrastForTextEnabled(z);
                solutionParams.setAutoContrastEnabled(false);
            }
            solutionParams.setSumBinninMode(ParamConstants.VALUE_BINNING_MODE.equals(params.get(ParamConstants.KEY_BINNING_PARAM)));
        }
    }

    private void updateTotalCaptureParam(SolutionParameters solutionParams, TotalCaptureResult result, int currentMode) throws IllegalArgumentException {
        if (solutionParams != null && result != null) {
            if (currentMode == 0) {
                solutionParams.setISO(((Integer) result.get(CaptureResult.SENSOR_SENSITIVITY)).intValue());
                solutionParams.setExposureTime(((Long) result.get(CaptureResult.SENSOR_EXPOSURE_TIME)).longValue());
                solutionParams.setCropRegion((Rect) result.get(CaptureResult.SCALER_CROP_REGION));
                Float luxIndex = (Float) result.get(ParamConstants.KEY_LUX_INDEX);
                solutionParams.setLuxIndex(luxIndex == null ? 0.0f : luxIndex.floatValue());
                Face[] faces = (Face[]) result.get(CaptureResult.STATISTICS_FACES);
                if (faces == null || faces.length <= 0) {
                    solutionParams.setFaceCount(0);
                } else {
                    solutionParams.setFaceCount(faces.length);
                    solutionParams.setFaceInfo(faces);
                }
                Integer isLowLight = (Integer) result.get(ParamConstants.KEY_LOW_LIGHT);
                boolean isLowLightDetected = isLowLight == null ? false : Integer.compare(1, isLowLight.intValue()) == 0;
                solutionParams.setLowLightDetected(isLowLightDetected);
            }
            Float userGain = (Float) result.get(ParamConstants.KEY_USER_GAIN);
            solutionParams.setRealGain(userGain == null ? 0.0f : userGain.floatValue());
            Float zoomRatio = (Float) result.getRequest().get(ParamConstants.KEY_ZOOM_RATIO);
            solutionParams.setZoomRatio(zoomRatio == null ? 1.0f : zoomRatio.floatValue());
            Integer backlightStatus = (Integer) result.get(ParamConstants.KEY_BACK_LIGHT_DETECTION);
            if (backlightStatus != null) {
                boolean z;
                if (Integer.compare(1, backlightStatus.intValue()) == 0) {
                    z = true;
                } else {
                    z = false;
                }
                solutionParams.setBackLightDetected(z);
            }
        }
    }

    public void setRecordingState(boolean isOn) {
        this.mIsRecording = isOn;
    }

    public void setCaptureResult(TotalCaptureResult result) {
        this.mLastCaptureResult = result;
    }

    public TotalCaptureResult getLastCaptureResult() {
        return this.mLastCaptureResult;
    }

    public void setShootMode(int mode) {
        this.mShootMode = mode;
    }

    public void reset() {
        int i = 0;
        setRecordingState(false);
        this.mLastCaptureResult = null;
        this.mIsEnabledAppShotSolution = false;
        SolutionParameters[] solutionParametersArr = this.mSolutionParameters;
        int length = solutionParametersArr.length;
        while (i < length) {
            SolutionParameters params = solutionParametersArr[i];
            if (params != null) {
                params.initParamters();
            }
            i++;
        }
    }

    public void determineAppShotSolution(Parameters2 params) {
        if (params != null) {
            if (ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED == 0) {
                this.mIsEnabledAppShotSolution = false;
            } else if (ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED == 1) {
                this.mIsEnabledAppShotSolution = true;
            } else if (ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED == 2) {
                this.mIsEnabledAppShotSolution = ParamConstants.VALUE_BINNING_MODE.equals(params.get(ParamConstants.KEY_BINNING_PARAM));
            }
            CamLog.m3d(CameraConstants.TAG, "determineAppShotSolution : " + this.mIsEnabledAppShotSolution);
        }
    }

    public boolean IsEnableAppShotSolution() {
        return this.mIsEnabledAppShotSolution;
    }

    private int checkSolutionOpen() {
        if (ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED == 1) {
            return FULL_SOLUTION_MASK;
        }
        if (ConfigurationUtil.sSHOT_SOLUTION_SUPPORTED == 2) {
            return FULL_SOLUTION_MASK & 8;
        }
        return FULL_SOLUTION_MASK;
    }
}
