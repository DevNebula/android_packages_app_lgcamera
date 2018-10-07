package com.lge.camera.managers;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.util.CamLog;

public class FocusFrontAE extends FocusTouch {
    public FocusFrontAE(ModuleInterface iModule) {
        super(iModule);
    }

    public void initFocusAreas() {
        CamLog.m3d(CameraConstants.TAG, "InitFocusAreas");
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameters = cameraDevice.getParameters();
            if (parameters != null) {
                parameters.setMeteringAreas(null);
                if (this.mGet.isRearCamera()) {
                    CameraDeviceUtils.setEnable3ALocks(cameraDevice, false, false);
                }
                try {
                    this.mGet.setParameters(parameters);
                } catch (RuntimeException e) {
                    CamLog.m8i(CameraConstants.TAG, "RuntimeException:", e);
                }
            }
        }
    }
}
