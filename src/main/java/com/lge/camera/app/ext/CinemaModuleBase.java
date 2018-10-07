package com.lge.camera.app.ext;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.CineZoomManager;
import com.lge.camera.managers.RecordingUIManager;
import com.lge.camera.managers.ext.CinemaFilterInterface;
import com.lge.camera.managers.ext.CinemaFilterManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

public class CinemaModuleBase extends RecordingPriorityModule implements CinemaFilterInterface {
    private int[] filterSelectedIcon = new int[]{C0088R.drawable.btn_quickbutton_cine_filter_selected, C0088R.drawable.btn_quickbutton_cine_filter_pressed};
    protected CineZoomManager mCineZoomManager = new CineZoomManager(this);
    protected CinemaFilterManager mCinemaFilterManager = new CinemaFilterManager(this);
    private View mCinemaInitGuideLayout;
    private boolean mInitGuideZoomInBtnDrawable = true;
    private int mLDUIntent = 0;
    private boolean mStateIdleAfterStopRecording = true;
    protected boolean mSteadyChangedWithReopenCamera = false;
    private int[] noneSelectedIcon = new int[]{C0088R.drawable.btn_quickbutton_cine_filter_normal, C0088R.drawable.btn_quickbutton_cine_filter_pressed};

    /* renamed from: com.lge.camera.app.ext.CinemaModuleBase$2 */
    class C03592 implements OnClickListener {
        C03592() {
        }

        public void onClick(View arg0) {
            CamLog.m3d(CameraConstants.TAG, "cine init guide OK button clicked");
            if (CinemaModuleBase.this.mCinemaInitGuideLayout != null) {
                CheckBox checkBox = (CheckBox) CinemaModuleBase.this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_checkBox);
                if (checkBox != null && checkBox.isChecked()) {
                    SharedPreferenceUtil.setCinemaInitGuideShown(CinemaModuleBase.this.getAppContext(), true);
                }
                CinemaModuleBase.this.hideInitGuide(true);
            }
        }
    }

    public CinemaModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void initializeSettingMenus() {
        this.mGet.setAllSettingMenuEnable(true);
        setSettingMenuEnable("picture-size", false);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, false);
        this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, ParamConstants.VIDEO_3840_BY_2160, false);
        this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, CameraConstants.VIDEO_FHD_60FPS, false);
        if (isUHDmode() || isFHD60()) {
            this.mGet.setSetting(Setting.KEY_VIDEO_RECORDSIZE, this.mGet.getListPreference(Setting.KEY_VIDEO_RECORDSIZE).getDefaultValue(), false);
        }
        this.mGet.refreshSettingByCameraId();
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "1", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable(Setting.KEY_FINGER_DETECTION, "off", false);
        restoreTrackingAFSetting();
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        setSettingMenuEnable("picture-size", true);
        setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, true);
        for (String size : ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS) {
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, size, true);
        }
        restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE);
        restoreSettingValue(Setting.KEY_VOICESHUTTER);
        restoreSettingValue("hdr-mode");
        restoreFlashSetting();
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSettingValue(Setting.KEY_FINGER_DETECTION);
        restoreTrackingAFSetting();
        this.mRecordingFlashOn = false;
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_FULLVISION);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    public int getVideoBitrate() {
        ListPreference pref = this.mGet.getListPreference(Setting.KEY_MANUAL_VIDEO_BITRATE);
        if (pref == null || pref.getEntryValues() == null) {
            return super.getVideoBitrate();
        }
        return Integer.valueOf(pref.getEntryValues()[0].toString()).intValue();
    }

    public int getCameraIdFromPref() {
        return 0;
    }

    public void init() {
        super.init();
        setmLDUIntent();
        showInitGuide();
        this.mStateIdleAfterStopRecording = true;
    }

    private void setmLDUIntent() {
        if (ModelProperties.isRetailModeInstalled()) {
            this.mLDUIntent = this.mGet.getActivityIntent().getIntExtra("ldu_cine_video", 0);
        }
    }

    private void showInitGuide() {
        if (!SharedPreferenceUtil.getCinemaInitGuideShown(getAppContext()) && this.mLDUIntent == 0 && !isCinemaInitGuideShowing()) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
            if (vg != null) {
                inflateView(C0088R.layout.cinema_mode_init_guide, vg);
                this.mCinemaInitGuideLayout = vg.findViewById(C0088R.id.cinema_init_guide_rotate_layout);
                if (this.mCinemaInitGuideLayout != null) {
                    setInitGuideLayout();
                    rotateInitGuide(getOrientationDegree());
                    this.mCinemaInitGuideLayout.setVisibility(0);
                    onShowMenu(256);
                    CamLog.m3d(CameraConstants.TAG, "show cinema init guide");
                }
            }
        }
    }

    private void hideInitGuide(boolean showInitDialog) {
        if (this.mCinemaInitGuideLayout != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
            if (vg != null) {
                vg.removeView(this.mCinemaInitGuideLayout);
                onHideMenu(256);
                this.mCinemaInitGuideLayout = null;
                if (showInitDialog) {
                    showInitDialog();
                }
            }
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        switch (this.mLDUIntent) {
            case 1:
                showCinemaLUTLayout();
                setCinemaQuickButtonIcon();
                break;
            case 2:
                if (this.mCineZoomManager != null) {
                    this.mCineZoomManager.onEnableCineZoom();
                    break;
                }
                break;
        }
        this.mLDUIntent = 0;
    }

    public boolean isCinemaInitGuideShowing() {
        return this.mCinemaInitGuideLayout == null ? false : this.mCinemaInitGuideLayout.isShown();
    }

    private void rotateInitGuide(int degree) {
        if (this.mCinemaInitGuideLayout != null && this.mCinemaInitGuideLayout.getVisibility() == 0) {
            boolean isLand;
            int sizeCalculatedByPercentage;
            int paddingStartEnd;
            ((RotateLayout) this.mCinemaInitGuideLayout).rotateLayout(degree);
            if (degree == 90 || degree == 270) {
                isLand = true;
            } else {
                isLand = false;
            }
            View targetLayout = this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_title);
            LayoutParams targetLp = targetLayout.getLayoutParams();
            if (isLand) {
                sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.144f);
            } else {
                sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.094f);
            }
            targetLp.height = sizeCalculatedByPercentage;
            targetLayout.setLayoutParams(targetLp);
            targetLayout = this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_top_layout);
            if (isLand) {
                paddingStartEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.132f);
            } else {
                paddingStartEnd = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.083f);
            }
            if (degree == 180) {
                sizeCalculatedByPercentage = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
            } else {
                sizeCalculatedByPercentage = 0;
            }
            targetLayout.setPaddingRelative(paddingStartEnd, sizeCalculatedByPercentage, paddingStartEnd, 0);
            rotateInitGuideItemLayout(this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_filter_layout), isLand);
            targetLayout = this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_zoom_layout);
            if (isLand) {
                sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.042f);
            } else {
                sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.036f);
            }
            targetLayout.setPaddingRelative(0, sizeCalculatedByPercentage, 0, 0);
            rotateInitGuideItemLayout(targetLayout, isLand);
            targetLayout = this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_bottom);
            if (isLand) {
                paddingStartEnd = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
            } else {
                paddingStartEnd = 0;
            }
            if (degree == 0) {
                sizeCalculatedByPercentage = RatioCalcUtil.getNavigationBarHeight(this.mGet.getAppContext());
            } else {
                sizeCalculatedByPercentage = 0;
            }
            targetLayout.setPaddingRelative(0, 0, paddingStartEnd, sizeCalculatedByPercentage);
            targetLayout = this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_checkBox_wrapper);
            targetLp = targetLayout.getLayoutParams();
            int checkBoxPaddingStart = Utils.getPx(getAppContext(), C0088R.dimen.init_guide_checkbox_inner_padding);
            if (isLand) {
                Drawable cineZoomDrawable = getAppContext().getDrawable(C0088R.drawable.camera_initial_image_cine_zoom_land);
                if (cineZoomDrawable != null) {
                    checkBoxPaddingStart = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.167f) + cineZoomDrawable.getIntrinsicWidth();
                }
            }
            targetLayout.setPaddingRelative(checkBoxPaddingStart, 0, 0, 0);
            targetLayout.setLayoutParams(targetLp);
        }
    }

    private void rotateInitGuideItemLayout(View layout, boolean isLand) {
        if (layout != null) {
            ImageView imageView = (ImageView) layout.findViewById(C0088R.id.cinema_init_guide_imageView);
            final ImageView btnView = (ImageView) layout.findViewById(C0088R.id.cinema_init_guide_imageView_btn);
            View imageLayout = layout.findViewById(C0088R.id.cinema_init_guide_imageView_wrapper);
            LinearLayout textLayout = (LinearLayout) layout.findViewById(C0088R.id.cinema_init_guide_text_layout);
            if (imageView != null && textLayout != null && imageLayout != null) {
                LinearLayout.LayoutParams imageLayoutLp = (LinearLayout.LayoutParams) imageLayout.getLayoutParams();
                FrameLayout.LayoutParams imageLp = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                LinearLayout.LayoutParams textLp = (LinearLayout.LayoutParams) textLayout.getLayoutParams();
                if (imageLayoutLp != null && imageLp != null && textLp != null) {
                    Drawable d = imageView.getDrawable();
                    if (d != null) {
                        if (isLand) {
                            imageLp.width = -2;
                            ((LinearLayout) layout).setOrientation(0);
                            imageLayoutLp.setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.035f));
                            d.setLevel(1);
                            textLayout.setPaddingRelative(0, 0, 0, 0);
                            textLp.gravity = 16;
                        } else {
                            imageLp.width = -1;
                            ((LinearLayout) layout).setOrientation(1);
                            imageLayoutLp.setMarginEnd(0);
                            d.setLevel(0);
                            textLayout.setPaddingRelative(0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.021f), 0, 0);
                            textLp.gravity = 0;
                        }
                        imageLayout.setLayoutParams(imageLayoutLp);
                        imageView.setLayoutParams(imageLp);
                        textLayout.setLayoutParams(textLp);
                        if (btnView != null) {
                            this.mInitGuideZoomInBtnDrawable = true;
                            final int[] drawableIds = isLand ? new int[]{C0088R.drawable.camera_initial_image_cine_zoom_in_land, C0088R.drawable.camera_initial_image_cine_zoom_out_land} : new int[]{C0088R.drawable.camera_initial_image_cine_zoom_in, C0088R.drawable.camera_initial_image_cine_zoom_out};
                            btnView.setImageResource(drawableIds[this.mInitGuideZoomInBtnDrawable ? 0 : 1]);
                            btnView.setLayoutParams(imageLp);
                            AnimationUtil.startCinemaInitGuideAnim(imageView, isLand, new AnimationListener() {
                                public void onAnimationStart(Animation animation) {
                                }

                                public void onAnimationEnd(Animation animation) {
                                }

                                public void onAnimationRepeat(Animation animation) {
                                    boolean z;
                                    int i = 0;
                                    CinemaModuleBase cinemaModuleBase = CinemaModuleBase.this;
                                    if (CinemaModuleBase.this.mInitGuideZoomInBtnDrawable) {
                                        z = false;
                                    } else {
                                        z = true;
                                    }
                                    cinemaModuleBase.mInitGuideZoomInBtnDrawable = z;
                                    if (btnView != null && drawableIds != null) {
                                        ImageView imageView = btnView;
                                        int[] iArr = drawableIds;
                                        if (!CinemaModuleBase.this.mInitGuideZoomInBtnDrawable) {
                                            i = 1;
                                        }
                                        imageView.setImageResource(iArr[i]);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private void setInitGuideLayout() {
        if (this.mCinemaInitGuideLayout != null) {
            View view = this.mCinemaInitGuideLayout.findViewById(C0088R.id.cinema_init_guide_ok_btn);
            if (view != null) {
                view.setOnClickListener(new C03592());
            }
        }
    }

    public void onPauseAfter() {
        if ((this.mGet.isPaused() || this.mGet.isModuleChanging()) && this.mCameraDevice != null) {
            CameraParameters parameter = this.mCameraDevice.getParameters();
            if (parameter != null) {
                setParamUpdater(parameter, ParamConstants.KEY_CINEMA_MODE, ParamConstants.CINEMA_OFF);
                this.mCameraDevice.setParameters(parameter);
            }
        }
        hideCinemaLUTLayout();
        super.onPauseAfter();
    }

    protected void changeRequester() {
        super.changeRequester();
        if (this.mParamUpdater != null && this.mCinemaFilterManager != null) {
            if (FunctionProperties.isSupportedHDR10()) {
                this.mParamUpdater.addRequester("hdr10", "on", false, false);
            }
            this.mParamUpdater.setParamValue(ParamConstants.KEY_VIDEO_HDR_MODE, "on");
            this.mParamUpdater.addRequester(ParamConstants.KEY_CINEMA_MODE, ParamConstants.CINEMA_PREVIEW_AND_VIDEO, false, false);
            this.mParamUpdater.addRequester(ParamConstants.KEY_CINEMA_LUT, this.mCinemaFilterManager.getCurrentLUTNum() + "-" + this.mCinemaFilterManager.getCurrentLUTStrength(0), false, true);
            this.mParamUpdater.addRequester(ParamConstants.KEY_CINEMA_VIGNETTE, "1-" + this.mCinemaFilterManager.getCurrentLUTStrength(1), false, true);
        }
    }

    protected void setupVideosize(CameraParameters parameters, ListPreference listPref) {
        super.setupVideosize(parameters, listPref);
        if (this.mCameraDevice != null && parameters != null && listPref != null) {
            String str;
            setParamUpdater(parameters, ParamConstants.KEY_VIDEO_SIZE, listPref.getValue());
            String str2 = ParamConstants.KEY_PREVIEW_SIZE;
            if (ParamConstants.VIDEO_3840_BY_2160.equals(listPref.getValue())) {
                str = "1920x1080";
            } else {
                str = listPref.getValue();
            }
            setParamUpdater(parameters, str2, str);
            setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
            setParameters(parameters);
            if (this.mCineZoomManager != null) {
                this.mCineZoomManager.setupVideosize(listPref.getValue());
            }
        }
    }

    protected void setVideoHDR(CameraParameters parameters, boolean isRecordingStarting) {
    }

    public void onDestroy() {
        super.onDestroy();
        hideInitGuide(false);
        this.mStateIdleAfterStopRecording = true;
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        rotateInitGuide(degree);
    }

    public void onZoomShow() {
        super.onZoomShow();
        hideCinemaLUTLayout();
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        hideCinemaLUTLayout();
    }

    public void prepareRecordingVideo() {
        boolean z = true;
        super.prepareRecordingVideo();
        if (isTimerShotCountdown()) {
            hideCinemaLUTLayout();
        }
        if (this.mRecordingUIManager != null && this.mCinemaFilterManager != null) {
            RecordingUIManager recordingUIManager = this.mRecordingUIManager;
            int currentLUTNum = this.mCinemaFilterManager.getCurrentLUTNum();
            CinemaFilterManager cinemaFilterManager = this.mCinemaFilterManager;
            if (currentLUTNum == 1) {
                z = false;
            }
            recordingUIManager.setCineEffectOn(z);
        }
    }

    public boolean onVideoShutterClickedBefore() {
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        hideCinemaLUTLayout();
        return true;
    }

    private boolean hideCinemaLUTLayout() {
        if (this.mCineZoomManager == null || this.mCinemaFilterManager == null || !this.mCinemaFilterManager.hideCinemaLUTLayout()) {
            return false;
        }
        if (!isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            this.mCineZoomManager.setCineZoomLayoutVisibility(true);
            setQuickClipIcon(false, true);
        }
        setQuickButtonIndex(C0088R.id.quick_button_cinema_filter, 0);
        return true;
    }

    private boolean showCinemaLUTLayout() {
        if (this.mCineZoomManager == null || this.mFocusManager == null || isRecordingState() || this.mCinemaFilterManager == null || !this.mCinemaFilterManager.showCinemaLUTLayout()) {
            return false;
        }
        hideZoomBar();
        this.mCineZoomManager.setCineZoomLayoutVisibility(false);
        hideMenu(CameraConstants.MENU_TYPE_ALL, false, true);
        this.mFocusManager.hideFocus(true);
        setQuickClipIcon(true, false);
        return true;
    }

    public void onShowAEBar() {
        super.onShowAEBar();
        hideCinemaLUTLayout();
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mCinemaFilterManager != null) {
            return extraStr + "cine_lut=" + this.mCinemaFilterManager.getCurrentLUTNum() + ";cine_strength=" + this.mCinemaFilterManager.getCurrentLUTStrength(0) + ";cine_vignette=" + this.mCinemaFilterManager.getCurrentLUTStrength(1) + ";";
        }
        return extraStr;
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        hideCinemaLUTLayout();
        setQuickClipIcon(true, false);
        return true;
    }

    protected void doOnQuickViewShown(boolean isAutoReview) {
        hideCinemaLUTLayout();
        super.doOnQuickViewShown(isAutoReview);
    }

    protected boolean mainHandlerHandleMessage(Message msg) {
        switch (msg.what) {
            case 98:
                showCinemaLUTLayout();
                return true;
            case 99:
                hideCinemaLUTLayout();
                return true;
            default:
                return super.mainHandlerHandleMessage(msg);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mCinemaFilterManager == null || !this.mCinemaFilterManager.isCinemaLUTVisible()) {
            return super.onTouchEvent(event);
        }
        if (event.getActionMasked() != 1) {
            return true;
        }
        hideCinemaLUTLayout();
        return true;
    }

    public void addQuickButtonListDone() {
        super.addQuickButtonListDone();
        setCinemaQuickButtonIcon();
    }

    public void setCinemaQuickButtonIcon() {
        int i = 1;
        if (this.mQuickButtonManager != null && this.mCinemaFilterManager != null) {
            boolean isNoneFilter;
            int currentLUTNum = this.mCinemaFilterManager.getCurrentLUTNum();
            CinemaFilterManager cinemaFilterManager = this.mCinemaFilterManager;
            if (currentLUTNum == 1) {
                isNoneFilter = true;
            } else {
                isNoneFilter = false;
            }
            this.mQuickButtonManager.updateButtonIcon(C0088R.id.quick_button_cinema_filter, isNoneFilter ? this.noneSelectedIcon : this.filterSelectedIcon, C0088R.drawable.btn_quickbutton_cine_filter_pressed);
            if (!isCinemaLUTVisible()) {
                i = 0;
            }
            setQuickButtonIndex(C0088R.id.quick_button_cinema_filter, i);
        }
    }

    public String getAssistantStringFlag(String key, String defaultValue) {
        return this.mGet.getAssistantStringFlag(key, defaultValue);
    }

    public boolean isVoiceAssistantSpecified() {
        return this.mGet.isVoiceAssistantSpecified();
    }

    protected String getMimeType() {
        return FunctionProperties.isSupportedHDR10() ? "video/hevc" : null;
    }

    protected void setupPreview(CameraParameters params) {
        if (this.mGet.isPaused() || checkModuleValidate(64)) {
            super.setupPreview(params);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "skip setup preview");
        setCameraState(1);
    }

    protected void stopRecorder() {
        this.mStateIdleAfterStopRecording = false;
        super.stopRecorder();
    }

    protected void startPreviewDone() {
        this.mStateIdleAfterStopRecording = true;
        super.startPreviewDone();
    }

    public boolean onRecordStartButtonClicked() {
        if (this.mStateIdleAfterStopRecording) {
            return super.onRecordStartButtonClicked();
        }
        CamLog.m3d(CameraConstants.TAG, "startPreviewDone not received. return");
        return false;
    }

    public boolean doBackKey() {
        if (this.mCinemaInitGuideLayout != null && this.mCinemaInitGuideLayout.getVisibility() == 0) {
            hideInitGuide(true);
            return true;
        } else if (this.mSteadyChangedWithReopenCamera) {
            CamLog.m7i(CameraConstants.TAG, "prevent back key while reopen camera");
            return true;
        } else if (hideCinemaLUTLayout() || super.doBackKey()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCinemaLUTVisible() {
        return this.mCinemaFilterManager != null && this.mCinemaFilterManager.isCinemaLUTVisible();
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (this.mCinemaFilterManager != null) {
            this.mCinemaFilterManager.init();
        }
    }
}
