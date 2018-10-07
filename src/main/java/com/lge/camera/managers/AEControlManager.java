package com.lge.camera.managers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.AeControlBar;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraManager;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraManagerFactory;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.hardware.LGCamera.EVCallbackData;
import com.lge.hardware.LGCamera.EVCallbackListener;

public class AEControlManager extends FocusManagerBase {
    private final float BAR_ADJUST_BOTTOM_MARGIN = 0.0266f;
    private final float BAR_ADJUST_BOTTOM_MARGIN_LONG_LCD = 0.0121f;
    private final float BAR_HEIGHT = 0.0556f;
    protected RotateLayout mAEAFLockLayout = null;
    protected AEControlBarInterface mAEControlBarListener = new C07932();
    protected TextView mAFLockTextView;
    protected View mAeControl = null;
    protected AeControlBar mAeControlBar;
    protected LinearLayout mAeControlLayout = null;
    protected EVCallbackListener mEVCallbackDataListener = null;
    protected CameraParameters mEVParam = null;
    protected RotateImageView mEndIcon;
    private boolean mIsAECallbackSet = false;
    protected int[] mLCDSize;
    protected int mPrevAeControlX = 0;
    protected int mPrevAeControlY = 0;
    protected int mPrevX = 0;
    protected int mPrevY = 0;
    protected HandlerRunnable mResetEVRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (AEControlManager.this.mAEControlBarListener == null) {
                return;
            }
            if ((AEControlManager.this.isAEControlBarEnableCondition() || AEControlManager.this.isAeControlBarValueChanged()) && !AEControlManager.this.isAeControlBarShowing() && AEControlManager.this.mAeControlBar.getCurrentValue() != AEControlManager.this.mAeControlBar.getDefaultBarValue() && AEControlManager.this.mGet.checkModuleValidate(16) && !AEControlManager.this.mGet.isMultishotState(7) && !AEControlManager.this.mGet.isCameraChanging() && !AEControlManager.this.mGet.isManualFocusModeEx()) {
                CamLog.m3d(CameraConstants.TAG, "- AE control - resetEVValue");
                CameraProxy cameraDevice = AEControlManager.this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    AEControlManager.this.mEVParam = cameraDevice.getParameters();
                    AEControlManager.this.setEVParam(0);
                    AEControlManager.this.mEVParam = null;
                }
            }
        }
    };
    protected RotateImageView mStartIcon;
    private HandlerRunnable releaseAeControlBarRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            AEControlManager.this.mEVParam = null;
            if (AEControlManager.this.mAeControlLayout != null && AEControlManager.this.mAeControlLayout.getVisibility() == 0) {
                AEControlManager.this.setState(1, false);
                if (!AEControlManager.this.mGet.isManualFocusModeEx()) {
                    AEControlManager.this.setManualFocusButtonVisibility(false);
                }
                if (AEControlManager.this.mAeControlLayout.getVisibility() == 0) {
                    AEControlManager.this.mAeControlLayout.setVisibility(8);
                    AEControlManager.this.mGet.onHideAEBar();
                }
            }
        }
    };

    /* renamed from: com.lge.camera.managers.AEControlManager$1 */
    class C07921 implements EVCallbackListener {
        C07921() {
        }

        public void onDataListen(EVCallbackData data, Camera camera) {
            AEControlManager.this.onEVDataCallback();
        }
    }

    /* renamed from: com.lge.camera.managers.AEControlManager$2 */
    class C07932 implements AEControlBarInterface {
        C07932() {
        }

        public void onBarDown() {
            CamLog.m3d(CameraConstants.TAG, "- AE control - onAEBarDown");
            AEControlManager.this.setState(2, true);
            if (AEControlManager.this.mTouchFocusInterface != null) {
                AEControlManager.this.mTouchFocusInterface.onAEControlBarDown();
            }
            AEControlManager.this.mEVParam = null;
        }

        public void onBarValueChanged(int value) {
            if (AEControlManager.this.mAeControlBar != null && AEControlManager.this.isAEControlBarEnableCondition() && !AEControlManager.this.mGet.isCameraChanging() && !AEControlManager.this.mGet.isMultishotState(7) && AEControlManager.this.isAeControlBarShowing() && AEControlManager.this.mGet.checkModuleValidate(16) && value >= 0 && value <= AEControlManager.this.mAeControlBar.getMaxStep()) {
                if (AEControlManager.this.mEVParam == null) {
                    CameraProxy cameraDevice = AEControlManager.this.mGet.getCameraDevice();
                    if (cameraDevice != null) {
                        AEControlManager.this.mEVParam = cameraDevice.getParameters();
                    }
                }
                AEControlManager.this.setEVParam(value - (AEControlManager.this.mAeControlBar.getMaxStep() / 2));
            }
        }

        public void onBarUp() {
            CamLog.m3d(CameraConstants.TAG, "- AE control - onAEBarUp");
            AEControlManager.this.setState(2, false);
            AEControlManager.this.mEVParam = null;
            if (AEControlManager.this.mTouchFocusInterface == null) {
                return;
            }
            if (!AEControlManager.this.mGet.isAFSupported() || !AEControlManager.this.mGet.isRearCamera() || AEControlManager.this.mGet.getFocusState() != 1) {
                AEControlManager.this.mTouchFocusInterface.onAEControlBarUp();
            }
        }
    }

    public AEControlManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        createAEControlBar();
        if (this.mAeControlBar != null) {
            this.mAeControlBar.setOnAEControlBarListener(this.mAEControlBarListener);
        }
        this.mLCDSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        setState(31, false);
        init();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        setAEControlBarEnable(true);
        if (isAeControlBarValueChanged()) {
            this.mGet.removePostRunnable(this.mResetEVRunnable);
            CameraProxy cameraDevice = this.mGet.getCameraDevice();
            if (cameraDevice != null) {
                setEVParam(cameraDevice.getParameters(), 0);
            }
        }
    }

    protected void createAEControlBar() {
        if (this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "- AE control - createAEControlBar");
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
            this.mAeControl = this.mGet.inflateView(C0088R.layout.ae_control_bar);
            if (this.mAeControl != null) {
                if (vg != null) {
                    vg.addView(this.mAeControl);
                    this.mAeControlLayout = (LinearLayout) this.mAeControl.findViewById(C0088R.id.ae_control_bar_wrapper);
                    this.mAeControlBar = (AeControlBar) this.mAeControl.findViewById(C0088R.id.ae_control_bar);
                    this.mStartIcon = (RotateImageView) this.mAeControl.findViewById(C0088R.id.ae_control_bar_start_image);
                    this.mEndIcon = (RotateImageView) this.mAeControl.findViewById(C0088R.id.ae_control_bar_end_image);
                    int maxStep = this.mGet.getMaxEVStep();
                    if (maxStep < 0) {
                        maxStep = 12;
                    }
                    CamLog.m3d(CameraConstants.TAG, String.format("- AE control - minStep = %d, maxStep = %d", new Object[]{Integer.valueOf(-maxStep), Integer.valueOf(maxStep)}));
                    this.mAeControlBar.init(this.mGet, 0, maxStep * 2, maxStep);
                }
                initAEControlBarLayout();
                changeAEBarBottomMargin();
                initAEControlBarTalkback();
            }
        }
    }

    protected void initAEControlBarTalkback() {
        if (this.mAeControlBar != null) {
            this.mAeControlBar.setContentDescription(null);
            this.mAeControlBar.setFocusable(true);
        }
    }

    public int getAEControlLayoutButtomMargin() {
        String shotMode = this.mGet.getShotMode();
        boolean isNotch = ModelProperties.getLCDType() == 2;
        if (shotMode != null && shotMode.contains(CameraConstants.MODE_SQUARE)) {
            float f;
            Context appContext = getAppContext();
            if (this.mGet.isRearCamera()) {
                f = 0.509f;
            } else {
                f = 0.608f;
            }
            return getSizeCalculatedByPercentage(appContext, true, f);
        } else if (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode())) {
            return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.23f);
        } else {
            if (!this.mGet.isRearCamera()) {
                return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.362f);
            }
            if (CameraConstants.MODE_FLASH_JUMPCUT.equals(this.mGet.getShotMode()) && this.mGet.isRearCamera()) {
                return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.3096f);
            }
            if (ModelProperties.isLongLCDModel()) {
                if (ModelProperties.isLongLCDModel() && !this.mGet.isUspZoneSupportedMode(this.mGet.getShotMode())) {
                    return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.2152f);
                }
                return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, isNotch ? 0.315f : 0.289f);
            } else if (CameraConstants.MODE_FRONT_OUTFOCUS.equals(this.mGet.getShotMode())) {
                return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.3093f);
            } else {
                return getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.255f);
            }
        }
    }

    public int getAEControlLayoutWidth() {
        return RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0556f);
    }

    public void changeAEBarBottomMargin() {
        if (this.mAeControlLayout != null) {
            LayoutParams lp = (LayoutParams) this.mAeControlLayout.getLayoutParams();
            if (lp != null) {
                lp.bottomMargin = getAEControlLayoutButtomMargin();
            }
            this.mAeControlLayout.setLayoutParams(lp);
        }
        updateManualFocusLayout();
    }

    private int getSizeCalculatedByPercentage(Context context, boolean isLongSide, float percentage) {
        float bottomMarginRatio;
        if (ModelProperties.isLongLCDModel()) {
            bottomMarginRatio = 0.0121f;
        } else {
            bottomMarginRatio = 0.0266f;
        }
        return RatioCalcUtil.getSizeCalculatedByPercentage(context, isLongSide, percentage) - RatioCalcUtil.getSizeCalculatedByPercentage(context, isLongSide, bottomMarginRatio);
    }

    private void initAEControlBarLayout() {
        if (this.mAeControlBar != null && this.mAeControlLayout != null) {
            LayoutParams layoutParam = (LayoutParams) this.mAeControlLayout.getLayoutParams();
            layoutParam.addRule(12, 1);
            layoutParam.addRule(14, 1);
            this.mAeControlLayout.setLayoutParams(layoutParam);
            LinearLayout.LayoutParams barParam = (LinearLayout.LayoutParams) this.mAeControlBar.getLayoutParams();
            barParam.width = Utils.getPx(getAppContext(), C0088R.dimen.ae_control_bar_width) + this.mAeControlBar.getCursorSize().getWidth();
            barParam.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0556f);
            int marginStartEnd = Utils.getPx(getAppContext(), C0088R.dimen.ae_control_bar_start_end_margin) - (this.mAeControlBar.getCursorSize().getWidth() / 2);
            barParam.setMarginStart(marginStartEnd);
            barParam.setMarginEnd(marginStartEnd);
            this.mAeControlBar.setLayoutParams(barParam);
        }
    }

    public void showAEControlBar(boolean initCusorPosition) {
        CamLog.m3d(CameraConstants.TAG, "- AE control - showAEControlBar, initCusorPosition = " + initCusorPosition);
        if (this.mAeControlLayout == null || !this.mGet.canUseAeControlBar()) {
            return;
        }
        if ((this.mGet.getFocusState() == 1 || this.mGet.getFocusState() == 3 || this.mGet.getFocusState() == 11) && !this.mGet.isGestureGuideShowing() && !this.mGet.isBarVisible(1) && this.mGet.checkModuleValidate(16)) {
            registerEVCallback(false, false);
            this.mGet.removePostRunnable(this.mResetEVRunnable);
            if (initCusorPosition) {
                this.mAeControlBar.initValue();
            }
            setState(1, true);
            if (this.mAeControlLayout.getVisibility() != 0) {
                this.mAeControlLayout.setVisibility(0);
                this.mGet.onShowAEBar();
            }
            if (!this.mGet.isManualFocusModeEx() && this.mGet.isAvailableManualFocus(false)) {
                setManualFocusButtonVisibility(true);
            }
        }
    }

    protected Rect adjustFocusRect(Rect focusRect) {
        int focusWidth = focusRect.width();
        int focusHeight = focusRect.height();
        if ((checkingArea(focusRect) & 1) != 0) {
            focusRect.left = 5;
            focusRect.right = focusRect.left + focusWidth;
        } else if ((checkingArea(focusRect) & 4) != 0) {
            focusRect.right = this.mLCDSize[1] - 5;
            focusRect.left = focusRect.right - focusWidth;
        }
        if ((checkingArea(focusRect) & 2) != 0) {
            focusRect.top = getFocusAreaRect(focusRect).top;
            focusRect.bottom = focusRect.top + focusHeight;
        } else if ((checkingArea(focusRect) & 8) != 0) {
            focusRect.bottom = getFocusAreaRect(focusRect).bottom;
            focusRect.top = focusRect.bottom - focusHeight;
        }
        return focusRect;
    }

    protected int checkingArea(Rect focusRect) {
        int retVal = 0;
        if (focusRect.left < 0) {
            retVal = 0 | 1;
        } else if (focusRect.right > this.mLCDSize[1]) {
            retVal = 0 | 4;
        }
        if (focusRect.top < getFocusAreaRect(focusRect).top) {
            return retVal | 2;
        }
        if (focusRect.bottom > getFocusAreaRect(focusRect).bottom) {
            return retVal | 8;
        }
        return retVal;
    }

    public Rect getFocusAreaRect(Rect focusRect) {
        Rect areaRect = new Rect(focusRect);
        int topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
        int bottomMargin = getShutterAreaWidth();
        if (this.mStartMargin > topMargin) {
            topMargin = this.mStartMargin;
        }
        int previewBottomMargin = (this.mLCDSize[0] - this.mStartMargin) - this.mPreviewHeightOnScreen;
        if (previewBottomMargin > bottomMargin) {
            bottomMargin = previewBottomMargin;
        }
        areaRect.top = topMargin;
        areaRect.bottom = this.mLCDSize[0] - bottomMargin;
        return areaRect;
    }

    public void registerEVCallback(boolean isSet, boolean postResetRunnable) {
        if (isAEControlBarEnableCondition() && this.mIsAECallbackSet != isSet) {
            if (!(isSet && (isFocusLock() || isAELock())) && isAeControlBarValueChanged()) {
                CamLog.m3d(CameraConstants.TAG, "- AE control - setEVCallbackDataListener, isSet = " + isSet + ", postResetRunnable = " + postResetRunnable);
                this.mGet.removePostRunnable(this.mResetEVRunnable);
                if (!isSet) {
                    this.mEVCallbackDataListener = null;
                } else if (this.mEVCallbackDataListener == null) {
                    this.mEVCallbackDataListener = new C07921();
                }
                CameraProxy cameraDevice = this.mGet.getCameraDevice();
                if (cameraDevice != null) {
                    cameraDevice.setEVCallbackDataListener(this.mEVCallbackDataListener);
                    this.mIsAECallbackSet = isSet;
                }
                if (isSet && postResetRunnable) {
                    resetEVValue(3000);
                }
            }
        }
    }

    protected void setEVParam(int value) {
        setEVParam(this.mEVParam, value);
    }

    protected void setEVParam(CameraParameters param, int value) {
        CameraParameters evParam = param;
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null && evParam != null) {
            CamLog.m3d(CameraConstants.TAG, "- AE control - setAEParam, value = " + value);
            evParam.setExposureCompensation(value);
            evParam.set(ParamConstants.KEY_JOG_ZOOM, this.mGet.getParamValue(ParamConstants.KEY_JOG_ZOOM));
            cameraDevice.setParameters(evParam);
            CameraManager cameraManager = CameraManagerFactory.getAndroidCameraManager(this.mGet.getActivity());
            if (cameraManager != null) {
                cameraManager.setParamToBackup(ParamConstants.KEY_EXPOSURE_COMPENSATION, Integer.valueOf(value));
            }
        }
    }

    public boolean isAEControlBarEnableCondition() {
        return this.mAeControlBar != null && this.mGet.canUseAeControlBar() && (!isTrackingState() || isFocusLock() || isAELock());
    }

    public void releaseAeControlBar() {
        this.mGet.runOnUiThread(this.releaseAeControlBarRunnable);
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mAeControlBar != null && this.mStartIcon != null && this.mEndIcon != null) {
            this.mAeControlBar.setDegree(degree);
            if (degree == 0 || degree == 270) {
                this.mStartIcon.setImageResource(C0088R.drawable.camera_focus_sun_minus);
                this.mEndIcon.setImageResource(C0088R.drawable.camera_focus_sun_plus);
            } else {
                this.mStartIcon.setImageResource(C0088R.drawable.camera_focus_sun_plus);
                this.mEndIcon.setImageResource(C0088R.drawable.camera_focus_sun_minus);
            }
            this.mStartIcon.setDegree(degree, true);
            this.mEndIcon.setDegree(degree, true);
            if (isAeControlBarShowing()) {
                showAEControlBar(false);
            }
        }
    }

    public boolean isTrackingState() {
        boolean isTrackingAFEnabled = false;
        if (this.mGet.isFocusTrackingSupported()) {
            isTrackingAFEnabled = "on".equals(this.mGet.getSettingValue("tracking-af"));
        }
        if ((this.mGet.isUHDmode() || this.mGet.isFHD60()) && !this.mGet.checkModuleValidate(128)) {
            return false;
        }
        return isTrackingAFEnabled;
    }

    public void resetEVValueDirectly(CameraParameters param) {
        this.mGet.removePostRunnable(this.mResetEVRunnable);
        if (this.mAEControlBarListener == null) {
            return;
        }
        if ((isAEControlBarEnableCondition() || isAeControlBarValueChanged()) && this.mAeControlBar.getCurrentValue() != this.mAeControlBar.getDefaultBarValue() && param != null) {
            CamLog.m3d(CameraConstants.TAG, "- AE control - resetEVValueDirectly");
            param.setExposureCompensation(0);
        }
    }

    public void resetEVValue(int delay) {
        resetEVValue(delay, true);
    }

    public void resetEVValue(int delay, boolean post) {
        if (this.mResetEVRunnable != null) {
            this.mGet.removePostRunnable(this.mResetEVRunnable);
            if (post) {
                this.mGet.postOnUiThread(this.mResetEVRunnable, (long) delay);
            } else {
                this.mResetEVRunnable.run();
            }
        }
    }

    public void onEVDataCallback() {
        CamLog.m3d(CameraConstants.TAG, "- AE control - onEVDataCallback");
        registerEVCallback(false, false);
        if (!this.mGet.isJogZoomMoving() && !this.mGet.isZoomControllerTouched()) {
            resetEVValue(0, true);
        }
    }

    public void hideFocus() {
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.hideFocus();
        }
        if (!this.mGet.isManualFocusModeEx()) {
            setManualFocusButtonVisibility(false);
        }
    }

    public void hideFocusForce() {
        if (this.mTouchFocusInterface != null) {
            this.mTouchFocusInterface.hideFocusForce();
        }
        setManualFocusButtonVisibility(false);
    }

    public void hideFocus(boolean hideOnlyAEBar) {
        if (hideOnlyAEBar) {
            releaseAeControlBar();
        } else {
            releaseTouchFocus();
        }
    }

    public int getCurrentValue() {
        if (this.mAeControlBar != null) {
            return this.mAeControlBar.getCurrentValue();
        }
        return 12;
    }

    public boolean isAeControlBarValueChanged() {
        if (this.mAeControlBar == null || this.mAeControlBar.getCurrentValue() == this.mAeControlBar.getDefaultBarValue()) {
            return false;
        }
        return true;
    }

    public void setFocusAreaWindow(int width, int height, int startMargin, int topMargin) {
        if (isFocusLock() || isAELock()) {
            int diff = startMargin - this.mStartMargin;
            if (diff != 0) {
                showAEAFText(this.mPrevX, this.mPrevY + diff);
            }
        }
        super.setFocusAreaWindow(width, height, startMargin, topMargin);
    }

    public void showAEAFText() {
        showAEAFText(this.mPrevX, this.mPrevY);
    }

    protected void showAEAFText(int x, int y) {
        if (this.mAFLockTextView != null) {
            if (isFocusLock() || isAELock()) {
                this.mPrevX = x;
                this.mPrevY = y;
                CamLog.m3d(CameraConstants.TAG, "showAEAFText in focus lock x = " + x + " y = " + y);
                this.mAFLockTextView.setVisibility(0);
                setTextPosition(x, y);
                if (this.mGet.getCameraId() == 0) {
                    this.mAFLockTextView.setText(this.mGet.getAppContext().getString(C0088R.string.ae_af_lock));
                    this.mAFLockTextView.setTextColor(-3584);
                } else {
                    this.mAFLockTextView.setText(this.mGet.getAppContext().getString(C0088R.string.ae_lock));
                    if (this.mGet.getCameraId() == 1) {
                        this.mAFLockTextView.setTextColor(-3584);
                    } else {
                        this.mAFLockTextView.setTextColor(-3584);
                    }
                }
                CamLog.m3d(CameraConstants.TAG, "mRectWidth = " + this.mRectWidth + " mRectHeight = " + this.mRectHeight);
                return;
            }
            this.mAFLockTextView.setVisibility(8);
        }
    }

    protected void setTextPosition(int x, int y) {
        int textWidth;
        LayoutParams rl = (LayoutParams) this.mAEAFLockLayout.getLayoutParams();
        int halfWidth = this.mRectWidth / 2;
        int halfHeight = this.mRectHeight / 2;
        int textHeight = (int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.ae_af_lock_txt_height);
        int textBelowMargin = (int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.ae_af_lock_txt_below_margin);
        Rect focusRect = adjustFocusRect(new Rect(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight));
        Rect checkRect = new Rect(focusRect);
        if (this.mGet.getCameraId() == 0) {
            textWidth = (int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.ae_af_lock_txt_width);
        } else {
            textWidth = (int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.ae_lock_txt_width);
        }
        int degree = this.mGet.getOrientationDegree();
        if (degree == 0) {
            rl.leftMargin = focusRect.left - ((textWidth - this.mRectWidth) / 2);
            checkRect.top = (focusRect.top - textHeight) - textBelowMargin;
            if ((checkingArea(checkRect) & 2) != 0) {
                rl.topMargin = (focusRect.top + textBelowMargin) + this.mRectHeight;
            } else {
                rl.topMargin = checkRect.top;
            }
        } else if (degree == 90) {
            rl.topMargin = focusRect.top - ((textWidth - this.mRectWidth) / 2);
            checkRect.left = (focusRect.left - textHeight) - textBelowMargin;
            if ((checkingArea(checkRect) & 1) != 0) {
                rl.leftMargin = (focusRect.left + textBelowMargin) + this.mRectWidth;
            } else {
                rl.leftMargin = checkRect.left;
            }
        } else if (degree == 180) {
            rl.leftMargin = focusRect.left - ((textWidth - this.mRectWidth) / 2);
            checkRect.bottom = (focusRect.bottom + textHeight) + textBelowMargin;
            if ((checkingArea(checkRect) & 8) != 0) {
                rl.topMargin = ((focusRect.bottom - textHeight) - textBelowMargin) - this.mRectHeight;
            } else {
                rl.topMargin = focusRect.bottom + textBelowMargin;
            }
        } else {
            rl.topMargin = focusRect.top - ((textWidth - this.mRectWidth) / 2);
            checkRect.right = (focusRect.right + textHeight) + textBelowMargin;
            if ((checkingArea(checkRect) & 4) != 0) {
                rl.leftMargin = ((focusRect.right - textHeight) - textBelowMargin) - this.mRectWidth;
            } else {
                rl.leftMargin = focusRect.right + textBelowMargin;
            }
        }
        this.mAEAFLockLayout.measure(0, 0);
        this.mAEAFLockLayout.setLayoutParams(rl);
    }

    public void setAEControlBarEnable(boolean enable) {
        if (this.mAeControlBar != null && this.mStartIcon != null && this.mEndIcon != null) {
            CamLog.m3d(CameraConstants.TAG, "-AE control- setEnable, enable = " + enable);
            this.mAeControlBar.setEnabled(enable);
            this.mStartIcon.setEnabled(enable);
            this.mEndIcon.setEnabled(enable);
            ColorFilter cf = enable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            this.mStartIcon.setColorFilter(cf);
            this.mEndIcon.setColorFilter(cf);
        }
    }

    public boolean isAeControlBarTouched() {
        return getState(2);
    }

    public boolean isAeControlBarShowing() {
        return getState(1);
    }
}
