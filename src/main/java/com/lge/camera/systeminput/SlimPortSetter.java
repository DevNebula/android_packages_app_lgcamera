package com.lge.camera.systeminput;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.systemservice.core.OsManager;

public class SlimPortSetter {
    private static final int ORIENTATION_UPDTAE_SLIMPORT = 201;
    public static SlimPortSetter sSetter = null;
    private OrientationHandler mOrientationHandler = null;

    private class OrientationHandler extends Handler {
        public OrientationHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 201:
                        SlimPortSetter.this.setSlimPortDegree((OsManager) msg.obj, msg.arg1);
                        return;
                    default:
                        return;
                }
            } catch (Exception e) {
                CamLog.m6e(CameraConstants.TAG, "OrientationHandler exception : ", e);
            }
        }
    }

    public static SlimPortSetter get() {
        if (sSetter == null) {
            sSetter = new SlimPortSetter();
        }
        return sSetter;
    }

    public SlimPortSetter() {
        HandlerThread ht = new HandlerThread("Orientation Handler");
        ht.start();
        this.mOrientationHandler = new OrientationHandler(ht.getLooper());
    }

    public void unbind() {
    }

    public void setSlimPortProperty(OsManager osManager, int degree) {
        if (this.mOrientationHandler != null && osManager != null) {
            this.mOrientationHandler.obtainMessage(201, degree, 0, osManager).sendToTarget();
        }
    }

    private void setSlimPortDegree(OsManager osManager, int degree) {
        if (osManager != null) {
            String strOrientation = String.valueOf(degree);
            try {
                osManager.setSystemProperty("sys.camera_orientation", strOrientation);
            } catch (SecurityException e) {
                CamLog.m11w(CameraConstants.TAG, "setSlimPortDegree : SecurityException.");
            }
            CamLog.m3d(CameraConstants.TAG, "setSlimPortDegree = " + strOrientation);
        }
    }
}
