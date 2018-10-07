package com.lge.camera.managers;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.Timer;
import java.util.TimerTask;

public class TimerManager extends ManagerInterfaceImpl {
    public static final int ANI_DURATION = 850;
    public static final float ANI_FROM_SCALE = 0.8f;
    public static final int CAMCORDER_TIMER = 1;
    public static final int CAMERA_TIMER = 0;
    public static final int GESTURE_TIMER = 2;
    public static final int JUMPCUT_TIMER = 3;
    public static final int TIMER_10SEC = 10;
    public static final int TIMER_2SEC = 2;
    public static final int TIMER_3SEC = 3;
    public static final int TIMER_5SEC = 5;
    private static final int TIMER_CANCEL_COUNTER = 3;
    private static final int TIMER_INIT_COUNTER = 0;
    private static final float TIMER_MARGIN_TOP = 0.08f;
    private static final float TIMER_MARGIN_TOP_LONG_LCD = 0.052f;
    public static final int TIMER_SETTING_VALUE = 0;
    private static final int TIMER_START_COUNTER = 1;
    private static final int TIMER_STOP_COUNTER = 2;
    private View mCounterView = null;
    private int mCurrentTimerCount = 0;
    private final Handler mHandler = new C11721();
    private boolean mInTimerShotCountdown = false;
    private boolean mIsFlashNotiEnabled = true;
    private boolean mIsGestureShotProgress = false;
    private String mPrevFlashValue = null;
    private int mShutterType = 0;
    private int mTimerCaptureMode = 0;
    private int mTimerCount;
    private Timer mTimerCountDown = null;
    private int mTimerCountDownCalled = 0;
    private int[] timerDrawable = new int[]{C0088R.drawable.timer_num_1, C0088R.drawable.timer_num_2, C0088R.drawable.timer_num_3, C0088R.drawable.timer_num_4, C0088R.drawable.timer_num_5, C0088R.drawable.timer_num_6, C0088R.drawable.timer_num_7, C0088R.drawable.timer_num_8, C0088R.drawable.timer_num_9, C0088R.drawable.timer_num_10};

    /* renamed from: com.lge.camera.managers.TimerManager$1 */
    class C11721 extends Handler {
        C11721() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    CamLog.m3d(CameraConstants.TAG, "timer INIT msg.arg1 = " + msg.arg1);
                    TimerManager.this.displayInitCounter(msg.arg1);
                    return;
                case 1:
                    CamLog.m3d(CameraConstants.TAG, "timer START msg.arg1 = " + msg.arg1);
                    TimerManager.this.displayStartCounter(msg.arg1);
                    return;
                case 2:
                    CamLog.m3d(CameraConstants.TAG, "timer STOP msg.arg1 = " + msg.arg1);
                    TimerManager.this.displayStopCounter();
                    return;
                case 3:
                    CamLog.m3d(CameraConstants.TAG, "timer CANCEL msg.arg1 = " + msg.arg1);
                    TimerManager.this.cancelCounter();
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.lge.camera.managers.TimerManager$2 */
    class C11732 extends TimerTask {
        C11732() {
        }

        public void run() {
            CamLog.m3d(CameraConstants.TAG, "timer task (count down) " + TimerManager.this.mTimerCount);
            TimerManager.this.addTimerMessage(1, TimerManager.this.mTimerCount);
            if (TimerManager.this.mTimerCount > 0) {
                TimerManager.this.mGet.playSound(4, false, TimerManager.this.mTimerCount);
                TimerManager.this.mTimerCount = TimerManager.this.mTimerCount - 1;
                return;
            }
            if (TimerManager.this.mTimerCountDown != null) {
                TimerManager.this.mTimerCountDown.purge();
                TimerManager.this.mTimerCountDown.cancel();
                TimerManager.this.mTimerCountDown = null;
            }
            TimerManager.this.mTimerCount = TimerManager.this.mTimerCaptureMode;
        }
    }

    /* renamed from: com.lge.camera.managers.TimerManager$3 */
    class C11743 extends TimerTask {
        C11743() {
        }

        public void run() {
            TimerManager.this.mTimerCountDownCalled = TimerManager.this.mTimerCountDownCalled + 1;
            if (TimerManager.this.mTimerCount > 1) {
                if (TimerManager.this.mTimerCountDownCalled % 10 == 5) {
                    TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_OFF);
                } else if (TimerManager.this.mTimerCountDownCalled % 10 == 0) {
                    TimerManager.this.addTimerMessage(1, TimerManager.this.mTimerCount);
                    TimerManager.this.mGet.playSound(4, false, TimerManager.this.mTimerCount);
                    TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_ON);
                    TimerManager.this.mTimerCount = TimerManager.this.mTimerCount - 1;
                }
            } else if (TimerManager.this.mTimerCount > 0) {
                if (TimerManager.this.mTimerCountDownCalled % 10 == 1) {
                    TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_OFF);
                } else if (TimerManager.this.mTimerCountDownCalled % 10 == 2) {
                    TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_ON);
                } else if (TimerManager.this.mTimerCountDownCalled % 10 == 3) {
                    TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_OFF);
                } else if (TimerManager.this.mTimerCountDownCalled % 10 == 0) {
                    TimerManager.this.addTimerMessage(1, TimerManager.this.mTimerCount);
                    TimerManager.this.mGet.playSound(4, false, TimerManager.this.mTimerCount);
                    TimerManager.this.mTimerCount = TimerManager.this.mTimerCount - 1;
                    if (!TimerManager.this.isLowFps()) {
                        TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_ON);
                    }
                }
            } else if (TimerManager.this.mTimerCountDownCalled % 10 == 1) {
                TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_OFF);
            } else if (TimerManager.this.mTimerCountDownCalled % 10 == 2) {
                if (!TimerManager.this.isLowFps()) {
                    TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_ON);
                }
            } else if (TimerManager.this.mTimerCountDownCalled % 10 == 3) {
                TimerManager.this.setFlashMode(ParamConstants.FLASH_MODE_TIMER_OFF);
            } else if (TimerManager.this.mTimerCountDownCalled % 10 == 4) {
                if (TimerManager.this.mPrevFlashValue != null) {
                    TimerManager.this.setFlashMode(TimerManager.this.mPrevFlashValue);
                }
            } else if (TimerManager.this.mTimerCountDownCalled % 10 == 0) {
                TimerManager.this.addTimerMessage(1, TimerManager.this.mTimerCount);
                if (TimerManager.this.mTimerCountDown != null) {
                    TimerManager.this.mTimerCountDown.purge();
                    TimerManager.this.mTimerCountDown.cancel();
                    TimerManager.this.mTimerCountDown = null;
                }
                TimerManager.this.mTimerCount = TimerManager.this.mTimerCaptureMode;
                TimerManager.this.mTimerCountDownCalled = 0;
                TimerManager.this.mPrevFlashValue = null;
            }
        }
    }

    public static class TimerType {
        protected int mShutterType = 0;
        protected int mStorageType = 0;
        protected String mTime = "0";

        public TimerType(String time) {
            this.mTime = time;
        }

        public String getTime() {
            return this.mTime;
        }

        public void setTime(String mChangeTime) {
            this.mTime = mChangeTime;
        }

        public int getStorage() {
            return this.mStorageType;
        }

        public int getShutter() {
            return this.mShutterType;
        }
    }

    public static class TimerTypeCamera extends TimerType {
    }

    public static class TimerTypeFlashJumpCut extends TimerType {
        public TimerTypeFlashJumpCut() {
            this.mTime = "3";
            this.mStorageType = 1;
            this.mShutterType = 3;
        }
    }

    public static class TimerTypeGestureShot extends TimerType {
        public TimerTypeGestureShot() {
            this.mTime = "3";
            this.mStorageType = 1;
            this.mShutterType = 2;
        }
    }

    public static class TimerTypeMultiview extends TimerType {
        public TimerTypeMultiview() {
            this.mTime = "2";
        }
    }

    public static class TimerTypeShutterless extends TimerType {
        public TimerTypeShutterless() {
            this.mTime = "2";
        }
    }

    public TimerManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        this.mTimerCount = this.mTimerCaptureMode;
        this.mInTimerShotCountdown = false;
    }

    public void onPauseBefore() {
        stopTimerShot();
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "timer onDestroy");
        releaseCounterView();
    }

    public void setType(int type) {
        this.mShutterType = type;
    }

    public void enableFlashNoti(boolean bool) {
        this.mIsFlashNotiEnabled = bool;
        CamLog.m3d(CameraConstants.TAG, "disableFlashNoti camera flash mIsFlashNotiEnabled: " + this.mIsFlashNotiEnabled);
    }

    private void updateTimerView(int timerCount) {
        if (this.mCounterView != null) {
            this.mCurrentTimerCount = timerCount;
            if (timerCount > 0) {
                int resId = this.timerDrawable[timerCount - 1];
                RotateImageView iv_timer_num = (RotateImageView) this.mCounterView.findViewById(C0088R.id.timer_count);
                if (iv_timer_num != null) {
                    setTimerLayoutParam(iv_timer_num);
                    iv_timer_num.setImageResource(resId);
                    timerAnimation(true);
                }
            }
        }
    }

    private boolean needFlashHelper() {
        if (FunctionProperties.isSupportedTimerHelper() && this.mGet != null && this.mGet.isFlashSupported() && this.mGet.isRearCamera() && this.mIsFlashNotiEnabled) {
            return true;
        }
        return false;
    }

    public void startTimerShot(int time, int shutterType) {
        boolean z;
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (vg != null) {
            if (this.mCounterView != null) {
                vg.removeView(this.mCounterView);
                this.mHandler.removeMessages(3);
            }
            this.mCounterView = this.mGet.inflateView(C0088R.layout.timer_view);
            if (this.mCounterView != null) {
                vg.addView(this.mCounterView, new LayoutParams(-1, -1));
            }
        }
        AudioUtil.setAudioFocus(this.mGet.getAppContext(), true);
        if (time == 0) {
            this.mTimerCaptureMode = Integer.parseInt(this.mGet.getSettingValue(Setting.KEY_TIMER));
        } else {
            this.mTimerCaptureMode = time;
        }
        if (this.mTimerCaptureMode > 10) {
            this.mTimerCaptureMode = 10;
        }
        this.mTimerCount = this.mTimerCaptureMode;
        this.mShutterType = shutterType;
        CamLog.m3d(CameraConstants.TAG, "startTimerShot - type : " + this.mShutterType);
        if (shutterType == 2) {
            z = true;
        } else {
            z = false;
        }
        this.mIsGestureShotProgress = z;
        this.mGet.playSound(4, false, this.mTimerCount);
        if (needFlashHelper()) {
            this.mPrevFlashValue = this.mGet.getParamValue("flash-mode");
            setFlashMode(ParamConstants.FLASH_MODE_TIMER_ON);
        }
        this.mInTimerShotCountdown = true;
        this.mTimerCountDown = new Timer("timer_countdown");
        TimerTask taskCountDown = new C11732();
        TimerTask taskCountDownFlash = new C11743();
        addTimerMessage(0, this.mTimerCount);
        this.mTimerCount--;
        if (needFlashHelper()) {
            this.mTimerCountDown.scheduleAtFixedRate(taskCountDownFlash, 100, 100);
        } else {
            this.mTimerCountDown.scheduleAtFixedRate(taskCountDown, 1000, 1000);
        }
    }

    public boolean isLowFps() {
        if (this.mGet == null) {
            return false;
        }
        CameraProxy mCamDevice = this.mGet.getCameraDevice();
        if (mCamDevice == null || !mCamDevice.isLowFps()) {
            return false;
        }
        return true;
    }

    public boolean stopTimerShot() {
        boolean result = false;
        timerAnimation(false);
        if (this.mInTimerShotCountdown) {
            if (!"on".equals(this.mGet.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(getAppContext(), false);
            }
            if (this.mTimerCountDown != null) {
                this.mTimerCountDown.purge();
                this.mTimerCountDown.cancel();
                this.mTimerCountDown = null;
            }
            this.mTimerCount = this.mTimerCaptureMode;
            this.mInTimerShotCountdown = false;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            addTimerMessage(3, 0);
            result = true;
        }
        this.mIsGestureShotProgress = false;
        this.mTimerCountDownCalled = 0;
        if (this.mPrevFlashValue != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setFlashMode(ParamConstants.FLASH_MODE_TIMER_OFF);
            setFlashMode("off");
            setFlashMode(this.mPrevFlashValue);
            this.mPrevFlashValue = null;
        }
        return result;
    }

    public void displayInitCounter(int startTime) {
        if (this.mCounterView != null) {
            if (startTime > this.timerDrawable.length) {
                startTime = this.timerDrawable.length;
            }
            int resId = this.timerDrawable[startTime - 1];
            this.mCurrentTimerCount = startTime;
            RotateImageView iv_timer_num = (RotateImageView) this.mCounterView.findViewById(C0088R.id.timer_count);
            if (!(resId == 0 || iv_timer_num == null)) {
                setTimerLayoutParam(iv_timer_num);
                iv_timer_num.setImageResource(resId);
            }
            setRotateDegree(getOrientationDegree(), false);
            timerAnimation(true);
        }
    }

    public void displayStartCounter(int timerCapturedDelay) {
        timerAnimation(false);
        if (timerCapturedDelay == 0) {
            addTimerMessage(2, 0);
        } else {
            updateTimerView(timerCapturedDelay);
        }
    }

    private void releaseCounterView() {
        if (this.mCounterView != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                vg.removeView(this.mCounterView);
                this.mCounterView = null;
            }
        }
    }

    public void displayStopCounter() {
        releaseCounterView();
        if (this.mInTimerShotCountdown) {
            this.mGet.takePictureByTimer(this.mShutterType);
            this.mIsGestureShotProgress = false;
            this.mInTimerShotCountdown = false;
        }
    }

    private void cancelCounter() {
        releaseCounterView();
        if (FunctionProperties.isSupportedCameraRoll()) {
            this.mGet.setTilePreviewLayout("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW)));
        }
    }

    private synchronized void setFlashMode(String mode) {
        if (this.mGet != null) {
            CameraProxy mCamDevice = this.mGet.getCameraDevice();
            if (mCamDevice != null) {
                CameraParameters param = mCamDevice.getParameters();
                if (param != null) {
                    param.set("flash-mode", mode);
                    mCamDevice.setParameters(param);
                }
            }
        }
    }

    public boolean isTimerShotCountdown() {
        return this.mInTimerShotCountdown;
    }

    public void timerAnimation(boolean start) {
        if (this.mCounterView != null) {
            View view = this.mCounterView.findViewById(C0088R.id.timer_count);
            if (view == null) {
                return;
            }
            if (start) {
                AnimationUtil.startTimerCountDownAnimation(view, 0.8f, 0, 850, null);
            } else {
                view.clearAnimation();
            }
        }
    }

    public void addTimerMessage(int what, int arg) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg;
        this.mHandler.sendMessage(msg);
    }

    public void onConfigurationChanged(Configuration config) {
        if (this.mInTimerShotCountdown) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (!(vg == null || this.mCounterView == null)) {
                vg.removeView(this.mCounterView);
                this.mCounterView = this.mGet.inflateView(C0088R.layout.timer_view);
                if (this.mCounterView != null) {
                    vg.addView(this.mCounterView, new LayoutParams(-1, -1));
                }
            }
            if (isTimerShotCountdown() || getIsGesureTimerShotProgress()) {
                updateTimerView(this.mCurrentTimerCount);
            }
        }
        super.onConfigurationChanged(config);
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mInTimerShotCountdown && this.mCounterView != null) {
            RotateImageView countView = (RotateImageView) this.mCounterView.findViewById(C0088R.id.timer_count);
            if (countView != null) {
                setTimerLayoutParam(countView);
                countView.setDegree(degree, animation);
            }
        }
    }

    public void setIsGesureTimerShotProgress(boolean progress) {
        this.mIsGestureShotProgress = progress;
    }

    public boolean getIsGesureTimerShotProgress() {
        return this.mIsGestureShotProgress;
    }

    private void setTimerLayoutParam(View timerView) {
        int top;
        if (ModelProperties.isLongLCDModel()) {
            top = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, TIMER_MARGIN_TOP_LONG_LCD) + RatioCalcUtil.getNotchDisplayHeight(getAppContext());
        } else {
            top = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.08f);
        }
        LayoutParams lp = (LayoutParams) timerView.getLayoutParams();
        if (lp != null) {
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                top += RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.11112f);
            }
            int px = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.timer_text_size);
            lp.height = px;
            lp.width = px;
            lp.setMarginsRelative(0, top, 0, 0);
            timerView.setLayoutParams(lp);
        }
    }
}
