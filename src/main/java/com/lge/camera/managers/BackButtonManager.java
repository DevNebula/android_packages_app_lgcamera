package com.lge.camera.managers;

import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;

public class BackButtonManager extends ManagerInterfaceImpl {
    private BackButtonListener mBackButtonInterface = null;
    private View mBackButtonView = null;

    public interface BackButtonListener {
        boolean doBackKey();
    }

    /* renamed from: com.lge.camera.managers.BackButtonManager$1 */
    class C08301 implements OnClickListener {
        C08301() {
        }

        public void onClick(View arg0) {
            BackButtonManager.this.doBackButtonEvent(true);
        }
    }

    /* renamed from: com.lge.camera.managers.BackButtonManager$2 */
    class C08312 implements OnTouchListener {
        C08312() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 0) {
                BackButtonManager.this.setBlurredBitmapToCover();
                if (!BackButtonManager.this.mGet.checkModuleValidate(1)) {
                    v.performHapticFeedback(1);
                }
            }
            return false;
        }
    }

    public BackButtonManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        if (!ModelProperties.isNavigationBarShowingModel()) {
            this.mBackButtonView = this.mGet.findViewById(C0088R.id.back_button_layout);
            if (this.mBackButtonView != null) {
                if (ModelProperties.isKeyPadSupported(getAppContext())) {
                    this.mBackButtonView.setFocusable(true);
                }
                this.mBackButtonView.setVisibility(4);
            }
            initBackButton();
        }
    }

    public void setBackButtonListener(BackButtonListener listener) {
        this.mBackButtonInterface = listener;
    }

    public void onConfigurationChanged(Configuration config) {
        initBackButton();
        super.onConfigurationChanged(config);
    }

    public void onResumeAfter() {
        regitsterListener();
        super.onResumeAfter();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        unRegisterListener();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mBackButtonView = null;
        this.mBackButtonInterface = null;
    }

    private void initBackButton() {
        setupView();
        regitsterListener();
        setBackButton(false);
        setRotateDegree(getOrientationDegree(), false);
    }

    public void setBackButton(boolean isCleanView) {
        if (this.mBackButtonView != null) {
            View normalBackBtn = this.mBackButtonView.findViewById(C0088R.id.back_button);
            if (normalBackBtn != null) {
                normalBackBtn.setBackgroundResource(C0088R.drawable.btn_quickbutton_backkey);
            }
        }
    }

    private void setupView() {
        if (this.mBackButtonView != null) {
            this.mBackButtonView.setVisibility(0);
            setBackButtonLayout();
        }
    }

    public void setBackButtonLayout() {
        if (this.mBackButtonView != null) {
            View normalBackBtn = this.mBackButtonView.findViewById(C0088R.id.back_button);
            LayoutParams params = (LayoutParams) this.mBackButtonView.getLayoutParams();
            LayoutParams normalBackBtnParams = (LayoutParams) normalBackBtn.getLayoutParams();
            if (params != null && normalBackBtnParams != null) {
                boolean isCinemaSize;
                Utils.resetLayoutParameter(params);
                if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && ManualUtil.isCinemaSize(getAppContext(), this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
                    isCinemaSize = true;
                } else {
                    isCinemaSize = false;
                }
                int margin = Utils.getPx(getAppContext(), C0088R.dimen.extra_button_marginTop);
                if (ModelProperties.isTablet(getAppContext())) {
                    margin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.066f);
                } else if (isCinemaSize) {
                    if (ModelProperties.isLongLCDModel()) {
                        margin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.167f);
                    } else {
                        margin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.152f);
                    }
                }
                if (Utils.isConfigureLandscape(this.mGet.getAppContext().getResources())) {
                    params.addRule(12);
                    params.addRule(20, 0);
                    params.addRule(21);
                    params.setMarginStart(0);
                    params.bottomMargin = margin;
                    params.setMarginEnd(RatioCalcUtil.getCommandBottomMargin(getAppContext()));
                } else {
                    params.addRule(12);
                    params.addRule(20);
                    params.addRule(21, 0);
                    params.setMarginStart(margin);
                    params.setMarginEnd(0);
                    params.bottomMargin = RatioCalcUtil.getCommandBottomMargin(getAppContext());
                }
                this.mBackButtonView.setLayoutParams(params);
                normalBackBtn.setLayoutParams(normalBackBtnParams);
            }
        }
    }

    private void regitsterListener() {
        if (this.mBackButtonView != null) {
            this.mBackButtonView.setOnClickListener(new C08301());
            this.mBackButtonView.setOnTouchListener(new C08312());
        }
    }

    private void setBlurredBitmapToCover() {
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        if (this.mGet.getPreviewCoverVisibility() != 0) {
            this.mGet.getCurPreviewBlurredBitmap(lcdSize[1] / 10, lcdSize[0] / 10, 10, false, true);
        }
    }

    public void doBackButtonEvent(final boolean checkSystemUINotVisible) {
        CamLog.m3d(CameraConstants.TAG, "doBackButtonEvent checkSystemUINotVisible = " + checkSystemUINotVisible);
        if (!this.mGet.checkModuleValidate(1) || (checkSystemUINotVisible && SystemBarUtil.isSystemUIVisible(getActivity()))) {
            CamLog.m3d(CameraConstants.TAG, "return");
        } else {
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (BackButtonManager.this.mBackButtonInterface != null && !BackButtonManager.this.mBackButtonInterface.doBackKey()) {
                        CamLog.m3d(CameraConstants.TAG, "finish by backKey");
                        if (!BackButtonManager.this.isScreenPinningState()) {
                            if (!checkSystemUINotVisible) {
                                BackButtonManager.this.setBlurredBitmapToCover();
                            }
                            BackButtonManager.this.mGet.removeSpliceDimColor();
                            BackButtonManager.this.mGet.setPreviewCoverVisibility(0, false, null, true, true);
                        }
                        BackButtonManager.this.mGet.getActivity().finish();
                    }
                }
            }, 0);
        }
    }

    private void unRegisterListener() {
        if (this.mBackButtonView != null) {
            this.mBackButtonView.setOnClickListener(null);
            this.mBackButtonView.setOnLongClickListener(null);
            this.mBackButtonView.setOnTouchListener(null);
        }
    }

    public void hide() {
        if (this.mBackButtonView != null) {
            this.mBackButtonView.setVisibility(4);
        }
    }

    public void show() {
        if (this.mBackButtonView != null) {
            this.mBackButtonView.setVisibility(0);
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mBackButtonView != null && !this.mGet.isActivatedQuickview()) {
            ((RotateImageButton) this.mBackButtonView.findViewById(C0088R.id.back_button)).setDegree(degree, animation);
        }
    }

    public void setButtonDimByNaviBar(boolean enable) {
        if (this.mBackButtonView != null) {
            this.mBackButtonView.setEnabled(enable);
        }
    }
}
