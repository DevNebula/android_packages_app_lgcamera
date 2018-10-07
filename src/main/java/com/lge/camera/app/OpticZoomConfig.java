package com.lge.camera.app;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.util.CamLog;
import java.util.HashMap;

public class OpticZoomConfig {
    public static HashMap<String, Boolean> sMap = new HashMap();

    static {
        sMap.put("com.lge.camera.app.DefaultCameraModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.RecordingPriorityModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.AttachCameraModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.PanoramaCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.BeautyShotCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.AKACoverCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.QuickCircleCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.DznyCoverCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.GestureShotCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ManualDefaultCameraModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.SnapMovieSingleCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.SnapMovieFrontCameraModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.FlashJumpCutFrontModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.FlashJumpCutRearModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.SlowMotionModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.TimeLapseVideoModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ManualDefaultVideoModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.PopoutFrameModuleExpand", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.PopoutShapeModule", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.PanoramaModuleLGNormal", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.PanoramaModuleLGRaw", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.PanoramaModuleLG360Proj", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.FoodModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.SquareSnapCameraModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.SquareGridCameraModuleExpand", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.SquareOverlapCameraModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.SquareSpliceCameraModuleExpand", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.DualPopCameraModuleExpand", Boolean.valueOf(false));
        sMap.put("com.lge.camera.app.ext.SmartCamModule", Boolean.valueOf(true));
        sMap.put("com.lge.camera.app.ext.SmartCamFrontModule", Boolean.valueOf(false));
    }

    public static boolean setOpticZoomSupported(String module, boolean value) {
        if (!FunctionProperties.isSupportedOpticZoom() || module == null) {
            return false;
        }
        if (sMap.get(module) != null) {
            return ((Boolean) sMap.put(module, Boolean.valueOf(value))).booleanValue();
        }
        CamLog.m11w(CameraConstants.TAG, "OpticZoom not specified for this module; set failed");
        return false;
    }

    public static boolean isOpticZoomSupported(String module) {
        if (!FunctionProperties.isSupportedOpticZoom() || module == null) {
            return false;
        }
        Boolean isSupported = (Boolean) sMap.get(module);
        if (isSupported != null) {
            return isSupported.booleanValue();
        }
        CamLog.m11w(CameraConstants.TAG, "OpticZoom not specified for this module; set to false");
        return false;
    }
}
