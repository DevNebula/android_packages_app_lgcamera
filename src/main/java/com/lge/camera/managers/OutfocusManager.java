package com.lge.camera.managers;

import android.content.res.Configuration;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.OutfocusBar;
import com.lge.camera.components.OutfocusBar.OnOutfocusBarListener;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public abstract class OutfocusManager extends OutfocusInterface {
    private static final int OUTFOCUS_MAX_STATE_CHANGE = 3;
    private static final int OUTFOCUS_STATE_THRESHOLD = 250;
    protected TextView mBarText;
    protected RotateLayout mBarTextLayout;
    protected ModuleInterface mGet = null;
    protected RotateTextView mGuideTextView;
    protected RelativeLayout mGuideView;
    protected int mGuideViewLandTopMargin;
    protected int mGuideViewLandWidth;
    private HandlerRunnable mHideTextRunnable;
    private HandlerRunnable mHideToastRunnable;
    private boolean mIsApplyCallbackThreshold;
    private boolean mIsShowToast;
    protected OutfocusBar mOutfocusBar;
    protected View mOutfocusBarView;
    private long mOutfocusStateTime;
    protected View mOutfocusView;
    private int mStateChangeCount;

    /* renamed from: com.lge.camera.managers.OutfocusManager$2 */
    class C08212 implements OnOutfocusBarListener {
        C08212() {
        }

        public void onBarTouchDownAndMove(int value) {
            if (OutfocusManager.this.mGet.checkModuleValidate(15) && !OutfocusManager.this.mGet.isAnimationShowing()) {
                if (value != OutfocusManager.this.mBlurLevel) {
                    OutfocusManager.this.setBlurLevel(value);
                }
                OutfocusManager.this.mGet.pauseShutterless();
                OutfocusManager.this.setBarText();
                OutfocusManager.this.setBarTextVisibility(0);
                OutfocusManager.this.mGet.removePostRunnable(OutfocusManager.this.mHideTextRunnable);
            }
        }

        public void onBarTouchUp() {
            OutfocusManager.this.mGet.removePostRunnable(OutfocusManager.this.mHideTextRunnable);
            OutfocusManager.this.mGet.postOnUiThread(OutfocusManager.this.mHideTextRunnable, 1500);
        }
    }

    public OutfocusManager(ModuleInterface moduleInterface, int defaultBlurLevel) {
        super(moduleInterface, defaultBlurLevel);
        this.mIsApplyCallbackThreshold = FunctionProperties.getSupportedHal() == 2;
        this.mOutfocusStateTime = 0;
        this.mStateChangeCount = 0;
        this.mIsShowToast = false;
        this.mOutfocusView = null;
        this.mOutfocusBarView = null;
        this.mGuideView = null;
        this.mGuideTextView = null;
        this.mOutfocusBar = null;
        this.mBarTextLayout = null;
        this.mBarText = null;
        this.mGuideViewLandTopMargin = 0;
        this.mGuideViewLandWidth = 0;
        this.mHideTextRunnable = new HandlerRunnable(this) {
            public void handleRun() {
                OutfocusManager.this.setBarTextVisibility(8);
                OutfocusManager.this.mGet.resumeShutterless();
            }
        };
        this.mHideToastRunnable = new HandlerRunnable(this) {
            public void handleRun() {
                OutfocusManager.this.mIsShowToast = false;
                OutfocusManager.this.setUIVisibility(0);
            }
        };
        this.mGet = moduleInterface;
    }

    public void init() {
        super.init();
        setupView();
    }

    private void setupView() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (vg != null && this.mOutfocusView == null) {
            this.mOutfocusView = this.mGet.inflateView(C0088R.layout.outfocus);
            vg.addView(this.mOutfocusView, 0, new LayoutParams(-1, -1));
            boolean isNotch = ModelProperties.getLCDType() == 2;
            this.mOutfocusBarView = this.mOutfocusView.findViewById(C0088R.id.outfocus_bar_total_layout);
            this.mGuideView = (RelativeLayout) this.mOutfocusView.findViewById(C0088R.id.outfocus_screen_text_layout);
            this.mGuideTextView = (RotateTextView) this.mOutfocusView.findViewById(C0088R.id.outfocus_screen_text);
            LayoutParams param = (LayoutParams) this.mOutfocusBarView.getLayoutParams();
            param.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, isNotch ? 0.315f : 0.289f) - RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.02f);
            this.mOutfocusBarView.setLayoutParams(param);
            int height = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.12f);
            RotateImageView minus = (RotateImageView) this.mOutfocusView.findViewById(C0088R.id.outfocus_minus_image);
            minus.setDegree(this.mGet.getOrientationDegree());
            LinearLayout.LayoutParams minusParams = (LinearLayout.LayoutParams) minus.getLayoutParams();
            minusParams.height = height;
            minus.setLayoutParams(minusParams);
            RotateImageView plus = (RotateImageView) this.mOutfocusView.findViewById(C0088R.id.outfocus_plus_image);
            plus.setDegree(this.mGet.getOrientationDegree());
            LinearLayout.LayoutParams plusParams = (LinearLayout.LayoutParams) plus.getLayoutParams();
            plusParams.height = height;
            plus.setLayoutParams(plusParams);
            this.mOutfocusBar = (OutfocusBar) this.mOutfocusView.findViewById(C0088R.id.outfocus_bar);
            LinearLayout.LayoutParams barParam = (LinearLayout.LayoutParams) this.mOutfocusBar.getLayoutParams();
            barParam.width = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.5f);
            barParam.height = height;
            this.mOutfocusBar.setLayoutParams(barParam);
            setBarListener();
            this.mBarTextLayout = (RotateLayout) this.mOutfocusView.findViewById(C0088R.id.outfocus_bar_text_layout);
            LinearLayout.LayoutParams barTextParam = (LinearLayout.LayoutParams) this.mBarTextLayout.getLayoutParams();
            barTextParam.bottomMargin = -RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.02f);
            this.mBarTextLayout.setLayoutParams(barTextParam);
            this.mBarText = (TextView) this.mOutfocusView.findViewById(C0088R.id.outfocus_bar_text);
            this.mBarText.setTextSize(0, (float) RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.05f));
        }
    }

    private void setBarListener() {
        this.mOutfocusBar.init(0, 10, getBlurLevel());
        this.mOutfocusBar.setListener(new C08212());
    }

    private void releaseView() {
        this.mOutfocusBarView = null;
        this.mGuideView = null;
        this.mGuideTextView = null;
        if (this.mOutfocusBar != null) {
            this.mOutfocusBar.unbind();
            this.mOutfocusBar.setListener(null);
            this.mOutfocusBar = null;
        }
        this.mBarTextLayout = null;
        this.mBarText = null;
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (vg != null && this.mOutfocusView != null) {
            vg.removeView(this.mOutfocusView);
            this.mOutfocusView = null;
        }
    }

    public void showGuideText(boolean isShow, String msg) {
        if (msg == null || !isShow) {
            setGuideVisibility(8);
            setBarVisibility(0);
            return;
        }
        if (this.mGuideView != null) {
            this.mGuideTextView.setText(msg);
        }
        setGuideVisibility(0);
        setBarVisibility(8);
    }

    public void setUIVisibility(int visibility) {
        if (this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || this.mGet.isOpeningSettingMenu() || this.mGet.isAeControlBarShowing() || this.mIsShowToast) {
            visibility = 8;
        }
        if (this.mOutfocusView != null) {
            this.mOutfocusView.setVisibility(visibility);
        }
    }

    private void setBarVisibility(int visibility) {
        if (this.mOutfocusBarView != null) {
            this.mOutfocusBarView.setVisibility(visibility);
        }
    }

    private void setGuideVisibility(int visibility) {
        if (this.mGuideView != null) {
            this.mGuideView.setVisibility(visibility);
        }
    }

    public void setBarEnable(boolean enabled) {
        if (this.mOutfocusBarView != null) {
            this.mOutfocusBarView.setEnabled(enabled);
            this.mOutfocusBarView.setAlpha(enabled ? 1.0f : 0.35f);
        }
        if (this.mOutfocusBar != null) {
            this.mOutfocusBar.setEnabled(enabled);
        }
        if (!enabled) {
            setBarTextVisibility(8);
        }
    }

    public void setBarTextVisibility(int visibility) {
        if (this.mBarTextLayout != null) {
            this.mBarTextLayout.setVisibility(visibility);
        }
    }

    public boolean isBarTextVisible() {
        if (this.mBarTextLayout != null && this.mBarTextLayout.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    protected void setBarText() {
        if (this.mOutfocusBar != null) {
            boolean isPort;
            this.mBarTextLayout.setAngle(this.mGet.getOrientationDegree());
            String level = "" + getBlurLevel();
            this.mBarText.setText(level);
            int degree = this.mGet.getOrientationDegree();
            if (degree == 0 || degree == 180) {
                isPort = true;
            } else {
                isPort = false;
            }
            int textWidth = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, isPort ? 0.0277f : 0.0673f);
            if (isPort) {
                textWidth *= level.length();
            }
            int width = this.mOutfocusBar.getWidth();
            int pos = this.mOutfocusBar.getScrollPosition();
            switch (degree) {
                case 90:
                case 180:
                    pos = width - pos;
                    break;
            }
            int buttonWidth = this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_preview_bar_minus).getIntrinsicWidth();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.mBarTextLayout.getLayoutParams();
            params.setMarginStart(Math.max(Math.min((buttonWidth + pos) - (textWidth / 2), buttonWidth + width), buttonWidth));
            this.mBarTextLayout.setLayoutParams(params);
        }
    }

    public boolean isOutfocusBarTouched() {
        if (this.mOutfocusBar == null) {
            return false;
        }
        return this.mOutfocusBar.isTouched();
    }

    public void onCameraSwitchingStart() {
        setUIVisibility(8);
    }

    public void onPauseAfter() {
        showGuideText(false, null);
        setUIVisibility(8);
        super.onPauseAfter();
    }

    public void onDestroy() {
        this.mBlurLevel = this.mDefaultBlurLevel;
        releaseView();
        super.onDestroy();
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mOutfocusView != null) {
            setBarTextVisibility(8);
            if (this.mBarTextLayout != null) {
                this.mBarTextLayout.setAngle(degree);
            }
            ((RotateImageView) this.mOutfocusView.findViewById(C0088R.id.outfocus_minus_image)).setDegree(degree);
            ((RotateImageView) this.mOutfocusView.findViewById(C0088R.id.outfocus_plus_image)).setDegree(degree);
            ((RotateLayout) this.mOutfocusView.findViewById(C0088R.id.outfocus_screen_text_rotate)).setAngle(degree);
            setGuideLayoutParam();
            View barView = this.mOutfocusView.findViewById(C0088R.id.outfocus_bar_layout);
            switch (degree) {
                case 90:
                case 180:
                    barView.setRotation(180.0f);
                    break;
                default:
                    barView.setRotation(0.0f);
                    break;
            }
        }
        super.setDegree(degree, animation);
    }

    public void onConfigurationChanged(Configuration config) {
        releaseView();
        setupView();
        setUIVisibility(0);
        super.onConfigurationChanged(config);
    }

    private void setGuideLayoutParam() {
        if (this.mGuideView != null) {
            boolean isNotch;
            if (ModelProperties.getLCDType() == 2) {
                isNotch = true;
            } else {
                isNotch = false;
            }
            LayoutParams params = (LayoutParams) this.mGuideView.getLayoutParams();
            Utils.resetLayoutParameter(params);
            switch (this.mGet.getOrientationDegree()) {
                case 0:
                case 180:
                    params.width = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.8f);
                    params.height = -2;
                    params.addRule(12);
                    params.addRule(14);
                    params.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, isNotch ? 0.315f : 0.289f);
                    this.mGuideView.setHorizontalGravity(17);
                    this.mGuideView.setVerticalGravity(0);
                    break;
                case 90:
                case 270:
                    params.width = -1;
                    params.height = this.mGuideViewLandWidth;
                    params.addRule(20);
                    params.addRule(10);
                    params.addRule(15);
                    params.setMargins(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.023f), this.mGuideViewLandTopMargin, 0, 0);
                    this.mGuideView.setHorizontalGravity(0);
                    this.mGuideView.setVerticalGravity(17);
                    break;
            }
            this.mGuideView.setLayoutParams(params);
            if (this.mGuideView.getVisibility() == 0) {
                this.mGuideTextView.setText(this.mGuideTextView.getText());
            }
        }
    }

    public int getOutfocusErrorHeight() {
        if (this.mGuideView == null || this.mGuideView.getVisibility() != 0) {
            return 0;
        }
        RotateLayout guideTextRoateLayout = (RotateLayout) this.mOutfocusView.findViewById(C0088R.id.outfocus_screen_text_rotate);
        if (guideTextRoateLayout == null) {
            return 0;
        }
        switch (this.mGet.getOrientationDegree()) {
            case 90:
            case 270:
                return guideTextRoateLayout.getWidth();
            default:
                return guideTextRoateLayout.getHeight();
        }
    }

    public void setLayoutParam(LayoutParams params) {
        this.mGuideViewLandTopMargin = params.topMargin;
        this.mGuideViewLandWidth = RatioCalcUtil.getPreviewTextureRect().height();
        setGuideLayoutParam();
    }

    public boolean isUpdateNewState(int newState, int oldState) {
        if (newState == oldState) {
            this.mStateChangeCount = 0;
            return false;
        } else if (!this.mIsApplyCallbackThreshold) {
            return true;
        } else {
            if (this.mStateChangeCount < 3) {
                CamLog.m7i(CameraConstants.TAG, "Drop Result not enough change, new state:" + newState + " change count:" + this.mStateChangeCount);
                this.mStateChangeCount++;
                return false;
            }
            this.mStateChangeCount = 0;
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - this.mOutfocusStateTime < 250) {
                CamLog.m7i(CameraConstants.TAG, " Drop Result new error type" + newState + " time diff " + (currentTime - this.mOutfocusStateTime));
                return false;
            }
            this.mOutfocusStateTime = currentTime;
            return true;
        }
    }

    public void showToast(final long hideDelayMillis) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                OutfocusManager.this.setUIVisibility(4);
                OutfocusManager.this.mGet.removePostRunnable(OutfocusManager.this.mHideToastRunnable);
                OutfocusManager.this.mGet.postOnUiThread(OutfocusManager.this.mHideToastRunnable, hideDelayMillis + 30);
                OutfocusManager.this.mIsShowToast = true;
            }
        });
    }

    public void hideToast() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                OutfocusManager.this.mGet.removePostRunnable(OutfocusManager.this.mHideToastRunnable);
                OutfocusManager.this.mIsShowToast = false;
                OutfocusManager.this.setUIVisibility(0);
            }
        });
    }
}
