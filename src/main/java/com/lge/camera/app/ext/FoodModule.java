package com.lge.camera.app.ext;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.QuickclipManagerIF.onQuickClipListListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.TalkBackUtil;

public class FoodModule extends FoodModuleBase {
    protected static final int BAR_TYPE_AE = 2;
    protected static final int BAR_TYPE_WB = 1;
    public static final String FILM_FOOD = "1_food.dat";
    private boolean isSupportedColorCorrectionGains = false;
    private FoodWBValue[] mFoodWBValue = new FoodWBValue[3];
    protected int mLastShownBarType = 1;
    protected RotateImageButton mWbBtn;
    private boolean mWbBtnSelected = true;
    protected View mWbLayout;
    private SeekBar mWbSeekBar;
    private OnSeekBarChangeListener mWbSeekBarChangeListener = new C03823();

    /* renamed from: com.lge.camera.app.ext.FoodModule$1 */
    class C03801 implements OnTouchListener {
        C03801() {
        }

        public boolean onTouch(View arg0, MotionEvent arg1) {
            if ((FoodModule.this.mWbLayout == null || FoodModule.this.mWbLayout.getVisibility() == 0) && FoodModule.this.mFoodWBValue[FoodModule.this.mCameraId] != null && FoodModule.this.mFoodWBValue[FoodModule.this.mCameraId].isFixedInitValue()) {
                return false;
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.app.ext.FoodModule$2 */
    class C03812 implements OnClickListener {
        C03812() {
        }

        public void onClick(View v) {
            boolean z = true;
            int i = 0;
            if (FoodModule.this.mWbSeekBar != null && FoodModule.this.mSnapShotChecker != null && FoodModule.this.mSnapShotChecker.getSnapShotState() < 1) {
                if (v.isSelected()) {
                    z = false;
                }
                v.setSelected(z);
                SeekBar access$300 = FoodModule.this.mWbSeekBar;
                if (!v.isSelected()) {
                    i = 8;
                }
                access$300.setVisibility(i);
                FoodModule.this.mWbBtnSelected = v.isSelected();
                if (FoodModule.this.mWbBtnSelected) {
                    FoodModule.this.onShowWBBar();
                } else {
                    FoodModule.this.onHideWBBar();
                }
                TalkBackUtil.setTalkbackDescOnDoubleTap(FoodModule.this.getAppContext(), FoodModule.this.mGet.getAppContext().getString(FoodModule.this.mWbBtnSelected ? C0088R.string.talkback_btn_selected : C0088R.string.talkback_btn_not_selected));
            }
        }
    }

    /* renamed from: com.lge.camera.app.ext.FoodModule$3 */
    class C03823 implements OnSeekBarChangeListener {
        C03823() {
        }

        public void onStopTrackingTouch(SeekBar arg0) {
        }

        public void onStartTrackingTouch(SeekBar arg0) {
        }

        public void onProgressChanged(SeekBar bar, int level, boolean arg2) {
            if (FoodModule.this.mFoodWBValue[FoodModule.this.mCameraId] == null) {
                CamLog.m3d(CameraConstants.TAG, "mFoodWBValue is null mCameraId = " + FoodModule.this.mCameraId);
                return;
            }
            FoodModule.this.mCurrentLevel = level;
            FoodModule.this.mFoodWBValue[FoodModule.this.mCameraId].onProgressChanged(bar, level);
        }
    }

    /* renamed from: com.lge.camera.app.ext.FoodModule$4 */
    class C03834 implements onQuickClipListListener {
        C03834() {
        }

        public void onListOpend() {
            FoodModule.this.access$1300(false);
            FoodModule.this.setWBLayoutVisibility(false);
            FoodModule.this.access$1200(false);
        }

        public void onListClosed() {
            FoodModule.this.access$1300(true);
            FoodModule.this.setWBLayoutVisibility(true);
            if (FoodModule.this.mLastShownBarType == 2) {
                FoodModule.this.access$1400();
            }
        }
    }

    public FoodModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected boolean checkToUseFilmEffect() {
        return true;
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, FILM_FOOD, false);
        if (FunctionProperties.isAppyingFilmLimitation()) {
            setSpecificSettingValueAndDisable("hdr-mode", "0", false);
            setSettingMenuEnable("flash-mode", true);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        if (FunctionProperties.isSupportedFilmRecording()) {
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, ParamConstants.VIDEO_3840_BY_2160, false);
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, CameraConstants.VIDEO_FHD_60FPS, false);
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, ParamConstants.VIDEO_3840_BY_2160, false);
            this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, CameraConstants.VIDEO_FHD_60FPS, false);
            if (isUHDmode() || isFHD60()) {
                this.mGet.setSetting(Setting.KEY_VIDEO_RECORDSIZE, this.mGet.getListPreference(Setting.KEY_VIDEO_RECORDSIZE).getDefaultValue(), false);
                this.mGet.setSetting(Setting.KEY_VIDEO_RECORDSIZE_SUB, this.mGet.getListPreference(Setting.KEY_VIDEO_RECORDSIZE_SUB).getDefaultValue(), false);
            }
        } else {
            setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, false);
            setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, false);
        }
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        if (FunctionProperties.isAppyingFilmLimitation()) {
            restoreSettingValue("hdr-mode");
        }
        restoreSettingValue(Setting.KEY_QR);
        if (FunctionProperties.isSupportedFilmRecording()) {
            restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE);
            restoreSettingValue(Setting.KEY_VIDEO_RECORDSIZE_SUB);
            for (String size : ConfigurationResolutionUtilBase.sVIDEO_SIZE_REAR_SUPPORTED_ITEMS) {
                this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, size, true);
                this.mGet.setSettingChildMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, size, true);
            }
        } else {
            setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE, true);
            setSettingMenuEnable(Setting.KEY_VIDEO_RECORDSIZE_SUB, true);
        }
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected boolean isAvailableSteadyCam() {
        if (FunctionProperties.isSupportedFilmRecording()) {
            return super.isAvailableSteadyCam();
        }
        return false;
    }

    public void initUI(View baseParent) {
        super.initUI(baseParent);
        initFoodUI(baseParent);
    }

    private void initFoodUI(View baseParent) {
        if (baseParent == null) {
            CamLog.m3d(CameraConstants.TAG, "baseParent is null");
            return;
        }
        inflateView(C0088R.layout.food_wb_controller, (ViewGroup) baseParent);
        this.mWbLayout = baseParent.findViewById(C0088R.id.food_wb_controller);
        this.mWbSeekBar = (SeekBar) baseParent.findViewById(C0088R.id.food_wb_seekBar);
        this.mWbBtn = (RotateImageButton) baseParent.findViewById(C0088R.id.food_wb_btn);
        if (this.mWbLayout == null || this.mWbSeekBar == null || this.mWbBtn == null) {
            CamLog.m3d(CameraConstants.TAG, "mWbLayout or mWbSeekBar or mWbBtn is null");
            return;
        }
        LayoutParams layoutLp = (LayoutParams) this.mWbLayout.getLayoutParams();
        RelativeLayout.LayoutParams barLp = (RelativeLayout.LayoutParams) this.mWbSeekBar.getLayoutParams();
        Drawable wbBtn = getAppContext().getDrawable(C0088R.drawable.btn_food_wb);
        Drawable progressBar = getAppContext().getDrawable(C0088R.drawable.bg_camera_food_progressbar);
        if (barLp == null || layoutLp == null || progressBar == null || wbBtn == null) {
            CamLog.m3d(CameraConstants.TAG, "getLayoutParams or getDrawable fail");
            return;
        }
        layoutLp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.209f) - Math.max(0, (wbBtn.getIntrinsicHeight() - progressBar.getIntrinsicHeight()) / 2);
        layoutLp.height = wbBtn.getIntrinsicHeight();
        barLp.width = progressBar.getIntrinsicWidth() + (this.mWbSeekBar.getPaddingStart() * 2);
        this.mWbLayout.setLayoutParams(layoutLp);
        this.mWbSeekBar.setLayoutParams(barLp);
        if (this.mCurrentLevel != -1) {
            this.mWbSeekBar.setProgress(this.mCurrentLevel);
        }
        this.mWbBtn.setSelected(this.mWbBtnSelected);
        this.mWbBtn.setDegree(getOrientationDegree(), false);
        this.mWbSeekBar.setVisibility(this.mWbBtnSelected ? 0 : 8);
    }

    public void initUIDone() {
        super.initUIDone();
        if (this.mWbSeekBar == null || this.mWbBtn == null) {
            CamLog.m3d(CameraConstants.TAG, "mWbSeekBar or mWbBtn is null");
            return;
        }
        this.mWbSeekBar.setOnTouchListener(new C03801());
        this.mWbSeekBar.setOnSeekBarChangeListener(this.mWbSeekBarChangeListener);
        this.mWbBtn.setOnClickListener(new C03812());
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (!FunctionProperties.isSupportedFilmRecording()) {
            this.mCaptureButtonManager.changeButtonByMode(12);
        }
    }

    public int getShutterButtonType() {
        return FunctionProperties.isSupportedFilmRecording() ? 3 : 4;
    }

    public void onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "shutter large clicked.");
        if (checkModuleValidate(15) && !this.mGet.isAnimationShowing() && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            switch (this.mCaptureButtonManager.getShutterButtonMode(4)) {
                case 12:
                    onShutterBottomButtonClickListener();
                    return;
                default:
                    super.onShutterLargeButtonClicked();
                    return;
            }
        }
    }

    protected void updateWBValueFromMetadataCallback(float currentWB) {
        super.updateWBValueFromMetadataCallback(currentWB);
        this.mFoodWBValue[this.mCameraId].setValue(3, ((int) (currentWB / 100.0f)) * 100);
    }

    public void onPauseAfter() {
        for (FoodWBValue value : this.mFoodWBValue) {
            if (value != null) {
                value.onPauseAfter();
            }
        }
        super.onPauseAfter();
    }

    protected void changeRequester() {
        int i = 2;
        super.changeRequester();
        if (this.mFoodWBValue[this.mCameraId] == null) {
            FoodWBValue[] foodWBValueArr;
            if (this.isSupportedColorCorrectionGains) {
                this.mFoodWBValue[this.mCameraId] = new API2FoodWBValue();
                foodWBValueArr = this.mFoodWBValue;
                if (this.mCameraId != 0) {
                    i = 0;
                }
                FoodWBValue anotherValue = foodWBValueArr[i];
                if (anotherValue != null && anotherValue.isFixedLastValue()) {
                    this.mFoodWBValue[this.mCameraId].setValue(4);
                }
            } else {
                foodWBValueArr = this.mFoodWBValue;
                FoodWBValue[] foodWBValueArr2 = this.mFoodWBValue;
                API1FoodWBValue aPI1FoodWBValue = new API1FoodWBValue();
                foodWBValueArr2[2] = aPI1FoodWBValue;
                foodWBValueArr[0] = aPI1FoodWBValue;
                this.mFoodWBValue[this.mCameraId].setValue(2);
            }
        }
        this.mFoodWBValue[this.mCameraId].changeRequseter();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        if (this.mWbSeekBar != null) {
            this.mWbSeekBar.setOnSeekBarChangeListener(null);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (!(vg == null || this.mWbLayout == null)) {
            vg.removeView(this.mWbLayout);
        }
        this.mWbLayout = null;
        this.mWbSeekBar = null;
        this.mWbBtn = null;
    }

    public void onOrientationChanged(int degree, boolean isFirst) {
        super.onOrientationChanged(degree, isFirst);
        if (this.mWbBtn != null) {
            this.mWbBtn.setRotated(degree);
        }
    }

    public void onZoomShow() {
        super.onZoomShow();
        setWBLayoutVisibility(false);
    }

    public void onZoomHide() {
        super.onZoomHide();
        setWBLayoutVisibility(true);
    }

    public void setWBLayoutVisibility(boolean visible) {
        if (this.mWbLayout != null) {
            View view = this.mWbLayout;
            int i = (!visible || this.mZoomManager.isZoomBarVisible() || this.mZoomManager.isJogZoomMoving() || isRecordingState()) ? 8 : 0;
            view.setVisibility(i);
        }
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        setWBLayoutVisibility(false);
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL)) {
            return true;
        }
        setWBLayoutVisibility(true);
        return true;
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        setWBLayoutVisibility(false);
        return true;
    }

    public void prepareRecordingVideo() {
        super.prepareRecordingVideo();
        if (isTimerShotCountdown()) {
            setWBLayoutVisibility(false);
        }
    }

    public boolean onVideoShutterClickedBefore() {
        setWBLayoutVisibility(false);
        return super.onVideoShutterClickedBefore();
    }

    protected void afterStopRecording() {
        setWBLayoutVisibility(true);
        super.afterStopRecording();
    }

    protected boolean displayUIComponentAfterOneShot() {
        setWBLayoutVisibility(true);
        return super.displayUIComponentAfterOneShot();
    }

    protected void onTakePictureBefore() {
        super.onTakePictureBefore();
        if (isTimerShotCountdown()) {
            setWBLayoutVisibility(false);
        }
    }

    protected void onTakePictureAfter() {
        super.access$100();
        setWBLayoutVisibility(true);
    }

    protected void setQuickClipListListener() {
        if (this.mQuickClipManager == null) {
            CamLog.m3d(CameraConstants.TAG, "mQuickClipManager is null");
        } else {
            this.mQuickClipManager.setQuickClipListListener(new C03834());
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return true;
    }

    public String getShotMode() {
        return "mode_food";
    }

    protected void setParameterByLGSF(CameraParameters parameters, String shotMode, boolean isRecording) {
        super.setParameterByLGSF(parameters, "mode_normal", isRecording);
    }

    public String getRecordingType() {
        return CameraConstants.VIDEO_FOOD_TYPE;
    }

    public void onShowWBBar() {
        access$1200(false);
        this.mLastShownBarType = 1;
    }

    public void onHideWBBar() {
        access$1400();
    }

    public void onShowAEBar() {
        super.onShowAEBar();
        if (!(!this.mWbBtnSelected || this.mWbSeekBar == null || this.mWbBtn == null)) {
            this.mWbSeekBar.setVisibility(8);
            this.mWbBtn.setSelected(false);
        }
        this.mLastShownBarType = 2;
    }

    public void onHideAEBar() {
        super.onHideAEBar();
        if (this.mWbBtnSelected && this.mWbSeekBar != null && this.mWbBtn != null) {
            this.mWbSeekBar.setVisibility(0);
            this.mWbBtn.setSelected(true);
        }
    }

    protected String getLDBNonSettingString() {
        String extraStr = super.getLDBNonSettingString();
        if (this.mFoodWBValue[this.mCameraId] != null) {
            return extraStr + "food_wb_level=" + this.mCurrentLevel + ";" + this.mFoodWBValue[this.mCameraId].getLdbStringExtra();
        }
        return extraStr;
    }

    public boolean isSupportedFilterMenu() {
        return false;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        initFoodUI(this.mBaseParentView);
    }
}
