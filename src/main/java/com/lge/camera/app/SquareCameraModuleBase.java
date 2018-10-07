package com.lge.camera.app;

import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.GalleryManagerExpand;
import com.lge.camera.managers.SquareModeInitGuideManager;
import com.lge.camera.managers.SquareSnapGalleryItem;
import com.lge.camera.managers.TimerManager.TimerType;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class SquareCameraModuleBase extends BeautyShotCameraModule {
    protected GalleryManagerExpand mGalleryManager = new GalleryManagerExpand(this);
    protected SquareModeInitGuideManager mSquareInitGuideManger = new SquareModeInitGuideManager(this);

    public SquareCameraModuleBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    public void init() {
        super.init();
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode()) || CameraConstants.MODE_SQUARE_OVERLAP.equals(getShotMode())) {
            this.mGalleryManager.initLayout();
        }
    }

    public void onResumeJustAfter() {
        super.onResumeJustAfter();
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode()) && AppControlUtil.isStartFromOnCreate()) {
            this.mSquareInitGuideManger.setupInitGuideView();
        }
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode()) || CameraConstants.MODE_SQUARE_OVERLAP.equals(getShotMode())) {
            this.mGalleryManager.showMainAccessViewForInitState();
            this.mGalleryManager.setRotateDegree(getOrientationDegree(), false);
        }
    }

    protected void addModuleManager() {
        super.addModuleManager();
        if (!CameraConstants.MODE_SQUARE_GRID.equals(getShotMode())) {
            this.mManagerList.add(this.mGalleryManager);
        }
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            this.mManagerList.add(this.mSquareInitGuideManger);
        }
    }

    protected void changeRequester() {
        super.changeRequester();
        changeInitParameters();
    }

    protected void changeInitParameters() {
    }

    protected void setPreviewLayoutParam() {
        int[] size = Utils.getLCDsize(getAppContext(), true);
        int startMargin = 0;
        if (ModelProperties.getLCDType() == 2) {
            startMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        }
        this.mGet.setTextureLayoutParams(size[1], size[1], startMargin);
        setAnimationLayout(1);
        if (this.mFocusManager != null) {
            this.mFocusManager.setFocusAreaWindow(size[1], size[1], startMargin, 0);
        }
    }

    public void setAnimationLayout(int type) {
        int startMargin = 0;
        if (ModelProperties.getLCDType() == 2) {
            startMargin = RatioCalcUtil.getQuickButtonWidth(this.mGet.getAppContext());
        }
        int[] size = Utils.getLCDsize(this.mGet.getAppContext(), true);
        size[0] = size[1];
        if ((type & 1) != 0) {
            this.mAnimationManager.setSnapshotAniLayout(size[0], size[1], startMargin);
        }
        if ((type & 2) != 0) {
            this.mAnimationManager.setSwitchAniLayout(size[0], size[1], -1);
        }
    }

    protected String getLiveSnapShotSize(CameraParameters parameters, String value) {
        if (isRearCamera()) {
            return "2340x2340";
        }
        return "1440x1440";
    }

    protected void onCameraSwitchingStart() {
        this.mGet.setCameraChangingOnSquareSnap(true);
        super.onCameraSwitchingStart();
    }

    protected void onCameraSwitchingEnd() {
        super.onCameraSwitchingEnd();
        SquareSnapGalleryItem item = getCurSquareSnapItem();
        if (!isSquareGalleryBtn()) {
            return;
        }
        if (item == null || item.mType == -1) {
            this.mReviewThumbnailManager.setEnabled(false);
        }
    }

    protected void onChangeModuleBefore() {
        super.onChangeModuleBefore();
        String mode = getSettingValue(Setting.KEY_MODE);
        if (!this.mGet.isCameraChangingOnSquareSnap() && mode != null) {
            if (!mode.contains(CameraConstants.MODE_SQUARE) || mode.equals(CameraConstants.MODE_SQUARE_SPLICE)) {
                ListPreference listPref = this.mGet.getListPreference(Setting.KEY_MODE);
                String defaultValue = listPref == null ? "mode_normal" : listPref.getDefaultValue();
                String modeRear = this.mGet.getSpecificSetting(true).getSettingValue(Setting.KEY_MODE);
                String modeFront = this.mGet.getSpecificSetting(false).getSettingValue(Setting.KEY_MODE);
                boolean isNormalMode = "mode_normal".equals(mode);
                Setting specificSetting = this.mGet.getSpecificSetting(true);
                String str = Setting.KEY_MODE;
                if (isNormalMode) {
                    modeRear = defaultValue;
                }
                specificSetting.setSetting(str, modeRear, true);
                specificSetting = this.mGet.getSpecificSetting(false);
                str = Setting.KEY_MODE;
                if (isRearCamera() || isNormalMode) {
                    modeFront = defaultValue;
                }
                specificSetting.setSetting(str, modeFront, true);
                if (CameraConstants.MODE_SNAP.equals(mode)) {
                    this.mGet.getSpecificSetting(true).setSetting(Setting.KEY_MODE, mode, true);
                    this.mGet.getSpecificSetting(false).setSetting(Setting.KEY_MODE, mode, true);
                }
            }
        }
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_LIGHTFRAME, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_MOTION_QUICKVIEWER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue(Setting.KEY_LIGHTFRAME);
        restoreSettingValue(Setting.KEY_MOTION_QUICKVIEWER);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_FULLVISION);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected boolean isFastShotAvailable(int checkItem) {
        return false;
    }

    public boolean doBackKey() {
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode()) && this.mSquareInitGuideManger != null && this.mSquareInitGuideManger.isShowingInitGuide()) {
            this.mSquareInitGuideManger.hideInitGuide();
            return true;
        }
        if (isSquareGalleryBtn() && checkModuleValidate(192)) {
            this.mGalleryManager.showTouchBlockCoverView(false);
        }
        return super.doBackKey();
    }

    public String getCurrentViewModeToString() {
        return ParamConstants.PARAM_VIEW_MODE_SQUARE;
    }

    protected void startTimerShotByType(TimerType timerType, String timerValue) {
        super.startTimerShotByType(timerType, timerValue);
        if (isSquareGalleryBtn()) {
            this.mGalleryManager.showTouchBlockCoverView(true);
        }
    }

    protected void onTakePictureAfter() {
        super.onTakePictureAfter();
        if (isSquareGalleryBtn()) {
            this.mGalleryManager.showTouchBlockCoverView(false);
        }
    }

    public void prepareRecordingVideo() {
        super.prepareRecordingVideo();
        if (isShutterKeyOptionTimerActivated() && CameraConstants.MODE_SQUARE_SNAPSHOT.equals(getShotMode())) {
            this.mGalleryManager.showTouchBlockCoverView(true);
        }
    }

    public boolean isSquareInitGuideShowing() {
        return this.mSquareInitGuideManger == null ? false : this.mSquareInitGuideManger.isShowingInitGuide();
    }

    public boolean isInitialHelpSupportedModule() {
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        if (isRecordingState()) {
            switch (indicatorId) {
                case C0088R.id.indicator_item_hdr_or_flash:
                    return "1".equals(getSettingValue("hdr-mode"));
                case C0088R.id.indicator_item_steady:
                    return isAvailableSteadyCam();
            }
        }
        switch (indicatorId) {
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
            case C0088R.id.indicator_item_hdr_or_flash:
                return true;
        }
        return false;
    }
}
