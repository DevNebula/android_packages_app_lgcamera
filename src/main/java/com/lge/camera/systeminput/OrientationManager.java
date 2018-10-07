package com.lge.camera.systeminput;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.Utils;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.OsManager;

public class OrientationManager {
    private static final int ORIENTATION_HYSTERESIS = 5;
    private static final int ORIENTATION_UNKNOWN_LAST = -2;
    private int mDegree = 0;
    private Thread mEnableThread = null;
    private boolean mIsEnable = false;
    private int mLastOrientation = -1;
    private int mLastOrientationBackup = -2;
    private OrientationChangedListener mListener = null;
    private int mOrientationChangedCnt = 0;
    private MyOrientationEventListener mOrientationListener = null;
    private boolean mOrientationLocked = true;
    private OsManager mOsManager = null;

    public interface OrientationChangedListener {
        Activity getActivity();

        Context getAppContext();

        boolean isActivityPaused();

        void onOrientationChanged(int i, boolean z);
    }

    /* renamed from: com.lge.camera.systeminput.OrientationManager$1 */
    class C14251 implements Runnable {
        C14251() {
        }

        public void run() {
            if (OrientationManager.this.mListener.isActivityPaused()) {
                CamLog.m3d(CameraConstants.TAG, "orientationManager app paused!");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "orientationManager enable Start");
            if (OrientationManager.this.mOrientationListener != null) {
                OrientationManager.this.mOrientationListener.enable();
            }
            CamLog.m3d(CameraConstants.TAG, "orientationManager enable End");
            OrientationManager.this.mIsEnable = true;
        }
    }

    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        public void onOrientationChanged(int degree) {
            boolean z = true;
            if (degree != -1) {
                int roundDegree = OrientationManager.roundOrientation(degree, 0);
                OrientationManager.this.mLastOrientation = roundDegree % 360;
                if (OrientationManager.this.checkDegreePadding(roundDegree, degree) && OrientationManager.this.mListener != null) {
                    if (!(OrientationManager.this.mLastOrientationBackup == OrientationManager.this.mLastOrientation || SecureImageUtil.isScreenLocked())) {
                        SlimPortSetter.get().setSlimPortProperty(OrientationManager.this.mOsManager, OrientationManager.this.mLastOrientation);
                    }
                    if (OrientationManager.this.mOrientationLocked) {
                        OrientationManager.this.mDegree = OrientationManager.this.convertDegreeForWindowOrientation(roundDegree);
                        if (OrientationManager.this.mLastOrientationBackup != OrientationManager.this.mLastOrientation) {
                            OrientationChangedListener access$000 = OrientationManager.this.mListener;
                            int access$900 = OrientationManager.this.mDegree;
                            if (OrientationManager.this.mOrientationChangedCnt != 0) {
                                z = false;
                            }
                            access$000.onOrientationChanged(access$900, z);
                            if (OrientationManager.this.mOrientationChangedCnt == 0) {
                                OrientationManager.this.mOrientationChangedCnt = OrientationManager.this.mOrientationChangedCnt + 1;
                            }
                        }
                    } else {
                        OrientationManager.this.mDegree = 0;
                        OrientationManager.this.mOrientationChangedCnt = 0;
                        if (OrientationManager.this.mLastOrientationBackup != OrientationManager.this.mLastOrientation) {
                            OrientationManager.this.mListener.onOrientationChanged(OrientationManager.this.mDegree, true);
                        }
                    }
                    OrientationManager.this.mLastOrientationBackup = OrientationManager.this.mLastOrientation;
                }
            }
        }
    }

    public OrientationManager(OrientationChangedListener listener) {
        this.mListener = listener;
        this.mLastOrientationBackup = -2;
        this.mOrientationListener = new MyOrientationEventListener(listener.getAppContext());
        this.mOsManager = (OsManager) new LGContext(listener.getAppContext()).getLGSystemService("osservice");
    }

    public void resume(Activity activity) {
        if (!this.mIsEnable) {
            if (this.mEnableThread == null || !this.mEnableThread.isAlive()) {
                this.mEnableThread = new Thread(new C14251());
                this.mLastOrientationBackup = -2;
                this.mEnableThread.start();
            } else {
                CamLog.m3d(CameraConstants.TAG, "orientationManager enableThread alive. return!");
                return;
            }
        }
        if (activity.getRequestedOrientation() != 1) {
            this.mOrientationLocked = false;
        }
    }

    public void pause() {
        if (this.mEnableThread != null && this.mEnableThread.isAlive()) {
            CamLog.m3d(CameraConstants.TAG, "orientationManager enableThread alive. join!");
            try {
                this.mEnableThread.join(CameraConstants.TOAST_LENGTH_LONG);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.mEnableThread = null;
        if (this.mOrientationListener != null) {
            this.mOrientationListener.disable();
            CamLog.m3d(CameraConstants.TAG, "orientationManager disable");
        }
        this.mIsEnable = false;
        this.mOrientationChangedCnt = 0;
        this.mLastOrientationBackup = -2;
        SlimPortSetter.get().setSlimPortProperty(this.mOsManager, 0);
    }

    public void unbind() {
        SlimPortSetter.get().unbind();
        this.mListener = null;
        this.mOrientationListener = null;
        this.mOsManager = null;
    }

    public void lockOrientationPortrait(Activity activity) {
        if (!this.mOrientationLocked) {
            this.mOrientationLocked = true;
            activity.setRequestedOrientation(1);
            this.mDegree = convertDegreeForWindowOrientation(this.mLastOrientation);
            CamLog.m3d(CameraConstants.TAG, "mLastOrientation = " + this.mLastOrientation + ", mDegree = " + this.mDegree);
        }
    }

    public void unlockOrientation(Activity activity) {
        if (this.mOrientationLocked) {
            this.mOrientationLocked = false;
            CamLog.m3d(CameraConstants.TAG, "unlock orientation");
            activity.setRequestedOrientation(10);
        }
    }

    public boolean isOrientationLocked() {
        return this.mOrientationLocked;
    }

    public int getOrientationManagerDegree() {
        return this.mDegree;
    }

    private boolean checkDegreePadding(int roundDegree, int inputDegree) {
        if (roundDegree == 0) {
            if (inputDegree <= 20 && inputDegree >= 0) {
                return true;
            }
            if (inputDegree >= 340 && inputDegree <= 360) {
                return true;
            }
        } else if (inputDegree <= (roundDegree + 20) % 360 && inputDegree >= (roundDegree - 20) % 360) {
            return true;
        }
        return false;
    }

    private int convertDegreeForWindowOrientation(int inputDegree) {
        if (this.mListener == null || inputDegree == -1) {
            return 0;
        }
        int convertDegree = Utils.convertDegree(this.mListener.getAppContext().getResources(), inputDegree);
        if (Utils.isLandscapeOrientaionModel(this.mListener.getActivity())) {
            return (convertDegree + 270) % 360;
        }
        return convertDegree;
    }

    private static int roundOrientation(int degree, int orientationHistory) {
        boolean changeOrientation;
        if (orientationHistory == -1) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(degree - orientationHistory);
            changeOrientation = Math.min(dist, 360 - dist) >= 50;
        }
        if (changeOrientation) {
            return (((degree + 45) / 90) * 90) % 360;
        }
        return orientationHistory;
    }

    public void onConfigurationChanged(Configuration config) {
        this.mDegree = convertDegreeForWindowOrientation(this.mLastOrientation);
        CamLog.m3d(CameraConstants.TAG, "onConfigurationChanged - mDegree = " + this.mDegree);
    }
}
