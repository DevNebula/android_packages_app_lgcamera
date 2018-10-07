package com.lge.camera.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.lge.camera.constants.CameraConstants;
import com.lge.ellievision.IEllieVision;
import com.lge.ellievision.IEllieVision.Stub;

public class SmartcamUtil {
    private static final String CLASS_NAME = "com.lge.ellievision.EllieVisionService";
    private static final int DETECTION_OPENCV = 20;
    private static final String EXTRA_DETECTION = "Detection";
    private static final String EXTRA_RECOGNITION = "Recognition";
    private static final String PACKAGE_NAME = "com.lge.ellievision";
    private static final int RECOGNITION_EYEEM = 30;
    public static final String TAG_SERVICE_ACTION = "com.lge.gallery.contentservice.UPDATE_TAG";
    public static final String TAG_SERVICE_CLASS_NAME = "com.lge.gallery.tagservice.service.BackgroundTagService";
    public static final String TAG_SERVICE_PACKAGE_NAME = "com.lge.gallery.contentservice";
    private static IEllieVision mIEllieVision = null;
    public static ServiceConnection mServiceConnection = new C14321();

    /* renamed from: com.lge.camera.util.SmartcamUtil$1 */
    static class C14321 implements ServiceConnection {
        C14321() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            CamLog.m3d(CameraConstants.TAG, "SmartCam onServiceConnected");
            SmartcamUtil.mIEllieVision = Stub.asInterface(service);
            CamLog.m3d(CameraConstants.TAG, "mIEllieVision : " + SmartcamUtil.mIEllieVision);
        }

        public void onServiceDisconnected(ComponentName name) {
            CamLog.m3d(CameraConstants.TAG, "SmartCam onServiceDisconnected");
            SmartcamUtil.mIEllieVision = null;
        }
    }

    public static void smartcamBindService(Context context) {
        if (!isSmartcamBindService()) {
            CamLog.m3d(CameraConstants.TAG, "smartcam BindService");
            ComponentName cn = new ComponentName(PACKAGE_NAME, CLASS_NAME);
            Intent startService = new Intent();
            startService.setComponent(cn);
            startService.putExtra(EXTRA_RECOGNITION, 30);
            startService.putExtra(EXTRA_DETECTION, 20);
            context.bindService(startService, mServiceConnection, 65);
        }
    }

    public static void smartcamUnBindService(Context context) {
        if (isSmartcamBindService()) {
            CamLog.m3d(CameraConstants.TAG, "smartcam unBindService");
            context.unbindService(mServiceConnection);
            mIEllieVision = null;
        }
    }

    public static IEllieVision getSmartcamEllieVisionService() {
        if (mIEllieVision == null) {
            return null;
        }
        return mIEllieVision;
    }

    public static boolean isSmartcamBindService() {
        return mIEllieVision != null;
    }
}
