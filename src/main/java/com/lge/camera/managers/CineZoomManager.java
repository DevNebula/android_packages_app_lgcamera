package com.lge.camera.managers;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.R;
import com.lge.camera.C0088R;
import com.lge.camera.components.CineZoomBar;
import com.lge.camera.components.CineZoomBarDisabled;
import com.lge.camera.components.CineZoomGuideView;
import com.lge.camera.components.CineZoomView;
import com.lge.camera.components.HorizontalSeekBar.OnVerticalSeekBarListener;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;

public class CineZoomManager extends CineZoomManagerBase {
    protected static int sCineZoomBarBtnMargin = 0;
    protected static int sCineZoomBarMargin = 0;
    protected static int sCineZoomButtonsBottomMargin = 0;
    protected static int sCineZoomButtonsRightMargin = 0;
    protected static int sCineZoomButtonsSize = 0;
    protected static int sCineZoomButtonsTextPadding = 0;
    protected static int sCineZoomSpeedBarWidth = 0;
    private final int DEFAULT_ZOG_ZOOM_PROGRESS_VALUE = 28;
    private final int MAX_ZOG_ZOOM_PROGRESS_VALUE = 60;
    private OnClickListener mCineZoomButtonOnClickListener = new C08592();
    private OnClickListener mPlayPauseButtonOnClickListener = new C08614();
    private int mPreValue = 28;
    private OnClickListener mSpeedButtonOnClickListener = new C08625();
    private OnClickListener mZoomInOutButtonOnClickListener = new C08603();

    /* renamed from: com.lge.camera.managers.CineZoomManager$1 */
    class C08581 implements OnVerticalSeekBarListener {
        C08581() {
        }

        public void onBarValueChanged(int progress) {
            CamLog.m3d(CameraConstants.TAG, "jyj Cine jog zoom value : " + progress);
            CineZoomManager.this.onCineZoomBarValueChanged(progress);
        }

        public void onBarTouchUp() {
            CineZoomManager.this.onCineZoomBarTouchUp();
        }

        public void onBarTouchDown() {
        }
    }

    /* renamed from: com.lge.camera.managers.CineZoomManager$2 */
    class C08592 implements OnClickListener {
        C08592() {
        }

        public void onClick(View arg0) {
            if (CineZoomManager.this.mGet.checkModuleValidate(64) && CineZoomManager.this.mGet.getFocusState() != 1 && !CineZoomManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !CineZoomManager.this.mGet.isCinemaLUTVisible()) {
                if (CineZoomManager.this.mCineZoomButton.isSelected()) {
                    CineZoomManager.this.onDisableCineZoom();
                } else {
                    CineZoomManager.this.onEnableCineZoom();
                }
                CamLog.m3d(CameraConstants.TAG, "cine zoom button clicked");
            }
        }
    }

    /* renamed from: com.lge.camera.managers.CineZoomManager$3 */
    class C08603 implements OnClickListener {
        C08603() {
        }

        public void onClick(View v) {
            String zoom_in = CineZoomManager.this.mGet.getAppContext().getString(C0088R.string.camera_cz_zoom_in);
            String zoom_out = CineZoomManager.this.mGet.getAppContext().getString(C0088R.string.camera_cz_zoom_out);
            if (CineZoomManager.this.mStatus == 1) {
                CineZoomManager.this.setCineZoom(2);
                ((RotateImageButton) v).setText(zoom_out);
                ((RotateImageButton) v).setImageLevel(1);
            } else if (CineZoomManager.this.mStatus == 3) {
                CineZoomManager.this.setCineZoom(1);
                ((RotateImageButton) v).setText(zoom_in);
                ((RotateImageButton) v).setImageLevel(0);
            } else if (CineZoomManager.this.mStatus != 5) {
            } else {
                if (CineZoomManager.this.mDirection == 0) {
                    CineZoomManager.this.mDirection = 1;
                    ((RotateImageButton) v).setText(zoom_out);
                    ((RotateImageButton) v).setImageLevel(1);
                    return;
                }
                CineZoomManager.this.mDirection = 0;
                ((RotateImageButton) v).setText(zoom_in);
                ((RotateImageButton) v).setImageLevel(0);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.CineZoomManager$4 */
    class C08614 implements OnClickListener {
        C08614() {
        }

        public void onClick(View v) {
            if (CineZoomManager.this.mStatus == 1 || CineZoomManager.this.mStatus == 3) {
                CineZoomManager.this.setCineZoom(3);
                return;
            }
            CineZoomManager.this.mCineZoomView.setTarget(CineZoomManager.this.mCineZoomGuideView.getGuideRect(), CineZoomManager.this.mCineZoomGuideView.mScreen);
            CineZoomManager.this.mCineZoomGuideView.setVisibility(8);
            CineZoomManager.this.setCinemaModeGuideViewVisibility(false);
            if (CineZoomManager.this.mDirection == 1) {
                CineZoomManager.this.setCineZoom(2);
            } else {
                CineZoomManager.this.setCineZoom(1);
            }
            CineZoomManager.this.setBtnEnabled(CineZoomManager.this.mZoomInOutButton, true);
        }
    }

    /* renamed from: com.lge.camera.managers.CineZoomManager$5 */
    class C08625 implements OnClickListener {
        C08625() {
        }

        public void onClick(View v) {
            switch (CineZoomManager.this.mZoomSpeed) {
                case 100:
                    CineZoomManager.this.mZoomSpeed = 300;
                    ((RotateImageButton) v).setImageLevel(0);
                    break;
                case 200:
                    CineZoomManager.this.mZoomSpeed = 100;
                    ((RotateImageButton) v).setImageLevel(2);
                    break;
                case 300:
                    CineZoomManager.this.mZoomSpeed = 200;
                    ((RotateImageButton) v).setImageLevel(1);
                    break;
            }
            if (CineZoomManager.this.mStatus == 3) {
                CineZoomManager.this.setCineZoom(2);
            } else if (CineZoomManager.this.mStatus == 1) {
                CineZoomManager.this.setCineZoom(1);
            }
            CamLog.m3d(CameraConstants.TAG, "speed button clicked mZoomSpeed = " + CineZoomManager.this.mZoomSpeed);
        }
    }

    public CineZoomManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    private boolean is18BY9() {
        String videoSizeStr = this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), 0));
        if (videoSizeStr == null) {
            return false;
        }
        return is18BY9(videoSizeStr);
    }

    private boolean is18BY9(String str) {
        int[] size = Utils.sizeStringToArray(str.split("@")[0]);
        if (size[0] == 0 || Float.compare(((float) size[1]) / ((float) size[0]), 0.5f) != 0) {
            return false;
        }
        return true;
    }

    private boolean is16BY9() {
        String videoSizeStr = this.mGet.getSettingValue(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), 0));
        if (videoSizeStr == null) {
            return false;
        }
        return is16BY9(videoSizeStr);
    }

    private boolean is16BY9(String str) {
        int[] size = Utils.sizeStringToArray(str.split("@")[0]);
        if (size[0] == 0 || Float.compare(((float) size[1]) / ((float) size[0]), 0.5625f) != 0) {
            return false;
        }
        return true;
    }

    public void init() {
        super.init();
        this.mCineZoomGuideView = new CineZoomGuideView(getAppContext(), is16BY9());
        this.mCineZoomView = new CineZoomView(getAppContext());
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null && this.mCineZoomGuideView != null && this.mCineZoomView != null) {
            vg.addView(this.mCineZoomGuideView);
            vg.addView(this.mCineZoomView);
            this.mCineZoomGuideView.setVisibility(8);
            this.mCineZoomView.setVisibility(8);
            this.mGet.inflateView(C0088R.layout.cine_zoom_layout, vg);
            this.mCineZoomLayout = (LinearLayout) vg.findViewById(C0088R.id.cine_zoom_layout);
            this.mCZchildLayout = (LinearLayout) vg.findViewById(C0088R.id.cine_zoom_child_layout);
            this.mCineModeBaseView = this.mGet.inflateView(C0088R.layout.cine_mode_guide, (FrameLayout) this.mGet.findViewById(C0088R.id.contents_base));
            this.mGuideTextView = (TextView) this.mCineModeBaseView.findViewById(C0088R.id.cine_mode_guide_layout_textview);
            View cineJogZoomView = this.mGet.inflateView(C0088R.layout.cine_zoom_bar, (FrameLayout) this.mGet.findViewById(C0088R.id.contents_base));
            if (this.mCineZoomLayout != null && this.mCZchildLayout != null && cineJogZoomView != null) {
                LayoutParams lp;
                setupCineZoomButtons();
                this.mCineJogZoomLayout = (RelativeLayout) cineJogZoomView.findViewById(C0088R.id.cine_jog_zoom_controller_rotate);
                ((RelativeLayout.LayoutParams) this.mCineJogZoomLayout.getLayoutParams()).bottomMargin = sCineZoomBarMargin;
                this.mPlusButton = (RotateImageButton) this.mCineJogZoomLayout.findViewById(C0088R.id.btn_cine_zoom_plus);
                this.mMinusButton = (RotateImageButton) this.mCineJogZoomLayout.findViewById(C0088R.id.btn_cine_zoom_minus);
                setupTextViews();
                this.mCineJogZoomBar = (CineZoomBar) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_mini_activity_zoom_bar);
                this.mCineJogZoomSpeedBar = (LinearLayout) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_speed_bar);
                if (this.mCineJogZoomBar != null) {
                    this.mCineJogZoomBar.init(0, 60, 28);
                    this.mCineJogZoomBar.setOnVerticalSeekBarListener(new C08581());
                    lp = (LayoutParams) this.mCineJogZoomBar.getLayoutParams();
                    lp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.45f);
                    lp.height = this.mCineJogZoomBar.getBarHeight();
                    this.mCineJogZoomBar.setLayoutParams(lp);
                }
                this.mCineJogZoomBarDisabled = (CineZoomBarDisabled) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_mini_activity_zoom_bar_disabled);
                if (this.mCineJogZoomBarDisabled != null) {
                    this.mCineJogZoomBarDisabled.init(0, 60, 28);
                    lp = (LayoutParams) this.mCineJogZoomBarDisabled.getLayoutParams();
                    lp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.45f);
                    lp.height = this.mCineJogZoomBarDisabled.getBarHeight();
                    this.mCineJogZoomBarDisabled.setLayoutParams(lp);
                }
                setCineZoomBarBtnMargin();
                this.mCineJogZoomLayout.setVisibility(8);
            }
        }
    }

    private void setupTextViews() {
        int size = this.mGet.getActivity().getResources().getDimensionPixelSize(R.dimen.type_f02_sp) / 2;
        this.mTV1 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text1);
        this.mTV2 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text2);
        this.mTV3 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text3);
        this.mTV4 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text4);
        this.mTV5 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text5);
        this.mTV6 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text6);
        this.mTV7 = (RotateTextView) this.mCineJogZoomLayout.findViewById(C0088R.id.cine_jog_zoom_text7);
        this.mTV1.setText("x3");
        this.mTV1.setTextSize(size);
        this.mTV1.setTextColor(-1);
        this.mTV2.setText("x2");
        this.mTV2.setTextSize(size);
        this.mTV2.setTextColor(-1);
        this.mTV3.setText("x1");
        this.mTV3.setTextSize(size);
        this.mTV3.setTextColor(-1);
        this.mTV4.setText("x0");
        this.mTV4.setTextSize(size);
        this.mTV4.setVisibility(4);
        this.mTV5.setText("x1");
        this.mTV5.setTextSize(size);
        this.mTV5.setTextColor(-1);
        this.mTV6.setText("x2");
        this.mTV6.setTextSize(size);
        this.mTV6.setTextColor(-1);
        this.mTV7.setText("x3");
        this.mTV7.setTextSize(size);
        this.mTV7.setTextColor(-1);
    }

    private void setCineZoomBarBtnMargin() {
        LayoutParams lp;
        sCineZoomBarBtnMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.014f) * -1;
        if (this.mPlusButton != null) {
            lp = (LayoutParams) this.mPlusButton.getLayoutParams();
            lp.setMarginStart(sCineZoomBarBtnMargin);
            this.mPlusButton.setLayoutParams(lp);
        }
        if (this.mMinusButton != null) {
            lp = (LayoutParams) this.mMinusButton.getLayoutParams();
            lp.setMarginEnd(sCineZoomBarBtnMargin);
            this.mMinusButton.setLayoutParams(lp);
        }
        if (this.mCineJogZoomSpeedBar != null) {
            lp = (LayoutParams) this.mCineJogZoomSpeedBar.getLayoutParams();
            lp.width = sCineZoomSpeedBarWidth;
            this.mCineJogZoomSpeedBar.setLayoutParams(lp);
        }
    }

    private void onCineZoomBarValueChanged(int value) {
        this.mIsCineZoomJogBarTouching = true;
        if (this.mGet != null) {
            int mState = this.mGet.getCameraState();
            if ((!this.mShutterClickedRecordingNotStarted && mState == 1) || mState == 6) {
                int command;
                if (value < 5) {
                    command = 1;
                    this.mZoomSpeed = 100;
                    value = 10;
                } else if (value < 15) {
                    command = 1;
                    this.mZoomSpeed = 200;
                    value = 20;
                } else if (value < 25) {
                    command = 1;
                    this.mZoomSpeed = 300;
                    value = 30;
                } else if (value < 39) {
                    return;
                } else {
                    if (value < 50) {
                        command = 2;
                        this.mZoomSpeed = 300;
                        value = 40;
                    } else if (value < 60) {
                        command = 2;
                        this.mZoomSpeed = 200;
                        value = 50;
                    } else {
                        command = 2;
                        this.mZoomSpeed = 100;
                        value = 60;
                    }
                }
                if ((command == 2 && this.mStatus == 0) || ((command == 1 && this.mStatus == 2) || value == this.mPreValue)) {
                    CamLog.m3d(CameraConstants.TAG, "onCineZoomBarValueChanged return : ");
                    return;
                }
                setCineZoom(command);
                CamLog.m3d(CameraConstants.TAG, "onCineZoomBarValueChanged setCineZoom command : " + command + " mStatus: " + this.mStatus + " mZoomSpeed: " + this.mZoomSpeed + " value: " + value);
                this.mPreValue = value;
                AudioUtil.performHapticFeedback(this.mCineJogZoomBar, 65574);
            }
        }
    }

    private void onCineZoomBarTouchUp() {
        if (FunctionProperties.getSupportedHal() == 2) {
            this.mPreValue = 28;
            setCineZoom(3);
            AudioUtil.performHapticFeedback(this.mCineJogZoomBar, 65575);
        } else if (this.mStatus == 1 || this.mStatus == 3) {
            this.mPreValue = 28;
            setCineZoom(3);
            AudioUtil.performHapticFeedback(this.mCineJogZoomBar, 65575);
        }
        this.mIsCineZoomJogBarTouching = false;
    }

    private void setupCineZoomButtons() {
        if (this.mCineZoomLayout != null) {
            sCineZoomButtonsBottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.206f);
            sCineZoomBarMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.2f);
            sCineZoomButtonsRightMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.014f);
            sCineZoomButtonsSize = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.179f);
            sCineZoomButtonsTextPadding = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.01f);
            sCineZoomSpeedBarWidth = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.5f);
            FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) this.mCineZoomLayout.getLayoutParams();
            param.bottomMargin = sCineZoomButtonsBottomMargin;
            param.rightMargin = sCineZoomButtonsRightMargin;
            this.mCineZoomLayout.setLayoutParams(param);
            this.mArrowBtn = (ImageView) this.mCineZoomLayout.findViewById(C0088R.id.cine_zoom_arrow);
            this.mArrowBtn.setVisibility(8);
            this.mCineZoomButton = (RotateImageButton) this.mCineZoomLayout.findViewById(C0088R.id.btn_cine_zoom);
            this.mCineZoomButton.setText(getAppContext().getString(C0088R.string.initial_guide_title_point_zoom));
            this.mCineZoomButton.setTextPaddingBottom(sCineZoomButtonsTextPadding);
            this.mCineZoomButton.setTextSize((int) Utils.dpToPx(getAppContext(), 10.0f));
            this.mCineZoomButton.setOnClickListener(this.mCineZoomButtonOnClickListener);
            this.mZoomInOutButton = (RotateImageButton) this.mCineZoomLayout.findViewById(C0088R.id.btn_cine_zoom_in_out);
            this.mZoomInOutButton.setOnClickListener(this.mZoomInOutButtonOnClickListener);
            this.mPlayPauseButton = (RotateImageButton) this.mCineZoomLayout.findViewById(C0088R.id.btn_cine_zoom_play_pause);
            this.mPlayPauseButton.setOnClickListener(this.mPlayPauseButtonOnClickListener);
            this.mSpeedButton = (RotateImageButton) this.mCineZoomLayout.findViewById(C0088R.id.btn_cine_zoom_speed);
            this.mSpeedButton.setOnClickListener(this.mSpeedButtonOnClickListener);
            this.mSpeedButton.setImageLevel(1);
        }
    }

    public boolean isCenterZoomAvailable() {
        if (this.mCineZoomGuideView == null || this.mCineZoomView == null || this.mCineJogZoomLayout.getVisibility() == 8) {
            return true;
        }
        return false;
    }

    public boolean isFocusAvailable() {
        if (this.mCineZoomButton == null || !this.mCineZoomButton.isSelected()) {
            return true;
        }
        return false;
    }

    public void onStopCineZoom() {
        if (this.mCineZoomButton.isSelected()) {
            this.mZoomInOutButton.setText(this.mGet.getAppContext().getString(C0088R.string.camera_cz_zoom_in));
            this.mZoomInOutButton.setImageLevel(0);
            this.mPlayPauseButton.setText(this.start);
            this.mPlayPauseButton.setImageLevel(0);
            setBtnEnabled(this.mZoomInOutButton, false);
            setBtnEnabled(this.mPlayPauseButton, false);
        }
        if (!this.mCineZoomBarEnabled) {
            setCineZoomBarEnabled(true);
        }
        setBtnEnabled(this.mCineZoomButton, true);
        if (this.mStatus != 0) {
        }
    }

    public void onDisableCineZoom() {
        this.mCineZoomButton.setSelected(false);
        setBtnEnabled(this.mZoomInOutButton, false);
        setBtnEnabled(this.mPlayPauseButton, false);
        setBtnEnabled(this.mSpeedButton, false);
        if (this.mStatus != 0) {
        }
        setCineZoom(4);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                CineZoomManager.this.mCineZoomView.setVisibility(8);
                CineZoomManager.this.mCineZoomView.invalidate();
                CineZoomManager.this.mCineZoomGuideView.setVisibility(8);
            }
        }, 0);
        setCineZoomChildLayoutVisibility(false);
        setCinemaModeGuideViewVisibility(false);
    }

    public void onEnableCineZoom() {
        this.mCineZoomButton.setSelected(true);
        if (this.mGet.checkModuleValidate(192)) {
            setBtnEnabled(this.mPlayPauseButton, false);
            this.mGuideTextView.setText(C0088R.string.camera_cz_guide_zoom);
        } else {
            this.mPlayPauseButton.setText(this.start);
            this.mPlayPauseButton.setImageLevel(0);
            setBtnEnabled(this.mPlayPauseButton, true);
            this.mGuideTextView.setText(C0088R.string.camera_cz_guide_zoom_start);
        }
        setBtnEnabled(this.mSpeedButton, true);
        this.mGuideTextView.setText(C0088R.string.camera_cz_tap_area_drag_slider1);
        this.mZoomInOutButton.setText(this.mGet.getAppContext().getString(C0088R.string.camera_cz_zoom_in));
        this.mZoomInOutButton.setImageLevel(0);
        if (!this.mCineZoomBarEnabled) {
            setCineZoomBarEnabled(true);
        }
        setBtnEnabled(this.mZoomInOutButton, false);
        setCineZoomChildLayoutVisibility(true);
        setCinemaModeGuideViewVisibility(true);
        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEATURE_NAME_CINE_ZOOM);
        this.mGet.onPrepareCineZoom();
    }

    public void onVideoShutterClicked() {
        CamLog.m3d(CameraConstants.TAG, "CineZoom onVideoShutterClicked");
        if (this.mCineZoomButton.isSelected()) {
            this.mShutterClickedRecordingNotStarted = true;
            CamLog.m3d(CameraConstants.TAG, "CineZoom doSetParamForStartRecording visible");
        }
        if (!(this.mCineZoomLayout == null || this.mCineZoomLayout.getVisibility() == 0)) {
            this.mCineZoomLayout.setVisibility(0);
        }
        if (this.mCineJogZoomLayout != null && this.mCineZoomButton != null && this.mCineZoomButton.isSelected()) {
            this.mCineJogZoomLayout.setVisibility(0);
            if (this.mCineZoomGuideView != null && this.mStatus == 0) {
                this.mCineZoomGuideView.setVisibility(0);
                setCinemaModeGuideViewVisibility(true);
            }
        }
    }

    public void doRunnableStartRecorder() {
        if (this.mCineZoomButton.isSelected()) {
        }
        this.mShutterClickedRecordingNotStarted = false;
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        this.mCineZoomButton.setDegree(degree, animation);
        this.mZoomInOutButton.setDegree(degree, animation);
        this.mPlayPauseButton.setDegree(degree, animation);
        this.mSpeedButton.setDegree(degree, animation);
        this.mPlusButton.setDegree(degree, animation);
        this.mMinusButton.setDegree(degree, animation);
        this.mTV1.setDegree(degree, animation);
        this.mTV2.setDegree(degree, animation);
        this.mTV3.setDegree(degree, animation);
        this.mTV5.setDegree(degree, animation);
        this.mTV6.setDegree(degree, animation);
        this.mTV7.setDegree(degree, animation);
    }

    public void onVideoPauseClicked() {
        setBtnEnabled(this.mCineZoomButton, false);
        setBtnEnabled(this.mPlayPauseButton, false);
        if (this.mStatus == 1 || this.mStatus == 3) {
            this.mWasCineZoomBefoerRecordingPause = true;
            setCineZoom(3);
        }
        setCineZoomBarEnabled(false);
    }

    public void onVideoResumeClicked() {
        setBtnEnabled(this.mCineZoomButton, true);
        setBtnEnabled(this.mPlayPauseButton, true);
        if (this.mWasCineZoomBefoerRecordingPause) {
            this.mWasCineZoomBefoerRecordingPause = false;
        }
        setCineZoomBarEnabled(true);
    }

    public void onPauseBefore() {
        if (!(FunctionProperties.getSupportedHal() != 2 || this.mStatus == 0 || this.mStatus == 5)) {
            setCineZoom(3);
        }
        super.onPauseBefore();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        this.mCameraDevice = null;
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (this.mCineZoomLayout == null || this.mCineZoomLayout.getVisibility() == 0) {
            CamLog.m3d(CameraConstants.TAG, "onResumeAfter some Layout is null. return");
        } else {
            setCineZoomLayoutVisibility(true);
        }
        if (!(this.mCineZoomGuideView == null || this.mCineZoomButton == null || !this.mCineZoomButton.isSelected())) {
            this.mCineZoomGuideView.setVisibility(0);
            setCinemaModeGuideViewVisibility(true);
        }
        this.mStatus = 0;
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null && this.mCineZoomLayout != null) {
            if (FunctionProperties.getSupportedHal() == 2) {
                setCineZoom(4);
            }
            vg.removeView(this.mCineZoomLayout);
            vg.removeView(this.mCineZoomView);
            vg.removeView(this.mCineZoomGuideView);
            this.mCineZoomLayout = null;
            this.mCZchildLayout = null;
            this.mCineZoomGuideView = null;
            this.mCineZoomView = null;
            this.mCineModeBaseView = null;
            this.mGuideTextView = null;
            this.mCineZoomButton = null;
            this.mZoomInOutButton = null;
            this.mPlayPauseButton = null;
            this.mSpeedButton = null;
            this.mArrowBtn = null;
            this.mCameraDevice = null;
        }
    }

    public void setCineZoomLayoutVisibility(boolean show) {
        if (this.mCineZoomLayout == null || this.mCineZoomGuideView == null || this.mCZchildLayout == null || this.mCineZoomView == null || this.mGet == null || (this.mGet.isTimerShotCountdown() && show)) {
            CamLog.m3d(CameraConstants.TAG, "some Layout is null. return");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "some Layout is show : " + show);
        this.mCineZoomLayout.setVisibility(show ? 0 : 8);
        if (!show) {
            this.mCineJogZoomLayout.setVisibility(8);
            this.mCineZoomGuideView.setVisibility(8);
            setCinemaModeGuideViewVisibility(false);
        } else if (this.mCineZoomButton != null && this.mCineZoomButton.isSelected()) {
            this.mCineJogZoomLayout.setVisibility(0);
            if (this.mStatus == 0) {
                this.mCineZoomGuideView.setVisibility(0);
                setCinemaModeGuideViewVisibility(true);
            }
        }
    }

    public void setCineZoomChildLayoutVisibility(boolean show) {
        int i = 0;
        if (this.mCZchildLayout == null || this.mCineZoomGuideView == null || this.mArrowBtn == null) {
            CamLog.m3d(CameraConstants.TAG, "child some Layout is null. return");
            return;
        }
        int i2;
        CineZoomGuideView cineZoomGuideView = this.mCineZoomGuideView;
        if (show) {
            i2 = 0;
        } else {
            i2 = 8;
        }
        cineZoomGuideView.setVisibility(i2);
        RelativeLayout relativeLayout = this.mCineJogZoomLayout;
        if (!show) {
            i = 8;
        }
        relativeLayout.setVisibility(i);
        setCinemaModeGuideViewVisibility(show);
    }

    public void setRotateDegree(int degree, boolean animation) {
        super.setRotateDegree(degree, animation);
        if (this.mGet.isTimerShotCountdown() || this.mGet.isModeMenuVisible() || this.mGet.isSettingMenuVisible() || this.mGet.isZoomBarVisible() || this.mGet.isHelpListVisible()) {
            setCinemaModeGuideViewVisibility(false);
        } else if (this.mCineZoomLayout == null || this.mCZchildLayout == null) {
            setCinemaModeGuideViewVisibility(false);
        } else if (this.mCineZoomLayout.getVisibility() == 0 && this.mCineZoomButton != null && this.mCineZoomButton.isSelected() && this.mGuideTextView.getVisibility() == 0) {
            setCinemaModeGuideViewVisibility(true);
            updateCinemaModeGuideViewDegree(degree);
        } else {
            setCinemaModeGuideViewVisibility(false);
        }
    }

    public boolean isCineZoomButtonSelected() {
        return this.mCineZoomButton == null ? false : this.mCineZoomButton.isSelected();
    }

    public boolean isCineZooming() {
        if (this.mStatus == 1 || this.mStatus == 3) {
            return true;
        }
        return false;
    }

    public boolean isCineZoomJogBarTouching() {
        return this.mIsCineZoomJogBarTouching;
    }

    public void setupVideosize(String videoSize) {
        if (this.mCineZoomGuideView != null) {
            this.mCineZoomGuideView.setupVideosize(is16BY9(videoSize));
        }
    }
}
