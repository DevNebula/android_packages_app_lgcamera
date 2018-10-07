package com.lge.camera.device.api2;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.HashMap;

public class CameraOperationMode {
    public static final int OP_MODE_INVALID = 0;
    public static final int OP_SETTING_120_FPS_RECORDING = 57345;
    public static final int OP_SETTING_240_FPS_RECORDING = 57347;
    public static final int OP_SETTING_60_FPS_RECORDING = 57346;
    public static final int OP_SETTING_EIS_LOCK_AHEAD = 57352;
    public static final int OP_SETTING_EIS_REAL_TIME = 57348;
    public static final int OP_SETTING_HDR10 = 57408;
    public static final int OP_SETTING_HDR_PREVIEW = 57360;
    public static final int OP_SETTING_VIDEO_HDR = 57376;
    public static final int OP_SHOT_MODE_360_PANORAMA = 58752;
    public static final int OP_SHOT_MODE_AI_CAMERA = 59520;
    public static final int OP_SHOT_MODE_CINE_VIDEO = 59136;
    public static final int OP_SHOT_MODE_DUAL_POP = 59264;
    public static final int OP_SHOT_MODE_FOOD = 57728;
    public static final int OP_SHOT_MODE_GRID_SHOT = 58240;
    public static final int OP_SHOT_MODE_GUIDE_SHOT = 58112;
    public static final int OP_SHOT_MODE_LG_LENS = 59776;
    public static final int OP_SHOT_MODE_MANUAL = 57600;
    public static final int OP_SHOT_MODE_MANUAL_VIDEO = 58496;
    public static final int OP_SHOT_MODE_MATCH_SHOT = 58368;
    public static final int OP_SHOT_MODE_NORMAL = 57344;
    public static final int OP_SHOT_MODE_NORMAL_VIDEO = 57472;
    public static final int OP_SHOT_MODE_OUT_FOCUS = 59648;
    public static final int OP_SHOT_MODE_PANORAMA = 58624;
    public static final int OP_SHOT_MODE_POPOUT = 59392;
    public static final int OP_SHOT_MODE_SLOW_MOTION = 59008;
    public static final int OP_SHOT_MODE_SNAP_MOVIE = 58880;
    public static final int OP_SHOT_MODE_SNAP_SHOT = 57984;
    public static final int OP_SHOT_MODE_SUM_BINNING = 59904;
    public static final int OP_SHOT_MODE_SUM_BINNING_REC = 60032;
    public static final int OP_SHOT_MODE_SUM_BINNING_SENSOR = 60160;
    public static final int OP_SHOT_MODE_SUM_BINNING_SENSOR_REC = 60288;
    public static final int OP_SHOT_MODE_TIME_LAPSE = 57856;
    static final HashMap<String, Integer> sOpModeMap = new HashMap();

    static {
        sOpModeMap.put(ParamUtils.convertShotMode("mode_normal"), Integer.valueOf(57344));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_BEAUTY), Integer.valueOf(57344));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA), Integer.valueOf(58624));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA_LG), Integer.valueOf(58624));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA_LG_RAW), Integer.valueOf(58624));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA_LG_360_PROJ), Integer.valueOf(OP_SHOT_MODE_360_PANORAMA));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_REAR_OUTFOCUS), Integer.valueOf(59648));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_FRONT_OUTFOCUS), Integer.valueOf(59648));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_MANUAL_CAMERA), Integer.valueOf(57600));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_MANUAL_VIDEO), Integer.valueOf(OP_SHOT_MODE_MANUAL_VIDEO));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_TIME_LAPSE_VIDEO), Integer.valueOf(57856));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SNAP), Integer.valueOf(58880));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SLOW_MOTION), Integer.valueOf(OP_SHOT_MODE_SLOW_MOTION));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_POPOUT_CAMERA), Integer.valueOf(59392));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_DUAL_POP_CAMERA), Integer.valueOf(OP_SHOT_MODE_DUAL_POP));
        sOpModeMap.put(ParamUtils.convertShotMode("mode_food"), Integer.valueOf(OP_SHOT_MODE_FOOD));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SQUARE_SNAPSHOT), Integer.valueOf(OP_SHOT_MODE_SNAP_SHOT));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SQUARE_OVERLAP), Integer.valueOf(58112));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SQUARE_GRID), Integer.valueOf(OP_SHOT_MODE_GRID_SHOT));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SQUARE_SPLICE), Integer.valueOf(58368));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_CINEMA), Integer.valueOf(59136));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SMART_CAM), Integer.valueOf(OP_SHOT_MODE_AI_CAMERA));
        sOpModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SMART_CAM_FRONT), Integer.valueOf(OP_SHOT_MODE_AI_CAMERA));
    }

    private static int getOperationShotMode(Parameters2 parameters, boolean isRecording) {
        int operationShotMode = 57344;
        String mode = parameters.get(ParamConstants.KEY_APP_SHOT_MODE);
        String binningType = parameters.get(ParamConstants.KEY_APP_BINNING_TYPE);
        String binningParam = parameters.get(ParamConstants.KEY_BINNING_PARAM);
        CamLog.m7i(CameraConstants.TAG, "mode str : " + mode + " /  isRecording : " + isRecording + " /  KEY_BINNING_PARAM : " + binningParam + " /  KEY_APP_BINNING_TYPE : " + binningType);
        if (!ParamConstants.VALUE_BINNING_MODE.equals(binningParam)) {
            try {
                operationShotMode = ((Integer) sOpModeMap.get(mode)).intValue();
                if (operationShotMode == 57344 && isRecording) {
                    operationShotMode = OP_SHOT_MODE_NORMAL_VIDEO;
                }
                return operationShotMode;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return operationShotMode;
            } catch (Throwable th) {
                return operationShotMode;
            }
        } else if (!ParamConstants.VALUE_BINNING_SWPIXEL.equals(binningType)) {
            return isRecording ? OP_SHOT_MODE_SUM_BINNING_SENSOR_REC : 60160;
        } else {
            if (isRecording) {
                return OP_SHOT_MODE_SUM_BINNING_REC;
            }
            return 59904;
        }
    }

    public static int getOperationMode(Parameters2 parameters, boolean isRecording, boolean isHighSpeedCaptureSession, boolean isReprocess, int cameraId) {
        if (!Utils.checkOOS() || isReprocess) {
            CamLog.m7i(CameraConstants.TAG, "operationMode : OP_SHOT_MODE_INVALID");
            return 0;
        } else if (parameters == null) {
            return 57344;
        } else {
            int operationMode = getOperationShotMode(parameters, isRecording);
            if ("on".equals(parameters.get("hdr10"))) {
                CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_HDR10");
                operationMode |= OP_SETTING_HDR10;
            }
            if (isRecording) {
                int[] fps = new int[2];
                parameters.getPreviewFpsRange(fps);
                if (!isHighSpeedCaptureSession) {
                    if (fps[1] >= 60) {
                        CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_60_FPS_RECORDING");
                        operationMode |= OP_SETTING_60_FPS_RECORDING;
                    }
                    if ("on".equals(parameters.get(ParamConstants.KEY_STEADY_CAM))) {
                        if (cameraId == 1) {
                            CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_EIS_REAL_TIME");
                            operationMode |= OP_SETTING_EIS_REAL_TIME;
                        } else {
                            CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_EIS_LOCK_AHEAD");
                            operationMode |= OP_SETTING_EIS_LOCK_AHEAD;
                        }
                    }
                    if ("on".equals(parameters.get(ParamConstants.KEY_VIDEO_HDR_MODE))) {
                        CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_VIDEO_HDR");
                        operationMode |= OP_SETTING_VIDEO_HDR;
                    }
                } else if (fps[1] >= 240) {
                    CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_240_FPS_RECORDING");
                    operationMode |= OP_SETTING_240_FPS_RECORDING;
                } else {
                    CamLog.m7i(CameraConstants.TAG, "operationMode | OP_SETTING_120_FPS_RECORDING");
                    operationMode |= OP_SETTING_120_FPS_RECORDING;
                }
                CamLog.m7i(CameraConstants.TAG, "operationMode : " + operationMode + " hex : " + Integer.toHexString(operationMode).toUpperCase() + " / binary : " + Integer.toBinaryString(operationMode));
                return operationMode;
            }
            String hdrMode = parameters.get("hdr-mode");
            if (cameraId != 1 && ("1".equals(hdrMode) || "2".equals(hdrMode))) {
                CamLog.m7i(CameraConstants.TAG, "operationMode : OP_SETTING_HDR_PREVIEW");
                operationMode |= OP_SETTING_HDR_PREVIEW;
            }
            CamLog.m7i(CameraConstants.TAG, "operationMode : " + operationMode + " hex : " + Integer.toHexString(operationMode).toUpperCase() + " / binary : " + Integer.toBinaryString(operationMode));
            return operationMode;
        }
    }
}
