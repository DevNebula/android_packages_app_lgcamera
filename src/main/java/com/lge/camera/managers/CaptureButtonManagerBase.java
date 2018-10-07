package com.lge.camera.managers;

import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.ShutterButton;
import com.lge.camera.components.ShutterButtonBase;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.ManualUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class CaptureButtonManagerBase extends ManagerInterfaceImpl {
    protected View mCameraControlsView = null;
    protected int mCaptureToRecording;
    protected int mCenterLargeToTwoSmall;
    protected SparseIntArray mContentDescArray = null;
    protected int mCurrentMode = 1;
    protected int mDistanceBetweenTwoButtons;
    protected int mInitEnabledExtraBottom = 0;
    protected int mInitEnabledExtraTop = 0;
    protected int mInitEnabledShutterBottom = 0;
    protected int mInitEnabledShutterLarge = 0;
    protected int mInitEnabledShutterTop = 0;
    protected boolean mIsUseMorphingAnimation = true;
    protected boolean mIsWorkingShutterAnimation = false;
    protected int mLargeBtnToAllBtn;
    protected int mLargeButtonSize;
    protected float mNormSmallRatio;
    protected int mNormToTwoSmall;
    protected int mRecordingToCapture;
    protected int mShutterBaseMarginEnd;
    protected int mShutterBaseMarginStart;
    protected RelativeLayout mShutterBaseView = null;
    protected int mShutterBottomMode = 0;
    protected int mShutterLargeMode = 0;
    protected SparseIntArray mShutterResMap = null;
    protected int mShutterStrokeSize;
    protected int mShutterTopMode = 0;
    protected View mShutterZoomArrow = null;
    protected int mShutterZoomArrowSize;
    protected int mShutterZoomEndPadding = 0;
    protected int mShutterZoomStartPadding = 0;
    protected View mShutterZoomTrack = null;
    protected int mSmallButtonSize;
    protected int mStartMargin;

    public CaptureButtonManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        setUpAllViews(true);
        calculateAnimationDistance();
    }

    private void calculateAnimationDistance() {
        if (this.mGet != null) {
            this.mLargeButtonSize = this.mGet.getAppContext().getDrawable(C0088R.drawable.shutter_stroke_normal).getIntrinsicWidth();
            this.mSmallButtonSize = this.mGet.getAppContext().getDrawable(C0088R.drawable.shutter_icon_pause).getIntrinsicWidth();
            this.mShutterZoomArrowSize = (this.mGet.getAppContext().getDrawable(C0088R.drawable.camera_shutter_zoom_default).getIntrinsicWidth() - this.mLargeButtonSize) / 2;
            this.mShutterStrokeSize = (this.mLargeButtonSize - this.mSmallButtonSize) / 2;
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            this.mStartMargin = Utils.getPx(getAppContext(), C0088R.dimen.extra_button_marginTop);
            this.mDistanceBetweenTwoButtons = (int) (((((((float) lcdSize[1]) / 2.0f) - ((float) this.mStartMargin)) - (((float) this.mLargeButtonSize) / 2.0f)) - ((float) this.mShutterZoomArrowSize)) - ((float) this.mSmallButtonSize));
            this.mCaptureToRecording = -((((this.mLargeButtonSize / 2) + this.mShutterZoomArrowSize) + this.mDistanceBetweenTwoButtons) + (this.mSmallButtonSize / 2));
            this.mRecordingToCapture = -this.mCaptureToRecording;
        }
    }

    public void onResumeBefore() {
        changeButtonOrder();
        changeShutterButton(2, 1);
        changeShutterButton(1, 2);
        if (this.mCurrentMode == 2) {
            this.mCurrentMode = 1;
            changeButtonAlignment();
        }
        super.onResumeBefore();
        this.mIsWorkingShutterAnimation = false;
    }

    protected void changeButtonOrder() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.shutter_button_layout);
        if (vg != null) {
            vg.setLayoutDirection(this.mCurrentMode == 17 ? 1 : 0);
        }
    }

    protected void changeButtonAlignment() {
        ShutterButton shutterTopBtn = getShutterButtonComp(1);
        ShutterButton shutterBottomBtn = getShutterButtonComp(2);
        if (shutterTopBtn != null && shutterBottomBtn != null && shutterTopBtn.getVisibility() == 0 && shutterBottomBtn.getVisibility() == 0) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.shutter_button_layout);
            if (vg != null) {
                LayoutParams lp = (LayoutParams) vg.getLayoutParams();
                if (lp != null) {
                    lp.setMarginEnd(0);
                    vg.setLayoutParams(lp);
                }
            }
        }
    }

    protected void changeButtonPadding() {
        if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode()) && ManualUtil.isCinemaSize(getAppContext(), this.mGet.getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE))) {
            this.mShutterBaseMarginStart = 0;
            this.mShutterBaseMarginEnd = 0;
            return;
        }
        this.mShutterBaseMarginStart = Utils.getPx(getAppContext(), C0088R.dimen.shutter_btn_base_marginStart);
        this.mShutterBaseMarginEnd = Utils.getPx(getAppContext(), C0088R.dimen.shutter_btn_base_marginEnd);
    }

    public void onResumeAfter() {
        if (this.mShutterBaseView != null) {
            this.mShutterBaseView.setVisibility(0);
            setRotateDegree(getOrientationDegree(), false);
        }
        super.onResumeAfter();
    }

    protected void setUpViewByRatio() {
        if (this.mCameraControlsView != null && this.mGet != null) {
            this.mShutterBaseView = (RelativeLayout) this.mCameraControlsView.findViewById(C0088R.id.shutter_button_base_layout);
            ImageButton extraButton = (ImageButton) this.mGet.findViewById(C0088R.id.extra_button_top_comp);
            RelativeLayout shutterAnimationRearView = (RelativeLayout) this.mCameraControlsView.findViewById(C0088R.id.shutter_button_base_rear_anim_layout);
            RelativeLayout shutterAnimationFrontView = (RelativeLayout) this.mCameraControlsView.findViewById(C0088R.id.shutter_button_base_front_anim_layout);
            RelativeLayout shutterAnimationLargeView = (RelativeLayout) this.mCameraControlsView.findViewById(C0088R.id.shutter_large_comp_anim_layout);
            RelativeLayout morphingAnimationView = (RelativeLayout) this.mCameraControlsView.findViewById(C0088R.id.morphing_anim_view);
            if (this.mShutterBaseView != null && extraButton != null && shutterAnimationRearView != null && shutterAnimationFrontView != null && shutterAnimationLargeView != null && morphingAnimationView != null) {
                LayoutParams shutterLp = (LayoutParams) this.mShutterBaseView.getLayoutParams();
                LayoutParams shutterRearLp = (LayoutParams) shutterAnimationRearView.getLayoutParams();
                LayoutParams shutterFrontLp = (LayoutParams) shutterAnimationFrontView.getLayoutParams();
                LayoutParams shutterRearLargeLp = (LayoutParams) shutterAnimationLargeView.getLayoutParams();
                LayoutParams extraBtnLp = (LayoutParams) extraButton.getLayoutParams();
                LayoutParams morphingLp = (LayoutParams) morphingAnimationView.getLayoutParams();
                if (shutterLp != null && extraBtnLp != null && morphingLp != null) {
                    shutterLp.height = RatioCalcUtil.getCommandButtonHeight(getAppContext());
                    shutterRearLp.height = shutterLp.height;
                    shutterFrontLp.height = shutterLp.height;
                    shutterRearLargeLp.height = shutterLp.height;
                    morphingLp.height = shutterLp.height;
                    this.mShutterBaseView.setLayoutParams(shutterLp);
                    shutterAnimationRearView.setLayoutParams(shutterRearLp);
                    shutterAnimationFrontView.setLayoutParams(shutterFrontLp);
                    shutterAnimationLargeView.setLayoutParams(shutterRearLargeLp);
                    morphingAnimationView.setLayoutParams(morphingLp);
                    extraBtnLp.bottomMargin = RatioCalcUtil.getCommandBottomMargin(getAppContext());
                    extraButton.setLayoutParams(extraBtnLp);
                    if (this.mShutterZoomTrack != null) {
                        LayoutParams shutterZoomTrackLp = (LayoutParams) this.mShutterZoomTrack.getLayoutParams();
                        if (shutterZoomTrackLp != null) {
                            shutterZoomTrackLp.height = shutterLp.height;
                            this.mShutterZoomTrack.setLayoutParams(shutterZoomTrackLp);
                        }
                    }
                }
            }
        }
    }

    protected void setUpAllViews(boolean init) {
        this.mCameraControlsView = this.mGet.findViewById(C0088R.id.camera_controls);
        this.mShutterBaseView = (RelativeLayout) this.mCameraControlsView.findViewById(C0088R.id.shutter_button_base_layout);
        changeShutterZoomUI(this.mCurrentMode, this.mGet.getShutterButtonType());
        setUpViewByRatio();
        if (init) {
            this.mShutterBaseView.setVisibility(4);
        }
    }

    protected void changeShutterZoomUI(int shutterMode, int type) {
        boolean set = true;
        this.mShutterZoomTrack = this.mGet.findViewById(C0088R.id.shutter_zoom_track);
        int visibility = this.mGet.isShutterZoomSupported() ? 0 : 4;
        if (!(shutterMode == 1 || shutterMode == 12)) {
            visibility = 8;
        }
        if (visibility != 0) {
            set = false;
        }
        switch (type) {
            case 2:
            case 3:
                ShutterButton bottomBtn = getShutterButtonComp(2);
                if (bottomBtn != null) {
                    bottomBtn.setShutterZoomAvailable(set);
                }
                this.mShutterZoomArrow = this.mGet.findViewById(C0088R.id.shutter_zoom_cue);
                break;
            case 4:
                ShutterButtonBase largeBtn = (ShutterButtonBase) getShutterLargeButtonComp();
                if (largeBtn != null) {
                    largeBtn.setShutterZoomAvailable(set);
                }
                this.mShutterZoomArrow = this.mGet.findViewById(C0088R.id.shutter_zoom_cue_for_large);
                break;
        }
        setShutterZoomArrowVisibility(visibility);
    }

    public void setShutterZoomArrowVisibility(int visibility) {
        if ((this.mGet.isShutterZoomSupported() || visibility != 0) && this.mShutterZoomArrow != null) {
            this.mShutterZoomArrow.setVisibility(visibility);
        }
    }

    protected void changeShutterButton(int shutterMode, int type) {
        changeShutterButton(shutterMode, type, false);
    }

    protected void changeShutterButton(int shutterMode, int type, boolean smallButton) {
        ImageView shutterType;
        ImageButton shutterButton;
        setShutterButtonMode(shutterMode, type);
        if (type == 4) {
            shutterType = (ImageView) getShutterLargeButtonView();
        } else {
            shutterType = (ImageView) getShutterButtonView(type);
        }
        if (shutterType != null) {
            shutterType.setBackgroundResource(getIdFromMode(shutterMode));
        }
        if (type == 4) {
            shutterButton = getShutterLargeButtonComp();
        } else {
            shutterButton = getShutterButtonComp(type);
        }
        if (shutterButton != null) {
            shutterButton.setBackgroundResource(getShutterBackground(shutterMode));
            shutterButton.setRotation(useTopBigButton() ? 180.0f : 0.0f);
            shutterButton.setContentDescription(getContentDescription(shutterMode));
            changeShutterZoomUI(shutterMode, type);
        }
    }

    protected int getShutterBackground(int shutterMode) {
        switch (shutterMode) {
            case 1:
            case 12:
                return C0088R.drawable.btn_shutter_stroke;
            case 3:
            case 8:
            case 11:
            case 14:
                return C0088R.drawable.btn_recording_shutter_background;
            default:
                return C0088R.drawable.btn_shutter_top;
        }
    }

    public int getShutterZoomMaxDistance() {
        return getShutterButtonComp(2).getShutterZoomMaxDistance();
    }

    protected View getShutterLargeButtonView() {
        return this.mGet.findViewById(C0088R.id.shutter_large_comp_type);
    }

    protected ImageButton getShutterLargeButtonComp() {
        return (ImageButton) this.mGet.findViewById(C0088R.id.shutter_large_comp);
    }

    public View getShutterButtonView(int type) {
        switch (type) {
            case 2:
                return this.mGet.findViewById(C0088R.id.shutter_bottom_comp_type);
            default:
                return this.mGet.findViewById(C0088R.id.shutter_top_comp_type);
        }
    }

    public ShutterButton getShutterButtonComp(int type) {
        switch (type) {
            case 2:
                return (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_bottom_comp);
            default:
                return (ShutterButton) this.mGet.findViewById(C0088R.id.shutter_top_comp);
        }
    }

    protected View getShutterButtonLayout(int type) {
        switch (type) {
            case 2:
                return this.mGet.findViewById(C0088R.id.shutter_bottom_comp_layout);
            default:
                return this.mGet.findViewById(C0088R.id.shutter_top_comp_layout);
        }
    }

    private void setShutterButtonMode(int toBeChangeMode, int type) {
        switch (type) {
            case 2:
                this.mShutterBottomMode = toBeChangeMode;
                return;
            case 4:
                this.mShutterLargeMode = toBeChangeMode;
                return;
            default:
                this.mShutterTopMode = toBeChangeMode;
                return;
        }
    }

    private boolean useTopBigButton() {
        if (CameraConstants.MODE_SLOW_MOTION.equals(this.mGet.getShotMode()) || CameraConstants.MODE_TIME_LAPSE_VIDEO.equals(this.mGet.getShotMode())) {
            return true;
        }
        return false;
    }

    protected String getContentDescription(int type) {
        if (this.mContentDescArray == null) {
            this.mContentDescArray = new SparseIntArray();
            this.mContentDescArray.put(1, C0088R.string.accessibility_shutter_button);
            this.mContentDescArray.put(2, C0088R.string.accessiblity_start_recording);
            this.mContentDescArray.put(3, C0088R.string.accessiblity_stop_recording);
            this.mContentDescArray.put(4, C0088R.string.accessiblity_pause_recording);
            this.mContentDescArray.put(5, C0088R.string.accessiblity_start_recording);
            this.mContentDescArray.put(6, C0088R.string.sp_live_shot_NORMAL);
            this.mContentDescArray.put(8, C0088R.string.camera_accessibility_stop_button);
            this.mContentDescArray.put(9, C0088R.string.accessiblity_mode_button);
            this.mContentDescArray.put(10, C0088R.string.accessiblity_mode_button);
            this.mContentDescArray.put(11, C0088R.string.camera_accessibility_stop_button);
            this.mContentDescArray.put(0, C0088R.string.accessiblity_mode_button);
            this.mContentDescArray.put(12, C0088R.string.accessibility_shutter_button);
            this.mContentDescArray.put(14, C0088R.string.accessiblity_start_recording);
        }
        return this.mGet.getAppContext().getString(this.mContentDescArray.get(type));
    }

    protected int getIdFromMode(int mode) {
        if (this.mShutterResMap == null) {
            this.mShutterResMap = new SparseIntArray();
            this.mShutterResMap.put(1, C0088R.drawable.btn_shutter_mode_camera);
            this.mShutterResMap.put(2, C0088R.drawable.btn_shutter_mode_video_small);
            this.mShutterResMap.put(5, C0088R.drawable.btn_shutter_mode_video_resume);
            this.mShutterResMap.put(3, C0088R.drawable.btn_shutter_mode_stop);
            this.mShutterResMap.put(8, C0088R.drawable.shutter_icon_stop_normal);
            this.mShutterResMap.put(4, C0088R.drawable.btn_shutter_mode_video_pause);
            this.mShutterResMap.put(6, C0088R.drawable.btn_rec_snap);
            this.mShutterResMap.put(10, C0088R.drawable.btn_quickbutton_swap_button);
            this.mShutterResMap.put(11, C0088R.drawable.btn_shutter_mode_stop);
            this.mShutterResMap.put(12, C0088R.drawable.btn_shutter_mode_camera);
            this.mShutterResMap.put(0, 0);
            this.mShutterResMap.put(14, C0088R.drawable.btn_shutter_mode_video);
        }
        return this.mShutterResMap.get(mode);
    }

    protected void holdFocusOnDummyView(boolean focusable) {
        if (ModelProperties.isKeyPadSupported(getAppContext()) && !this.mGet.isVideoCaptureMode()) {
            View dummyView = this.mGet.findViewById(C0088R.id.shutter_btn_dummy);
            dummyView.setVisibility(focusable ? 0 : 4);
            dummyView.setFocusable(focusable);
            if (focusable) {
                dummyView.requestFocus();
            }
        }
    }

    public void onDestroy() {
        if (this.mShutterResMap != null) {
            this.mShutterResMap.clear();
            this.mShutterResMap = null;
        }
        if (this.mContentDescArray != null) {
            this.mContentDescArray.clear();
            this.mContentDescArray = null;
        }
        this.mCameraControlsView = null;
        this.mShutterBaseView = null;
        this.mShutterZoomArrow = null;
        this.mShutterZoomTrack = null;
    }
}
