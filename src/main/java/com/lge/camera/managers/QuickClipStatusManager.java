package com.lge.camera.managers;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;

public class QuickClipStatusManager extends ManagerInterfaceImpl {
    public static final int MSG_CIRCLE_VIEW_HIDE = 110;
    public static final int MSG_NOTIFY_URI = 111;
    public static final int NOTIFY_MSG_DELAY = 100;
    public static final int NOTIFY_MSG_DELAY_REC = 500;
    public static final int VIEW_CHANGE_TIMER = 3000;
    private static Status sStatus = Status.IDLE;
    private Status mDrawStatus = Status.IDLE;
    private Handler mHandler = new C11151();
    private boolean mIsSkipOnBurstShotUpdating = false;
    private boolean mIsUpdatedUri = false;
    private Status mPrevStatus = Status.IDLE;
    private OnSharedPreferenceChangeListener mSharedPreferenceListener = new C11162();
    private Uri mSkipOnBurstShotUri = null;
    private QuickClipStatusCallback mStatusCallback = null;

    public interface QuickClipStatusCallback {
        void onStatusChanged(Status status);

        void onThumbnailUpdated(Uri uri);
    }

    /* renamed from: com.lge.camera.managers.QuickClipStatusManager$1 */
    class C11151 extends Handler {
        C11151() {
        }

        public void handleMessage(Message msg) {
            QuickClipStatusManager.this.mHandler.removeMessages(msg.what);
            switch (msg.what) {
                case 110:
                    if (QuickClipStatusManager.sStatus == Status.CIRCLE_VIEW) {
                        QuickClipStatusManager.this.NotifyStatus(Status.NORMAL_VIEW);
                        return;
                    } else if (QuickClipStatusManager.sStatus == Status.INIT) {
                        QuickClipStatusManager.this.NotifyStatus(Status.NORMAL_VIEW);
                        QuickClipStatusManager.this.NotifyStatus(Status.INIT);
                        return;
                    } else {
                        return;
                    }
                case 111:
                    if (msg.obj instanceof Uri) {
                        Uri lastUri = msg.obj;
                        CamLog.m3d(CameraConstants.TAG, "HandleMessage : MSG_NOTIFY_URI - lastUri = " + lastUri);
                        QuickClipStatusManager.this.NotifyUri(lastUri);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.camera.managers.QuickClipStatusManager$2 */
    class C11162 implements OnSharedPreferenceChangeListener {
        C11162() {
        }

        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            if (QuickClipStatusManager.this.mGet.isQuickClipShowingCondition() && "quickclip_uri".equals(key)) {
                Uri lastUri;
                String uriStr = pref.getString("quickclip_uri", null);
                if (QuickClipStatusManager.this.isInitStatus()) {
                    QuickClipStatusManager.this.mIsUpdatedUri = true;
                }
                if (uriStr != null) {
                    lastUri = Uri.parse(uriStr);
                } else {
                    lastUri = QuickClipStatusManager.this.mGet.getUri();
                    if (lastUri != null) {
                        SharedPreferenceUtil.saveQuickClipUri(QuickClipStatusManager.this.mGet.getAppContext(), lastUri);
                    }
                }
                if (QuickClipStatusManager.this.mGet.getBurstProgress() || QuickClipStatusManager.this.mGet.isStillBurstShotSaving()) {
                    QuickClipStatusManager.this.mIsSkipOnBurstShotUpdating = true;
                    QuickClipStatusManager.this.mSkipOnBurstShotUri = lastUri;
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "quickclip_url update : " + uriStr + " Status : " + QuickClipStatusManager.sStatus);
                QuickClipStatusManager.this.mSkipOnBurstShotUri = null;
                QuickClipStatusManager.this.mIsSkipOnBurstShotUpdating = false;
                if (QuickClipStatusManager.this.mGet.getCameraState() == 9 || QuickClipStatusManager.this.mGet.getCameraState() == 8) {
                    QuickClipStatusManager.this.sendNotifyUriMessage(lastUri, 500);
                    return;
                }
                QuickClipStatusManager.this.mHandler.removeMessages(111);
                QuickClipStatusManager.this.NotifyUri(lastUri);
            }
        }
    }

    public enum Status {
        IDLE,
        INIT_LONG_SHOT,
        INIT_TIMER_SHOT,
        INIT,
        NORMAL_VIEW,
        CIRCLE_VIEW,
        RELEASE
    }

    public QuickClipStatusManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setStatusCallback(QuickClipStatusCallback cb) {
        this.mStatusCallback = cb;
    }

    public Status getStatus() {
        return sStatus;
    }

    public void releaseQuickClipStatus() {
        setPreferenceListener(false);
        setStatusCallback(null);
        this.mHandler.removeMessages(111);
    }

    public boolean rollbackStatus() {
        CamLog.m7i(CameraConstants.TAG, "RollBack Status : mPrevStatus = " + this.mPrevStatus);
        if (this.mPrevStatus == Status.NORMAL_VIEW || this.mPrevStatus == Status.CIRCLE_VIEW) {
            setStatus(this.mPrevStatus);
            return true;
        } else if (this.mPrevStatus == Status.INIT) {
            setStatus(Status.CIRCLE_VIEW);
            return true;
        } else {
            setStatus(this.mPrevStatus);
            return false;
        }
    }

    public boolean setStatus(Status newStatus) {
        if (newStatus == Status.CIRCLE_VIEW) {
            if (sStatus == Status.INIT || sStatus == Status.INIT_TIMER_SHOT || sStatus == Status.NORMAL_VIEW) {
                changeStatus(newStatus);
                this.mHandler.removeMessages(110);
                this.mHandler.sendEmptyMessageDelayed(110, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                return true;
            }
        } else if (newStatus != Status.NORMAL_VIEW) {
            changeStatus(newStatus);
            return true;
        } else if (sStatus == Status.INIT || sStatus == Status.INIT_TIMER_SHOT || sStatus == Status.CIRCLE_VIEW) {
            changeStatus(newStatus);
            return true;
        }
        return false;
    }

    private void changeStatus(Status newStatus) {
        if (newStatus != sStatus) {
            CamLog.m7i(CameraConstants.TAG, "Change Status old : " + sStatus + " newStatus : " + newStatus);
            this.mPrevStatus = sStatus;
            sStatus = newStatus;
            if (isInitStatus()) {
                this.mIsUpdatedUri = false;
            }
        }
    }

    public static void resetStatus() {
        sStatus = Status.IDLE;
    }

    public void setReadyStatus() {
        if (sStatus == Status.IDLE) {
            sStatus = Status.NORMAL_VIEW;
        }
    }

    public void setDrawStatus(Status stat) {
        this.mDrawStatus = stat;
    }

    public Status getDrawStatus() {
        return this.mDrawStatus;
    }

    public void resetCircleViewTimer() {
        if (this.mHandler.hasMessages(110)) {
            this.mHandler.removeMessages(110);
            this.mHandler.sendEmptyMessageDelayed(110, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
        }
    }

    public void clearCircleViewTimer() {
        this.mHandler.removeMessages(110);
    }

    public void sendNotifyUriMessage(Uri lastUri, int delay) {
        Message msg = new Message();
        msg.what = 111;
        msg.obj = lastUri;
        this.mHandler.removeMessages(111);
        this.mHandler.sendMessageDelayed(msg, delay == 0 ? 100 : (long) delay);
    }

    private void NotifyStatus(Status status) {
        CamLog.m3d(CameraConstants.TAG, "status : " + status);
        if (setStatus(status) && this.mStatusCallback != null) {
            this.mStatusCallback.onStatusChanged(status);
        }
    }

    public void NotifyUri(Uri uri) {
        if (sStatus != Status.IDLE && sStatus != Status.RELEASE) {
            if (this.mStatusCallback != null) {
                this.mStatusCallback.onThumbnailUpdated(uri);
            }
            if (uri == null) {
                NotifyStatus(Status.IDLE);
            }
            if (sStatus == Status.INIT) {
                NotifyStatus(Status.CIRCLE_VIEW);
            }
        }
    }

    public void setPreferenceListener(boolean isRegister) {
        CamLog.m3d(CameraConstants.TAG, "[QuickClip] isRegister : " + isRegister);
        SharedPreferences pref = this.mGet.getAppContext().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0);
        if (isRegister) {
            pref.registerOnSharedPreferenceChangeListener(this.mSharedPreferenceListener);
        } else {
            pref.unregisterOnSharedPreferenceChangeListener(this.mSharedPreferenceListener);
        }
    }

    public boolean isUpdatedUri() {
        return this.mIsUpdatedUri;
    }

    public boolean isSkipOnBurstShot() {
        return this.mIsSkipOnBurstShotUpdating;
    }

    public Uri getSkipOnBurstShotUri() {
        return this.mSkipOnBurstShotUri;
    }

    private boolean isInitStatus() {
        if (sStatus == Status.INIT || sStatus == Status.INIT_LONG_SHOT || sStatus == Status.INIT_TIMER_SHOT) {
            return true;
        }
        return false;
    }
}
