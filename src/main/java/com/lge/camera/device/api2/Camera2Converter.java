package com.lge.camera.device.api2;

import android.util.SparseArray;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUtils;
import java.util.HashMap;

public class Camera2Converter {
    private static SparseArray<String> sEVStateMap = new SparseArray();
    private static SparseArray<String> sFocueStateMap = new SparseArray();
    private static SparseArray<String> sFocusModeArr = new SparseArray();
    private static HashMap<String, Integer> sFocusModeMap = new HashMap();
    private static HashMap<String, Integer> sShootModeMap = new HashMap();

    static {
        sFocusModeMap.put(ParamConstants.FOCUS_MODE_FIXED, Integer.valueOf(0));
        sFocusModeMap.put("auto", Integer.valueOf(1));
        sFocusModeMap.put(ParamConstants.FOCUS_MODE_MACRO, Integer.valueOf(2));
        sFocusModeMap.put(ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO, Integer.valueOf(3));
        sFocusModeMap.put(ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE, Integer.valueOf(4));
        sFocusModeMap.put(ParamConstants.FOCUS_MODE_EDOF, Integer.valueOf(5));
        sFocusModeMap.put(ParamConstants.FOCUS_MODE_MULTIWINDOWAF, Integer.valueOf(4));
        sFocusModeArr.put(0, ParamConstants.FOCUS_MODE_FIXED);
        sFocusModeArr.put(1, "auto");
        sFocusModeArr.put(2, ParamConstants.FOCUS_MODE_MACRO);
        sFocusModeArr.put(3, ParamConstants.FOCUS_MODE_CONTINUOUS_VIDEO);
        sFocusModeArr.put(4, ParamConstants.FOCUS_MODE_CONTINUOUS_PICTURE);
        sFocusModeArr.put(5, ParamConstants.FOCUS_MODE_EDOF);
        sFocueStateMap.put(0, "INACTIVE");
        sFocueStateMap.put(1, "PASSIVE_SCAN");
        sFocueStateMap.put(2, "PASSIVE_FOCUSED");
        sFocueStateMap.put(3, "ACTIVE_SCAN");
        sFocueStateMap.put(4, "FOCUSED_LOCKED");
        sFocueStateMap.put(5, "NOT_FOCUSED_LOCKED");
        sFocueStateMap.put(6, "PASSIVE_UNFOCUSED");
        sEVStateMap.put(0, "INACTIVE");
        sEVStateMap.put(1, "SEARCHING");
        sEVStateMap.put(2, "CONVERGED");
        sEVStateMap.put(3, "LOCKED");
        sEVStateMap.put(4, "FLASH_REQUIRED");
        sEVStateMap.put(5, "PRECAPTURE");
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_BURST), Integer.valueOf(2));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_BEAUTY), Integer.valueOf(4));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_MULTIVIEW), Integer.valueOf(8));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SQUARE_SPLICE), Integer.valueOf(8));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_MANUAL_CAMERA), Integer.valueOf(16));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_MANUAL_VIDEO), Integer.valueOf(16));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_REAR_OUTFOCUS), Integer.valueOf(32));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_FRONT_OUTFOCUS), Integer.valueOf(32));
        sShootModeMap.put(ParamUtils.convertShotMode("mode_food"), Integer.valueOf(64));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SLOW_MOTION), Integer.valueOf(256));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_TIME_LAPSE_VIDEO), Integer.valueOf(128));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_CINEMA), Integer.valueOf(512));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SNAP), Integer.valueOf(1024));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SNAP_SINGLE), Integer.valueOf(1024));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_SNAP_FRONT), Integer.valueOf(1024));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA), Integer.valueOf(2048));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA_LG), Integer.valueOf(2048));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA_LG_RAW), Integer.valueOf(2048));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_PANORAMA_LG_360_PROJ), Integer.valueOf(2048));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_POPOUT_CAMERA), Integer.valueOf(4096));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_POPOUT_CAMERA), Integer.valueOf(8192));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_FLASH_JUMPCUT), Integer.valueOf(16384));
        sShootModeMap.put(ParamUtils.convertShotMode(CameraConstants.MODE_FLASH_JUMPCUT_FRONT), Integer.valueOf(16384));
    }

    public static int getFocusMode(String mode) {
        Integer ret = (Integer) sFocusModeMap.get(mode);
        return ret == null ? 0 : ret.intValue();
    }

    public static String getFocusModeStr(int mode) {
        String ret = (String) sFocusModeArr.get(mode);
        return ret == null ? ParamConstants.FOCUS_MODE_FIXED : ret;
    }

    public static String getStringFocusState(int state) {
        String ret = (String) sFocueStateMap.get(state);
        return ret == null ? "UNKNOWN" : ret;
    }

    public static String getStringExposureState(int state) {
        String ret = (String) sEVStateMap.get(state);
        return ret == null ? "UNKNOWN" : ret;
    }

    public static int getShootMode(String mode) {
        Integer ret = (Integer) sShootModeMap.get(ParamUtils.convertShotMode(mode));
        return ret == null ? 1 : ret.intValue();
    }
}
