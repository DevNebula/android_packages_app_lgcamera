package com.lge.camera.app.ext;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.text.SpannableString;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.app.DefaultCameraModule;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.ShiftImageSpan;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ConfigurationResolutionUtilBase;
import com.lge.camera.constants.ConfigurationUtil;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.device.ICameraCallback.CameraPreviewDataCallback;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;

public class PanoramaModuleCommonBase extends DefaultCameraModule {
    protected static final Object LGSF_SYNC_OBJ = new Object();
    protected final int ANIMATION_TIME = 300;
    protected final String SHUTTER_SYMBOL = "(###)";
    protected CharSequence[][] mBackupOriginalEntries = new CharSequence[2][];
    protected CharSequence[][] mBackupOriginalEntryValues = new CharSequence[2][];
    protected CharSequence[][] mBackupOriginalExtraInfoTwo = new CharSequence[2][];
    protected String mPanoramaPictureSize = null;
    protected RelativeLayout mStartAndStopGuideTextLayout = null;
    protected int mState = 0;

    protected class AnimationListenerVisibleOnEnd implements AnimationListener {
        private View mView = null;
        private int mVisibility = 0;

        public AnimationListenerVisibleOnEnd(View view, int visibility) {
            this.mView = view;
            this.mVisibility = visibility;
        }

        public void onAnimationEnd(Animation animation) {
            if (this.mView != null) {
                this.mView.setVisibility(this.mVisibility);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    public PanoramaModuleCommonBase(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setSpecificSettingValueAndDisable(Setting.KEY_FRAME_GRID, "off", false);
        setSpecificSettingValueAndDisable("flash-mode", "off", false);
        setSpecificSettingValueAndDisable("hdr-mode", "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_RAW_PICTURE, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_QR, "off", false);
        int cameraId = getPanoramaCameraId();
        if (getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraId)) != null) {
            String size = getPanoramaPictureSize(cameraId);
            setSetting(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraId), size, false);
            if (FunctionProperties.useWideRearAsDefault() && isRearCamera()) {
                int anotherCameraId;
                if (this.mCameraId == 0) {
                    anotherCameraId = 2;
                } else {
                    anotherCameraId = 0;
                }
                String key = SettingKeyWrapper.getPictureSizeKey(getShotMode(), anotherCameraId);
                String value = getPanoramaPictureSize(anotherCameraId);
                CamLog.m3d(CameraConstants.TAG, "picture size key : " + key + ", value : " + value);
                setSetting(key, value, false);
            }
            if (isLGUOEMCameraIntent()) {
                setSpecificSettingValueAndDisable(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraId), size, false);
            }
        }
        this.mGet.backupSetting(Setting.KEY_TIMER, getSettingValue(Setting.KEY_TIMER));
        setSettingMenuEnable(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraId), false);
        setSettingMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), cameraId), false);
        setSpecificSettingValueAndDisable(Setting.KEY_TIMER, "0", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FILM_EMULATOR, CameraConstants.FILM_NONE, false);
        setSpecificSettingValueAndDisable("tracking-af", "off", false);
        setSignatureSetting();
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LIVE_PHOTO, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_FULLVISION, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_BINNING, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_LENS_SELECTION, this.mGet.getCurSettingValue(Setting.KEY_LENS_SELECTION), false);
    }

    protected void restoreSettingMenus() {
        restoreSettingValue(Setting.KEY_FRAME_GRID);
        restoreSettingValue("hdr-mode");
        restoreFlashSetting();
        restoreSettingValue("picture-size");
        if (FunctionProperties.useWideRearAsDefault()) {
            restoreSettingValue(Setting.KEY_CAMERA_PICTURESIZE_SUB);
        }
        restoreSettingValue(Setting.KEY_RAW_PICTURE);
        restoreSettingValue(Setting.KEY_QR);
        restoreSettingValue(Setting.KEY_FILM_EMULATOR);
        restoreSignatureSetting();
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
        this.mGet.restoreBackupSetting(Setting.KEY_TIMER, false);
        if ("1".equals(getSettingValue("hdr-mode")) && getListPreference("flash-mode") != null) {
            setSetting("flash-mode", "off", false);
        }
        restoreSettingValue(Setting.KEY_LIVE_PHOTO);
        restoreSettingValue(Setting.KEY_FULLVISION);
        restoreSettingValue(Setting.KEY_BINNING);
        restoreSettingValue(Setting.KEY_LENS_SELECTION);
    }

    protected int getPanoramaCameraId() {
        return this.mGet.getSharedPreferenceCameraId();
    }

    protected synchronized String getPanoramaPictureSize(int cameraId) {
        String str;
        if (FunctionProperties.useWideRearAsDefault() || this.mPanoramaPictureSize == null) {
            ListPreference listPref = this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), cameraId));
            if (listPref != null) {
                if (ConfigurationUtil.sPANORAMA_PREVIEW_SIZE_VALUE.length > 1) {
                    this.mPanoramaPictureSize = ConfigurationUtil.sPANORAMA_PREVIEW_SIZE_VALUE[1];
                    if (listPref.findIndexOfValue(this.mPanoramaPictureSize) == -1) {
                        setPictureSizeListPref(listPref, cameraId);
                    }
                    CamLog.m3d(CameraConstants.TAG, "panorama picture size = " + this.mPanoramaPictureSize);
                    str = this.mPanoramaPictureSize;
                } else {
                    CharSequence[] entryValues = listPref.getEntryValues();
                    double beforeSize = -1.0d;
                    long totalMemory = -1;
                    ActivityManager actManager = (ActivityManager) getActivity().getSystemService("activity");
                    if (actManager != null) {
                        MemoryInfo memInfo = new MemoryInfo();
                        actManager.getMemoryInfo(memInfo);
                        totalMemory = (memInfo.totalMem / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
                    }
                    CamLog.m3d(CameraConstants.TAG, "total memory (MB) = " + totalMemory);
                    for (String value : entryValues) {
                        int[] size = Utils.sizeStringToArray(value);
                        this.mPanoramaPictureSize = null;
                        if (ModelProperties.isLongLCDModel()) {
                            if (Utils.isSquarePictureSize(size)) {
                                this.mPanoramaPictureSize = value;
                            }
                        } else if (Utils.isWidePictureSize(size)) {
                            this.mPanoramaPictureSize = value;
                        }
                        if (this.mPanoramaPictureSize != null) {
                            double currentSize = (double) (size[0] * size[1]);
                            if (totalMemory <= 0 || totalMemory > 1000 || Double.compare(currentSize, 5992704.0d) <= 0 || (Double.compare(beforeSize, 0.0d) > 0 && Double.compare(beforeSize, currentSize) > 0)) {
                                break;
                            }
                            beforeSize = currentSize;
                        }
                    }
                }
            }
            CamLog.m3d(CameraConstants.TAG, "panorama picture size = " + this.mPanoramaPictureSize);
            str = this.mPanoramaPictureSize;
        } else {
            CamLog.m3d(CameraConstants.TAG, "panorama picture size (saved) = " + this.mPanoramaPictureSize);
            str = this.mPanoramaPictureSize;
        }
        return str;
    }

    protected void setPreviewLayoutParam() {
        if (ModelProperties.getLCDType() == 2) {
            ListPreference listPref = getListPreference(SettingKeyWrapper.getPictureSizeKey(getShotMode(), this.mCameraId));
            if (listPref != null) {
                int[] size = Utils.sizeStringToArray(listPref.getExtraInfo(2));
                this.mGet.setTextureLayoutParams(size[1] * 2, size[1], -1);
                if (this.mPreviewFrameLayout != null) {
                    this.mPreviewFrameLayout.setAspectRatio(((double) size[0]) / ((double) size[1]));
                }
                setAnimationLayout(3);
                return;
            }
            return;
        }
        super.setPreviewLayoutParam();
    }

    private void setPictureSizeListPref(ListPreference listPref, int cameraId) {
        if (listPref == null) {
            return;
        }
        if (cameraId == 0 || cameraId == 2) {
            int arrayIndex;
            if (cameraId == 0) {
                arrayIndex = 0;
            } else {
                arrayIndex = 1;
            }
            this.mBackupOriginalEntries[arrayIndex] = listPref.getEntries();
            this.mBackupOriginalEntryValues[arrayIndex] = listPref.getEntryValues();
            this.mBackupOriginalExtraInfoTwo[arrayIndex] = new CharSequence[this.mBackupOriginalEntryValues[arrayIndex].length];
            for (int i = 0; i < this.mBackupOriginalEntryValues[arrayIndex].length; i++) {
                this.mBackupOriginalExtraInfoTwo[arrayIndex][i] = listPref.getExtraInfo(2, i);
            }
            CharSequence[] newEntries = new CharSequence[1];
            newEntries[0] = (ModelProperties.isLongLCDModel() ? "18:9" : "16:9") + " (" + Utils.getMegaPixelOfPictureSize(this.mPanoramaPictureSize, 1) + ") " + this.mPanoramaPictureSize;
            listPref.setEntries(newEntries);
            listPref.setEntryValues(new CharSequence[]{this.mPanoramaPictureSize});
            for (String size : ConfigurationResolutionUtilBase.sCAMERA_PREVIEWSIZEONSCREEN_REAR_SUPPORTED_ITEMS) {
                int[] itemSize = Utils.sizeStringToArray(size);
                if (itemSize[0] / itemSize[1] == 2) {
                    listPref.setExtraInfos(new CharSequence[]{size}, 2);
                    break;
                }
            }
            postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    PanoramaModuleCommonBase.this.setPreviewLayoutParam();
                }
            }, 0);
        }
    }

    public boolean onShutterBottomButtonLongClickListener() {
        return false;
    }

    protected boolean checkToUseFilmEffect() {
        return false;
    }

    public int getShutterButtonType() {
        return 4;
    }

    public String getShotMode() {
        return CameraConstants.MODE_PANORAMA;
    }

    protected boolean isAvailableSteadyCam() {
        return false;
    }

    protected int getLoopRecordingType() {
        return 0;
    }

    protected boolean isMaintainRecordingFlashValue() {
        return false;
    }

    protected boolean useAFTrackingModule() {
        return false;
    }

    protected void showCommandArearUI(boolean show) {
        if (!show || this.mState != 4) {
            super.access$1300(show);
        }
    }

    protected boolean isFastShotSupported() {
        return false;
    }

    public void hide() {
    }

    public void startEngine() {
    }

    public void stopEngine() {
    }

    public void startPanorama() {
    }

    public void stopPanorama(boolean needSaving, boolean stop360) {
    }

    public boolean useWideAngleExpand() {
        return true;
    }

    protected void setCameraCallbackAll(int callbackType, CameraImageCallback previewCb, CameraImageCallback fullFrameCb, CameraImageMetaCallback metaCb) {
        if (FunctionProperties.getSupportedHal() != 2) {
            CamLog.m11w(CameraConstants.TAG, "this method must be called in HAL3.x.");
        } else if (this.mCameraDevice == null) {
            CamLog.m11w(CameraConstants.TAG, "mCameraDevice is null. return.");
        } else {
            CamLog.m7i(CameraConstants.TAG, "previewCb : " + previewCb + ", fullFrameCb : " + fullFrameCb + ", metaCb : " + metaCb);
            switch (callbackType) {
                case -1:
                    this.mCameraDevice.setImageDataCallback(-1, null);
                    break;
                case 0:
                    this.mCameraDevice.setImageDataCallback(0, previewCb);
                    break;
                case 1:
                    this.mCameraDevice.setImageDataCallback(1, fullFrameCb);
                    break;
                default:
                    CamLog.m11w(CameraConstants.TAG, "You shoud set callback type.");
                    break;
            }
            if (previewCb == null && fullFrameCb == null && metaCb != null) {
                CamLog.m11w(CameraConstants.TAG, "metaCb should be null.");
            }
            this.mCameraDevice.setImageMetaCallback(metaCb);
        }
    }

    protected void setCameraCallbackAll(byte[] previewBuffer, CameraPreviewDataCallback previewCb) {
        if (FunctionProperties.getSupportedHal() == 2) {
            CamLog.m5e(CameraConstants.TAG, "this method must not be called. error.");
        } else if (this.mDirectCallbackManager != null) {
            this.mDirectCallbackManager.setPreviewDataCallback(previewCb);
            this.mDirectCallbackManager.addCallbackBuffer(previewBuffer);
            this.mDirectCallbackManager.setPreviewDataCallbackWithBuffer(previewCb);
        }
    }

    protected void startAnimationAlphaShowing(View view, boolean isVisible, boolean isForced) {
        int visibility = isVisible ? 0 : 4;
        if (view == null) {
            return;
        }
        if (isForced || view.getVisibility() != visibility) {
            Animation anim;
            if (isVisible) {
                anim = new AlphaAnimation(0.0f, 1.0f);
            } else {
                anim = new AlphaAnimation(1.0f, 0.0f);
            }
            anim.setDuration(300);
            anim.setAnimationListener(new AnimationListenerVisibleOnEnd(view, visibility));
            if (view.getAnimation() != null && view.getAnimation().hasStarted()) {
                anim.setStartOffset(300);
            }
            view.startAnimation(anim);
        }
    }

    protected void startAnimationGuideArrowShowing(View view, boolean isVisible, boolean isForced) {
        int visibility = isVisible ? 0 : 4;
        if (view != null) {
            Animation anim;
            if (!isForced) {
                if (view.getVisibility() == visibility) {
                    return;
                }
                if (view.getAnimation() != null && view.getAnimation().hasStarted()) {
                    return;
                }
            }
            if (isVisible) {
                anim = AnimationUtils.loadAnimation(getAppContext(), C0088R.anim.panorama_arrow_show_blink);
            } else {
                anim = AnimationUtils.loadAnimation(getAppContext(), C0088R.anim.panorama_arrow_hide);
            }
            anim.setAnimationListener(new AnimationListenerVisibleOnEnd(view, visibility));
            view.startAnimation(anim);
        }
    }

    protected void showGuideText(final boolean isVisible) {
        postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                int i = 180;
                if (PanoramaModuleCommonBase.this.mStartAndStopGuideTextLayout != null) {
                    if (isVisible) {
                        PanoramaModuleCommonBase panoramaModuleCommonBase = PanoramaModuleCommonBase.this;
                        View view = PanoramaModuleCommonBase.this.mStartAndStopGuideTextLayout;
                        if (PanoramaModuleCommonBase.this.getOrientationDegree() != 180) {
                            i = 0;
                        }
                        panoramaModuleCommonBase.startRotateGuideText(view, i);
                        PanoramaModuleCommonBase.this.mStartAndStopGuideTextLayout.setVisibility(0);
                        return;
                    }
                    PanoramaModuleCommonBase.this.mStartAndStopGuideTextLayout.setVisibility(4);
                }
            }
        }, 300);
    }

    protected SpannableString makeSpannableGuideString(Context mContext, String mText) {
        if (mContext == null || mText == null) {
            return null;
        }
        String mChangeString = "(###)";
        int spanStartIndex = mText.indexOf(mChangeString);
        int spanEndIndex = spanStartIndex + mChangeString.length();
        SpannableString ss = new SpannableString(mText);
        Drawable d = mContext.getDrawable(C0088R.drawable.camera_guide_spannable_shutter);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ss.setSpan(new ShiftImageSpan(mContext, d, 1), spanStartIndex, spanEndIndex, 17);
        return ss;
    }

    protected void startRotateGuideText(View layout, int degree) {
        if (layout != null) {
            RelativeLayout textInnerLayout = (RelativeLayout) layout.findViewById(C0088R.id.guide_text_rotate_layout);
            LayoutParams lpInnerLayout = (LayoutParams) textInnerLayout.getLayoutParams();
            Utils.resetLayoutParameter(lpInnerLayout);
            View textGuide = layout.findViewById(C0088R.id.guide_text);
            if (textGuide != null) {
                LayoutParams lpTextGuide = (LayoutParams) textGuide.getLayoutParams();
                Utils.resetLayoutParameter(lpTextGuide);
                textInnerLayout.setLayoutDirection(0);
                lpTextGuide.width = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 1000.0f);
                if (!Utils.isConfigureLandscape(this.mGet.getActivity().getResources())) {
                    switch (degree) {
                        case 0:
                            lpInnerLayout.addRule(12, 1);
                            lpInnerLayout.addRule(14, 1);
                            lpInnerLayout.bottomMargin = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 384.0f) - ((TextView) textGuide).getLineHeight();
                            break;
                        case 90:
                            lpInnerLayout.addRule(20, 1);
                            lpInnerLayout.addRule(15, 1);
                            lpInnerLayout.setMarginStart(RatioCalcUtil.getSizeRatioInPano(getAppContext(), 100.0f));
                            lpTextGuide.width = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 1600.0f);
                            break;
                        case 180:
                            lpInnerLayout.addRule(12, 1);
                            lpInnerLayout.addRule(14, 1);
                            lpInnerLayout.bottomMargin = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 384.0f);
                            break;
                        case 270:
                            lpInnerLayout.addRule(20, 1);
                            lpInnerLayout.addRule(15, 1);
                            lpInnerLayout.setMarginStart(RatioCalcUtil.getSizeRatioInPano(getAppContext(), 100.0f) - ((TextView) textGuide).getLineHeight());
                            lpTextGuide.width = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 1600.0f);
                            break;
                    }
                }
                switch (degree) {
                    case 0:
                        lpInnerLayout.addRule(12, 1);
                        lpInnerLayout.addRule(14, 1);
                        lpInnerLayout.bottomMargin = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 384.0f);
                        break;
                    case 90:
                        lpInnerLayout.addRule(20, 1);
                        lpInnerLayout.addRule(15, 1);
                        lpInnerLayout.setMarginStart(RatioCalcUtil.getSizeRatioInPano(getAppContext(), 100.0f));
                        lpTextGuide.width = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 1600.0f);
                        break;
                    case 180:
                        lpInnerLayout.addRule(12, 1);
                        lpInnerLayout.addRule(14, 1);
                        lpInnerLayout.bottomMargin = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 384.0f);
                        break;
                    case 270:
                        lpInnerLayout.addRule(20, 1);
                        lpInnerLayout.addRule(15, 1);
                        lpInnerLayout.setMarginStart(RatioCalcUtil.getSizeRatioInPano(getAppContext(), 100.0f));
                        lpTextGuide.width = RatioCalcUtil.getSizeRatioInPano(getAppContext(), 1600.0f);
                        break;
                }
                textInnerLayout.setLayoutParams(lpInnerLayout);
                textGuide.setLayoutParams(lpTextGuide);
                ((RotateLayout) textInnerLayout).rotateLayout(degree);
            }
        }
    }

    protected void setZoomCompensation(CameraParameters parameters) {
        if (!isZoomAvailable()) {
            this.mZoomManager.setZoomValue(0);
            updateZoomParam(parameters, 0);
        }
    }

    protected boolean checkEnableRecogSucess() {
        if (this.mState <= 2) {
            return super.checkEnableRecogSucess();
        }
        CamLog.m3d(CameraConstants.TAG, "exit doVoiceRecogSuccess state=" + this.mState);
        return false;
    }

    public boolean isAvailableToChangeAutoModeByWatch() {
        return this.mState == 2;
    }

    protected void setSignatureSetting() {
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
    }

    protected void restoreSignatureSetting() {
        restoreSettingValue(Setting.KEY_SIGNATURE);
    }

    protected boolean isAEAFLockSupportedMode() {
        return false;
    }

    protected boolean isAEControlSupportedMode() {
        return false;
    }

    public boolean isHALSignatureCaptureMode() {
        return false;
    }

    public boolean isShutterZoomSupported() {
        return false;
    }

    protected void configPreviewCallback() {
        if (this.mDirectCallbackManager != null) {
            this.mDirectCallbackManager.configPreviewCallback(this.mCameraDevice, 0);
        }
    }

    public boolean isInitialHelpSupportedModule() {
        return false;
    }

    public boolean isShutterKeyOptionTimerActivated() {
        return false;
    }

    protected boolean doVoiceAssistantTakeCommand() {
        return onCameraShutterButtonClicked();
    }

    protected boolean doVoiceAssistantTimerCommand(int timerDuration) {
        return true;
    }

    public boolean isEVShutterSupportedMode() {
        return false;
    }

    public boolean isSupportedFilterMenu() {
        return false;
    }

    public boolean isFaceDetectionSupported() {
        return false;
    }

    public boolean isIndicatorSupported(int indicatorId) {
        switch (indicatorId) {
            case C0088R.id.indicator_item_battery:
            case C0088R.id.indicator_item_cheese_shutter_or_timer:
                return true;
            default:
                return false;
        }
    }
}
