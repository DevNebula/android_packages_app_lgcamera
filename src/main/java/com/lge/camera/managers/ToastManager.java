package com.lge.camera.managers;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.lge.camera.components.OnScreenHint;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class ToastManager extends ManagerInterfaceImpl {
    public static final int TOAST_NORMAL_CENTER_LOCATION = 1;
    public static final int TOAST_NORMAL_LOCATION = 0;
    private static int sToastLocation = 0;
    private int mDegree = 0;
    private final Handler mHandler = new Handler();
    private final Runnable mHide = new C11806();
    private boolean mIsAllowDisturb = true;
    private final Runnable mResetAllowDisturb = new C11751();
    private OnScreenHint mStorageToast = null;
    private final Runnable mStorageToastHide = new C11817();
    private OnScreenHint mToast = null;

    /* renamed from: com.lge.camera.managers.ToastManager$1 */
    class C11751 implements Runnable {
        C11751() {
        }

        public void run() {
            ToastManager.this.mIsAllowDisturb = true;
        }
    }

    /* renamed from: com.lge.camera.managers.ToastManager$4 */
    class C11784 implements Runnable {
        C11784() {
        }

        public void run() {
            if (ToastManager.this.mStorageToast != null) {
                ToastManager.this.mStorageToast.showImmediately();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ToastManager$5 */
    class C11795 implements Runnable {
        C11795() {
        }

        public void run() {
            if (ToastManager.this.mStorageToast != null) {
                ToastManager.this.mStorageToast.cancelImmediately();
                ToastManager.this.mStorageToast = null;
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ToastManager$6 */
    class C11806 implements Runnable {
        C11806() {
        }

        public void run() {
            ToastManager.this.hide(ToastManager.this.mToast, false);
            ToastManager.this.mToast = null;
        }
    }

    /* renamed from: com.lge.camera.managers.ToastManager$7 */
    class C11817 implements Runnable {
        C11817() {
        }

        public void run() {
            ToastManager.this.storageToasthide(false);
        }
    }

    public ToastManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    private void show(String message) {
        show(message, false);
    }

    private void show(String message, boolean immediately) {
        show(message, immediately, getOrientationDegree());
    }

    private synchronized void show(String message, boolean immediately, int degree) {
        boolean z = true;
        synchronized (this) {
            if (isShowing() && this.mIsAllowDisturb) {
                this.mHandler.removeCallbacks(this.mHide);
                hide();
            }
            this.mToast = OnScreenHint.makeText(this.mGet.getActivity(), message, degree);
            if (1 != sToastLocation) {
                z = false;
            }
            OnScreenHint.setToastLocation(z);
            if (this.mToast != null) {
                if (immediately) {
                    this.mToast.showImmediately();
                } else {
                    this.mToast.show();
                }
                this.mDegree = degree;
                doTalkBack();
            }
            this.mGet.deleteImmediatelyNotUndo();
        }
    }

    private void doTalkBack() {
        AccessibilityManager am = (AccessibilityManager) this.mGet.getAppContext().getSystemService("accessibility");
        if (am.isEnabled() && am.isTouchExplorationEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(16384);
            event.getText().add(this.mToast.getText());
            am.sendAccessibilityEvent(event);
        }
    }

    public void hideAndResetDisturb(long hideDelayMillis) {
        this.mHandler.removeCallbacks(this.mHide);
        if (!this.mHandler.postDelayed(this.mHide, hideDelayMillis)) {
            hide(this.mToast, true);
        }
        this.mHandler.removeCallbacks(this.mResetAllowDisturb);
        if (!this.mHandler.postDelayed(this.mResetAllowDisturb, hideDelayMillis)) {
            this.mIsAllowDisturb = true;
        }
    }

    private synchronized boolean checkDisturb(boolean needDisturb, long hideDelayMillis) {
        boolean needStopShow;
        needStopShow = false;
        if (this.mIsAllowDisturb) {
            if (!needDisturb) {
                this.mIsAllowDisturb = false;
                this.mHandler.removeCallbacks(this.mHide);
                hide(this.mToast, false);
                hideAndResetDisturb(hideDelayMillis);
            }
        } else if (needDisturb) {
            this.mHandler.removeCallbacks(this.mResetAllowDisturb);
            this.mIsAllowDisturb = true;
        } else {
            hideAndResetDisturb(hideDelayMillis);
            needStopShow = true;
        }
        return needStopShow;
    }

    public void showShortToast(String message) {
        sToastLocation = 0;
        showShortToast(message, true);
    }

    public void showShortToast(String message, boolean needDisturb) {
        sToastLocation = 0;
        showShortToast(message, needDisturb, sToastLocation);
    }

    public void showShortToast(String message, boolean needDisturb, int location) {
        showToast(message, CameraConstants.TOAST_LENGTH_SHORT, needDisturb);
    }

    public void showShortToast(String message, int location) {
        sToastLocation = location;
        showShortToast(message, true, location);
    }

    public void showLongToast(String message) {
        showLongToast(message, true);
    }

    public void showLongToast(String message, boolean needDisturb) {
        showToast(message, CameraConstants.TOAST_LENGTH_LONG, needDisturb);
    }

    public void showToast(String message, long hideDelayMillis) {
        showToast(message, hideDelayMillis, true);
    }

    public void showToast(String message, long hideDelayMillis, boolean needDisturb) {
        final boolean z = needDisturb;
        final long j = hideDelayMillis;
        final String str = message;
        this.mGet.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (!ToastManager.this.checkDisturb(z, j)) {
                    ToastManager.this.show(str);
                    if (ToastManager.this.mIsAllowDisturb && !ToastManager.this.mHandler.postDelayed(ToastManager.this.mHide, j)) {
                        ToastManager.this.hide(ToastManager.this.mToast, true);
                        ToastManager.this.mToast = null;
                    }
                }
            }
        });
    }

    public static void showToastForSecure(final Activity activity, Handler handler, final String message, int delay) {
        handler.postDelayed(new Runnable() {
            public void run() {
                ToastManager.sToastLocation = 0;
                OnScreenHint.setToastLocation(false);
                OnScreenHint.makeText(activity, message).showImmediately();
            }
        }, (long) delay);
    }

    private void hide() {
        hide(this.mToast, false);
        this.mToast = null;
    }

    public synchronized void hide(OnScreenHint toast, boolean immediately) {
        if (toast != null) {
            if (immediately) {
                toast.cancelImmediately();
            } else {
                toast.cancel();
            }
        }
    }

    public synchronized void hideAllToast() {
        hide(this.mToast, true);
        hide(this.mStorageToast, true);
        this.mToast = null;
        this.mStorageToast = null;
    }

    public synchronized void storageToastShow(String message, boolean immediately, boolean shortToast) {
        if (isStorageToastShowing()) {
            this.mHandler.removeCallbacks(this.mStorageToastHide);
            storageToasthide(false);
        }
        sToastLocation = 0;
        OnScreenHint.setToastLocation(false);
        int degree = getOrientationDegree();
        this.mStorageToast = OnScreenHint.makeText(this.mGet.getActivity(), message, degree, 1);
        if (this.mStorageToast != null) {
            if (immediately) {
                this.mGet.getActivity().runOnUiThread(new C11784());
            } else {
                this.mStorageToast.show();
            }
            this.mDegree = degree;
        }
        if (shortToast && !this.mHandler.postDelayed(this.mStorageToastHide, CameraConstants.TOAST_LENGTH_SHORT)) {
            storageToasthide(true);
        }
    }

    public synchronized void storageToasthide(boolean immediately) {
        if (this.mStorageToast != null) {
            if (immediately) {
                this.mGet.getActivity().runOnUiThread(new C11795());
            } else {
                this.mStorageToast.cancel();
                this.mStorageToast = null;
            }
        }
    }

    public void onConfigurationChanged(Configuration config) {
        int degree = getOrientationDegree();
        if (this.mDegree != degree) {
            afterConfigChanged(degree);
        }
        super.onConfigurationChanged(config);
    }

    public void beforeConfigChanged() {
        if (this.mToast != null) {
            this.mToast.cancel();
        }
        if (this.mStorageToast != null) {
            this.mStorageToast.cancel();
        }
    }

    public void afterConfigChanged(int degree) {
        if (this.mToast != null) {
            this.mToast = OnScreenHint.changeOrientation(this.mGet.getActivity(), degree);
        }
        if (this.mStorageToast != null) {
            this.mStorageToast = OnScreenHint.changeOrientation(this.mGet.getActivity(), degree, 1);
        }
        this.mDegree = degree;
    }

    public boolean isShowing() {
        return this.mToast != null;
    }

    public boolean isStorageToastShowing() {
        return this.mStorageToast != null;
    }

    public void onPauseBefore() {
        CamLog.m3d(CameraConstants.TAG, "onPause");
        if (this.mToast != null) {
            this.mHandler.removeCallbacks(this.mHide);
            this.mToast.cancel();
            this.mToast = null;
        }
        if (this.mStorageToast != null) {
            this.mHandler.removeCallbacks(this.mStorageToastHide);
            this.mStorageToast.cancel();
            this.mStorageToast = null;
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (degree != this.mDegree) {
            beforeConfigChanged();
            afterConfigChanged(degree);
        }
    }

    public void resetToastLocation() {
        sToastLocation = 0;
    }

    public void showToastForcely(String msg) {
        show(msg, true);
    }

    public void hideToastForcely() {
        this.mHandler.removeCallbacks(this.mHide);
        this.mHandler.postDelayed(this.mHide, CameraConstants.TOAST_LENGTH_SHORT);
    }
}
