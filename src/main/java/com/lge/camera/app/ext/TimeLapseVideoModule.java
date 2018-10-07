package com.lge.camera.app.ext;

import android.view.MotionEvent;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.managers.ext.TimeLapseManager;
import com.lge.camera.managers.ext.TimeLapseManager.OnTimeLapseFpsBtnListener;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import java.io.File;

public class TimeLapseVideoModule extends RecordingPriorityModule {
    private int mSelectedIndex = 0;
    private double mTimeLapseFps = 2.0d;
    protected TimeLapseManager mTimeLapseManager = new TimeLapseManager(this);

    /* renamed from: com.lge.camera.app.ext.TimeLapseVideoModule$1 */
    class C05251 implements OnTimeLapseFpsBtnListener {
        C05251() {
        }

        public void onTimeLapseBtnClick(int speedValue) {
            CamLog.m3d(CameraConstants.TAG, "onTimeLapseBtnClick : " + speedValue);
            TimeLapseVideoModule.this.setVideoFpsSetting((double) speedValue);
            TimeLapseVideoModule.this.mRecordingUIManager.setTimeLapseSpeedValue(speedValue);
        }
    }

    public TimeLapseVideoModule(ActivityBridge activityBridge) {
        super(activityBridge);
    }

    protected void addModuleManager() {
        super.addModuleManager();
        this.mManagerList.add(this.mTimeLapseManager);
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        setTimeLapseFpsBtnListener();
        if (this.mTimeLapseManager != null) {
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
        }
        this.mSelectedIndex = SharedPreferenceUtil.getTimeLapseSpeedValue(this.mGet.getAppContext());
        CamLog.m3d(CameraConstants.TAG, "mSelectedIndex : " + this.mSelectedIndex);
        setVideoFpsSetting((double) this.mSelectedIndex);
        if (this.mRecordingUIManager != null) {
            this.mRecordingUIManager.setTimeLapseSpeedValue(this.mSelectedIndex);
        }
    }

    protected void afterStopRecording() {
        super.afterStopRecording();
        if (this.mTimeLapseManager != null) {
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
        }
    }

    protected String getLDBNonSettingString() {
        return super.getLDBNonSettingString() + "timelapse_fps=" + getVideoFPS() + ";";
    }

    public double getVideoFPS() {
        return this.mTimeLapseFps;
    }

    private void setVideoFps(double fps) {
        this.mTimeLapseFps = fps;
    }

    public boolean onVideoShutterClickedBefore() {
        if (!super.onVideoShutterClickedBefore()) {
            return false;
        }
        if (this.mTimeLapseManager != null) {
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(false, false);
        }
        return true;
    }

    public void prepareRecordingVideo() {
        if (this.mTimeLapseManager != null) {
            if (this.mTimeLapseManager.isDrawerOpen()) {
                this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
            }
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(false, false);
            super.prepareRecordingVideo();
        }
    }

    protected void initializeSettingMenus() {
        super.initializeSettingMenus();
        setEnablePictureAndVideoSizeMenu(this.mCameraId, false, true);
        setSpecificSettingValueAndDisable(Setting.KEY_VOICESHUTTER, "off", false);
        setSpecificSettingValueAndDisable(Setting.KEY_TILE_PREVIEW, "off", false);
        restoreTrackingAFSetting();
        setSpecificSettingValueAndDisable(Setting.KEY_SIGNATURE, "off", false);
    }

    protected void restoreSettingMenus() {
        super.restoreSettingMenus();
        setEnablePictureAndVideoSizeMenu(0, true, false);
        restoreEnablePictureAndVideoSizeMenu(0);
        setEnablePictureAndVideoSizeMenu(2, true, false);
        restoreEnablePictureAndVideoSizeMenu(2);
        restoreSettingValue(Setting.KEY_VOICESHUTTER);
        restoreTrackingAFSetting();
        restoreSettingValue(Setting.KEY_SIGNATURE);
        restoreSettingValue(Setting.KEY_TILE_PREVIEW);
    }

    private void setEnablePictureAndVideoSizeMenu(int mCameraId, boolean enable, boolean isInit) {
        if (isInit) {
            this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId), ParamConstants.VIDEO_3840_BY_2160, enable);
            this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId), CameraConstants.VIDEO_FHD_60FPS, false);
            if (isUHDmode() || isFHD60()) {
                ListPreference pref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId));
                if (pref != null) {
                    this.mGet.setSetting(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId), pref.getDefaultValue(), enable);
                    access$800(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId), pref.getDefaultValue(), enable);
                    return;
                }
                return;
            }
            return;
        }
        ListPreference picPref = this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId));
        if (picPref != null) {
            CharSequence[] videoSize = picPref.getEntryValues();
            if (videoSize != null) {
                for (CharSequence charSequence : videoSize) {
                    this.mGet.setSettingChildMenuEnable(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId), (String) charSequence, enable);
                }
            }
        }
    }

    private void restoreEnablePictureAndVideoSizeMenu(int mCameraId) {
        restoreSettingValue(SettingKeyWrapper.getVideoSizeKey(getShotMode(), mCameraId));
        restoreSettingValue(SettingKeyWrapper.getPictureSizeKey(getShotMode(), mCameraId));
    }

    protected void setupVideosize(CameraParameters parameters, ListPreference listPref) {
        if (this.mCameraDevice != null && parameters != null && listPref != null) {
            setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_FORMAT, "yuv420sp");
            setParamUpdater(parameters, ParamConstants.KEY_VIDEO_SIZE, listPref.getValue());
            setupVideoPreviewSize(listPref, parameters);
            parameters.set(ParamConstants.KEY_HFR, "off");
            if (FunctionProperties.getSupportedHal() == 2) {
                access$4700(parameters);
            } else {
                setParameters(parameters);
            }
            this.mGet.setSwitchingAniViewParam(false);
        }
    }

    private void setupVideoPreviewSize(ListPreference listPref, CameraParameters parameters) {
        if (listPref != null && parameters != null) {
            String videoSize = listPref.getValue();
            String previewSize = listPref.getExtraInfo(1);
            String screenSize = listPref.getExtraInfo(2);
            int[] screenSizeInt = Utils.sizeStringToArray(screenSize);
            int[] videoSizeInt = Utils.sizeStringToArray(videoSize);
            if ("".equalsIgnoreCase(previewSize)) {
                if (videoSizeInt[0] > screenSizeInt[0] || videoSizeInt[1] > screenSizeInt[1]) {
                    previewSize = screenSize;
                } else {
                    previewSize = videoSize;
                }
            }
            setParamUpdater(parameters, ParamConstants.KEY_PREVIEW_SIZE, previewSize);
        }
    }

    public String getShotMode() {
        return CameraConstants.MODE_TIME_LAPSE_VIDEO;
    }

    public void onZoomShow() {
        super.onZoomShow();
        if (this.mTimeLapseManager != null) {
            if (isRecordingState()) {
                this.mTimeLapseManager.setTimeLapseChildViewVisibility(false, false);
            } else {
                this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
            }
        }
    }

    public long getRecDurationTime(String recordingFilePath) {
        long mRecDuration = 0;
        File mRecordingFile = new File(recordingFilePath);
        if (mRecordingFile != null && mRecordingFile.exists()) {
            mRecDuration = (long) FileUtil.getDurationFromFilePath(this.mGet.getAppContext(), recordingFilePath);
            CamLog.m3d(CameraConstants.TAG, "timelapse play duration = " + mRecDuration);
        }
        if (mRecDuration <= 1000) {
            return 1000;
        }
        return mRecDuration;
    }

    protected void showHeadsetRecordingToastPopup() {
    }

    public void removeUIBeforeModeChange() {
        super.removeUIBeforeModeChange();
        if (this.mTimeLapseManager != null) {
            this.mTimeLapseManager.setTimeSelectViewVisibility(false, false);
        }
    }

    public String getRecordingType() {
        return CameraConstants.VIDEO_VIDEO_TIMELAPSE_TYPE;
    }

    public void setTimeLapseFpsBtnListener() {
        this.mTimeLapseManager.setOnViewModeButtonListener(new C05251());
    }

    private void setVideoFpsSetting(double speedValue) {
        if (speedValue == 10.0d) {
            setVideoFps(3.0d);
        } else if (speedValue == 15.0d) {
            setVideoFps(2.0d);
        } else if (speedValue == 30.0d) {
            setVideoFps(1.0d);
        } else if (speedValue == 60.0d) {
            setVideoFps(0.5d);
        }
    }

    public boolean isLiveSnapshotSupported() {
        return false;
    }

    public boolean doBackKey() {
        if (!this.mTimeLapseManager.isDrawerOpen()) {
            return super.doBackKey();
        }
        this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isMenuShowing(CameraConstants.MENU_TYPE_ALL) && (event.getAction() == 1 || event.getAction() == 3)) {
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
        }
        if (!this.mTimeLapseManager.isDrawerOpen() || event.getAction() != 1) {
            return super.onTouchEvent(event);
        }
        this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
        return true;
    }

    protected void onTakePictureBefore() {
        if (this.mTimeLapseManager != null) {
            if (this.mTimeLapseManager.isDrawerOpen()) {
                this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
            }
            this.mTimeLapseManager.setTimLapseBtnEnable(false);
            super.onTakePictureBefore();
        }
    }

    protected void onTakePictureAfter() {
        LdbUtil.setMultiShotState(0);
        super.access$100();
        if (this.mTimeLapseManager != null) {
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
            this.mTimeLapseManager.setTimLapseBtnEnable(true);
        }
    }

    public boolean onShowMenu(int menuType) {
        if (!super.onShowMenu(menuType)) {
            return false;
        }
        if (this.mTimeLapseManager != null) {
            this.mTimeLapseManager.setTimeLapseChildViewVisibility(false, false);
        }
        return true;
    }

    public boolean onHideMenu(int menuType) {
        if (!super.onHideMenu(menuType)) {
            return false;
        }
        if (isMenuShowing((menuType ^ -1) & CameraConstants.MENU_TYPE_ALL) || this.mTimeLapseManager == null) {
            return true;
        }
        this.mTimeLapseManager.setTimeLapseChildViewVisibility(true, false);
        return true;
    }

    public boolean isZoomAvailable() {
        return true;
    }

    public boolean isAudioZoomAvailable() {
        return false;
    }

    protected boolean isAudioRecordingAvailable() {
        return false;
    }

    protected boolean displayUIComponentAfterOneShot() {
        if (!super.displayUIComponentAfterOneShot()) {
            return false;
        }
        if (this.mTimeLapseManager == null) {
            return true;
        }
        this.mTimeLapseManager.setTimeSelectViewVisibility(true, false);
        return true;
    }
}
