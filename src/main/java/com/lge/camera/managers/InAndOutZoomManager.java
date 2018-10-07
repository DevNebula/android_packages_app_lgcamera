package com.lge.camera.managers;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.InAndOutZoomBarInterface;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.SwitchableBar;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class InAndOutZoomManager extends ZoomManager implements InAndOutZoomBarInterface {
    private final int MIN_GAP = 3;
    private final int SKIP_JOG_ZOOM_RANGE = 50;
    private final int SKIP_JOG_ZOOM_TIME = 400;
    private final int SKIP_JOG_ZOOM_TIMER_DURATION = 50;
    private final String TIMER_NAME = "skip_jog_zoom";
    private int mBackupRearCameraInitZoomStep = 0;
    protected RotateImageButton mBtnRecordingZoom = null;
    private final HandlerRunnable mDrawExceedsLevels = new HandlerRunnable(this) {
        public void handleRun() {
            if (InAndOutZoomManager.this.mInAndOutZoomBar != null) {
                if (InAndOutZoomManager.this.mGet.checkModuleValidate(64)) {
                    int step = InAndOutZoomManager.this.mInAndOutZoomBar.drawExceedsLevels();
                    if (step != -1 && InAndOutZoomManager.this.mInAndOutZoomBar.isDrawingExceedsLevels()) {
                        InAndOutZoomManager.this.setValue(InAndOutZoomManager.this.mZoomValue + step);
                        InAndOutZoomManager.this.mGet.postOnUiThread(InAndOutZoomManager.this.mDrawExceedsLevels, 0);
                        return;
                    }
                    return;
                }
                InAndOutZoomManager.this.mGet.removePostRunnable(InAndOutZoomManager.this.mDrawExceedsLevels);
                InAndOutZoomManager.this.mInAndOutZoomBar.stopDrawingExceedsLevels();
            }
        }
    };
    private SwitchableBar mInAndOutZoomBar = null;
    private boolean mIsSetOpticZoomRange = false;
    private int mSecondaryCameraId = 2;
    private TimerTask mSkipJogZoom = null;
    private int mSkipTime = 0;
    private Timer mTimer = null;
    private int mWideCameraMaxZoom = 280;
    private boolean minitialized = false;

    /* renamed from: com.lge.camera.managers.InAndOutZoomManager$1 */
    class C09961 implements OnClickListener {
        C09961() {
        }

        public void onClick(View arg0) {
            if (!InAndOutZoomManager.this.mIsJogZoomWorking) {
                InAndOutZoomManager.this.resetBarDisappearTimer(2, 3000);
                InAndOutZoomManager.this.setRotateDegree(InAndOutZoomManager.this.mGet.getOrientationDegree(), false);
                InAndOutZoomManager.this.setZoomBarVisibility(0);
                InAndOutZoomManager.this.mBtnRecordingZoom.setVisibility(8);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.InAndOutZoomManager$3 */
    class C09983 extends TimerTask {
        C09983() {
        }

        public void run() {
            InAndOutZoomManager.this.mSkipTime = InAndOutZoomManager.this.mSkipTime + 50;
            if (InAndOutZoomManager.this.mSkipTime <= 400) {
                CamLog.m3d(CameraConstants.TAG, "[shutterZoom] skipping shutterZoom while " + InAndOutZoomManager.this.mSkipTime + " ms");
            } else if (InAndOutZoomManager.this.mTimer != null) {
                InAndOutZoomManager.this.mTimer.cancel();
                InAndOutZoomManager.this.mTimer.purge();
                InAndOutZoomManager.this.mTimer = null;
            }
        }
    }

    public InAndOutZoomManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
        setCameraIds();
    }

    public void initializeAfterStartPreviewDone() {
        super.initializeAfterStartPreviewDone();
        initInAndOutZoomBarValue();
        setRecordingZoomButton();
    }

    private void setRecordingZoomButton() {
        this.mBtnRecordingZoom = (RotateImageButton) this.mGet.findViewById(C0088R.id.rec_zoom_button);
        if (this.mBtnRecordingZoom != null) {
            this.mBtnRecordingZoom.setOnClickListener(new C09961());
            this.mBtnRecordingZoom.setDegree(this.mGet.getOrientationDegree(), false);
        }
    }

    public void initInAndOutZoomBarValue() {
        int backupValue = this.mZoomValue >= 0 ? this.mZoomValue : getInitZoomStep(this.mGet.getCameraId());
        if (this.mInAndOutZoomBar == null) {
            return;
        }
        if (this.mGet.getCameraId() == this.mSecondaryCameraId) {
            int initialValue;
            this.mInAndOutZoomBar.setUpperBarMode(false);
            if (this.mInAndOutZoomBar.isSwitching()) {
                initialValue = getMaxZoomForWideCamera();
            } else {
                initialValue = backupValue;
            }
            this.mInAndOutZoomBar.setBarValue(initialValue);
            return;
        }
        this.mInAndOutZoomBar.setUpperBarMode(true);
        this.mInAndOutZoomBar.setBarValue(backupValue);
    }

    public View getZoomBarView() {
        return this.mInAndOutZoomBar;
    }

    public void resetZoomLevel() {
        super.resetZoomLevel();
        if (this.mInAndOutZoomBar != null) {
            this.mInAndOutZoomBar.setBarValue(getInitZoomStep(this.mGet.getCameraId()));
        }
    }

    public void onBarSwitchedDuringRecording() {
        if (this.mInAndOutZoomBar == null) {
            return;
        }
        if (this.mGet.getCameraId() == this.mSecondaryCameraId) {
            int zoomLevel = this.mInAndOutZoomBar.isSwitching() ? getMaxZoomForWideCamera() : 0;
            this.mInAndOutZoomBar.setMaxValue(getMaxZoomForWideCamera());
            this.mInAndOutZoomBar.setUpperBarMode(false);
            this.mInAndOutZoomBar.setBarValue(zoomLevel);
            return;
        }
        this.mInAndOutZoomBar.setMaxValue(this.mZoomMaxValue);
        this.mInAndOutZoomBar.setUpperBarMode(true);
        this.mInAndOutZoomBar.setBarValue(0);
    }

    private void setCameraIds() {
        if (FunctionProperties.getCameraTypeFront() != 1) {
            this.mSecondaryCameraId = 2;
        } else if (FunctionProperties.getCameraTypeRear() != 1) {
            this.mSecondaryCameraId = 2;
        }
    }

    public void initZoomBar() {
        super.initZoomBar();
        if (this.minitialized) {
            CamLog.m3d(CameraConstants.TAG, "Already initialized, return");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "initZoomManager in InAndOutZoomManager");
        boolean isOpticZoom = FunctionProperties.isSupportedOpticZoom();
        if (isOpticZoom) {
            this.mInAndOutZoomBar = (SwitchableBar) this.mGet.findViewById(C0088R.id.optic_zoom_bar);
        } else {
            this.mInAndOutZoomBar = (SwitchableBar) this.mGet.findViewById(C0088R.id.in_and_out_zoom_bar);
        }
        CamLog.m3d(CameraConstants.TAG, "mInAndOutZoomBar = " + this.mInAndOutZoomBar);
        if (this.mInAndOutZoomBar != null) {
            this.mInAndOutZoomBar.setInterface(this);
            int maxValue = this.mZoomMaxValue;
            if (!isOpticZoom && this.mGet.getCameraId() == 2) {
                maxValue = getMaxZoomForWideCamera();
            }
            this.mInAndOutZoomBar.setMaxValue(maxValue);
            setRotateDegree(this.mGet.getOrientationDegree(), false);
        }
        this.minitialized = true;
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        int convertDegree = degree;
        int marginStart = Utils.getPx(getAppContext(), C0088R.dimen.in_and_out_zoom_marginEnd);
        int marginStartOnReverse = Utils.getPx(getAppContext(), C0088R.dimen.in_and_out_zoom_marginEnd_reverse);
        if (ModelProperties.isLongLCDModel()) {
            Drawable icon = getAppContext().getDrawable(C0088R.drawable.camera_preview_bar_plus);
            if (icon != null) {
                int width = Utils.getPx(getAppContext(), C0088R.dimen.in_and_out_zoom_width);
                int iconWidth = icon.getIntrinsicWidth();
                if (this.mGet == null || !this.mGet.isUspVisible() || this.mInAndOutZoomBar == null) {
                    marginStart = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.06f) * -1;
                    marginStartOnReverse = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.106f);
                } else {
                    int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
                    marginStart = (lcdSize[1] - this.mInAndOutZoomBar.getInAndOutZoomBarEndMargin(false)) - (getAppContext().getDrawable(C0088R.drawable.btn_rec_zoom_normal).getMinimumHeight() * 3);
                    marginStartOnReverse = (lcdSize[1] - this.mInAndOutZoomBar.getInAndOutZoomBarEndMargin(true)) - (getAppContext().getDrawable(C0088R.drawable.btn_rec_zoom_normal).getMinimumHeight() * 3);
                }
            }
        } else if (isPreview4by3()) {
            marginStart = Utils.getPx(getAppContext(), C0088R.dimen.in_and_out_zoom_marginEnd_only_4by3);
            marginStartOnReverse = Utils.getPx(getAppContext(), C0088R.dimen.in_and_out_zoom_marginEnd_reverse_only_4by3);
        }
        if (degree == 0) {
            convertDegree = 270;
        } else if (degree == 180) {
            convertDegree = 90;
        }
        setInAndOutZoomBarLayoutParams(degree, convertDegree, marginStart, marginStartOnReverse);
        if (this.mBtnRecordingZoom != null) {
            this.mBtnRecordingZoom.setDegree(degree, animation);
        }
        RotateLayout rl = (RotateLayout) this.mGet.findViewById(C0088R.id.zoombar_rotate);
        if (rl != null) {
            rl.rotateLayout(convertDegree);
        }
    }

    private void setInAndOutZoomBarLayoutParams(int degree, int convertDegree, int marginStart, int marginStartOnReverse) {
        if (this.mInAndOutZoomBar != null) {
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
                int btnAreaHeight = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.086f);
                int resourceHeight = getAppContext().getDrawable(C0088R.drawable.btn_rec_zoom_normal).getMinimumHeight();
                marginStart = ((lcdSize[1] - this.mInAndOutZoomBar.getInAndOutZoomBarEndMargin(false)) + ((btnAreaHeight - resourceHeight) / 2)) + Utils.getPx(getAppContext(), C0088R.dimen.square_zoom_bar_marginBottom);
                marginStartOnReverse = ((lcdSize[1] - this.mInAndOutZoomBar.getInAndOutZoomBarEndMargin(true)) + ((btnAreaHeight - resourceHeight) / 2)) + Utils.getPx(getAppContext(), C0088R.dimen.square_zoom_bar_marginBottom);
            }
            LayoutParams barParams = (LayoutParams) this.mInAndOutZoomBar.getLayoutParams();
            Utils.resetLayoutParameter(barParams);
            switch (convertDegree) {
                case 90:
                    barParams.addRule(20, 1);
                    barParams.addRule(15, 1);
                    barParams.setMarginStart(marginStartOnReverse);
                    barParams.setMarginEnd(0);
                    break;
                case 270:
                    barParams.addRule(21, 1);
                    barParams.addRule(15, 1);
                    barParams.setMarginStart(0);
                    barParams.setMarginEnd(marginStart);
                    break;
            }
            this.mInAndOutZoomBar.setLayoutParams(barParams);
            this.mInAndOutZoomBar.setDegree(degree);
        }
    }

    public void setZoomMaxValue(int value) {
        int maxValue = value;
        if (this.mInAndOutZoomBar != null) {
            if (!this.mGet.isOpticZoomSupported(null) && this.mGet.getCameraId() == 2) {
                maxValue = getMaxZoomForWideCamera();
            }
            this.mInAndOutZoomBar.setMaxValue(maxValue);
        } else {
            CamLog.m3d(CameraConstants.TAG, "mInAndOutZoomBar's null");
        }
        super.setZoomMaxValue(maxValue);
    }

    public void setZoomButtonVisibility(int visibility) {
        if ((visibility != 0 || (!this.mGet.isManualVideoAudioPopupShowing() && !this.mGet.isManualDrumShowing(31))) && this.mBtnRecordingZoom != null) {
            this.mBtnRecordingZoom.setVisibility(visibility);
        }
    }

    public void onPauseBefore() {
        stopSkipShutterZoom();
        super.onPauseBefore();
    }

    public void onDestroy() {
        this.mBtnRecordingZoom = null;
        this.minitialized = false;
        this.mIsSetOpticZoomRange = false;
        super.onDestroy();
    }

    public void setZoomBarVisibility(int visibility) {
        if (this.mZoomInterface != null && this.mInAndOutZoomBar != null && this.mInAndOutZoomBar.getVisibility() != visibility) {
            if (visibility != 0) {
                this.mInAndOutZoomBar.setVisibility(visibility);
                this.mZoomInterface.onZoomHide();
            } else if (this.mZoomInterface.isZoomAvailable() && checkZoomBarVisibilityCondition()) {
                setRotateDegree(this.mGet.getOrientationDegree(), false);
                this.mInAndOutZoomBar.setVisibility(visibility);
                this.mZoomInterface.onZoomShow();
                if (this.mJogZoomMinimapLayout != null) {
                    this.mJogZoomMinimapLayout.setVisibility(8);
                }
            }
        }
    }

    public boolean isZoomBarVisible() {
        if (this.mInAndOutZoomBar != null && this.mInAndOutZoomBar.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public void doZoomAction(int cursorStep, int factor, int gapSpan, boolean scaleEnd, boolean forJogZoom) {
        int zoom = -1;
        if (!forJogZoom) {
            if (this.mInAndOutZoomBar != null) {
                zoom = this.mInAndOutZoomBar.setBarCursorByGestureStep(factor * cursorStep, gapSpan, scaleEnd);
            }
            if (zoom == -1 || zoom == this.mZoomValue) {
                return;
            }
        }
        setZoomStep(cursorStep, factor, scaleEnd, forJogZoom, zoom);
    }

    public void setValue(int value) {
        setZoomValue(value);
        if (this.mZoomInterface != null && value >= 0) {
            this.mZoomInterface.setZoomStep(this.mZoomValue, false, false, false);
        }
    }

    public void onBarSwitching(boolean isBarTouched) {
        this.mZoomValue = -1;
        CamLog.m3d(CameraConstants.TAG, "mZoomValue = -1");
        if (isJogZoomMoving() && FunctionProperties.getSupportedHal() == 2 && this.mGet.getCameraDevice() != null) {
            CameraParameters param = this.mGet.getCameraDevice().getParameters();
            if (param != null) {
                param.set(ParamConstants.KEY_JOG_ZOOM, 0);
                this.mGet.getCameraDevice().setParameters(param);
                CamLog.m3d(CameraConstants.TAG, "[jog] KEY_JOG_ZOOM set to 0");
            }
        }
        if (this.mZoomInterface != null) {
            this.mZoomInterface.doInAndOutZoom(false, isBarTouched);
        }
    }

    public void setZoomValue(int zoomValue) {
        if (isInAndOutSwithing()) {
            CamLog.m3d(CameraConstants.TAG, "[jog] setZoomValue return! now camera switching..");
        } else {
            super.setZoomValue(zoomValue);
        }
    }

    public void drawExceedsLevel() {
        this.mGet.postOnUiThread(this.mDrawExceedsLevels, 0);
    }

    public void stopDrawingExceedsLevel() {
        CamLog.m3d(CameraConstants.TAG, "stopDrawingExceedsLevel mZoomValue = " + this.mZoomValue);
        this.mGet.removePostRunnable(this.mDrawExceedsLevels);
        if (this.mInAndOutZoomBar != null) {
            if (this.mInAndOutZoomBar.isSwitching()) {
                this.mInAndOutZoomBar.notifySwitchingFinished();
            }
            this.mInAndOutZoomBar.stopDrawingExceedsLevels();
        }
    }

    public void resetBarDisappearTimer() {
        resetBarDisappearTimer(2, 3000);
    }

    public boolean isZoomControllersMoving() {
        if (super.isZoomControllersMoving()) {
            return true;
        }
        if (this.mInAndOutZoomBar != null) {
            return this.mInAndOutZoomBar.isBarTouched();
        }
        return false;
    }

    public boolean isZoomControllersGetTouched() {
        if (super.isZoomControllersGetTouched()) {
            return true;
        }
        if (this.mInAndOutZoomBar != null) {
            return this.mInAndOutZoomBar.isBarTouched();
        }
        return false;
    }

    public void setMaxZoomForWideCamera(int wideCameraMaxZoom) {
        this.mWideCameraMaxZoom = wideCameraMaxZoom;
    }

    public int getMaxZoomForWideCamera() {
        return this.mWideCameraMaxZoom;
    }

    public void notifySwitchingFinished() {
        super.notifySwitchingFinished();
        if (this.mInAndOutZoomBar == null) {
            return;
        }
        if (this.mInAndOutZoomBar.isSwitching()) {
            if (isJogZoomMoving()) {
                setZoomChangeCallback(this.mZoomChangeCallback);
                resetJogZoomSpeed();
            }
            this.mInAndOutZoomBar.notifySwitchingFinished();
            if (isJogZoomMoving()) {
                CamLog.m3d(CameraConstants.TAG, "[jog] switch done. start to jog continuously");
                moveJogZoom(this.mLastSeekBarProgressValue);
                return;
            }
            return;
        }
        this.mInAndOutZoomBar.setBarValue(this.mZoomValue);
    }

    public void resetJogZoomSpeed() {
        this.mResetJogSpeed = true;
        this.mCurJogZoomSpeed = 0;
        CamLog.m3d(CameraConstants.TAG, "[jog] mResetJogSpeed set to true");
    }

    public boolean isInAndOutSwithing() {
        if (this.mGet.checkCameraChanging(4)) {
            return true;
        }
        if (this.mInAndOutZoomBar == null) {
            return false;
        }
        return this.mInAndOutZoomBar.isSwitching();
    }

    public boolean isRunningFilmEmulator() {
        if (this.mGet.checkModuleValidate(128) || this.mGet.getFilmState() <= 3) {
            return false;
        }
        return true;
    }

    public boolean isDoubleCameraSwitchingAvailable() {
        return this.mGet.checkDoubleCameraSwitchingAvailable();
    }

    public void setRecordingZoomBtnPosition(boolean manualMode) {
        if (this.mBtnRecordingZoom != null) {
            LayoutParams lp = (LayoutParams) this.mBtnRecordingZoom.getLayoutParams();
            if (lp != null) {
                int marginStart = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.039f);
                if (manualMode) {
                    marginStart += RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.131f);
                }
                int marginBottom = 0;
                if (ModelProperties.isLongLCDModel()) {
                    marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.206f);
                    if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                        marginBottom = Utils.getLCDsize(this.mGet.getAppContext(), true)[1] + ((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.086f) - getAppContext().getDrawable(C0088R.drawable.btn_rec_zoom_normal).getMinimumHeight()) / 2);
                    }
                    if (this.mGet.isAttachIntent()) {
                        marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.206f);
                    }
                } else if (this.mGet.isAttachIntent() && isPreview4by3()) {
                    marginBottom = Utils.getPx(getAppContext(), C0088R.dimen.rec_zoom_btn_marginBottom_4by3);
                }
                lp.setMarginStart(marginStart);
                lp.bottomMargin = marginBottom;
                this.mBtnRecordingZoom.setLayoutParams(lp);
            }
        }
    }

    protected void changeJogZoomValue(int value, int factor) {
        if (this.mInAndOutZoomBar != null && !checkCameraSwitched(factor)) {
            super.changeJogZoomValue(value, factor);
        }
    }

    protected boolean checkCameraSwitched(int factor) {
        if (this.mInAndOutZoomBar == null) {
            return false;
        }
        int cameraId = this.mGet.getCameraId();
        int zoomValue = getZoomValue();
        if (!this.mGet.checkModuleValidate(16) || this.mGet.isOpticZoomSupported(null)) {
            return false;
        }
        switch (factor) {
            case -1:
                if (cameraId == this.mSecondaryCameraId || zoomValue > 0) {
                    return false;
                }
                this.mInAndOutZoomBar.switchBar();
                if (this.mJogZoomMinimap != null) {
                    this.mJogZoomMinimap.moveInAndOutZoomLine(1.0f, this.mSecondaryCameraId);
                }
                return true;
            case 1:
                if (cameraId != this.mSecondaryCameraId || zoomValue < getMaxZoomForWideCamera()) {
                    return false;
                }
                this.mInAndOutZoomBar.switchBar();
                if (this.mJogZoomMinimap != null) {
                    this.mJogZoomMinimap.moveInAndOutZoomLine(0.0f, 0);
                }
                return true;
            default:
                return false;
        }
    }

    public void updateJogZoomMinimap() {
        if (this.mGet == null || this.mJogZoomMinimap == null || isInAndOutSwithing()) {
            CamLog.m3d(CameraConstants.TAG, "[jog] switching! updateJogZoomMinimap return!");
        } else if (this.mGet.isOpticZoomSupported(null)) {
            this.mJogZoomMinimap.moveZoomLine(this.mZoomValue);
        } else {
            int cameraId = this.mGet.getCameraId();
            this.mJogZoomMinimap.moveInAndOutZoomLine(((float) this.mZoomValue) / ((float) (cameraId == this.mSecondaryCameraId ? getMaxZoomForWideCamera() : this.mZoomMaxValue)), cameraId);
        }
    }

    private boolean isSwitchableArea() {
        int zoomValue = getZoomValue();
        if (this.mGet.isOpticZoomSupported(null)) {
            int maxValue = getMaxZoomForWideCamera() + 50;
            if (getMaxZoomForWideCamera() - 50 >= zoomValue || zoomValue >= maxValue) {
                return false;
            }
            return true;
        }
        int cameraId = this.mGet.getCameraId();
        if (cameraId == this.mSecondaryCameraId && zoomValue >= getMaxZoomForWideCamera() - 50) {
            return true;
        }
        if (cameraId == this.mSecondaryCameraId || zoomValue > 50) {
            return false;
        }
        return true;
    }

    public boolean skipShutterZoom(int aimValue) {
        if (isJogZoomMoving()) {
            stopSkipShutterZoom();
            return false;
        } else if (this.mSkipTime > 400) {
            stopSkipShutterZoom();
            return false;
        } else if (!isSwitchableArea()) {
            return false;
        } else {
            int gap = 9 - aimValue;
            if ((this.mGet.getCameraId() == 0 && gap > 0) || (this.mGet.getCameraId() == 2 && gap < 0)) {
                return false;
            }
            if (Math.abs(gap) <= 3) {
                if (this.mTimer == null) {
                    this.mTimer = new Timer("skip_jog_zoom");
                }
                if (this.mSkipJogZoom == null) {
                    this.mSkipJogZoom = new C09983();
                    this.mTimer.scheduleAtFixedRate(this.mSkipJogZoom, 0, 50);
                }
                if (this.mSkipTime <= 400) {
                    return true;
                }
            }
            return false;
        }
    }

    public void stopSkipShutterZoom() {
        this.mSkipTime = 0;
        if (this.mSkipJogZoom != null) {
            CamLog.m3d(CameraConstants.TAG, "[shutterZoom] stop skip");
            this.mSkipJogZoom.cancel();
            this.mSkipJogZoom = null;
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer.purge();
            this.mTimer = null;
        }
    }

    public boolean moveJogZoom(int aimValue) {
        boolean isJogZoomEnable = super.moveJogZoom(aimValue);
        if (isJogZoomEnable) {
            this.mInAndOutZoomBar.setVisibility(8);
        }
        return isJogZoomEnable;
    }

    public void stopJogZoom() {
        stopSkipShutterZoom();
        super.stopJogZoom();
    }

    public void initZoomValues(CameraParameters param) {
        if (param != null) {
            super.initZoomValues(param);
            if (this.mGet.isOpticZoomSupported(null)) {
                String value = param.get(ParamConstants.KEY_OPTIC_CUTOVER_VALUES);
                CamLog.m7i(CameraConstants.TAG, " KEY_OPTIC_CUTOVER_VALUES " + value);
                if (value != null) {
                    StringSplitter<String> splitter = new SimpleStringSplitter(',');
                    splitter.setString(value);
                    ArrayList<Integer> zoomRange = new ArrayList();
                    for (String s : splitter) {
                        zoomRange.add(Integer.valueOf(Integer.parseInt(s)));
                    }
                    if (zoomRange.size() == 2) {
                        setMaxZoomForWideCamera(((Integer) zoomRange.get(0)).intValue());
                        if (this.mInAndOutZoomBar != null) {
                            this.mInAndOutZoomBar.setBorderPosition((((Integer) zoomRange.get(1)).intValue() + ((Integer) zoomRange.get(0)).intValue()) / 2);
                        }
                        CamLog.m7i(CameraConstants.TAG, "set optic zoom Range " + zoomRange.get(0) + " ~ " + zoomRange.get(1));
                    }
                    this.mIsSetOpticZoomRange = true;
                    this.mBackupRearCameraInitZoomStep = getInitZoomStep(0);
                    if (this.mJogZoomMinimap != null) {
                        this.mJogZoomMinimap.setWideCameraMaxZoomValue(getMaxZoomForWideCamera());
                        return;
                    }
                    return;
                }
                return;
            }
            setMaxZoomForWideCamera(getSecondCameraMaxZoomLevel());
            if (this.mJogZoomMinimap != null) {
                this.mJogZoomMinimap.setWideCameraMaxZoomValue(getSecondCameraMaxZoomLevel());
            }
        }
    }

    public int getInitZoomStep(int cameraId) {
        int init_val = 0;
        if (FunctionProperties.isSupportedOpticZoom()) {
            if (cameraId == 0 && this.mGet.isOpticZoomSupported(null) && isReadyZoom()) {
                return this.mWideCameraMaxZoom + 1;
            }
            return 0;
        } else if (this.mInAndOutZoomBar == null || !this.mInAndOutZoomBar.isSwitching()) {
            return 0;
        } else {
            if (cameraId == this.mSecondaryCameraId) {
                init_val = getMaxZoomForWideCamera();
            }
            CamLog.m3d(CameraConstants.TAG, "getInitZoomStep cameraId : " + init_val);
            return init_val;
        }
    }

    protected void unbindZoomBarView() {
        if (this.mInAndOutZoomBar != null) {
            this.mInAndOutZoomBar.unbind();
            this.mInAndOutZoomBar.setVisibility(8);
            this.mInAndOutZoomBar = null;
        }
    }

    public boolean isReadyZoom() {
        if (this.mGet.isOpticZoomSupported(null)) {
            return this.mIsSetOpticZoomRange;
        }
        return true;
    }

    public boolean isInitZoomStep(int zoomStep) {
        if (this.mGet.getCameraId() == 0) {
            return zoomStep == 0 || zoomStep == this.mBackupRearCameraInitZoomStep;
        } else {
            return super.isInitZoomStep(zoomStep);
        }
    }

    public void onConfigurationChanged(Configuration config) {
        this.minitialized = false;
        super.onConfigurationChanged(config);
        if (this.mGet.checkModuleValidate(13)) {
            initInAndOutZoomBarValue();
            setRecordingZoomButton();
        }
    }
}
