package com.lge.camera.util;

import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.lge.camera.constants.CameraConstants;

public class MiniViewUtil {
    private static IWindowManager sWindowManager;

    private static boolean getWindowManager() {
        try {
            sWindowManager = Stub.asInterface(ServiceManager.getService("window"));
        } catch (Exception e) {
            sWindowManager = null;
            CamLog.m3d(CameraConstants.TAG, "MiniViewUtil fail " + e.getMessage());
        }
        return sWindowManager != null;
    }

    public static boolean isMiniViewState() {
        boolean z = true;
        if ((sWindowManager == null && !getWindowManager()) || sWindowManager == null) {
            return false;
        }
        try {
            if (sWindowManager.getMiniViewState() != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Rect getMiniViewRect() {
        if (sWindowManager == null && !getWindowManager()) {
            return null;
        }
        try {
            if (sWindowManager.getMiniViewState() == 1) {
                return sWindowManager.getMiniViewPosition();
            }
            return null;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void unbind() {
        sWindowManager = null;
    }
}
