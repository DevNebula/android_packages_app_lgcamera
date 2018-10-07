package com.lge.camera.managers.ext;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.AsyncTask.Status;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.lge.camera.C0088R;
import com.lge.camera.app.ActivityBridge;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.device.ParamUpdater;
import com.lge.camera.file.DBScanManager;
import com.lge.camera.managers.AnimationManager;
import com.lge.camera.managers.PhoneStorageManager;
import com.lge.camera.managers.PostviewManager;
import com.lge.camera.managers.ReviewThumbnailManager;
import com.lge.camera.managers.TimerManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MDMUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.TelephonyUtil;

public class SnapMovieManagerTrigger extends SnapMovieManager {
    private HandlerRunnable mHandlerInitUi = new HandlerRunnable(this) {
        public void handleRun() {
            int i = 1;
            if (SnapMovieManagerTrigger.this.mPlayButton != null && SnapMovieManagerTrigger.this.mSaveButton != null && SnapMovieManagerTrigger.this.mResetButton != null) {
                int shotSize = SnapMovieManagerTrigger.this.loadShotList();
                if (SecureImageUtil.isSecureCamera() && SecureImageUtil.useSecureLockImage()) {
                    boolean z;
                    SnapMovieManagerTrigger.this.mPlayButton.setEnabled(false);
                    SnapMovieManagerTrigger.this.mSaveButton.setEnabled(false);
                    SnapMovieManagerTrigger.this.mResetButton.setEnabled(false);
                    SnapMovieManagerTrigger.this.mPlayButton.setColorFilter(ColorUtil.getDimColorByAlpha());
                    int lockedSize = SecureImageUtil.get().getSnapLockedSize();
                    SnapMovieManagerTrigger snapMovieManagerTrigger = SnapMovieManagerTrigger.this;
                    if (lockedSize <= -1 || lockedSize >= SnapMovieManagerTrigger.this.mShotList.size()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    snapMovieManagerTrigger.setVisibleHandler(z);
                } else {
                    SnapMovieManagerTrigger.this.mPlayButton.setEnabled(true);
                    SnapMovieManagerTrigger.this.mSaveButton.setEnabled(true);
                    SnapMovieManagerTrigger.this.mResetButton.setEnabled(true);
                    SnapMovieManagerTrigger.this.mPlayButton.clearColorFilter();
                    SnapMovieManagerTrigger.this.mBar.setDisabledWidth(0, 0);
                    SnapMovieManagerTrigger.this.setVisibleHandler(SnapMovieManagerTrigger.this.mShotList.size() > 0);
                }
                if (shotSize <= 0) {
                    SnapMovieManagerTrigger.this.rotateView(SnapMovieManagerTrigger.this.getOrientationDegree(), false);
                } else if (SnapMovieManagerTrigger.this.mFixedDegree > -1) {
                    CamLog.m7i(CameraConstants.TAG, "Snap movie fix orientation : " + SnapMovieManagerTrigger.this.mFixedDegree);
                    SnapMovieManagerTrigger.this.rotateView(SnapMovieManagerTrigger.this.mFixedDegree, false);
                    SnapMovieManagerTrigger.this.setOrientationFixed(true);
                } else {
                    SnapMovieManagerTrigger.this.rotateView(SnapMovieManagerTrigger.this.getOrientationDegree(), false);
                }
                if (!SnapMovieManagerTrigger.this.mGet.isRearCamera()) {
                    SnapMovieManagerTrigger.this.mGet.setBarVisible(1, false, false);
                }
                SnapMovieManagerTrigger snapMovieManagerTrigger2 = SnapMovieManagerTrigger.this;
                if (SnapMovieManagerTrigger.this.isProcessingSave()) {
                    i = 3;
                }
                snapMovieManagerTrigger2.setStatus(i);
                CamLog.m7i(CameraConstants.TAG, "Snap loading done statue = " + SnapMovieManagerTrigger.this.mStatus);
            }
        }
    };
    private boolean mIsUseTimer = false;
    private boolean mSetMinRec = false;

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerTrigger$1 */
    class C13041 implements OnTouchListener {
        C13041() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (2 == event.getAction()) {
                view.setPressed(true);
                return true;
            } else if (!SnapMovieManagerTrigger.this.mIsRec3sec && 6 == SnapMovieManagerTrigger.this.mGet.getCameraState() && 1 == event.getAction()) {
                SnapMovieManagerTrigger.this.onReleaseShutterLongPress(view);
                return true;
            } else if (5 != SnapMovieManagerTrigger.this.mGet.getCameraState() || 1 != event.getAction()) {
                return false;
            } else {
                CamLog.m3d(CameraConstants.TAG, "min recording case");
                SnapMovieManagerTrigger.this.mGet.getHandler().sendEmptyMessage(77);
                SnapMovieManagerTrigger.this.mGet.getHandler().sendEmptyMessage(79);
                return true;
            }
        }
    }

    public SnapMovieManagerTrigger(SnapMovieInterface snapMovieInterface) {
        super(snapMovieInterface);
    }

    public void init(ActivityBridge activityBridge, View baseParent) {
        if (this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "init baseParent=" + baseParent);
            View snapBase = this.mGet.findViewById(C0088R.id.snap_movie_view_layout);
            if (!(snapBase == null || baseParent == null)) {
                ((ViewGroup) baseParent).removeView(snapBase);
            }
            createViews(activityBridge.layoutInflate(C0088R.layout.snap_movie_view, (ViewGroup) baseParent));
            this.mGet.getReviewThumbnailManager().setThumbnailVisibility(8, false, true);
            this.mGet.getReviewThumbnailManager().setHideMode(SecureImageUtil.useSecureLockImage());
        }
    }

    public boolean isUseTimer() {
        return this.mIsUseTimer;
    }

    private boolean doRecordStartButtonClicked(boolean isUseTimer) {
        boolean success = false;
        this.mIsUseTimer = isUseTimer;
        if (this.mGet != null) {
            success = this.mGet.onRecordStartButtonClicked();
        }
        this.mIsUseTimer = false;
        return success;
    }

    private boolean isEnableConditionOfShutter() {
        if (this.mGet == null) {
            return false;
        }
        if (!this.mGet.checkModuleValidate(223) || !this.mGet.checkStorage(2, 0) || this.mGet.getCameraState() != 1 || this.mGet.isModuleChanging() || this.mGet.isRotateDialogVisible() || this.mCountShutterClicked > 0 || this.mStatus < 0 || this.mStatus >= 3 || isProcessingSave()) {
            return true;
        }
        return false;
    }

    public boolean onShutterLargeButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "click count = " + this.mCountShutterClicked + " status=" + this.mStatus);
        if (isEnableConditionOfShutter()) {
            return true;
        }
        if (!(this.mGet == null || this.mGet.getCaptureButtonManager() == null || this.mGet.getCaptureButtonManager().getShutterButtonMode(4) != 14)) {
            setRecLimit3sec(true);
        }
        if (isHaveEnoughTime(1)) {
            this.mCountShutterClicked++;
            if (this.mGet == null || this.mGet.getCaptureButtonManager() == null) {
                this.mCountShutterClicked = 0;
                return true;
            }
            switch (this.mGet.getCaptureButtonManager().getShutterButtonMode(4)) {
                case 14:
                    if (!doRecordStartButtonClicked(true)) {
                        this.mCountShutterClicked = 0;
                    }
                    setVisibleButton(false);
                    return true;
                case 18:
                    this.mGet.onShutterStopButtonClicked();
                    this.mGet.getCaptureButtonManager().changeButtonByMode(14);
                    return true;
                default:
                    this.mCountShutterClicked = 0;
                    return false;
            }
        }
        setRecLimit3sec(false);
        return true;
    }

    public boolean onShutterLargeButtonLongClicked() {
        setRecLimit3sec(false);
        if (!isHaveEnoughTime(1)) {
            return true;
        }
        CamLog.m3d(CameraConstants.TAG, "shutter long click / status=" + this.mStatus);
        if (isEnableConditionOfShutter() || this.mGet == null || this.mGet.getCaptureButtonManager() == null) {
            return true;
        }
        switch (this.mGet.getCaptureButtonManager().getShutterButtonMode(4)) {
            case 2:
            case 14:
                doRecordStartButtonClicked(false);
                return true;
            default:
                setRecLimit3sec(true);
                return false;
        }
    }

    public boolean onShutterTopButtonClickListener(PostviewManager postViewMng, ReviewThumbnailManager reviewThumbMng) {
        if (this.mGet == null) {
            return false;
        }
        if ((postViewMng != null && postViewMng.isPostviewShowing()) || SystemBarUtil.isSystemUIVisible(getActivity()) || !this.mGet.checkModuleValidate(95) || (reviewThumbMng != null && reviewThumbMng.isQuickViewAniStarted())) {
            return true;
        }
        if (this.mGet.getCaptureButtonManager() == null) {
            return true;
        }
        switch (this.mGet.getCaptureButtonManager().getShutterButtonMode(1)) {
            case 14:
                setRecLimit3sec(true);
                if (!doRecordStartButtonClicked(true)) {
                    setRecLimit3sec(false);
                }
                return true;
            default:
                return false;
        }
    }

    public boolean onShutterTopButtonLongClickListener(PostviewManager postViewMng, ReviewThumbnailManager reviewThumbMng) {
        if (this.mGet == null) {
            return false;
        }
        if ((postViewMng != null && postViewMng.isPostviewShowing()) || SystemBarUtil.isSystemUIVisible(getActivity()) || !this.mGet.checkModuleValidate(95) || (reviewThumbMng != null && reviewThumbMng.isQuickViewAniStarted())) {
            return true;
        }
        if (this.mGet.getCaptureButtonManager() == null) {
            return true;
        }
        switch (this.mGet.getCaptureButtonManager().getShutterButtonMode(1)) {
            case 2:
            case 14:
                setRecLimit3sec(false);
                if (doRecordStartButtonClicked(false)) {
                    setPressLongButtonListener(true);
                }
                return true;
            default:
                setRecLimit3sec(true);
                return false;
        }
    }

    private void setRecLimit3sec(boolean set) {
        this.mIsRec3sec = set;
        if (this.mGet != null && this.mGet.getRecordingUIManager() != null) {
            this.mGet.getRecordingUIManager().setRec3sec(set);
        }
    }

    public void setPressLongButtonListener(boolean set) {
        if (this.mGet != null) {
            ImageButton shutterTop = (ImageButton) this.mGet.findViewById(C0088R.id.shutter_large_comp);
            if (shutterTop == null) {
                return;
            }
            if (set) {
                shutterTop.setOnTouchListener(new C13041());
                return;
            }
            shutterTop.setOnTouchListener(null);
            this.mGet.setShutterButtonListener(true);
        }
    }

    public void onReleaseShutterLongPress(final View view) {
        setPressLongButtonListener(false);
        int delay = (int) (((long) MultimediaProperties.getMinRecordingTime()) - this.mGet.getRecDurationTime());
        CamLog.m3d(CameraConstants.TAG, "delay = " + delay);
        if (delay > 0) {
            this.mGet.getHandler().sendEmptyMessage(77);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SnapMovieManagerTrigger.this.mGet.checkModuleValidate(128)) {
                        CamLog.m3d(CameraConstants.TAG, "already stopped");
                        return;
                    }
                    if (view != null) {
                        view.setPressed(false);
                    }
                    SnapMovieManagerTrigger.this.mGet.onVideoStopClicked(true, false);
                    SnapMovieManagerTrigger.this.mGet.getHandler().sendEmptyMessage(78);
                }
            }, (long) delay);
            return;
        }
        if (view != null) {
            view.setPressed(false);
        }
        this.mGet.onVideoStopClicked(true, false);
    }

    public boolean onShutterBottomButtonClickListener() {
        if (this.mGet == null || this.mGet.getCaptureButtonManager() == null) {
            return true;
        }
        switch (this.mGet.getCaptureButtonManager().getShutterButtonMode(2)) {
            case 1:
                this.mGet.onCameraShutterButtonClicked();
                return true;
            default:
                return false;
        }
    }

    public int getRemainShotTime() {
        return getRemainShotTime(this.mIsRec3sec);
    }

    public void startRecorder() {
        doRecodingJob(true);
        if (this.mGet != null && this.mGet.getCaptureButtonManager() != null && !this.mIsRec3sec) {
            this.mGet.getCaptureButtonManager().changeButtonByMode(14);
        }
    }

    public int getVideoOrientation() {
        if (this.mFixedDegree == -1) {
            this.mFixedDegree = this.mGet.getOrientationDegree();
        }
        return CameraDeviceUtils.getOrientationHint(this.mGet == null ? 0 : (this.mGet.getDisplayOrientation() + this.mFixedDegree) % 360, 0);
    }

    public void stopRecorder() {
        if (this.mGet != null) {
            final boolean isRec3Sec = this.mIsRec3sec;
            setRecLimit3sec(false);
            this.mCountShutterClicked = 0;
            setStatus(1);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SnapMovieManagerTrigger.this.mGet == null || SnapMovieManagerTrigger.this.mGet.getRecDurationTime() >= ((long) MultimediaProperties.getMinRecordingTime())) {
                        SnapMovieManagerTrigger.this.increaseShotCountAndTime(1, isRec3Sec);
                        SnapMovieManagerTrigger.this.doRecodingJob(false);
                        return;
                    }
                    SnapMovieManagerTrigger.this.addShotTime(SnapMovieManagerTrigger.this.updateVideoLastItem());
                }
            }, 0);
            if (this.mGet != null && this.mGet.isSupportedQuickClip()) {
                this.mGet.getQuickClipManager().rollbackStatus();
            }
        }
    }

    public void recorderOnError(MediaRecorder mr, int what, int extra) {
        if (this.mGet != null && this.mGet.getRecDurationTime() < ((long) MultimediaProperties.getMinRecordingTime())) {
            decreaseShotCountAndTime(1);
        }
        setRecLimit3sec(false);
        this.mCountShutterClicked = 0;
        setStatus(1);
    }

    public boolean isHaveEnoughTime(int type) {
        if (type == 0) {
            return false;
        }
        if (getRemainShotTime(this.mIsRec3sec) >= (type == 0 ? AnimationManager.HOLD_DURATION : SHOT_TIME_VIDEO_MIN)) {
            return true;
        }
        showGuideLimitWarning();
        return false;
    }

    public void onVideoShutterClicked() {
        if (this.mGet == null) {
            return;
        }
        if (this.mGet == null || this.mGet.getCaptureButtonManager() != null) {
            if (this.mIsRec3sec) {
                this.mGet.getCaptureButtonManager().setShutterButtonVisibility(4, 4);
            } else {
                setPressLongButtonListener(true);
            }
            setVisibleBar(true);
            setThumbHandlersPressed(false);
            if (this.mGet.isUHDmode() || !this.mGet.isLiveSnapshotSupported()) {
                this.mGet.getCaptureButtonManager().changeExtraButton(0, 1);
                this.mGet.getCaptureButtonManager().setExtraButtonVisibility(8, 1);
            }
        }
    }

    public void afterStopRecording() {
        if (this.mGet != null && this.mGet != null && this.mGet.getCaptureButtonManager() != null) {
            this.mGet.getCaptureButtonManager().changeButtonByMode(14);
        }
    }

    public void onVideoStopClicked() {
        setPressLongButtonListener(false);
        if (this.mGet != null && this.mGet.getCaptureButtonManager() != null) {
            this.mGet.getCaptureButtonManager().changeButtonByMode(14);
        }
    }

    public void onCameraShutterButtonClicked(TimerManager timerMng) {
        setThumbHandlersPressed(false);
        if (this.mGet != null && this.mGet.getCaptureButtonManager() != null && timerMng != null && !timerMng.isTimerShotCountdown()) {
            this.mGet.getCaptureButtonManager().changeButtonByMode(14);
        }
    }

    public String getCurTempDir(PhoneStorageManager storageMng) {
        if (storageMng != null) {
            return storageMng.getTempDir(0);
        }
        return "";
    }

    public String getCurDir(PhoneStorageManager storageMng, int needProgressDuringCapture) {
        if (this.mGet == null || storageMng == null) {
            return "";
        }
        int storage = this.mGet.getCurStorage();
        if (needProgressDuringCapture > 0 || getStatus() == 2) {
            return storageMng.getTempDir(storage);
        }
        return storageMng.getDir(storage);
    }

    public boolean isProcessingSave() {
        if (this.mSaveSnapMovieClipsTask == null || this.mSaveSnapMovieClipsTask.getStatus() == Status.FINISHED) {
            return false;
        }
        return true;
    }

    public void onResumeAfter() {
        if (this.mGet != null) {
            this.mGet.getCaptureButtonManager().changeButtonByMode(14);
            boolean isSaving = isProcessingSave();
            CamLog.m3d(CameraConstants.TAG, "is saving = " + isSaving + ", status = " + this.mStatus);
            if (isSaving) {
                setStatus(3);
                this.mGet.showSavingDialog(true, 0);
            }
            this.mFixedDegree = SharedPreferenceUtil.getSnapMovieOrientation(getAppContext());
            setOrientationFixed(false);
            rotateView(this.mFixedDegree, true);
            this.mGet.postOnUiThread(this.mHandlerInitUi, 0);
        }
    }

    public void updateRecordingTime() {
        if (this.mGet != null && this.mGet.getCameraState() == 6) {
            int recTime = (int) this.mGet.getRecDurationTime();
            if (this.mSetMinRec && recTime >= SHOT_TIME_VIDEO_MIN) {
                CamLog.m3d(CameraConstants.TAG, "recording time = " + recTime);
                this.mGet.getHandler().sendEmptyMessageDelayed(14, 500);
                this.mGet.getHandler().sendEmptyMessage(78);
                this.mSetMinRec = false;
            }
            if (this.mIsRec3sec && recTime >= 3000) {
                recTime = 3000;
            }
            int curTime = getShotTime() + recTime;
            updateSnapDurationTime(curTime);
            setBarTime(curTime, false);
        }
    }

    public boolean isUpdateThumbCondition() {
        return (this.mStatus == 1 || this.mStatus == 2) ? false : true;
    }

    public void resultVideoEditor(int resultCode, Intent data) {
        if (this.mGet != null) {
            if (this.mGet.isSupportedQuickClip()) {
                this.mGet.getQuickClipManager().setLayout();
                this.mGet.getQuickClipManager().setAfterShot();
            }
            if (resultCode != 0 && data != null) {
                String resultData = data.getStringExtra(SnapMovieInterfaceImpl.EXTRA_NAME_RESULT);
                if (SnapMovieInterfaceImpl.RESULT_FAIL.equals(resultData)) {
                    CamLog.m3d(CameraConstants.TAG, "saving is failed");
                    if (this.mGet.isSupportedQuickClip()) {
                        this.mGet.getQuickClipManager().rollbackStatus();
                        return;
                    }
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "saving is completed : " + resultData);
                startScan(resultData);
                SharedPreferenceUtil.saveLastThumbnailPath(getAppContext(), resultData);
            } else if (this.mGet.isSupportedQuickClip()) {
                this.mGet.getQuickClipManager().rollbackStatus();
            }
        }
    }

    public void startScan(String filePath) {
        DBScanManager scanManager = new DBScanManager(getAppContext(), this);
        if (scanManager != null) {
            CamLog.m3d(CameraConstants.TAG, "## snap start scan  : " + filePath);
            scanManager.startScan(filePath);
        }
    }

    public void doShutterStopButtonClicked() {
        CamLog.m3d(CameraConstants.TAG, "video stop clicked.");
        if (this.mGet != null && this.mGet.checkModuleValidate(79) && !SystemBarUtil.isSystemUIVisible(getActivity())) {
            if (this.mGet.getRecordingUIManager() == null || this.mGet.getRecordingUIManager().checkMinRecTime(2500)) {
                this.mGet.onVideoStopClicked(true, false);
            } else {
                CamLog.m7i(CameraConstants.TAG, "Video recording time is too short.");
            }
        }
    }

    public boolean doMainHandlerHandleMessage(Message msg) {
        if (msg == null || this.mGet == null) {
            return false;
        }
        switch (msg.what) {
            case 14:
                setPressLongButtonListener(false);
                ImageButton shutterTop = (ImageButton) this.mGet.findViewById(C0088R.id.shutter_large_comp);
                if (shutterTop != null) {
                    shutterTop.setPressed(false);
                }
                this.mGet.onVideoStopClicked(true, false);
                return true;
            case 31:
                onShowSpecificMenu();
                return false;
            case 71:
                this.mGet.getRecordingUIManager().getArcProgress().updateArcProgress(3);
                return true;
            case 77:
                this.mGet.setCaptureButtonEnable(false, 4);
                return true;
            case 78:
                this.mGet.setCaptureButtonEnable(true, 4);
                return true;
            case 79:
                this.mSetMinRec = true;
                return true;
            default:
                return false;
        }
    }

    public void onShowSpecificMenu() {
        setVisibleBar(false);
        setVisibleGuideText(false);
    }

    public void onHideSpecificMenu() {
        boolean z = true;
        setStatus(1);
        setVisibleBar(true);
        if (this.mShotList == null || this.mShotList.size() > 0) {
            z = false;
        }
        setVisibleGuideText(z);
    }

    public void doCleanView(boolean doByAction, boolean useAnimation, boolean saveState) {
        boolean z;
        boolean z2 = true;
        int size = this.mShotList == null ? 0 : this.mShotList.size();
        if (this.mIsUseTimer) {
            setVisibleGuideText(false);
        } else {
            setVisibleBar(true);
        }
        if (size > 0) {
            z = true;
        } else {
            z = false;
        }
        setVisibleButton(z);
        if (size > 0) {
            z = true;
        } else {
            z = false;
        }
        setVisibleThumbLayout(z);
        if (size <= 0) {
            z2 = false;
        }
        setVisibleDuration(z2);
    }

    public void setInputSizeParam(ParamUpdater paramUpdater) {
        if (this.mGet != null && paramUpdater != null) {
            ListPreference listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
            if (listPref != null) {
                String[] size = getInputSize(listPref);
                paramUpdater.setParamValue("picture-size", size[0]);
                paramUpdater.setParamValue(ParamConstants.KEY_PREVIEW_SIZE, size[1]);
            }
            listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
            if (listPref != null) {
                paramUpdater.setParamValue(ParamConstants.KEY_VIDEO_SIZE, getInputSize(listPref)[0]);
            }
        }
    }

    public void setInputSize() {
        if (this.mGet != null) {
            this.mGet.setSettingMenuEnable("picture-size", false);
            this.mGet.setSettingMenuEnable(Setting.KEY_CAMERA_PICTURESIZE_SUB, false);
            ListPreference listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
            if (listPref != null) {
                String[] size = getInputSize(listPref);
                this.mGet.setSetting(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()), size[0], false);
                this.mGet.setSpecificSettingValueAndDisable(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()), size[0], false);
            }
        }
    }

    public void doEnableContorls(boolean enable) {
        if (this.mGet != null) {
            if (TelephonyUtil.phoneInCall(this.mGet.getAppContext()) || !MDMUtil.allowMicrophone()) {
                enable = false;
            }
            this.mGet.setCaptureButtonEnable(enable, 4);
        }
    }

    public void doPhoneStateAction(int state) {
        if (state == 0) {
            if (this.mGet.checkModuleValidate(192)) {
                boolean enable = MDMUtil.allowMicrophone();
                if (!enable) {
                    stopRecorder();
                }
                this.mGet.setCaptureButtonEnable(enable, 4);
            }
        } else if (state == 1 && this.mGet.getCameraState() != 6 && this.mGet.getCameraState() != 7) {
            setVisibleThumb(false);
            this.mGet.setCaptureButtonEnable(false, 4);
        }
    }

    public void doConfigurationChanged(ActivityBridge activityBridge, Configuration config) {
        if (this.mGet != null) {
            CamLog.m3d(CameraConstants.TAG, "doConfigurationChanged status=" + this.mStatus + " fixedDegree=" + this.mFixedDegree);
            View baseParent = this.mGet.findViewById(C0088R.id.contents_base);
            if (baseParent != null) {
                init(activityBridge, baseParent);
            }
        }
    }

    public void onZoomBarVisibilityChanged(boolean zoomBarVisible) {
        if (zoomBarVisible || this.mGet.isModeMenuVisible() || this.mGet.isHelpListVisible() || this.mGet.isSettingMenuVisible()) {
            setVisibleBar(false);
        } else {
            setVisibleBar(true);
        }
    }

    public void removeSaveButtonTouchListener() {
        if (this.mSaveButton != null) {
            this.mSaveButton.setOnTouchListener(null);
        }
    }
}
