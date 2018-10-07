package com.lge.camera.managers;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.FlashControlBar;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;

public class FlashControlManager extends ManagerInterfaceImpl {
    protected static final int FRONT_FLASH_LEVEL0 = 5;
    protected static final int FRONT_FLASH_LEVEL1 = 8;
    protected static final int FRONT_FLASH_LEVEL2 = 11;
    protected static final int FRONT_FLASH_LEVEL3 = 15;
    protected static final int OVER_BOTTOM = 8;
    protected static final int OVER_LEFT = 1;
    protected static final int OVER_RIGHT = 4;
    protected static final int OVER_TOP = 2;
    private final float BAR_ADJUST_BOTTOM_MARGIN = 0.0121f;
    private final float BAR_HEIGHT = 0.361f;
    protected RotateImageView mEndIcon;
    protected View mFlashControl = null;
    protected FlashControlBar mFlashControlBar;
    private AEControlBarInterface mFlashControlBarListener;
    protected LinearLayout mFlashControlLayout = null;
    protected int mFlashStep = 1;
    private boolean mIsFlashLevelControlBarVisible = false;
    protected boolean mIsPressed = false;
    protected int[] mLCDSize;
    protected int mPrevFlashControlX = 0;
    protected int mPrevFlashControlY = 0;
    protected int mPrevX = 0;
    protected int mPrevY = 0;
    protected RotateImageView mStartIcon;
    protected RotateTextView mText;

    /* renamed from: com.lge.camera.managers.FlashControlManager$1 */
    class C09041 implements AEControlBarInterface {
        C09041() {
        }

        public void onBarValueChanged(int value) {
            FlashControlManager.this.setFlashLevel(value);
        }

        public void onBarUp() {
            FlashControlManager.this.mIsPressed = false;
        }

        public void onBarDown() {
            FlashControlManager.this.mIsPressed = true;
        }
    }

    public FlashControlManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        this.mFlashStep = SharedPreferenceUtilBase.getFrontFlashStep(this.mGet.getAppContext());
        createFlashControlBar();
        setFlashBarEnabled(true);
        if (this.mFlashControlBar != null) {
            this.mFlashControlBarListener = new C09041();
            this.mFlashControlBar.setOnAEControlBarListener(this.mFlashControlBarListener);
        }
        this.mLCDSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mGet == null || this.mGet.isRearCamera() || !this.mGet.isFlashSupported() || !ParamConstants.FLASH_MODE_TORCH.equals(this.mGet.getParamValue("flash-mode"))) {
            showAndHideFlashControlBar(false);
        } else {
            showAndHideFlashControlBar(true);
        }
    }

    protected void createFlashControlBar() {
        if (this.mGet != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
            this.mFlashControl = this.mGet.inflateView(C0088R.layout.flash_control_bar);
            if (this.mFlashControl != null) {
                if (vg != null) {
                    vg.addView(this.mFlashControl);
                    this.mFlashControlLayout = (LinearLayout) this.mFlashControl.findViewById(C0088R.id.flash_control_bar_wrapper);
                    this.mFlashControlBar = (FlashControlBar) this.mFlashControl.findViewById(C0088R.id.flash_control_bar);
                    this.mFlashControlBar.init(this.mGet, 0, 3, this.mFlashStep);
                    this.mText = (RotateTextView) this.mFlashControl.findViewById(C0088R.id.flash_control_text);
                }
                initFlashControlBarLayout();
            }
        }
    }

    private void initFlashControlBarLayout() {
        if (this.mFlashControlBar != null && this.mFlashControlLayout != null) {
            LayoutParams layoutParam = (LayoutParams) this.mFlashControlLayout.getLayoutParams();
            layoutParam.addRule(12, 1);
            layoutParam.addRule(14, 1);
            layoutParam.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.361f);
            this.mFlashControlLayout.setLayoutParams(layoutParam);
            LinearLayout.LayoutParams barParam = (LinearLayout.LayoutParams) this.mFlashControlBar.getLayoutParams();
            barParam.width = Utils.getPx(getAppContext(), C0088R.dimen.flash_control_bar_width) + this.mFlashControlBar.getCursorSize().getWidth();
            barParam.height = this.mFlashControlBar.getCursorSize().getHeight();
            this.mFlashControlBar.setLayoutParams(barParam);
            if ("on".equals(this.mGet.getSettingValue("flash-mode"))) {
                this.mIsFlashLevelControlBarVisible = true;
                this.mFlashControlLayout.setVisibility(0);
                return;
            }
            this.mIsFlashLevelControlBarVisible = false;
            this.mFlashControlLayout.setVisibility(8);
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mText != null) {
            this.mText.setDegree(degree, animation);
        }
    }

    public int getCurrentValue() {
        if (this.mFlashControlBar != null) {
            return this.mFlashControlBar.getCurrentValue();
        }
        return 1;
    }

    public void showAndHideFlashControlBar(boolean show) {
        this.mIsFlashLevelControlBarVisible = show;
        if (show) {
            this.mFlashControlLayout.setVisibility(0);
            return;
        }
        this.mFlashControlLayout.setVisibility(8);
        this.mIsPressed = false;
    }

    public boolean isFlashControlBarShowing() {
        return this.mIsFlashLevelControlBarVisible;
    }

    public void setFlashBarEnabled(boolean enabled) {
        if (this.mFlashControlBar != null) {
            this.mFlashControlBar.setAlpha(enabled ? 1.0f : 0.35f);
        }
    }

    public boolean isFlashBarPressed() {
        return this.mIsPressed;
    }

    public static int getLevelFromStep(int cursorStep) {
        if (cursorStep == 0) {
            return 5;
        }
        if (cursorStep == 1) {
            return 8;
        }
        if (cursorStep == 2) {
            return 11;
        }
        return 15;
    }

    public int getStepFromLevel(int val) {
        if (val == 5) {
            return 0;
        }
        if (val == 8) {
            return 1;
        }
        if (val == 11) {
            return 2;
        }
        return 3;
    }

    public void setFlashLevel(int val) {
        this.mFlashStep = val;
        int valTobe = getLevelFromStep(this.mFlashStep);
        if (this.mGet != null && this.mIsFlashLevelControlBarVisible) {
            CameraProxy mCamDevice = this.mGet.getCameraDevice();
            if (mCamDevice != null) {
                CameraParameters param = mCamDevice.getParameters();
                if (param != null) {
                    CamLog.m3d(CameraConstants.TAG, "val = " + val + " valTobe = " + valTobe);
                    param.set("flash-mode", "off");
                    mCamDevice.setParameters(param);
                    this.mGet.setParamUpdater(param, ParamConstants.KEY_FLASH_LEVEL, Integer.toString(valTobe));
                    mCamDevice.setParameters(param);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.mGet.setParamUpdater(param, "flash-mode", ParamConstants.FLASH_MODE_TORCH);
                    mCamDevice.setParameters(param);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        showAndHideFlashControlBar(false);
        SharedPreferenceUtilBase.saveFrontFlashStep(this.mGet.getAppContext(), this.mFlashStep);
    }
}
