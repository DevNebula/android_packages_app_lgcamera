package com.lge.camera.device;

import android.content.Context;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.ICameraCallback.CameraOpenErrorCallback;
import com.lge.camera.util.CamLog;

public class CameraSecondHolder extends SubCameraApis {
    private static CameraSecondHolder sSecondHolder;

    public static synchronized CameraSecondHolder subinstance() {
        CameraSecondHolder cameraSecondHolder;
        synchronized (CameraSecondHolder.class) {
            if (sSecondHolder == null) {
                sSecondHolder = new CameraSecondHolder();
            }
            cameraSecondHolder = sSecondHolder;
        }
        return cameraSecondHolder;
    }

    public static boolean isSecondCameraOpened() {
        if (sSecondHolder == null || !sSecondHolder.isCameraOpened()) {
            return false;
        }
        return true;
    }

    public CameraProxy getCameraDevice() {
        return this.mCameraDevice;
    }

    public synchronized CameraProxy open(Handler handler, int cameraId, CameraOpenErrorCallback cb, boolean skipNextReconnection, Context context) {
        this.mCameraManager = CameraManagerFactory.getAndroidSubCameraManager(context);
        CamLog.m7i(CameraConstants.TAG, " Seceond Camera " + cameraId);
        super.open(handler, cameraId, cb, skipNextReconnection, context);
        CamLog.m7i(CameraConstants.TAG, " Seceond Camera open done");
        return null;
    }

    public synchronized void release() {
        CamLog.m7i(CameraConstants.TAG, " Seceond Camera release");
        releaseCommon();
        CameraManagerFactory.releaseAndroidSubCameraManager();
        sSecondHolder = null;
        CamLog.m7i(CameraConstants.TAG, " Seceond Camera release Done");
    }
}
