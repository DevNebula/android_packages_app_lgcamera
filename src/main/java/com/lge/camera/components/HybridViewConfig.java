package com.lge.camera.components;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.HashMap;

public class HybridViewConfig {
    public static final String FRONT_VIEW = "front-view";
    public static final String REAR_VIEW = "rear-view";
    public static final String SURFACE = "surface";
    public static final String TEXTURE = "texture";
    public static HashMap<String, String> sMap = new HashMap();

    public static void makeHybridViewConfig() {
        sMap.put("com.lge.camera.app.DefaultCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.AttachCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.ext.PanoramaCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.BeautyShotCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.AKACoverCameraModule", TEXTURE);
        sMap.put("com.lge.camera.app.QuickCircleCameraModule", TEXTURE);
        sMap.put("com.lge.camera.app.DznyCoverCameraModule", TEXTURE);
        sMap.put("com.lge.camera.app.GestureShotCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.ManualDefaultCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.ext.SnapMovieSingleCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.ext.SnapMovieFrontCameraModule", SURFACE);
        sMap.put("com.lge.camera.app.ext.SlowMotionModule", SURFACE);
        sMap.put("com.lge.camera.app.ext.TimeLapseVideoModule", SURFACE);
        sMap.put("com.lge.camera.app.ManualDefaultVideoModule", SURFACE);
        sMap.put("com.lge.camera.app.ext.PanoramaModuleLGNormal", SURFACE);
        sMap.put("com.lge.camera.app.ext.PanoramaModuleLGRaw", SURFACE);
        sMap.put("com.lge.camera.app.ext.PanoramaModuleLG360Proj", SURFACE);
        sMap.put("com.lge.camera.app.ext.DualPopCameraModule", SURFACE);
    }

    public static String getCurrentView(String module) {
        CamLog.m3d(CameraConstants.TAG, "module = " + module);
        String view = (String) sMap.get(module);
        if (view != null) {
            return view;
        }
        CamLog.m11w(CameraConstants.TAG, "preview type is not specified for this module; set to SurfaceView");
        return SURFACE;
    }
}
