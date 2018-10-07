package com.lge.camera.managers;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;

public class ArcOutfocusManager extends OutfocusManager {
    public ArcOutfocusManager(ModuleInterface moduleInterface, int defaultBlurLevel) {
        super(moduleInterface, defaultBlurLevel);
    }

    public void setBlurLevel(int level) {
        CameraProxy device = this.mGet.getCameraDevice();
        if (device != null) {
            CameraParameters params = device.getParameters();
            if (params != null) {
                CamLog.m3d(CameraConstants.TAG, "[outfocus] blur level : " + level);
                super.setBlurLevel(level);
                params.set(ParamConstants.KEY_OUTFOCUS_LEVEL, "" + getBlurLevelForParam());
                this.mGet.setParamUpdater(params, ParamConstants.KEY_OUTFOCUS_LEVEL, "" + getBlurLevelForParam());
                device.setParameters(params);
            }
        }
    }
}
