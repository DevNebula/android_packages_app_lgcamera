package com.lge.camera.device;

import android.content.Context;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.api1.AndroidCameraManagerImpl;
import com.lge.camera.device.api2.AndroidCameraManagerImpl2;

public class CameraManagerFactory {
    private static AndroidCameraManagerImpl sAndroidCameraManager;
    private static AndroidCameraManagerImpl2 sAndroidCameraManager2;
    private static AndroidCameraManagerImpl sAndroidSubCameraManager;
    private static AndroidCameraManagerImpl2 sAndroidSubCameraManager2;

    public static synchronized CameraManager getAndroidCameraManager(Context context) {
        CameraManager cameraManager;
        synchronized (CameraManagerFactory.class) {
            if (FunctionProperties.getSupportedHal() != 2 || context == null) {
                if (sAndroidCameraManager == null) {
                    sAndroidCameraManager = new AndroidCameraManagerImpl();
                }
                cameraManager = sAndroidCameraManager;
            } else {
                if (sAndroidCameraManager2 == null) {
                    sAndroidCameraManager2 = new AndroidCameraManagerImpl2(context);
                }
                cameraManager = sAndroidCameraManager2;
            }
        }
        return cameraManager;
    }

    public static synchronized CameraManager getAndroidSubCameraManager(Context context) {
        CameraManager cameraManager;
        synchronized (CameraManagerFactory.class) {
            if (FunctionProperties.getSupportedHal() != 2 || context == null) {
                if (sAndroidSubCameraManager == null) {
                    sAndroidSubCameraManager = new AndroidCameraManagerImpl();
                }
                cameraManager = sAndroidSubCameraManager;
            } else {
                if (sAndroidSubCameraManager2 == null) {
                    sAndroidSubCameraManager2 = new AndroidCameraManagerImpl2(context);
                }
                cameraManager = sAndroidSubCameraManager2;
            }
        }
        return cameraManager;
    }

    public static synchronized void releaseAndroidCameraManager() {
        synchronized (CameraManagerFactory.class) {
            sAndroidCameraManager = null;
            sAndroidCameraManager2 = null;
        }
    }

    public static synchronized void releaseAndroidSubCameraManager() {
        synchronized (CameraManagerFactory.class) {
            sAndroidSubCameraManager = null;
            sAndroidSubCameraManager2 = null;
        }
    }
}
