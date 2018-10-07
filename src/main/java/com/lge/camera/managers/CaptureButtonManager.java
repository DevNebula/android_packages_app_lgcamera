package com.lge.camera.managers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable2.AnimationCallback;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.ShutterButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class CaptureButtonManager extends CaptureButtonManagerBase {
    private int DUR_REC = 400;
    private int mExtraButtonBottomMode = 0;
    private int mExtraButtonTopMode = 0;
    private int mMoveXLast = 0;
    private int sCustomTouchSlop = Math.round(Utils.dpToPx(getAppContext(), 6.5f));

    /* renamed from: com.lge.camera.managers.CaptureButtonManager$1 */
    class C08481 implements OnFocusChangeListener {
        C08481() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                CaptureButtonManager.this.requestShutterButtonFocus(true, 2);
            }
        }
    }

    class CaptureAnimationViewInfo {
        public int mBottomBackgroundResId;
        public int mBottomButtonMoveDistance;
        public int mBottomImgViewResId;
        public float mEndAlpha;
        public int mExtraGapMargin;
        public float mStartAlpha;
        public int mTopBackgroundResId;
        public int mTopButtonMoveDistance;
        public int mTopImgViewResId;

        public CaptureAnimationViewInfo(int topImg, int topBg, int bottomImg, int bottomBg, int topDistance, int bottomDistance, int extraGap) {
            this.mTopImgViewResId = topImg;
            this.mTopBackgroundResId = topBg;
            this.mBottomImgViewResId = bottomImg;
            this.mBottomBackgroundResId = bottomBg;
            this.mTopButtonMoveDistance = topDistance;
            this.mBottomButtonMoveDistance = bottomDistance;
            this.mExtraGapMargin = extraGap;
        }

        public void setAlpha(float start, float end) {
            this.mStartAlpha = start;
            this.mEndAlpha = end;
        }
    }

    public CaptureButtonManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onPauseAfter() {
        this.mIsWorkingShutterAnimation = false;
        setExtraButtonLayout();
        super.onPauseAfter();
    }

    public void changeExtraButton(int extraMode, int type) {
        setExtraButtonMode(extraMode, type);
        setExtraButtonLayout(extraMode, type);
    }

    public void setExtraButtonLayout() {
        setExtraButtonLayout(0, 1);
    }

    public void setExtraButtonLayout(int extraMode, int type) {
        ImageButton extraButtonImg = getExtraButtonComp(type);
        if (extraButtonImg != null) {
            boolean isManualVideoMode = CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && ManualUtil.isCinemaSize(getAppContext(), this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE));
            int marginEnd = Utils.getPx(getAppContext(), C0088R.dimen.extra_button_marginTop);
            if (ModelProperties.isTablet(getAppContext())) {
                marginEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.07f);
            } else if (extraMode == 6 && isManualVideoMode) {
                marginEnd = ModelProperties.isLongLCDModel() ? RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.167f) : RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.152f);
            }
            LayoutParams params = (LayoutParams) extraButtonImg.getLayoutParams();
            params.setMarginEnd(marginEnd);
            extraButtonImg.setLayoutParams(params);
            if (extraMode != 0) {
                extraButtonImg.setBackgroundResource(getIdFromMode(extraMode));
                extraButtonImg.setContentDescription(getContentDescription(extraMode));
                extraButtonImg.setEnabled(true);
                extraButtonImg.setFocusable(ModelProperties.isKeyPadSupported(getAppContext()));
                if (extraButtonImg.getVisibility() != 0) {
                    Animation scaleAni = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 0.5f, 1, 0.5f);
                    scaleAni.setDuration((long) this.DUR_REC);
                    scaleAni.setInterpolator(new DecelerateInterpolator());
                    extraButtonImg.startAnimation(scaleAni);
                }
                extraButtonImg.setVisibility(0);
                return;
            }
            extraButtonImg.clearAnimation();
            extraButtonImg.setFocusable(false);
            extraButtonImg.setVisibility(8);
        }
    }

    public int getShutterButtonMode(int type) {
        switch (type) {
            case 2:
                return this.mShutterBottomMode;
            case 4:
                return this.mShutterLargeMode;
            default:
                return this.mShutterTopMode;
        }
    }

    public int getExtraButtonMode(int type) {
        switch (type) {
            case 2:
                return this.mExtraButtonBottomMode;
            default:
                return this.mExtraButtonTopMode;
        }
    }

    public void setExtraButtonMode(int toBeChangeMode, int type) {
        switch (type) {
            case 2:
                this.mExtraButtonBottomMode = toBeChangeMode;
                return;
            default:
                this.mExtraButtonTopMode = toBeChangeMode;
                return;
        }
    }

    public void onConfigurationChanged(Configuration config) {
        setUpAllViews(false);
        setRotateDegree(getOrientationDegree(), false);
        refreshButtonByCurrentMode();
        super.onConfigurationChanged(config);
        changeShutterZoomUI(this.mCurrentMode, this.mGet.getShutterButtonType());
    }

    private void setShutterButtonEnableByInitValue(int initValue, int type) {
        boolean z = true;
        if (!(initValue == 0 || initValue == 1)) {
            z = false;
        }
        setShutterButtonEnable(z, type);
    }

    public void setShutterButtonEnable(boolean enable, int type) {
        boolean z = true;
        CamLog.m3d(CameraConstants.TAG, "setShutterButtonEnable : enanble : " + enable + ", type : " + type);
        if (this.mGet.checkModuleValidate(8)) {
            boolean focusable = ModelProperties.isKeyPadSupported(getAppContext());
            ImageView shutterType;
            if (type == 4) {
                ImageButton shutterBtn = getShutterLargeButtonComp();
                shutterType = (ImageView) getShutterLargeButtonView();
                if (shutterBtn != null && shutterType != null) {
                    shutterBtn.setEnabled(enable);
                    shutterType.setEnabled(enable);
                    shutterBtn.setFocusable(focusable);
                    shutterType.setFocusable(focusable);
                    shutterBtn.setNextFocusDownId(shutterBtn.getId());
                    return;
                }
                return;
            }
            ShutterButton shutterBtn2;
            if ((type & 1) != 0) {
                shutterBtn2 = getShutterButtonComp(1);
                shutterType = (ImageView) getShutterButtonView(1);
                if (!(shutterBtn2 == null || shutterType == null)) {
                    shutterBtn2.setEnabled(enable);
                    shutterType.setEnabled(enable);
                    shutterBtn2.setFocusable(focusable);
                    shutterType.setFocusable(focusable);
                    shutterBtn2.setNextFocusDownId(shutterBtn2.getId());
                }
            }
            if ((type & 2) != 0) {
                shutterBtn2 = getShutterButtonComp(2);
                shutterType = (ImageView) getShutterButtonView(2);
                if (shutterBtn2 != null && shutterType != null) {
                    shutterBtn2.setEnabled(enable);
                    shutterType.setEnabled(enable);
                    shutterBtn2.setFocusable(focusable);
                    shutterType.setFocusable(focusable);
                    shutterBtn2.setNextFocusDownId(shutterBtn2.getId());
                    if (enable) {
                        z = false;
                    }
                    holdFocusOnDummyView(z);
                    return;
                }
                return;
            }
            return;
        }
        if ((type & 1) != 0) {
            this.mInitEnabledShutterTop = enable ? 1 : -1;
        }
        if ((type & 2) != 0) {
            int i;
            if (enable) {
                i = 1;
            } else {
                i = -1;
            }
            this.mInitEnabledShutterBottom = i;
        }
        if ((type & 4) != 0) {
            int i2;
            if (!enable) {
                i2 = -1;
            }
            this.mInitEnabledShutterLarge = i2;
        }
        CamLog.m3d(CameraConstants.TAG, "Exit camera is changing enable = " + enable + "type=" + type);
    }

    private void requestShutterButtonFocus(boolean focused, int type) {
        requestShutterButtonFocus(focused, type, false);
    }

    public void requestShutterButtonFocus(boolean focused, int type, boolean isInTouchMode) {
        int buttonType;
        if ((type & 1) != 0) {
            buttonType = 1;
        } else {
            buttonType = 2;
        }
        boolean focusable = ModelProperties.isKeyPadSupported(getAppContext());
        ShutterButton shutterButtonComp = getShutterButtonComp(buttonType);
        if (shutterButtonComp != null) {
            shutterButtonComp.setFocusable(focusable);
            if (this.mGet.isRotateDialogVisible()) {
                shutterButtonComp.clearFocus();
                return;
            } else if (focusable && focused) {
                if (isInTouchMode) {
                    shutterButtonComp.requestFocusFromTouch();
                } else {
                    shutterButtonComp.requestFocus();
                }
            }
        }
        View shutterButtonView = getShutterButtonView(2);
        if (shutterButtonView != null) {
            shutterButtonView.setOnFocusChangeListener(new C08481());
        }
    }

    public void setShutterButtonPressed(boolean pressed, int type) {
        ImageView shutterType;
        if (type == 4) {
            ImageButton shutterBtn = getShutterLargeButtonComp();
            shutterType = (ImageView) getShutterLargeButtonView();
            if (shutterType != null && shutterBtn != null) {
                shutterType.setPressed(pressed);
                shutterBtn.setPressed(pressed);
                return;
            }
            return;
        }
        ShutterButton shutterBtn2;
        if ((type & 1) != 0) {
            shutterBtn2 = getShutterButtonComp(1);
            shutterType = (ImageView) getShutterButtonView(1);
            if (!(shutterType == null || shutterBtn2 == null)) {
                shutterType.setPressed(pressed);
                shutterBtn2.setPressed(pressed);
            }
        }
        if ((type & 2) != 0) {
            shutterBtn2 = getShutterButtonComp(2);
            shutterType = (ImageView) getShutterButtonView(2);
            if (shutterType != null && shutterBtn2 != null) {
                shutterType.setPressed(pressed);
                shutterBtn2.setPressed(pressed);
            }
        }
    }

    public boolean isShutterButtonPressed(int type) {
        if (type == 4) {
            ImageButton shutterBtn = getShutterLargeButtonComp();
            if (!(((ImageView) getShutterLargeButtonView()) == null || shutterBtn == null)) {
                return shutterBtn.isPressed();
            }
        }
        ShutterButton shutterBtn2;
        if ((type & 1) != 0) {
            shutterBtn2 = getShutterButtonComp(1);
            if (!(((ImageView) getShutterButtonView(1)) == null || shutterBtn2 == null)) {
                return shutterBtn2.isPressed();
            }
        }
        if ((type & 2) != 0) {
            shutterBtn2 = getShutterButtonComp(2);
            if (!(((ImageView) getShutterButtonView(2)) == null || shutterBtn2 == null)) {
                return shutterBtn2.isPressed();
            }
        }
        return false;
    }

    private ImageButton getExtraButtonComp(int type) {
        switch (type) {
            case 2:
                return (ImageButton) this.mGet.findViewById(C0088R.id.extra_button_bottom_comp);
            default:
                return (ImageButton) this.mGet.findViewById(C0088R.id.extra_button_top_comp);
        }
    }

    private View getShutterLargeButtonLayout() {
        return this.mGet.findViewById(C0088R.id.shutter_large_comp_layout);
    }

    public void setShutterButtonVisibility(int visible, int type) {
        setShutterButtonVisibility(visible, type, false);
    }

    private void setShutterButtonSize(View largeBtnLayout) {
        LayoutParams params = (LayoutParams) largeBtnLayout.getLayoutParams();
        params.height = RatioCalcUtil.getCommandButtonHeight(getAppContext());
        largeBtnLayout.setLayoutParams(params);
    }

    public void setShutterLargeButtonVisibility(int visible, int type, boolean useAnimation, boolean isAlphaAnimation) {
        if (!isAlphaAnimation) {
            setShutterButtonVisibility(visible, type, useAnimation);
        } else if (type == 4) {
            View largeBtnLayout = getShutterLargeButtonLayout();
            if (largeBtnLayout != null) {
                setShutterButtonSize(largeBtnLayout);
                if (useAnimation) {
                    AnimationUtil.startShowingAnimation(largeBtnLayout, visible == 0, 200, null);
                } else {
                    largeBtnLayout.setVisibility(visible);
                }
            }
        }
    }

    public void setShutterButtonVisibility(int visible, int type, boolean useAnimation) {
        boolean z = true;
        if (type == 4) {
            View largeBtnLayout = getShutterLargeButtonLayout();
            if (largeBtnLayout != null) {
                setShutterButtonSize(largeBtnLayout);
                if (useAnimation) {
                    if (visible != 0) {
                        z = false;
                    }
                    AnimationUtil.startTransAnimation(largeBtnLayout, z, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, false);
                    return;
                }
                largeBtnLayout.setVisibility(visible);
                return;
            }
            return;
        }
        boolean z2;
        View shutterBtnLayout;
        if (type == 3) {
            if (this.mShutterBaseView == null) {
                return;
            }
            if (useAnimation) {
                View view = this.mShutterBaseView;
                if (visible == 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                AnimationUtil.startTransAnimation(view, z2, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, false);
            } else {
                this.mShutterBaseView.setVisibility(visible);
            }
        }
        if ((type & 1) != 0) {
            shutterBtnLayout = getShutterButtonLayout(1);
            if (shutterBtnLayout == null) {
                return;
            }
            if (useAnimation) {
                if (visible == 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                AnimationUtil.startTransAnimation(shutterBtnLayout, z2, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, false);
            } else {
                shutterBtnLayout.setVisibility(visible);
            }
        }
        if ((type & 2) != 0) {
            shutterBtnLayout = getShutterButtonLayout(2);
            if (shutterBtnLayout == null) {
                return;
            }
            if (useAnimation) {
                if (visible != 0) {
                    z = false;
                }
                AnimationUtil.startTransAnimation(shutterBtnLayout, z, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, false);
                return;
            }
            shutterBtnLayout.setVisibility(visible);
        }
    }

    public void setExtraButtonVisibility(int visible, int type) {
        setExtraButtonVisibility(visible, type, false);
    }

    public void setExtraButtonVisibility(int visible, int type, boolean useAnimation) {
        ImageButton extraImg;
        boolean z = false;
        if ((type & 1) != 0) {
            extraImg = getExtraButtonComp(1);
            if (extraImg != null) {
                if (useAnimation) {
                    boolean z2;
                    if (visible == 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    AnimationUtil.startTransAnimation(extraImg, z2, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, false);
                } else {
                    extraImg.setVisibility(visible);
                }
            }
        }
        if ((type & 2) != 0) {
            extraImg = getExtraButtonComp(2);
            if (extraImg == null) {
                return;
            }
            if (useAnimation) {
                if (visible == 0) {
                    z = true;
                }
                AnimationUtil.startTransAnimation(extraImg, z, Utils.isConfigureLandscape(this.mGet.getAppContext().getResources()), null, true);
                return;
            }
            extraImg.setVisibility(visible);
        }
    }

    private void setExtraButtonEnableByInitValue(int initValue, int type) {
        boolean z = true;
        if (!(initValue == 0 || initValue == 1)) {
            z = false;
        }
        setExtraButtonEnable(z, type);
    }

    public void setExtraButtonEnable(boolean enabled, int type) {
        int i = 1;
        if (this.mGet.checkModuleValidate(8)) {
            ImageButton extraImg;
            if ((type & 1) != 0) {
                extraImg = getExtraButtonComp(1);
                if (extraImg != null) {
                    extraImg.setEnabled(enabled);
                }
            }
            if ((type & 2) != 0) {
                extraImg = getExtraButtonComp(2);
                if (extraImg != null) {
                    extraImg.setEnabled(enabled);
                    return;
                }
                return;
            }
            return;
        }
        if ((type & 1) != 0) {
            this.mInitEnabledExtraTop = enabled ? 1 : 0;
        }
        if ((type & 2) != 0) {
            if (!enabled) {
                i = 0;
            }
            this.mInitEnabledExtraBottom = i;
        }
        CamLog.m3d(CameraConstants.TAG, "Exit camera is changing enable = " + enabled + "type=" + type);
    }

    public void setExtraButtonPressed(boolean pressed, int type) {
        ImageButton extraImg;
        if ((type & 1) != 0) {
            extraImg = getExtraButtonComp(1);
            if (extraImg != null) {
                extraImg.setPressed(pressed);
            }
        }
        if ((type & 2) != 0) {
            extraImg = getExtraButtonComp(2);
            if (extraImg != null) {
                extraImg.setPressed(pressed);
            }
        }
    }

    public void changeButtonByMode(int mode) {
        int i = 8;
        CamLog.m3d(CameraConstants.TAG, "from mode = " + this.mCurrentMode + ", to mode = " + mode);
        changeButtonOrder();
        switch (mode) {
            case 1:
                changeExtraButton(0, 1);
                setShutterButtonVisibility(8, 4);
                setShutterButtonVisibility(0, 3);
                changeShutterButton(2, 1);
                changeShutterButton(1, 2);
                changeExtraButton(0, 2);
                break;
            case 2:
                if (this.mCurrentMode == 2) {
                    changeExtraButton(6, 1);
                    setShutterButtonVisibility(0, 3);
                    setShutterButtonVisibility(8, 4);
                    changeShutterButton(4, 1);
                    changeShutterButton(3, 2, true);
                    requestShutterButtonFocus(true, 1);
                    break;
                }
                recordingStartAnimation();
                break;
            case 3:
                if (this.mCurrentMode == 11) {
                    changeExtraButton(0, 1);
                    setShutterButtonVisibility(8, 4);
                    if (this.mGet.getShutterButtonType() != 4) {
                        i = 0;
                    }
                    setShutterButtonVisibility(i, 3);
                    changeShutterButton(2, 1);
                    changeShutterButton(1, 2);
                    changeExtraButton(0, 2);
                    break;
                }
                recordingStopAnimation();
                break;
            case 4:
                changeExtraButton(6, 1);
                setShutterButtonVisibility(0, 3);
                setShutterButtonVisibility(8, 4);
                changeShutterButton(5, 1, true);
                changeShutterButton(3, 2, true);
                requestShutterButtonFocus(true, 2);
                break;
            case 5:
                changeExtraButton(6, 1);
                setShutterButtonVisibility(0, 3);
                setShutterButtonVisibility(8, 4);
                changeShutterButton(4, 1, true);
                changeShutterButton(3, 2, true);
                requestShutterButtonFocus(true, 1);
                break;
            case 7:
                changeExtraButton(0, 1);
                setShutterButtonVisibility(8, 4);
                changeExtraButton(0, 2);
                break;
            case 8:
                changeExtraButton(0, 1);
                setShutterButtonVisibility(8, 3);
                setShutterButtonVisibility(0, 4);
                changeShutterButton(8, 4, true);
                changeExtraButton(0, 2);
                break;
            case 9:
                changeExtraButton(0, 1);
                setShutterButtonVisibility(8, 3);
                setShutterButtonVisibility(8, 4);
                changeExtraButton(0, 2);
                break;
            case 11:
                setShutterButtonVisibility(8, 3);
                setShutterButtonVisibility(0, 4);
                changeShutterButton(11, 4, true);
                requestShutterButtonFocus(true, 2);
                break;
            case 12:
                setShutterButtonVisibility(8, 3);
                setShutterButtonVisibility(0, 4);
                changeShutterButton(12, 4);
                requestShutterButtonFocus(true, 2);
                break;
            case 14:
                if (this.mCurrentMode != 2) {
                    changeExtraButton(0, 1);
                    setShutterButtonVisibility(8, 3);
                    setShutterButtonVisibility(0, 4);
                    changeShutterButton(14, 4);
                    requestShutterButtonFocus(true, 2);
                    break;
                }
                recordingStopAnimation();
                break;
            case 17:
                if (this.mCurrentMode != 2) {
                    changeExtraButton(0, 1);
                    setShutterButtonVisibility(8, 4);
                    setShutterButtonVisibility(0, 3);
                    changeShutterButton(14, 1);
                    changeShutterButton(1, 2, true);
                    changeExtraButton(0, 2);
                    break;
                }
                recordingStopAnimation();
                break;
            case 18:
                changeExtraButton(0, 1);
                setShutterButtonVisibility(8, 3);
                setShutterButtonVisibility(0, 4);
                changeShutterButton(8, 4);
                changeExtraButton(0, 2);
                break;
        }
        this.mCurrentMode = mode;
        changeButtonAlignment();
    }

    private void recordingStopAnimation() {
        if (this.mCurrentMode != 3 && this.mCurrentMode != 14 && this.mCurrentMode != 17) {
            changeExtraButton(0, 1);
            switch (this.mGet.getShutterButtonType()) {
                case 3:
                    changeExtraButton(0, 2);
                    setShutterButtonVisibility(8, 4);
                    setShutterButtonVisibility(4, 3);
                    changeShutterButton(2, 1);
                    changeShutterButton(1, 2);
                    if (this.mIsUseMorphingAnimation) {
                        doRecordingMorphingAnimation(false);
                        return;
                    } else {
                        doRecordingShutterAnimationForAllShutterType(false);
                        return;
                    }
                default:
                    setShutterButtonVisibility(4, 3);
                    setShutterButtonVisibility(4, 4);
                    if (this.mIsUseMorphingAnimation) {
                        doRecordingPriorityMorphingAnimation(false);
                        return;
                    } else {
                        doRecordingShutterAnimationForLargeShutterType(false);
                        return;
                    }
            }
        }
    }

    private void recordingStartAnimation() {
        changeExtraButton(6, 1);
        setShutterButtonVisibility(4, 3);
        setShutterButtonVisibility(8, 4);
        changeShutterButton(3, 2);
        changeShutterButton(4, 1, true);
        switch (this.mGet.getShutterButtonType()) {
            case 3:
                if (this.mIsUseMorphingAnimation) {
                    doRecordingMorphingAnimation(true);
                    return;
                } else {
                    doRecordingShutterAnimationForAllShutterType(true);
                    return;
                }
            default:
                if (this.mIsUseMorphingAnimation) {
                    doRecordingPriorityMorphingAnimation(true);
                    return;
                } else {
                    doRecordingShutterAnimationForLargeShutterType(true);
                    return;
                }
        }
    }

    private void doRecordingMorphingAnimation(boolean start) {
        final RelativeLayout rearLayout = (RelativeLayout) this.mGet.findViewById(C0088R.id.morphing_anim_view);
        ImageView captureButton = (ImageView) rearLayout.findViewById(C0088R.id.morphing_anim_view_capture_button);
        ImageView recordingButton = (ImageView) rearLayout.findViewById(C0088R.id.morphing_anim_view_recording_button);
        RotateLayout captureLayout = (RotateLayout) rearLayout.findViewById(C0088R.id.morphing_anim_view_capture_rotateview);
        RotateLayout recordingLayout = (RotateLayout) rearLayout.findViewById(C0088R.id.morphing_anim_view_recording_rotateview);
        int degree = getOrientationDegree();
        captureLayout.rotateLayout(degree);
        recordingLayout.rotateLayout(degree);
        if (start) {
            captureButton.setImageResource(C0088R.drawable.avd_shutter_stop);
            recordingButton.setImageResource(C0088R.drawable.avd_rec_pause);
        } else {
            captureButton.setImageResource(C0088R.drawable.avd_stop_shutter);
            recordingButton.setImageResource(C0088R.drawable.avd_pause_rec);
        }
        LayoutParams captureButtonLayoutParam = (LayoutParams) captureButton.getLayoutParams();
        captureButtonLayoutParam.width = this.mLargeButtonSize;
        captureButtonLayoutParam.height = this.mLargeButtonSize;
        captureButton.setLayoutParams(captureButtonLayoutParam);
        LayoutParams recordingButtonLayoutParam = (LayoutParams) recordingButton.getLayoutParams();
        recordingButtonLayoutParam.width = this.mSmallButtonSize;
        recordingButtonLayoutParam.height = this.mSmallButtonSize;
        recordingButton.setLayoutParams(recordingButtonLayoutParam);
        this.mIsWorkingShutterAnimation = true;
        ((AnimatedVectorDrawable) recordingButton.getDrawable()).start();
        AnimatedVectorDrawable captureAvd = (AnimatedVectorDrawable) captureButton.getDrawable();
        rearLayout.setVisibility(0);
        captureAvd.registerAnimationCallback(new AnimationCallback() {
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                rearLayout.setVisibility(8);
                CaptureButtonManager.this.setShutterButtonVisibility(0, 3);
                CaptureButtonManager.this.mIsWorkingShutterAnimation = false;
            }
        });
        captureAvd.start();
    }

    private void doRecordingPriorityMorphingAnimation(boolean start) {
        RelativeLayout rearLayout = (RelativeLayout) this.mGet.findViewById(C0088R.id.morphing_anim_view);
        ImageView mainButton = (ImageView) rearLayout.findViewById(C0088R.id.morphing_anim_view_capture_button);
        ImageView smallButton = (ImageView) rearLayout.findViewById(C0088R.id.morphing_anim_view_recording_button);
        smallButton.setBackgroundResource(getShutterBackground(4));
        smallButton.setImageResource(getIdFromMode(4));
        RotateLayout captureLayout = (RotateLayout) rearLayout.findViewById(C0088R.id.morphing_anim_view_capture_rotateview);
        RotateLayout recordingLayout = (RotateLayout) rearLayout.findViewById(C0088R.id.morphing_anim_view_recording_rotateview);
        int degree = getOrientationDegree();
        captureLayout.rotateLayout(degree);
        recordingLayout.rotateLayout(degree);
        if (start) {
            mainButton.setImageResource(C0088R.drawable.avd_manual_rec_stop);
        } else {
            mainButton.setImageResource(C0088R.drawable.avd_manual_stop_rec);
        }
        LayoutParams captureButtonLayoutParam = (LayoutParams) mainButton.getLayoutParams();
        captureButtonLayoutParam.width = this.mLargeButtonSize;
        captureButtonLayoutParam.height = this.mLargeButtonSize;
        mainButton.setLayoutParams(captureButtonLayoutParam);
        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) mainButton.getDrawable();
        rearLayout.setVisibility(0);
        final RelativeLayout relativeLayout = rearLayout;
        final boolean z = start;
        avd.registerAnimationCallback(new AnimationCallback() {
            public void onAnimationEnd(Drawable drawable) {
                relativeLayout.setVisibility(8);
                if (z) {
                    CaptureButtonManager.this.setShutterButtonVisibility(0, 3);
                } else {
                    CaptureButtonManager.this.setShutterButtonVisibility(0, 4);
                }
                CaptureButtonManager.this.mIsWorkingShutterAnimation = false;
            }
        });
        this.mIsWorkingShutterAnimation = true;
        avd.start();
        Animation scaleAni = new ScaleAnimation(start ? 0.0f : 1.0f, start ? 1.0f : 0.0f, start ? 0.0f : 1.0f, start ? 1.0f : 0.0f, 1, 0.5f, 1, 0.5f);
        scaleAni.setDuration((long) this.DUR_REC);
        scaleAni.setInterpolator(new DecelerateInterpolator());
        smallButton.startAnimation(scaleAni);
    }

    private void doRecordingShutterAnimationForAllShutterType(boolean isStart) {
        RelativeLayout rearLayout = (RelativeLayout) this.mGet.findViewById(C0088R.id.shutter_button_base_rear_anim_layout);
        RelativeLayout frontLayout = (RelativeLayout) this.mGet.findViewById(C0088R.id.shutter_button_base_front_anim_layout);
        LayoutParams rearRlp = (LayoutParams) rearLayout.getLayoutParams();
        LayoutParams frontRlp = (LayoutParams) frontLayout.getLayoutParams();
        if (rearLayout != null && frontLayout != null && rearRlp != null && frontRlp != null) {
            CaptureAnimationViewInfo rearViewInfo;
            CaptureAnimationViewInfo frontViewInfo;
            int degree = getOrientationDegree();
            RotateImageButton rearTopImgButton = (RotateImageButton) rearLayout.findViewById(C0088R.id.shutter_top_comp_rear_anim);
            RotateImageButton rearBottomImgButton = (RotateImageButton) rearLayout.findViewById(C0088R.id.shutter_bottom_comp_rear_anim);
            RotateImageButton frontTopImgButton = (RotateImageButton) frontLayout.findViewById(C0088R.id.shutter_top_comp_front_anim);
            RotateImageButton frontBottomImgButton = (RotateImageButton) frontLayout.findViewById(C0088R.id.shutter_bottom_comp_front_anim);
            LayoutParams frontBottomButtonLp = (LayoutParams) frontBottomImgButton.getLayoutParams();
            rearLayout.setVisibility(0);
            frontLayout.setVisibility(0);
            this.mIsWorkingShutterAnimation = true;
            if (isStart) {
                rearViewInfo = new CaptureAnimationViewInfo(getIdFromMode(1), getShutterBackground(1), getIdFromMode(2), getShutterBackground(2), this.mCaptureToRecording, this.mRecordingToCapture, this.mShutterZoomArrowSize);
                frontViewInfo = new CaptureAnimationViewInfo(getIdFromMode(4), getShutterBackground(4), getIdFromMode(3), getShutterBackground(3), this.mCaptureToRecording, this.mRecordingToCapture, this.mShutterZoomArrowSize + this.mShutterStrokeSize);
                frontBottomButtonLp.setMarginStart(this.mStartMargin - this.mShutterStrokeSize);
            } else {
                CaptureAnimationViewInfo captureAnimationViewInfo = new CaptureAnimationViewInfo(getIdFromMode(4), getShutterBackground(4), getIdFromMode(3), getShutterBackground(3), 0, 0 - this.mShutterStrokeSize, this.mShutterStrokeSize + this.mShutterZoomArrowSize);
                frontViewInfo = new CaptureAnimationViewInfo(getIdFromMode(1), getShutterBackground(1), getIdFromMode(2), getShutterBackground(2), 0, 0, this.mShutterZoomArrowSize);
                frontBottomButtonLp.setMarginStart(this.mStartMargin);
            }
            setAnimationViewLayout(rearViewInfo, rearTopImgButton, rearBottomImgButton, degree);
            setAnimationViewLayout(frontViewInfo, frontTopImgButton, frontBottomImgButton, degree);
            RotateImageButton rotateImageButton = rearTopImgButton;
            ObjectAnimator rearTopBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) rearViewInfo.mTopButtonMoveDistance});
            ObjectAnimator rearTopBtnAlphaAnimation = ObjectAnimator.ofFloat(rearTopImgButton, AnimationManager.ANI_ALPHA, new float[]{1.0f, 0.0f});
            rotateImageButton = rearBottomImgButton;
            ObjectAnimator rearBottomBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) rearViewInfo.mBottomButtonMoveDistance});
            ObjectAnimator rearBottomBtnAlphaAnimation = ObjectAnimator.ofFloat(rearBottomImgButton, AnimationManager.ANI_ALPHA, new float[]{1.0f, 0.0f});
            rotateImageButton = frontTopImgButton;
            ObjectAnimator frontTopBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) frontViewInfo.mTopButtonMoveDistance});
            ObjectAnimator frontTopBtnAlphaAnimation = ObjectAnimator.ofFloat(frontTopImgButton, AnimationManager.ANI_ALPHA, new float[]{0.0f, 1.0f});
            rotateImageButton = frontBottomImgButton;
            ObjectAnimator frontBottomBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) frontViewInfo.mBottomButtonMoveDistance});
            ObjectAnimator frontBottomBtnAlphaAnimation = ObjectAnimator.ofFloat(frontBottomImgButton, AnimationManager.ANI_ALPHA, new float[]{0.0f, 1.0f});
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration((long) this.DUR_REC);
            animatorSet.playTogether(new Animator[]{rearBottomBtnTransAnimator, frontBottomBtnTransAnimator, rearBottomBtnAlphaAnimation, frontBottomBtnAlphaAnimation, rearTopBtnTransAnimator, frontTopBtnTransAnimator, rearTopBtnAlphaAnimation, frontTopBtnAlphaAnimation});
            animatorSet.playTogether(new Animator[]{rearBottomBtnTransAnimator, frontBottomBtnTransAnimator, rearTopBtnTransAnimator, frontTopBtnTransAnimator});
            final RelativeLayout relativeLayout = rearLayout;
            final RelativeLayout relativeLayout2 = frontLayout;
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    relativeLayout.setVisibility(8);
                    relativeLayout2.setVisibility(8);
                    CaptureButtonManager.this.setShutterButtonVisibility(0, 3);
                    CaptureButtonManager.this.mIsWorkingShutterAnimation = false;
                }
            });
            animatorSet.setInterpolator(new DecelerateInterpolator(1.2f));
            animatorSet.start();
        }
    }

    protected void setAnimationViewLayout(CaptureAnimationViewInfo viewInfo, RotateImageButton top, RotateImageButton bottom, int degree) {
        top.setBackgroundResource(viewInfo.mTopBackgroundResId);
        top.setImageResource(viewInfo.mTopImgViewResId);
        top.setDegree(degree, false);
        bottom.setBackgroundResource(viewInfo.mBottomBackgroundResId);
        bottom.setImageResource(viewInfo.mBottomImgViewResId);
        bottom.setDegree(degree, false);
    }

    public void doRecordingShutterAnimationForLargeShutterType(boolean isStart) {
        final RelativeLayout aniLayout = (RelativeLayout) this.mGet.findViewById(C0088R.id.shutter_large_comp_anim_layout);
        if (aniLayout != null) {
            CaptureAnimationViewInfo first;
            CaptureAnimationViewInfo second;
            CaptureAnimationViewInfo third;
            int degree = getOrientationDegree();
            RotateImageButton firstView = (RotateImageButton) aniLayout.findViewById(C0088R.id.shutter_large_comp_type_anim);
            RotateImageButton secondView = (RotateImageButton) aniLayout.findViewById(C0088R.id.shutter_large_comp_type_anim2);
            RotateImageButton thridView = (RotateImageButton) aniLayout.findViewById(C0088R.id.shutter_large_comp_type_anim3);
            aniLayout.setVisibility(0);
            this.mIsWorkingShutterAnimation = true;
            if (isStart) {
                changeButtonPadding();
                first = new CaptureAnimationViewInfo(getIdFromMode(3), getShutterBackground(3), 0, 0, 0, 0, 0);
                second = new CaptureAnimationViewInfo(getIdFromMode(14), getShutterBackground(14), 0, 0, 0, 0, 0);
                third = new CaptureAnimationViewInfo(getIdFromMode(4), getShutterBackground(4), 0, 0, this.mCaptureToRecording, 0, 0);
                first.setAlpha(0.0f, 1.0f);
                second.setAlpha(1.0f, 0.0f);
                third.setAlpha(0.0f, 1.0f);
            } else {
                CaptureAnimationViewInfo captureAnimationViewInfo = new CaptureAnimationViewInfo(getIdFromMode(14), getShutterBackground(14), 0, 0, 0, 0, 0);
                captureAnimationViewInfo = new CaptureAnimationViewInfo(getIdFromMode(3), getShutterBackground(3), 0, 0, 0, 0, 0);
                third = new CaptureAnimationViewInfo(getIdFromMode(4), getShutterBackground(4), 0, 0, 0, 0, 0);
                captureAnimationViewInfo.setAlpha(0.0f, 1.0f);
                captureAnimationViewInfo.setAlpha(1.0f, 0.0f);
                third.setAlpha(1.0f, 0.0f);
                if (this.mGet.isAttachIntent()) {
                    captureAnimationViewInfo.setAlpha(0.0f, 0.0f);
                    captureAnimationViewInfo.setAlpha(0.0f, 0.0f);
                    third.setAlpha(0.0f, 0.0f);
                }
            }
            firstView.setBackgroundResource(first.mTopBackgroundResId);
            firstView.setImageResource(first.mTopImgViewResId);
            firstView.setDegree(degree, false);
            secondView.setBackgroundResource(second.mTopBackgroundResId);
            secondView.setImageResource(second.mTopImgViewResId);
            secondView.setDegree(degree, false);
            thridView.setBackgroundResource(third.mTopBackgroundResId);
            thridView.setImageResource(third.mTopImgViewResId);
            thridView.setDegree(degree, false);
            RotateImageButton rotateImageButton = firstView;
            ObjectAnimator rearLargeBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) first.mTopButtonMoveDistance});
            ObjectAnimator rearLargeBtnAlphaAnimation = ObjectAnimator.ofFloat(firstView, AnimationManager.ANI_ALPHA, new float[]{first.mStartAlpha, first.mEndAlpha});
            rotateImageButton = secondView;
            ObjectAnimator frontTopBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) second.mTopButtonMoveDistance});
            ObjectAnimator frontTopBtnAlphaAnimation = ObjectAnimator.ofFloat(secondView, AnimationManager.ANI_ALPHA, new float[]{second.mStartAlpha, second.mEndAlpha});
            rotateImageButton = thridView;
            ObjectAnimator frontBottomBtnTransAnimator = ObjectAnimator.ofFloat(rotateImageButton, "translationX", new float[]{(float) third.mTopButtonMoveDistance});
            ObjectAnimator frontBottomBtnAlphaAnimation = ObjectAnimator.ofFloat(thridView, AnimationManager.ANI_ALPHA, new float[]{third.mStartAlpha, third.mEndAlpha});
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration((long) this.DUR_REC);
            animatorSet.playTogether(new Animator[]{frontBottomBtnTransAnimator, frontBottomBtnAlphaAnimation, rearLargeBtnTransAnimator, frontTopBtnTransAnimator, rearLargeBtnAlphaAnimation, frontTopBtnAlphaAnimation});
            final boolean z = isStart;
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    aniLayout.setVisibility(8);
                    if (z) {
                        CaptureButtonManager.this.setShutterButtonVisibility(0, 3);
                    } else {
                        CaptureButtonManager.this.setShutterButtonVisibility(0, 4);
                    }
                    CaptureButtonManager.this.mIsWorkingShutterAnimation = false;
                }
            });
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.start();
        }
    }

    private void refreshButtonByCurrentMode() {
        if (this.mCurrentMode == 3 || this.mCurrentMode == 8) {
            switch (this.mGet.getShutterButtonType()) {
                case 3:
                    this.mCurrentMode = 1;
                    break;
                case 4:
                    this.mCurrentMode = this.mGet.getShotMode().contains(CameraConstants.MODE_PANORAMA) ? 12 : 14;
                    break;
            }
        }
        changeButtonByMode(this.mCurrentMode);
    }

    public void onCameraSwitchingStart() {
        CamLog.m3d(CameraConstants.TAG, "Disable buttons because end changing");
        setShutterButtonEnable(false, 3);
        setShutterButtonEnable(false, 4);
        setExtraButtonEnable(false, 3);
        this.mInitEnabledShutterTop = 0;
        this.mInitEnabledShutterBottom = 0;
        this.mInitEnabledShutterLarge = 0;
        this.mInitEnabledExtraTop = 0;
        this.mInitEnabledExtraBottom = 0;
        super.onCameraSwitchingStart();
    }

    public void onCameraSwitchingEnd() {
        int i = 2;
        CamLog.m3d(CameraConstants.TAG, "Enable buttons because end changing");
        setShutterButtonEnableByInitValue(this.mInitEnabledShutterTop, 1);
        setShutterButtonEnableByInitValue(this.mInitEnabledShutterBottom, 2);
        setShutterButtonEnableByInitValue(this.mInitEnabledShutterLarge, 4);
        setExtraButtonEnableByInitValue(this.mInitEnabledExtraTop, 1);
        setExtraButtonEnableByInitValue(this.mInitEnabledExtraBottom, 2);
        if (ModelProperties.isKeyPadSupported(getAppContext())) {
            if (this.mGet.isVideoCaptureMode()) {
                i = 1;
            }
            requestShutterButtonFocus(true, i);
        }
        super.onCameraSwitchingEnd();
    }

    public void setRotateDegree(int degree, boolean animation) {
        RotateImageButton bottomBtn = (RotateImageButton) this.mGet.findViewById(C0088R.id.shutter_bottom_comp_type);
        RotateImageButton largeBtn = (RotateImageButton) this.mGet.findViewById(C0088R.id.shutter_large_comp_type);
        RotateImageButton extraTopBtn = (RotateImageButton) this.mGet.findViewById(C0088R.id.extra_button_top_comp);
        RotateImageButton extraBottomBtn = (RotateImageButton) this.mGet.findViewById(C0088R.id.extra_button_bottom_comp);
        ((RotateImageButton) this.mGet.findViewById(C0088R.id.shutter_top_comp_type)).setDegree(degree, animation);
        bottomBtn.setDegree(degree, animation);
        largeBtn.setDegree(degree, animation);
        extraTopBtn.setDegree(degree, animation);
        extraBottomBtn.setDegree(degree, animation);
    }

    public void setButtonDimByNaviBar(boolean enable) {
        if (enable) {
            onCameraSwitchingEnd();
        } else {
            onCameraSwitchingStart();
        }
    }

    public void moveByShutterZoom(int moveX) {
        View shutter = null;
        RelativeLayout layout = null;
        switch (this.mGet.getShutterButtonType()) {
            case 2:
            case 3:
                setShutterButtonVisibility(8, 1);
                shutter = getShutterButtonView(2);
                layout = this.mShutterBaseView;
                break;
            case 4:
                shutter = getShutterLargeButtonView();
                layout = (RelativeLayout) getShutterLargeButtonLayout();
                break;
        }
        if (shutter != null) {
            shutter.setBackgroundResource(C0088R.drawable.shutter_camera_pressed);
        }
        if (layout != null) {
            layout.setPaddingRelative(this.mShutterZoomStartPadding + moveX, layout.getPaddingTop(), this.mShutterZoomEndPadding - moveX, layout.getPaddingBottom());
            if (Math.abs(this.mMoveXLast - moveX) >= this.sCustomTouchSlop) {
                AudioUtil.performHapticFeedback(layout, 65574);
                this.mMoveXLast = moveX;
            }
        }
        if (this.mShutterZoomArrow != null) {
            this.mShutterZoomArrow.setVisibility(4);
        }
        if (this.mShutterZoomTrack != null) {
            this.mShutterZoomTrack.setVisibility(0);
        }
    }

    public void stopShutterZoom() {
        int i = 0;
        if (this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "stop ShutterZoom - cur shutter type : " + this.mGet.getShutterButtonType());
            switch (this.mGet.getShutterButtonType()) {
                case 2:
                case 3:
                    getShutterButtonView(2).setBackgroundResource(getIdFromMode(1));
                    setShutterButtonVisibility(0, 1);
                    if (this.mShutterBaseView != null) {
                        this.mShutterBaseView.setPaddingRelative(0, 0, 0, 0);
                        break;
                    }
                    break;
                case 4:
                    getShutterLargeButtonView().setBackgroundResource(getIdFromMode(12));
                    RelativeLayout layout = (RelativeLayout) getShutterLargeButtonLayout();
                    if (layout != null) {
                        layout.setPaddingRelative(0, 0, 0, 0);
                        break;
                    }
                    break;
            }
            if (this.mShutterZoomArrow != null) {
                View view = this.mShutterZoomArrow;
                if (this.mGet.isPaused()) {
                    i = 8;
                }
                view.setVisibility(i);
            }
            this.mShutterZoomTrack.setVisibility(8);
            if (this.mShutterBaseView != null) {
                AudioUtil.performHapticFeedback(this.mShutterBaseView, 65575);
            }
        }
    }

    public boolean isWorkingShutterAnimation() {
        return this.mIsWorkingShutterAnimation;
    }
}
